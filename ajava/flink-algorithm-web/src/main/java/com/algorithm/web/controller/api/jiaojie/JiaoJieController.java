package com.algorithm.web.controller.api.jiaojie;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.entity.al.AlChart;
import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.service.al.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/al")
public class JiaoJieController extends BaseController {

	@Autowired
	private AlDataCleanService alDataCleanService;

	@Autowired
	private AlgorithmJobService algorithmJobService;

	@Autowired
	private final AlTaskService alTaskService;

	@Autowired
	private final AlFaultDiagnosisService alFaultDiagnosisService;

	@PostMapping("/execute")
	public RestResult exitName(@RequestBody AlChart alChart) {
		String endTime = alChart.getEndTime();
		String startTime = alChart.getStartTime();
		String monitorPointId = alChart.getMonitorCode();
		Long chartId = alChart.getAlgorithmId();
		AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisService.getById(chartId);
		Long id = Long.valueOf(alFaultDiagnosis.getId());
		AlTask alTask = new AlTask();
		alTask.setTaskState(0);
		// alTask.setCreator(this.getUserName());
		alTask.setIsDeleted(0);

		if (id.equals(11)) {
			alTask.setTaskMsg("{\"endTime\":" + '"' + endTime + '"' + ',' + "\"startTime\":" + '"' + startTime + '"'
					+ ',' + "\"monitorPointId\":" + '"' + monitorPointId + '"' + ','
					+ "\"filePath\":\"history/2023/7/1/P9800_1_002_obj3_4_s2_2023-07-01T03:57:36.496Z.csv\"" + '}');
		}
		else {
			alTask.setTaskMsg("{\"endTime\":" + '"' + endTime + '"' + ',' + "\"startTime\":" + '"' + startTime + '"'
					+ ',' + "\"monitorPointId\":" + '"' + monitorPointId + '"' + ','
					+ "\"filePath\":\"history/2023/7/1/N15_M01_F10_4096.csv\"" + '}');
		}
		alTask.setAlId(id);
		AlFaultDiagnosis alFaultDiagnosis1 = alFaultDiagnosisService.getbyId(Math.toIntExact(id));
		alFaultDiagnosis1.setModelNum(alFaultDiagnosis1.getModelNum() + 1);
		alFaultDiagnosisService.updateById(alFaultDiagnosis1);

		alTaskService.save(alTask);
		RestTemplate restTemplate = new RestTemplate();
		String url = "http://localhost:8180/api/altask/startJob/" + id;
		// String url = "http://192.168.20.105/api/altask/startJob/" + id;

		String s = restTemplate.getForObject(url, String.class);
		CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> waitForTaskCompletion(id));
		String taskResult = future.join();
		JSONObject jsonObject = JSONObject.parseObject(taskResult);

		// 处理result_table
		JSONObject resultTable = jsonObject.getJSONObject("result_table");
		resultTable.put("object", "滚动轴承");
		resultTable.put("function", "故障诊断");
		resultTable.put("situation", "未处理");

		// 获取当前时间并格式化
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		resultTable.put("time", dateFormat.format(currentDate));

		// 将 result_table 转化为 JSON 数组
		JSONArray resultTableArray = new JSONArray();
		resultTableArray.add(resultTable);
		jsonObject.put("result_table", resultTableArray);

		// 获取result_plt，并转化为数组
		JSONObject resultPlt = jsonObject.getJSONObject("result_plt");
		JSONArray pltArray = new JSONArray();
		for (String key : resultPlt.keySet()) {
			JSONObject pltObject = new JSONObject();
			pltObject.put("name", key);
			pltObject.put("value", resultPlt.getInteger(key));
			pltArray.add(pltObject);
		}
		// 替换result_plt为数组
		jsonObject.put("result_plt", pltArray);

		// 输出处理后的结果
		String processedResult = jsonObject.toJSONString();
		System.out.println("ok");
		System.out.println(processedResult);
		return RestResult.success(processedResult);
	}

	private String waitForTaskCompletion(Long id) {
		try {
			while (true) {
				AlTask task = alTaskService.getById(id);
				if (task.getTaskState() == 2) {
					return task.getTaskResult();
				}
				Thread.sleep(1000); // 间隔一秒进行轮询
			}
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

}
