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

package sw.model3d.configModel.controller;

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
import sw.model3d.configModel.entity.ConfigBomTree;
import sw.model3d.configModel.service.ConfigBomTreeService;

import java.util.List;

/**
 * BOM树设备详情
 *
 * @author pig code generator
 * @date 2024-05-09 11:36:07
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configbomtree")
@Tag(name = "BOM树设备详情管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigBomTreeController {

	private final ConfigBomTreeService configBomTreeService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param configBomTree BOM树
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getConfigBomTreePage(Page page, ConfigBomTree configBomTree) {
		return R.ok(configBomTreeService.page(page, Wrappers.query(configBomTree)));
	}

	/**
	 * 通过nodeName和turbineCode联合查询BOM树设备详情
	 * @param nodeName 节点名称（风机名称）
	 * @param turbineCode 风机编码
	 * @return R
	 */
	@Operation(summary = "通过nodeName和turbineCode联合查询BOM树设备详情", description = "通过nodeName和turbineCode联合查询BOM树设备详情")
	@GetMapping("/byNodeNameAndTurbineCode")
	public R getByNodeNameAndTurbineCode(@RequestParam("nodeName") String nodeName,
			@RequestParam("turbineCode") String turbineCode) {
		if (nodeName == null || nodeName.isEmpty() || turbineCode == null || turbineCode.isEmpty()) {
			return R.failed("节点名称和风机编码不能为空");
		}

		ConfigBomTree configBomTree = configBomTreeService.getOne(Wrappers.<ConfigBomTree>lambdaQuery()
			.eq(ConfigBomTree::getNodeName, nodeName)
			.eq(ConfigBomTree::getTurbineCode, turbineCode));

		if (configBomTree != null) {
			return R.ok(configBomTree);
		}

		return R.failed("未找到该风机的BOM树数据");
	}

	/**
	 * 新增BOM树
	 * @param configBomTree BOM树
	 * @return R
	 */
	@Operation(summary = "新增BOM树", description = "新增BOM树")
	@SysLog("新增BOM树")
	@PostMapping
	public R save(@RequestBody ConfigBomTree configBomTree) {
		return R.ok(configBomTreeService.save(configBomTree));
	}

	/**
	 * 通过nodeId删除BOM树
	 * @param nodeId 节点ID
	 * @return R
	 */
	@Operation(summary = "通过nodeId删除BOM树", description = "通过nodeId删除BOM树")
	@SysLog("通过nodeId删除BOM树")
	@DeleteMapping("/{nodeId}")
	public R removeById(@PathVariable String nodeId) {
		return R.ok(configBomTreeService.removeById(nodeId));
	}

	/**
	 * 修改BOM树
	 * @param configBomTree BOM树
	 * @return R
	 */
	@Operation(summary = "修改BOM树", description = "修改BOM树")
	@SysLog("修改BOM树")
	@PutMapping
	public R updateById(@RequestBody ConfigBomTree configBomTree) {
		return R.ok(configBomTreeService.updateById(configBomTree));
	}

}
