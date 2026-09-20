/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */
package com.algorithm.web.service.impl.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.enums.SysConfigEnum;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlgorithmConfigurationMapper;
import com.algorithm.web.mapper.al.DomainModelConfigurationMapper;
import com.algorithm.web.model.dto.al.Configuration.ConfigurationDto;
import com.algorithm.web.model.dto.al.Configuration.NodeDto;

import com.algorithm.web.model.dto.al.Configuration.*;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.model.entity.al.AlgorithmConfiguration;
import com.algorithm.web.model.entity.al.AlgorithmConfiguration;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.service.al.AlTaskService;
import com.algorithm.web.service.al.AlgorithmConfigurationService;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.utils.RestTemplateUtil;
import com.algorithm.web.utils.TaskMsgServices;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 领域模型组态管理表
 *
 * @author pig code generator
 * @date 2025-04-29 10:28:19
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AlgorithmConfigurationServiceImpl extends ServiceImpl<AlgorithmConfigurationMapper, AlgorithmConfiguration>
		implements AlgorithmConfigurationService {

	@Autowired
	private Map<String, BuildTaskMsgService> algorithmServiceMap;

	@Autowired
	private AlTaskService alTaskService;

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	AlgorithmConfigurationMapper algorithmConfigurationMapper;

	// 建议将线程池定义为 Spring Bean，但这里快速示例写在类中
	private static final ExecutorService executorService = Executors.newCachedThreadPool();

	@Override
	public Boolean deleteConfig(String code) {
		return this.baseMapper.deleteConfig(code);
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	static class TaskMsg {

		String programUrl;

	}

	@Override
	public Long startConfig(ChildDto childDto) throws MalformedURLException {
		AlTask alTask = new AlTask();

		// 依次执行算法
		String configUrl = childDto.getConfigUrl();

		Optional<BuildTaskMsgService> optional = Optional
			.ofNullable(algorithmServiceMap.get(TaskMsgServices.getTaskMsgService("train-" + childDto.getShortName())));
		if (optional.isPresent()) {
			BuildTaskMsgService buildTaskMsgService = optional.get();
			alTask.setTaskUrl(configUrl);
			alTask.setTaskState(0);
			alTask.setTaskReUrl(systemConfigService.getSystemConfigByKey(SysConfigEnum.ALGORITHM_CALLBACK_URL.getKey())
					+ "algorithm/job/algorithmJobCallback");
			alTask.setTaskMsg(buildTaskMsgService.buildTaskMsg(childDto.getTaskMsg()));
			alTask.setUseCase("算法组态");
			alTask.setStartTime(LocalDateTime.now());
			alTask.setAlClass("modelConfiguration");
			alTaskService.save(alTask);
			alTask.setTaskId(alTask.getId());

			String body = JsonUtil.toJson(alTask);
			log.info("开始执行任务 taskId={} algorithmUrl={} body={}", alTask.getTaskId(), configUrl, body);
			executorService.submit(() -> {
				try {
					executeAlgorithmUrls(alTask, configUrl, body);
				}
				catch (Exception e) {
					log.error("异步执行任务失败", e);
				}
			});
		}
		else {
			throw new BizException("组态算法 " + childDto.getShortName() + " 未注册");
		}

		return alTask.getId();

	}

	@Override
	public Object checkConfig(AlgorithmConfigurationDto configurationDto, Boolean isCheck) {

		if (configurationDto == null || configurationDto.getCode() == null || configurationDto.getModelName() == null) {
			return RestResult.error("配置数据不能为空");
		}
		// 1. 查询符合条件的记录
		LambdaQueryWrapper<AlgorithmConfiguration> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AlgorithmConfiguration::getCode, configurationDto.getCode())
			.eq(AlgorithmConfiguration::getModelName, configurationDto.getModelName());
		List<AlgorithmConfiguration> existingConfigs = this.list(queryWrapper);
		if (existingConfigs.isEmpty()) {
			return RestResult.error("未找到匹配的配置记录");
		}
		// 2. 批量更新记录
		existingConfigs.forEach(config -> {
			// 反转 isService 字段
			config.setIsService(config.getIsService() == 1 ? 0 : 1);
			// 根据 isCheck 决定是否更新 isDeployed
			if (isCheck) {
				config.setIsDeployed(config.getIsDeployed() == 1L ? 0L : 1L);
			}
		});
		// 3. 执行批量更新
		this.updateBatchById(existingConfigs);
		if (isCheck) {
			return RestResult.success("通过审核");
		}
		return RestResult.success("更新启用状态");
	}

	@Override
	public Object getCheckStatus(AlgorithmConfigurationDto configurationDto) {
		LambdaQueryWrapper<AlgorithmConfiguration> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AlgorithmConfiguration::getCode, configurationDto.getCode())
			.eq(AlgorithmConfiguration::getModelName, configurationDto.getModelName())
			.last("LIMIT 1"); // 优化：只查询一条记录
		AlgorithmConfiguration config = this.getOne(queryWrapper);
		return config != null ? config.getIsDeployed() : null;
	}

	@Override
	public Page<GroupedConfigDTO> getConfigs(Page page) {
		List<AlgorithmConfiguration> rawRecords;
		long totalGroups;
		Page<GroupedConfigDTO> resultPage;

		// 判断是否分页查询
		boolean isPaging = (page != null && page.getCurrent() != 0 && page.getSize() > 0 && page.getCurrent() > 0);

		if (isPaging) {
			// 分页查询逻辑
			int pageSize = (int) page.getSize();
			int current = (int) page.getCurrent();
			int offset = (current - 1) * pageSize;

			// 查询分组总数
			totalGroups = baseMapper.countDistinctGroups();

			resultPage = new Page<>(current, pageSize, totalGroups);

			// 查询当前页分组 codes
			List<String> groupedCodes = baseMapper.selectGroupedCodesOrdered(pageSize, offset);
			if (groupedCodes == null || groupedCodes.isEmpty()) {
				return resultPage; // 无数据直接返回空分页
			}
			rawRecords = baseMapper.selectByCodes(groupedCodes);

		}
		else {
			// 不分页，查询全部数据
			rawRecords = baseMapper.selectList(null);
			totalGroups = rawRecords.stream().map(AlgorithmConfiguration::getCode).distinct().count();

			resultPage = new Page<>(1, (int) totalGroups, totalGroups);
		}

		// 分组处理（与之前一致）
		Map<String, GroupedConfigDTO> groupedMap = rawRecords.stream()
			.collect(Collectors.toMap(AlgorithmConfiguration::getCode, item -> {
				GroupedConfigDTO dto = new GroupedConfigDTO();
				dto.setCode(item.getCode());
				dto.setModelName(item.getModelName());
				dto.setModelType(item.getModelType());
				dto.setModelObject(item.getModelObject());
				dto.setObjectId(item.getObjectId());
				dto.setModelIcon(item.getAlmodelIcon());
				dto.setIsService(item.getIsService());
				dto.setIsDeployed(item.getIsDeployed());
				dto.setIsPublished(item.getIsPublished());
				dto.setTaskProcess(new ArrayList<>());
				dto.setCreateTime(item.getCreateTime());
				dto.setPretreatment(item.getPretreatment());
				dto.setTrainMetrics(item.getTrainResult());
				dto.setLearningRate(item.getLearningRate());
				dto.setOptimizer(item.getOptimizer());
				dto.setTrainEpoch(item.getTrainTimes());
				dto.setTrainBatch(item.getTrainBatch());
				dto.setMaxLevel(item.getMaxLevel());
				dto.setSliceLength(item.getSliceLength());
				dto.setWaveMode(item.getWaveMode());
				dto.setWaveLet(item.getWaveLet());
				dto.setN(item.getN());
				return dto;
			}, (existing, replacement) -> replacement));

		rawRecords.forEach(item -> {
			GroupedConfigDTO group = groupedMap.get(item.getCode());
			GroupedConfigDTO.TaskProcessDTO taskDto = new GroupedConfigDTO.TaskProcessDTO();
			taskDto.setAlmodelName(item.getAlmodelName());
			taskDto.setAlmodelType(item.getAlmodelType());
			taskDto.setSequence(item.getSequence());
			taskDto.setAlmodelShortName(item.getAlmodelShortName());
			taskDto.setConfigUrl(item.getConfigUrl());
			taskDto.setTrainResult(item.getTrainResult());
			group.getTaskProcess().add(taskDto);
		});

		groupedMap.values().forEach(group -> {
			group.getTaskProcess().sort(Comparator.comparingInt(GroupedConfigDTO.TaskProcessDTO::getSequence));
		});

		// 排序输出
		List<GroupedConfigDTO> sortedRecords = groupedMap.values()
			.stream()
			.sorted(Comparator.comparingInt(group -> Integer.parseInt(group.getCode().split("-")[1])))
			.collect(Collectors.toList());

		resultPage.setRecords(sortedRecords);

		return resultPage;
	}

	@Override
	public Page<GroupedConfigDTO> getAllConfigs() {
		return getConfigs(null);
	}

	@Override
	public Object updateIspublishedConfig(AlgorithmConfigurationDto configurationDto) {
		// 参数校验
		if (configurationDto == null || configurationDto.getCode() == null || configurationDto.getModelName() == null) {
			return RestResult.error("配置数据不能为空");
		}
		// 1. 查询符合条件的记录
		LambdaQueryWrapper<AlgorithmConfiguration> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AlgorithmConfiguration::getCode, configurationDto.getCode())
			.eq(AlgorithmConfiguration::getModelName, configurationDto.getModelName());
		List<AlgorithmConfiguration> existingConfigs = this.list(queryWrapper);
		if (existingConfigs.isEmpty()) {
			return RestResult.error("未找到匹配的配置记录");
		}
		// 2. 获取第一条记录的状态
		AlgorithmConfiguration firstConfig = existingConfigs.get(0);
		// 3. 状态检查
		if (firstConfig.getIsDeployed() != 0) {
			return RestResult.error("算法组态未审核，请先审核算法");
		}
		if (firstConfig.getIsService() != 0) {
			return RestResult.error("算法组态未启用，请先启用算法");
		}
		// 4. 更新所有匹配记录的isPublished字段
		if (firstConfig.getIsDeployed() == 0 && firstConfig.getIsService() == 0) {
			List<AlgorithmConfiguration> updateList = existingConfigs.stream()
				.peek(config -> config.setIsPublished(0L))// 设置为已发布
				.collect(Collectors.toList());
			if (this.updateBatchById(updateList)) {
				return RestResult.success("发布状态更新成功");
			}
			return RestResult.error("发布状态更新失败");
		}
		return RestResult.error("未知状态，无法更新发布状态");
	}

	private void executeAlgorithmUrls(AlTask alTask, String configUrl, String body) throws MalformedURLException {
		ObjectMapper objectMapper = new ObjectMapper();
		URL url = new URL(configUrl);
		String base_url = url.getProtocol() + "://" + url.getHost();
		try {
			String res = RestTemplateUtil.post(configUrl, body); // 尝试请求
			log.info("任务 taskId={} 返回={}", alTask.getTaskId(), res);
			if (isValidJson(res)) {
				JsonNode rootNode = objectMapper.readTree(res);
				String content = rootNode.get("taskState").toString();

				if (content.equals("3") || res == null) {
					alTask.setTaskResult(null);
					alTask.setId(rootNode.get("taskId").asLong());
					alTask.setTaskState(3);
					alTask.setServerUrl(base_url);
				}
				else {
					alTask = JsonUtil.fromJson(res, AlTask.class);
					alTask.setId(rootNode.get("taskId").asLong());
					alTask.setTaskUrl(configUrl); // 记录实际使用的算法 URL
					alTask.setTaskState(2);
					alTask.setServerUrl(base_url);
				}
				alTask.setEndTime(LocalDateTime.now());
				alTaskService.updateById(alTask);
			}
		}
		catch (Exception e) {
			log.warn("请求算法服务 {} 失败，错误信息: {}", configUrl, e.getMessage());
			alTask.setTaskUrl(configUrl); // 记录实际使用的算法 URL
			alTask.setTaskState(3);
			alTask.setServerUrl(base_url);
		}

	}

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
	public String saveConfig(ConfigurationDto configurationDto) {
		// 获取所有类型为 child 的节点
		List<NodeDto> childNodes = configurationDto.getNodes()
			.stream()
			.filter(node -> "child".equals(node.getType()))
			.sorted(Comparator.comparingInt(child -> Integer.parseInt(child.getParent())))
			.collect(Collectors.toList());

		// 获取所有类型为 parent 的节点并映射成父节点ID -> 父节点对象的映射
		Map<String, NodeDto> parentMap = configurationDto.getNodes()
			.stream()
			.filter(node -> "parent".equals(node.getType()))
			.collect(Collectors.toMap(NodeDto::getId, node -> node));

		Optional<NodeDto> domainNode = parentMap.values()
			.stream()
			.filter(node -> Boolean.FALSE.equals(node.getIsPretreatment()))
			.findFirst();

		if (!domainNode.isPresent())
			return null;

		Long code = 0L;
		boolean isFirst = true;

		for (NodeDto child : childNodes) {
			try {
				// 创建新的配置对象
				AlgorithmConfiguration config = new AlgorithmConfiguration();
				config.setModelName(configurationDto.getModelName());
				config.setModelType(domainNode.get().getData().getLabel());
				config.setModelObject(configurationDto.getModelObject());
				config.setObjectId(configurationDto.getObjectId());
				config.setSequence(Integer.valueOf(child.getParent())); // 父节点ID
				config.setAlmodelName(child.getData().getLabel()); // 子节点标签
				config.setAlmodelShortName(child.getData().getShortName());
				config.setAlmodelType(parentMap.get(child.getParent()).getData().getLabel()); // 父节点标签
				config.setCreateTime(LocalDateTime.now());
				config.setAlmodelIcon(configurationDto.getImageUrl());
				config.setTrainTimes(child.getData().getTrainEpoch());
				config.setTrainBatch(child.getData().getTrainBatch());
				config.setLearningRate(child.getData().getLearningRate());
				config.setOptimizer(child.getData().getOptimizer());
				config.setConfigUrl(child.getData().getSaveUrl());
				config.setTrainResult(child.getData().getTrainResult());
				config.setPretreatment(child.getData().getPretreatment());
				config.setMaxLevel(child.getData().getMaxlevel());
				config.setSliceLength(child.getData().getSliceLength());
				config.setWaveLet(child.getData().getWavelet());
				config.setWaveMode(child.getData().getMode());
				config.setN(child.getData().getN());
				this.save(config);

				if (isFirst) {
					code = config.getId();
					isFirst = false;
				}

				// 更新配置的 code 字段
				config.setCode("ac-" + code);
				this.updateById(config);
			}
			catch (Exception e) {
				// 发生异常
				throw new RuntimeException("Error processing configuration for node: " + child.getId(), e);
			}
		}
		return "Configuration saved successfully";
	}

	@Override
	public Boolean exitConfigName(String modelName) {
		return algorithmConfigurationMapper.exitConfigName(modelName);
	}

}
