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

package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.dto.al.Configuration.AlgorithmConfigurationDto;
import com.algorithm.web.model.dto.al.Configuration.ChildDto;
import com.algorithm.web.model.dto.al.Configuration.ConfigurationDto;
import com.algorithm.web.service.al.AlgorithmConfigurationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

/**
 * 领域模型组态管理表
 *
 * @author pig code generator
 * @date 2025-04-29 10:28:19
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algorithmconfiguration")
@Tag(name = "领域模型组态管理表管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class AlgorithmConfigurationController {

	private final AlgorithmConfigurationService algorithmConfigurationService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public RestResult getConfigs(Page page) {
		return RestResult.success(algorithmConfigurationService.getConfigs(page));
	}

	/**
	 * 获取所有的组态
	 * @return
	 */
	@Operation(summary = "获取所有的组态", description = "获取所有的组态")
	@GetMapping("/getAllConfigs")
	public RestResult getAllConfigs() {
		return RestResult.success(algorithmConfigurationService.getAllConfigs());
	}

	@Operation(summary = "保存算法组态", description = "保存算法组态")
	@PostMapping("/saveConfig")
	public RestResult saveConfig(@RequestBody ConfigurationDto configurationDto) {
		String result = algorithmConfigurationService.saveConfig(configurationDto);
		if (result == null)
			return RestResult.error("该组态未涉及到领域模型！");
		else
			return RestResult.success(result);
	}

	@Operation(summary = "组态名检验", description = "组态名检验")
	@GetMapping("/exitConfigName/{modelName}")
	public RestResult exitConfigName(@PathVariable String modelName) {
		return RestResult.success(algorithmConfigurationService.exitConfigName(modelName));
	}

	@Operation(summary = "删除组态", description = "删除组态")
	@DeleteMapping("/deleteConfig")
	public RestResult deleteConfig(@RequestParam String code) {
		return RestResult.success(algorithmConfigurationService.deleteConfig(code));
	}

	@Operation(summary = "运行组态", description = "运行组态")
	@PostMapping("/startConfig")
	public RestResult startConfig(@RequestBody ChildDto childDto) throws MalformedURLException {
		return RestResult.success(algorithmConfigurationService.startConfig(childDto));
	}

	@Operation(summary = "修改审核状态", description = "修改审核状态")
	@PutMapping("/checkConfig") // 使用 PUT 或 PATCH 来进行修改
	public RestResult checkConfig(@RequestBody AlgorithmConfigurationDto configurationDto,
			@RequestParam Boolean isCheck) {
		return RestResult.success(algorithmConfigurationService.checkConfig(configurationDto, isCheck));
	}

	@Operation(summary = "修改发布状态", description = "修改发布状态")
	@PutMapping("/updateIspublishedConfig") // 使用 PUT 或 PATCH 来进行修改
	public RestResult checkConfig(@RequestBody AlgorithmConfigurationDto configurationDto) {
		return RestResult.success(algorithmConfigurationService.updateIspublishedConfig(configurationDto));
	}

	@Operation(summary = "查询审核状态", description = "查询审核状态")
	@PostMapping("/getCheckStatus") // 使用 PUT 或 PATCH 来进行修改
	public RestResult getCheckStatus(@RequestBody AlgorithmConfigurationDto configurationDto) {
		return RestResult.success(algorithmConfigurationService.getCheckStatus(configurationDto));
	}

}
