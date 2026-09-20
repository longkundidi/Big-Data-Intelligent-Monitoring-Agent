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

package sw.model3d.configFailureHistory.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import sw.model3d.configFailureHistory.entity.ConfigFailureHistory;
import sw.model3d.configFailureHistory.entity.ConfigFailureHistoryQueryRequest;
import sw.model3d.configFailureHistory.mapper.ConfigFailureHistoryMapper;
import sw.model3d.configFailureHistory.service.ConfigFailureHistoryService;

/**
 * @author pig code generator
 * @date 2026-03-19 16:15:58
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configfailurehistory")
@Tag(name = "故障历史管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigFailureHistoryController {

	private final ConfigFailureHistoryService configFailureHistoryService;

	private final ConfigFailureHistoryMapper configFailureHistoryMapper;

	/**
	 * 通过节点id分页查询
	 */
	@Operation(summary = "通过节点id分页查询", description = "通过节点id分页查询")
	@PostMapping("/page")
	public R getConfigFailureHistoryPage(@RequestBody ConfigFailureHistoryQueryRequest queryRequest) {
		int current = queryRequest.getCurrent();
		int pageSize = queryRequest.getPageSize();
		String nodeId = queryRequest.getNodeId();
		String sceneId = queryRequest.getSceneId();
		String deviceName = queryRequest.getDeviceName();

		QueryWrapper<ConfigFailureHistory> queryWrapper = new QueryWrapper<>();
		queryWrapper.lambda().eq(ConfigFailureHistory::getNodeId, nodeId);
		if (sceneId != null && !sceneId.isEmpty()) {
			queryWrapper.lambda().eq(ConfigFailureHistory::getSceneId, sceneId);
		}
		if (deviceName != null && !deviceName.isEmpty()) {
			queryWrapper.lambda().like(ConfigFailureHistory::getDeviceName, deviceName);
		}

		Page<ConfigFailureHistory> page = configFailureHistoryService.page(new Page<>(current, pageSize), queryWrapper);
		return R.ok(page, "查询成功");
	}

	/**
	 * 通过节点id分页查询（GET）
	 */
	@Operation(summary = "通过节点id分页查询(GET)", description = "通过节点id分页查询(GET)")
	@GetMapping("/page")
	public R getConfigFailureHistoryPageByGet(@RequestParam(value = "current", required = false) Integer current,
			@RequestParam(value = "pageSize", required = false) Integer pageSize,
			@RequestParam(value = "size", required = false) Integer size,
			@RequestParam(value = "nodeId", required = false) String nodeId,
			@RequestParam(value = "node_id", required = false) String nodeIdAlias,
			@RequestParam(value = "sceneId", required = false) String sceneId,
			@RequestParam(value = "deviceName", required = false) String deviceName) {

		int currentPage = (current == null || current < 1) ? 1 : current;
		int pageSizeValue = pageSize == null || pageSize < 1 ? ((size == null || size < 1) ? 10 : size) : pageSize;
		String realNodeId = StringUtils.hasText(nodeId) ? nodeId : nodeIdAlias;

		QueryWrapper<ConfigFailureHistory> queryWrapper = new QueryWrapper<>();
		if (StringUtils.hasText(realNodeId)) {
			queryWrapper.lambda().eq(ConfigFailureHistory::getNodeId, realNodeId);
		}
		if (StringUtils.hasText(sceneId)) {
			queryWrapper.lambda().eq(ConfigFailureHistory::getSceneId, sceneId);
		}
		if (StringUtils.hasText(deviceName)) {
			queryWrapper.lambda().like(ConfigFailureHistory::getDeviceName, deviceName);
		}

		Page<ConfigFailureHistory> page = configFailureHistoryService.page(new Page<>(currentPage, pageSizeValue),
				queryWrapper);
		return R.ok(page, "查询成功");
	}

	/**
	 * 通过节点id查询
	 */
	@Operation(summary = "通过节点id查询", description = "通过节点id查询")
	@GetMapping("/{nodeId}")
	public R getByNodeId(@PathVariable("nodeId") String nodeId) {
		return R.ok(configFailureHistoryService.getVoByNodeId(nodeId), "查询成功");
	}

	/**
	 * 新增
	 */
	@Operation(summary = "新增", description = "新增")
	@SysLog("新增")
	@PostMapping
	public R save(@RequestBody ConfigFailureHistory configFailureHistory) {
		boolean isSaved = configFailureHistoryService.save(configFailureHistory);
		if (isSaved) {
			return R.ok(configFailureHistory, "新增成功");
		}
		return R.failed("新增失败");
	}

	/**
	 * 修改
	 */
	@Operation(summary = "修改", description = "修改")
	@SysLog("修改")
	@PutMapping
	public R updateById(@RequestBody ConfigFailureHistory configFailureHistory) {
		boolean isUpdated = configFailureHistoryService.updateById(configFailureHistory);
		if (isUpdated) {
			return R.ok(configFailureHistory, "修改成功");
		}
		return R.failed(null, "修改失败，ID错误");
	}

	/**
	 * 通过id删除
	 */
	@Operation(summary = "通过id删除", description = "通过id删除")
	@SysLog("通过id删除")
	@DeleteMapping("/{failureId}")
	public R removeById(@PathVariable String failureId) {
		boolean isDeleted = configFailureHistoryService.removeById(failureId);
		if (isDeleted) {
			return R.ok(null, "删除成功");
		}
		return R.failed(null, "删除失败");
	}

	@Operation(summary = "获得故障历史总数", description = "获得故障历史总数")
	@SysLog("获得故障历史总数")
	@GetMapping("/all")
	public R getTotalNum() {
		Long count = configFailureHistoryMapper.selectCount(new QueryWrapper<>());
		return R.ok(count);
	}

}