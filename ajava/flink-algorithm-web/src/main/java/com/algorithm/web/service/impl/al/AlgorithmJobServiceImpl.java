package com.algorithm.web.service.impl.al;

import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.enums.SysConfigEnum;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.entity.al.*;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.utils.TaskMsgServices;
import com.algorithm.web.service.al.AlgorithmJobService;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.utils.RestTemplateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static net.minidev.json.JSONValue.isValidJson;

@Slf4j
@Service
public class AlgorithmJobServiceImpl implements AlgorithmJobService {

	@Autowired
	private Map<String, BuildTaskMsgService> algorithmServiceMap;

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public AlTask startJob(AlTask alTask, String backupModelUrl, String modelUrl, String modelShortName)
			throws JsonProcessingException, MalformedURLException {
		String serviceName = TaskMsgServices.getTaskMsgService(modelShortName);
		if (serviceName == null) {
			throw new BizException("算法 " + modelShortName + " 未注册");
		}
		return startJobWithService(alTask, backupModelUrl, modelUrl, serviceName);
	}

	@Override
	public AlTask startConfiguredJob(AlTask alTask, String backupModelUrl, String modelUrl, String executorType,
			String executorConfig) throws JsonProcessingException, MalformedURLException {
		return startJobWithService(alTask, backupModelUrl, modelUrl,
				resolveConfiguredTaskMsgService(executorType, executorConfig));
	}

	private AlTask startJobWithService(AlTask alTask, String backupModelUrl, String modelUrl, String serviceName)
			throws JsonProcessingException, MalformedURLException {
		if (modelUrl == null || modelUrl.trim().isEmpty()) {
			throw new BizException("模型调用地址不能为空");
		}
		List<String> algorithmUrls = new ArrayList<>();
		algorithmUrls.add(modelUrl);

		// 解析并添加备份 URL
		if (backupModelUrl != null && !backupModelUrl.trim().isEmpty())
			algorithmUrls.addAll(parseBackupUrls(backupModelUrl));

		Optional<BuildTaskMsgService> optional = Optional.ofNullable(algorithmServiceMap.get(serviceName));
		if (optional.isPresent()) {
			BuildTaskMsgService buildTaskMsgService = optional.get();

			alTask.setTaskUrl(modelUrl);
			alTask.setTaskReUrl(systemConfigService.getSystemConfigByKey(SysConfigEnum.ALGORITHM_CALLBACK_URL.getKey())
					+ "algorithm/job/algorithmJobCallback");
			alTask.setTaskMsg(buildTaskMsgService.buildTaskMsg(alTask.getTaskMsg()));
			String body = JsonUtil.toJson(alTask);
			log.info("开始执行任务 taskId={} algorithmUrl={} body={}", alTask.getTaskId(), modelUrl, body);
			return executeAlgorithmUrls(algorithmUrls, body);
		}
		else {
			throw new BizException("执行器 Bean " + serviceName + " 未注册");
		}
	}

	private String resolveConfiguredTaskMsgService(String executorType, String executorConfig) {
		if (executorType == null || executorType.trim().isEmpty()) {
			throw new BizException("算法未配置执行方式");
		}
		String normalized = executorType.trim().toUpperCase(Locale.ROOT);
		switch (normalized) {
			case "HTTP_JSON":
				return "JsonForwardTaskMsgService";
			case "ELEVATOR_INFLUX_MONITOR":
				return "ElevatorAnomalyMonitoringService";
			case "ELEVATOR_INFLUX_DIAGNOSIS":
				return "ElevatorFaultDiagnosisService";
			case "SPRING_BEAN":
				return readExecutorBeanName(executorConfig);
			default:
				throw new BizException("不支持的执行器类型: " + executorType);
		}
	}

	private String readExecutorBeanName(String executorConfig) {
		if (executorConfig == null || executorConfig.trim().isEmpty()) {
			throw new BizException("内置执行器缺少executorConfig");
		}
		try {
			JsonNode beanName = objectMapper.readTree(executorConfig).get("beanName");
			if (beanName == null || !beanName.isTextual() || beanName.asText().trim().isEmpty()) {
				throw new BizException("内置执行器缺少beanName");
			}
			return beanName.asText().trim();
		}
		catch (JsonProcessingException exception) {
			throw new BizException("executorConfig不是合法JSON");
		}
	}

	private List<String> parseBackupUrls(String backupModelUrl) throws JsonProcessingException {

		JsonNode rootNode = objectMapper.readTree(backupModelUrl);
		JsonNode backupUrlsNode = rootNode.get("backup_urls");
		List<String> algorithmUrls = new ArrayList<>();
		if (backupUrlsNode != null && backupUrlsNode.isArray()) {
			for (JsonNode urlNode : backupUrlsNode) {
				algorithmUrls.add(urlNode.asText());
			}
		}
		return algorithmUrls;
	}

	private AlTask executeAlgorithmUrls(List<String> algorithmUrls, String body) throws MalformedURLException {
		AlTask returnAlTask = new AlTask();
		for (String url : algorithmUrls) {
			URL urls = new URL(url);
			String base_url = urls.getProtocol() + "://" + urls.getHost();
			try {
				String res = RestTemplateUtil.post(url, body); // 尝试请求
				log.info("任务 taskId={} 返回={}", returnAlTask.getTaskId(), res);
				if (isValidJson(res)) {
					JsonNode rootNode = objectMapper.readTree(res);
					String content = rootNode.get("taskState").toString();
					if (content.equals("3") || res == null) {
						returnAlTask.setTaskResult(null);
						returnAlTask.setTaskState(3);
						returnAlTask.setServerUrl(base_url);
					}
					else {
						returnAlTask = JsonUtil.fromJson(res, AlTask.class);
						returnAlTask.setTaskUrl(url); // 记录实际使用的算法 URL
						returnAlTask.setServerUrl(base_url);
						break;
					}
				}
			}
			catch (Exception e) {
				log.warn("请求算法服务 {} 失败，错误信息: {}", url, e.getMessage());
				returnAlTask.setTaskUrl(url); // 记录实际使用的算法 URL
				returnAlTask.setTaskState(3);
				returnAlTask.setServerUrl(base_url);
			}
		}
		return returnAlTask; // 返回成功结果
	}

	// 方法用于检查字符串是否为有效的 JSON
	private boolean isValidJson(String json) {
		try {
			JsonUtil.fromJson(json, Object.class); // 尝试将其解析为 Object
			return true; // 如果没有抛出异常，说明是有效的 JSON
		}
		catch (Exception e) {
			return false; // 如果抛出异常，则不是有效的 JSON
		}
	}

	@Override
	public AlTask startAutoTest(AlTask alTask) throws MalformedURLException, JsonProcessingException {

		String modelUrl = "http://192.168.16.219:8834/createTask/";
		List<String> backupUrls = Arrays.asList("http://192.168.16.220:8834/createTask/");

		return startJob(alTask, JsonUtil.toJson(new HashMap<String, List<String>>() {
			{
				put("backup_urls", backupUrls);
			}
		}), modelUrl, "autoTest");
	}

	@Override
	public AlTask startAutoGetMetrics(AlTask alTask) throws MalformedURLException, JsonProcessingException {
		String modelUrl = "http://192.168.16.219:8851/createTask/";
		List<String> backupUrls = Arrays.asList("http://192.168.16.220:8851/createTask/");

		return startJob(alTask, JsonUtil.toJson(new HashMap<String, List<String>>() {
			{
				put("backup_urls", backupUrls);
			}
		}), modelUrl, "autoGetMetrics");
	}

	@Override
	public AlTask startOnlineTrain(AlTask alTask) throws MalformedURLException, JsonProcessingException {

		String modelUrl = "http://localhost:8000/createTask/";
		List<String> backupUrls = Arrays.asList(
		// "http://192.168.16.220:8852/createTask/"
		);

		return startJob(alTask, JsonUtil.toJson(new HashMap<String, List<String>>() {
			{
				put("backup_urls", backupUrls);
			}
		}), modelUrl, "onlineTrain");
	}

}
