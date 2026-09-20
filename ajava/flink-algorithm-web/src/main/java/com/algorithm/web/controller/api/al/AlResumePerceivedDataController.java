package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.dto.influxdb.PerceivedData;
import com.algorithm.web.model.entity.al.AlResumeDeviceData;
import com.algorithm.web.model.entity.al.AlResumePerceivedData;
import com.algorithm.web.service.al.AlResumeDeviceDataService;
import com.algorithm.web.service.al.AlResumePerceivedDataService;
import com.algorithm.web.service.al.influxdb.InfluxDBService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alresumeperceiveddata")

public class AlResumePerceivedDataController {

	private final AlResumePerceivedDataService alResumePerceivedDataService;

	@Autowired
	private InfluxDBService influxDBService;

	@GetMapping("/page")
	public RestResult getAlResumeFaultDataPage(Page page, AlResumePerceivedData alResumePerceivedData) {
		QueryWrapper<AlResumePerceivedData> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("project_id", alResumePerceivedData.getProjectId());
		return RestResult.success(alResumePerceivedDataService.list(queryWrapper));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success((alResumePerceivedDataService.getById(id)));
	}

	@PostMapping
	public RestResult save(@RequestBody AlResumePerceivedData alResumePerceivedData) {
		return RestResult.success(alResumePerceivedDataService.save(alResumePerceivedData));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlResumePerceivedData alResumePerceivedData) {
		return RestResult.success(alResumePerceivedDataService.updateById(alResumePerceivedData));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alResumePerceivedDataService.removeById(id));
	}

	// 由项目查询感知变量
	@GetMapping("/getVariablesByProject/{id}")
	public RestResult getvariablesByProject(@PathVariable("id") Long projectId) {

		return RestResult.success(alResumePerceivedDataService.getPreVariables(projectId));
	}

	// 由项目和风机查询感知变量数据
	@GetMapping("/getVarCharts")
	public RestResult getVarCharts(@RequestParam String project, @RequestParam String deviceId,
			@RequestParam String startTime, @RequestParam String endTime,
			// defaultValue 设置返回的条数
			@RequestParam(defaultValue = "", required = false) Integer recNum) {
		List<PerceivedData> perceivedData = influxDBService.getPerceivedData(project, deviceId,
				Instant.parse(startTime), Instant.parse(endTime), recNum);
		for (PerceivedData data : perceivedData) {
			switch (data.getVariable()) {
				case "ACurrent":
					data.setVariable("A相电流");
					break;
				case "BCurrent":
					data.setVariable("B相电流");
					break;
				case "CCurrent":
					data.setVariable("C相电流");
					break;
				case "AVoltage":
					data.setVariable("A相电压");
					break;
				case "BVoltage":
					data.setVariable("B相电压");
					break;
				case "CVoltage":
					data.setVariable("C相电压");
					break;
				case "Power":
					data.setVariable("有功功率");
					break;
				case "CabinTemperature":
					data.setVariable("机舱温度");
					break;
				case "UTemperature":
					data.setVariable("U相绕组温度");
					break;
				case "VTemperature":
					data.setVariable("V相绕组温度");
					break;
				case "WTemperature":
					data.setVariable("W相绕组温度");
					break;
				case "RotorSpeed":
					data.setVariable("低速轴转速");
					break;
				case "FrontBearingTemperature":
					data.setVariable("主轴前轴承温度");
					break;
				case "RearBearingTemperature":
					data.setVariable("主轴后轴承温度");
					break;
			}
		}
		return RestResult.success(perceivedData);
	}

}
