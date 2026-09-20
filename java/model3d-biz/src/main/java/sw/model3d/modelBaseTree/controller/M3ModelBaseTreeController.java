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

package sw.model3d.modelBaseTree.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTreeVo;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTree;
import sw.model3d.modelBaseTree.service.M3ModelBaseTreeService;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author pig code generator
 * @date 2023-10-28 09:20:35
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/m3modelbasetree")
@Tag(name = "管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class M3ModelBaseTreeController {

	private final M3ModelBaseTreeService m3ModelBaseTreeService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param m3ModelBaseTree
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	@PreAuthorize("@pms.hasPermission('modelBaseTree_m3modelbasetree_get')")
	public R getM3ModelBaseTreePage(Page page, M3ModelBaseTree m3ModelBaseTree) {
		return R.ok(m3ModelBaseTreeService.page(page, Wrappers.query(m3ModelBaseTree)));
	}

	/**
	 * 通过id查询
	 * @param nodeId id
	 * @return R
	 */
	@Operation(summary = "通过id查询", description = "通过id查询")
	@GetMapping("/{nodeId}")
	@PreAuthorize("@pms.hasPermission('modelBaseTree_m3modelbasetree_get')")
	public R getById(@PathVariable("nodeId") String nodeId) {
		return R.ok(m3ModelBaseTreeService.getById(nodeId));
	}

	/**
	 * 新增
	 * @param m3ModelBaseTree
	 * @return R
	 */
	@Operation(summary = "新增", description = "新增")
	@SysLog("新增")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('modelBaseTree_m3modelbasetree_add')")
	public R save(@RequestBody M3ModelBaseTree m3ModelBaseTree) {
		return R.ok(m3ModelBaseTreeService.save(m3ModelBaseTree));
	}

	/**
	 * 修改
	 * @param m3ModelBaseTree
	 * @return R
	 */
	@Operation(summary = "修改", description = "修改")
	@SysLog("修改")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('modelBaseTree_m3modelbasetree_edit')")
	public R updateById(@RequestBody M3ModelBaseTree m3ModelBaseTree) {
		return R.ok(m3ModelBaseTreeService.updateById(m3ModelBaseTree));
	}

	/**
	 * 通过id删除
	 * @param nodeId id
	 * @return R
	 */
	@Operation(summary = "通过id删除", description = "通过id删除")
	@SysLog("通过id删除")
	@DeleteMapping("/{nodeId}")
	@PreAuthorize("@pms.hasPermission('modelBaseTree_m3modelbasetree_del')")
	public R removeById(@PathVariable String nodeId) {
		return R.ok(m3ModelBaseTreeService.removeById(nodeId));
	}

	@Operation(summary = "根据模型库Id查询顶层节点", description = "根据模型库Id查询顶层节点")
	@GetMapping("/getRootNodeByMbId/{mbId}")
	public OpenResponse getRootNodeByMbId(@PathVariable("mbId") String mbId) {
		OpenResponse<M3ModelBaseTreeVo> response = new OpenResponse<>(OpenResponseCode.ERROR);
		response.setData(m3ModelBaseTreeService.getRootNodeByMbId(mbId));
		response.setCode(OpenResponseCode.SUCCESS);
		return response;
	}

	@Operation(summary = "根据节点编码查询下一层子节点", description = "根据节点编码查询下一层子节点")
	@GetMapping("/getSonNodes/{nodeCode}")
	public OpenResponse getSonNodes(@PathVariable("nodeCode") String nodeCode) {
		OpenResponse<List<M3ModelBaseTreeVo>> response = new OpenResponse<>(OpenResponseCode.ERROR);
		if (nodeCode != null) {
			response.setData(m3ModelBaseTreeService.getSonNodes(nodeCode));
			response.setCode(OpenResponseCode.SUCCESS);
		}
		return response;
	}

	@Operation(summary = "添加根节点", description = "添加根节点")
	@PostMapping("/addRootNode")
	public OpenResponse addRootNode(@RequestParam String nodeName, @RequestParam String mbId) {
		OpenResponse response = m3ModelBaseTreeService.addRootNode(nodeName, mbId);
		return response;
	}

	@Operation(summary = "添加子节点", description = "添加子节点")
	@PostMapping("/addSonNode/{parentNodeId}")
	public OpenResponse addSonNode(@PathVariable("parentNodeId") String parentNodeId,
			@RequestBody M3ModelBaseTree m3ModelBaseTree) {
		OpenResponse response = m3ModelBaseTreeService.addSonNode(parentNodeId, m3ModelBaseTree);
		return response;
	}

	@Operation(summary = "删除节点及其子节点", description = "删除节点及其子节点")
	@GetMapping("/deleteNodes/{nodeCode}")
	public OpenResponse deleteNodes(@PathVariable("nodeCode") String nodeCode) {
		OpenResponse response = new OpenResponse(OpenResponseCode.ERROR);
		try {
			response = m3ModelBaseTreeService.deleteNodesAndModels(nodeCode);
		}
		catch (Exception e) {
			response.setMessage("数据库删除操作失败，或者minio服务器操作失败！"); // 如果后台不捕获异常，响应消息将由前端\router\axios.js自动捕获处理，系统会显示：未知的错误
		}
		return response;
	}

}
