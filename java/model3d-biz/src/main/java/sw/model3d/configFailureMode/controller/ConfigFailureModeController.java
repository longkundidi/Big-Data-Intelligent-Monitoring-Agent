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

package sw.model3d.configFailureMode.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import sw.model3d.configFailureMode.entity.ConfigFailureMode;
import sw.model3d.configFailureMode.entity.ConfigFailureModeQueryRequest;
import sw.model3d.configFailureMode.mapper.ConfigFailureModeMapper;
import sw.model3d.configFailureMode.service.ConfigFailureModeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

/**
 * @author pig code generator
 * @date 2024-03-15 14:43:38
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configfailuremode")
@Tag(name = "故障记录管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigFailureModeController {

	private final ConfigFailureModeService configFailureModeService;

	private final ConfigFailureModeMapper configFailureModeMapper;

	/**
	 * 通过节点id分分页查询
	 */
	@Operation(summary = "通过节点id分分页查询", description = "通过节点id分分页查询")
	@PostMapping("/page")
	// @PreAuthorize("@pms.hasPermission('configFailureMode_configfailuremode_get')" )
	public R getConfigFailureModePage(@RequestBody ConfigFailureModeQueryRequest queryRequest) {
		int current = queryRequest.getCurrent();
		int pageSize = queryRequest.getPageSize();
		String nodeId = queryRequest.getNodeId();
		Page<ConfigFailureMode> page = configFailureModeService.page(new Page<>(current, pageSize),
				new QueryWrapper<ConfigFailureMode>().lambda().eq(ConfigFailureMode::getNodeId, nodeId));
		return R.ok(page, "查询成功");
	}

	/**
	 * 通过id查询
	 * @param failureId id
	 * @return R
	 */
	/*
	 * @Operation(summary = "通过id查询", description = "通过id查询")
	 *
	 * @GetMapping("/{failureId}")
	 *
	 * @PreAuthorize("@pms.hasPermission('configFailureMode_configfailuremode_get')" )
	 * public R getById(@PathVariable("failureId") String failureId) { return
	 * R.ok(configFailureModeService.getById(failureId), "查询成功"); }
	 */

	/**
	 * 根据节点id查询故障记录
	 * @param nodeId id
	 * @return R
	 */
	@Operation(summary = "通过节点id查询", description = "通过节点id查询")
	@GetMapping("/{nodeId}")
	// @PreAuthorize("@pms.hasPermission('configFailureMode_configfailuremode_get')" )
	public R getByNodeId(@PathVariable("nodeId") String nodeId) {
		return R.ok(configFailureModeService.getByNodeId(nodeId), "查询成功");
	}

	/**
	 * 新增
	 * @param configFailureMode
	 * @return R
	 */
	@Operation(summary = "新增", description = "新增")
	@SysLog("新增")
	@PostMapping
	// @PreAuthorize("@pms.hasPermission('configFailureMode_configfailuremode_add')" )
	public R save(@RequestBody ConfigFailureMode configFailureMode) {
		boolean isSaved = configFailureModeService.save(configFailureMode);
		if (isSaved) {
			return R.ok(configFailureMode, "新增成功");
		}
		return R.failed("新增失败");
	}

	/**
	 * 修改
	 * @param configFailureMode
	 * @return R
	 */
	@Operation(summary = "修改", description = "修改")
	@SysLog("修改")
	@PutMapping
	// @PreAuthorize("@pms.hasPermission('configFailureMode_configfailuremode_edit')" )
	public R updateById(@RequestBody ConfigFailureMode configFailureMode) {
		boolean isUpdated = configFailureModeService.updateById(configFailureMode);
		if (isUpdated) {
			return R.ok(configFailureMode, "修改成功");
		}
		return R.failed(null, "修改失败，ID错误");
	}

	/**
	 * 通过id删除
	 * @param failureId id
	 * @return R
	 */
	@Operation(summary = "通过id删除", description = "通过id删除")
	@SysLog("通过id删除")
	@DeleteMapping("/{failureId}")
	// @PreAuthorize("@pms.hasPermission('configFailureMode_configfailuremode_del')" )
	public R removeById(@PathVariable String failureId) {
		boolean isDeleted = configFailureModeService.removeById(failureId);
		if (isDeleted) {
			return R.ok(null, "删除成功");
		}
		return R.failed(null, "删除失败");
	}

	@Operation(summary = "获得故障模式总数", description = "获得故障模式总数")
	@SysLog("获得故障模式总数")
	@GetMapping("/all")
	public R getTotalNum() {
		Long count = configFailureModeMapper.selectCount(new QueryWrapper<>());
		return R.ok(count);
	}

	@Operation(summary = "通过实例风机节点名称查询", description = "通过实例风机节点名称查询")
	@GetMapping("/getByBomNodeName")
	// @PreAuthorize("@pms.hasPermission('configFailureMode_configfailuremode_get')" )
	public R getByBomNodeName(@RequestParam String nodeName) {
		return R.ok(configFailureModeService.getByBomNodeName(nodeName), "查询成功");
	}

	@GetMapping("/getFaultModeCount")
	public R getFaultModeCount() {
		return R.ok(configFailureModeService.count(), "查询成功");
	}

}
