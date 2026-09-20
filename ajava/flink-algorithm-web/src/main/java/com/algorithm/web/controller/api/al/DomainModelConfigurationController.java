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
import com.algorithm.web.model.dto.al.Configuration.ChildDto;
import com.algorithm.web.model.dto.al.Configuration.ConfigurationDto;
import com.algorithm.web.model.dto.al.Configuration.DomainModelConfigurationDto;
import com.algorithm.web.service.al.DomainModelConfigurationService;
import com.alibaba.nacos.shaded.com.google.protobuf.ServiceException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

/**
 * 状态感知模型信息
 *
 * @author pig code generator
 * @date 2025-04-08 15:53:44
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/domainmodelconfiguration")
@Tag(name = "状态感知模型信息管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DomainModelConfigurationController {

	private final DomainModelConfigurationService domainModelConfigurationService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public RestResult getConfigs(Page page) {
		return RestResult.success(domainModelConfigurationService.getConfigs(page));
	}

	@Operation(summary = "保存组态", description = "保存组态")
	@PostMapping("/saveConfig")
	public RestResult saveConfig(@RequestBody ConfigurationDto configurationDto) throws ServiceException {
		return RestResult.success(domainModelConfigurationService.saveConfig(configurationDto));
	}

	@Operation(summary = "组态名检验", description = "组态名检验")
	@GetMapping("/exitConfigName/{modelName}")
	public RestResult exitConfigName(@PathVariable String modelName) {
		return RestResult.success(domainModelConfigurationService.exitConfigName(modelName));
	}

	@Operation(summary = "删除组态", description = "删除组态")
	@DeleteMapping("/deleteConfig")
	public RestResult deleteConfig(@RequestParam String code) {
		return RestResult.success(domainModelConfigurationService.deleteConfig(code));
	}

	@Operation(summary = "运行组态", description = "运行组态")
	@PostMapping("/startConfig")
	public RestResult startConfig(@RequestBody ChildDto childDto) throws MalformedURLException {
		return RestResult.success(domainModelConfigurationService.startConfig(childDto));
	}

	@Operation(summary = "修改审核状态", description = "修改审核状态")
	@PutMapping("/checkConfig") // 使用 PUT 或 PATCH 来进行修改
	public RestResult checkConfig(@RequestBody DomainModelConfigurationDto configurationDto,
			@RequestParam Boolean isCheck) {
		return RestResult.success(domainModelConfigurationService.checkConfig(configurationDto, isCheck));
	}

	@Operation(summary = "查询审核状态", description = "查询审核状态")
	@PostMapping("/getCheckStatus") // 使用 PUT 或 PATCH 来进行修改
	public RestResult getCheckStatus(@RequestBody DomainModelConfigurationDto configurationDto) {
		return RestResult.success(domainModelConfigurationService.getCheckStatus(configurationDto));
	}

}
