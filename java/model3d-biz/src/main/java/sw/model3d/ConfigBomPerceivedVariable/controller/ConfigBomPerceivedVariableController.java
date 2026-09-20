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

package sw.model3d.ConfigBomPerceivedVariable.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import sw.model3d.ConfigBomPerceivedVariable.entity.ConfigBomPerceivedVariable;
import sw.model3d.ConfigBomPerceivedVariable.service.ConfigBomPerceivedVariableService;

/**
 * 项目风机节点对应的感知变量信息
 *
 * @author pig code generator
 * @date 2024-10-12 15:13:12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configbomperceivedvariable")
@Tag(name = "项目风机节点对应的感知变量信息管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigBomPerceivedVariableController {

	private final ConfigBomPerceivedVariableService configBomPerceivedVariableService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param configBomPerceivedVariable 项目风机节点对应的感知变量信息
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	// @PreAuthorize("@pms.hasPermission('demo_configbomperceivedvariable_get')" )
	public R getConfigBomPerceivedVariablePage(Page page, ConfigBomPerceivedVariable configBomPerceivedVariable) {
		return R.ok(configBomPerceivedVariableService.page(page, Wrappers.query(configBomPerceivedVariable)));
	}

	@Operation(summary = "准备任务实例变量", description = "按实例节点和模型准备可用于任务配置的实例变量")
	@GetMapping("/taskCandidates")
	public R getTaskCandidates(@RequestParam String nodeId, @RequestParam String modelId) {
		try {
			return R.ok(configBomPerceivedVariableService.prepareTaskVariables(nodeId, modelId), "查询成功");
		}
		catch (IllegalArgumentException exception) {
			return R.failed(exception.getMessage());
		}
	}

	/**
	 * 通过节点id查询
	 * @param nodeId id
	 * @return R
	 */
	@Operation(summary = "通过节点id查询", description = "通过节点id查询")
	@GetMapping("/{nodeId}")
	// @PreAuthorize("@pms.hasPermission('configPerceivedVariable_configperceivedvariable_get')"
	// )
	public R getById(@PathVariable("nodeId") String nodeId) {
		return R.ok(configBomPerceivedVariableService.getByNodeId(nodeId), "查询成功");
	}

	/**
	 * 新增项目风机节点对应的感知变量信息
	 * @param configBomPerceivedVariable 项目风机节点对应的感知变量信息
	 * @return R
	 */
	@Operation(summary = "新增项目风机节点对应的感知变量信息", description = "新增项目风机节点对应的感知变量信息")
	@SysLog("新增项目风机节点对应的感知变量信息")
	@PostMapping
	// @PreAuthorize("@pms.hasPermission('demo_configbomperceivedvariable_add')" )
	public R save(@RequestBody ConfigBomPerceivedVariable configBomPerceivedVariable) {
		System.out.println("超高层工程刚吃过成功过吃噶擦擦干吃噶吃噶");

		return R.ok(configBomPerceivedVariableService.save(configBomPerceivedVariable));
	}

	/**
	 * 修改项目风机节点对应的感知变量信息
	 * @param configBomPerceivedVariable 项目风机节点对应的感知变量信息
	 * @return R
	 */
	@Operation(summary = "修改项目风机节点对应的感知变量信息", description = "修改项目风机节点对应的感知变量信息")
	@SysLog("修改项目风机节点对应的感知变量信息")
	@PutMapping
	// @PreAuthorize("@pms.hasPermission('demo_configbomperceivedvariable_edit')" )
	public R updateById(@RequestBody ConfigBomPerceivedVariable configBomPerceivedVariable) {
		return R.ok(configBomPerceivedVariableService.updateById(configBomPerceivedVariable));
	}

	/**
	 * 通过id删除项目风机节点对应的感知变量信息
	 * @param id id
	 * @return R
	 */
	@Operation(summary = "通过id删除项目风机节点对应的感知变量信息", description = "通过id删除项目风机节点对应的感知变量信息")
	@SysLog("通过id删除项目风机节点对应的感知变量信息")
	@DeleteMapping("/{id}")
	// @PreAuthorize("@pms.hasPermission('demo_configbomperceivedvariable_del')" )
	public R removeById(@PathVariable Long id) {
		return R.ok(configBomPerceivedVariableService.removeById(id));
	}

}
