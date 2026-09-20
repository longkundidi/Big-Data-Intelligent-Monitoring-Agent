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

package sw.model3d.configGbomTree.controller;

import com.alibaba.fastjson.JSONObject;
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
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeVo;
import sw.model3d.configGbomTree.service.ConfigGbomTreeService;

import java.util.List;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-03-07 20:16:44
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configgbomtree")
@Tag(name = "GBOM树管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigGbomTreeController {

	private final ConfigGbomTreeService configGbomTreeService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param configGbomTree GBOM树
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	// @PreAuthorize("@pms.hasPermission('configGbomTree_configgbomtree_get')" )
	public R getConfigGbomTreePage(Page page, ConfigGbomTree configGbomTree) {
		return R.ok(configGbomTreeService.page(page, Wrappers.query(configGbomTree)));
	}

	/**
	 * 通过id查询GBOM树
	 * @param nodeId id
	 * @return R
	 */
	@Operation(summary = "通过id查询", description = "通过id查询")
	@GetMapping("/{nodeId}")
	// @PreAuthorize("@pms.hasPermission('configGbomTree_configgbomtree_get')" )
	public R getById(@PathVariable("nodeId") String nodeId) {
		return R.ok(configGbomTreeService.getById(nodeId));
	}

	/**
	 * 新增GBOM树
	 * @param configGbomTree GBOM树
	 * @return R
	 */
	@Operation(summary = "新增GBOM树", description = "新增GBOM树")
	@SysLog("新增GBOM树")
	@PostMapping
	// @PreAuthorize("@pms.hasPermission('configGbomTree_configgbomtree_add')" )
	public R save(@RequestBody ConfigGbomTree configGbomTree) {
		return R.ok(configGbomTreeService.save(configGbomTree));
	}

	/**
	 * 通过id删除GBOM树
	 * @param nodeId id
	 * @return R
	 */
	@Operation(summary = "通过id删除GBOM树", description = "通过id删除GBOM树")
	@SysLog("通过id删除GBOM树")
	@DeleteMapping("/{nodeId}")
	// @PreAuthorize("@pms.hasPermission('configGbomTree_configgbomtree_del')" )
	public R removeById(@PathVariable String nodeId) {
		return R.ok(configGbomTreeService.removeById(nodeId));
	}

	@Operation(summary = "获取模板的所有树节点", description = "获取模板的所有树节点")
	@GetMapping("/getTreeNodes/{nodeLevel}")
	public OpenResponse getTreeNodes(@PathVariable("nodeLevel") Integer nodeLevel) {
		OpenResponse<List<ConfigGbomTreeVo>> response = new OpenResponse<>(OpenResponseCode.ERROR);
		response.setData(configGbomTreeService.getTreeNodes(nodeLevel));
		response.setCode(OpenResponseCode.SUCCESS);
		return response;
	}

	@Operation(summary = "获取当前场景的元结构树", description = "获取当前场景的元结构树")
	@GetMapping("/reqTreeNodesBySceneId")
	public OpenResponse reqTreeNodesBySceneId(@RequestParam String sceneId, @RequestParam Integer nodeLevel) {
		OpenResponse<List<ConfigGbomTreeVo>> response = new OpenResponse<>(OpenResponseCode.ERROR);
		if (sceneId != null) {
			response.setData(configGbomTreeService.getTreeNodesBySceneId(sceneId, nodeLevel));
			response.setCode(OpenResponseCode.SUCCESS);
		}
		return response;
	}

	/**
	 * 查询型号proId对应的所有元结构树的节点
	 * @param proId 型号Id
	 * @return List<ConfigG> ids 节点id
	 */
	@Operation(summary = "查询型号proId对应的所有元结构树的节点", description = "查询型号proId对应的所有元结构树的节点")
	@SysLog("查询型号proId对应的所有元结构树的节点")
	@GetMapping("/getGBomTreeBySceneIdAndProId")
	public R getGBomTreeBySceneIdAndProId(@RequestParam String sceneId, @RequestParam String proId,
			@RequestParam Integer nodeLevel) {
		return R.ok(configGbomTreeService.getGBomTreeBySceneIdAndProId(sceneId, proId, nodeLevel));
	}

	@Operation(summary = "根据节点编码查询下一层子节点", description = "根据节点编码查询下一层子节点")
	@GetMapping("/getSonNodes/{nodeCode}")
	public OpenResponse getSonNodes(@PathVariable("nodeCode") String nodeCode) {
		OpenResponse<List<ConfigGbomTreeVo>> response = new OpenResponse<>(OpenResponseCode.ERROR);
		if (nodeCode != null) {
			response.setData(configGbomTreeService.getSonNodes(nodeCode));
			response.setCode(OpenResponseCode.SUCCESS);
		}
		return response;
	}

	@Operation(summary = "根据节点编码和场景id查询下一层子节点", description = "根据节点编码和场景id查询下一层子节点")
	@GetMapping("/reqSonNodesBySceneId")
	public OpenResponse reqSonNodesBySceneId(@RequestParam String sceneId, @RequestParam String nodeCode) {
		OpenResponse<List<ConfigGbomTreeVo>> response = new OpenResponse<>(OpenResponseCode.ERROR);
		if (nodeCode != null && sceneId != null) {
			response.setData(configGbomTreeService.getSonNodesBySceneId(sceneId, nodeCode));
			response.setCode(OpenResponseCode.SUCCESS);
		}
		return response;
	}

	/**
	 * 查询型号proId对应的元结构子节点
	 * @param proId 型号Id
	 * @return List<ConfigG> ids 节点id
	 */
	@Operation(summary = "查询型号proId对应的所有原结构子节点", description = "查询型号proId对应的所有原结构子节点")
	@SysLog("查询型号proId对应的所有原结构子节点")
	@GetMapping("/getGBomSonTreeBySceneIdAndProId")
	public R getGBomSonTreeBySceneIdAndProId(@RequestParam String sceneId, @RequestParam String proId,
			@RequestParam String nodeCode) {
		return R.ok(configGbomTreeService.getGBomSonTreeBySceneIdAndProId(sceneId, proId, nodeCode));
	}

	@Operation(summary = "添加子节点",
			description = "添加子moMetatree = {ConfigGbomTree@15469} \"ConfigGbomTree(nodeId=null, nodeCode=null, userCode=null, nodeName=11, nodeNo=null, swsort=null, nodeLevel=null, nodeType=null, modelCode=null, modelFileurl=null, urlImg=null, modelSize=null, modelType=null, xcoordinate=null, ycoordinate=null, zcoordinate=null, xrotationAngle=null, yrotationAngle=null, zrotationAngle=null, memo=22, projCode=null)\"节点")
	@PostMapping("/addSonNode/{parentNodeId}")
	public R addSonNode(@PathVariable("parentNodeId") String parentNodeId, @RequestBody ConfigGbomTree configGbomTree) {
		ConfigGbomTreeVo response = configGbomTreeService.addSonNode(parentNodeId, configGbomTree);
		return R.restResult(response, 200, "添加成功");
	}

	/**
	 * TODO待删除：没有使用sceneId
	 * @param nodeCode
	 * @return
	 */
	@Operation(summary = "删除节点及其子节点", description = "删除节点及其子节点")
	@GetMapping("/deleteNodes/{nodeCode}")
	public OpenResponse deleteNodes(@PathVariable("nodeCode") String nodeCode) {
		OpenResponse response = new OpenResponse(OpenResponseCode.ERROR);
		try {
			response = configGbomTreeService.deleteNodesAndModels(nodeCode);
		}
		catch (Exception e) {
			response.setMessage("节点删除失败"); // 如果后台不捕获异常，响应消息将由前端\router\axios.js自动捕获处理，系统会显示：未知的错误
		}
		return response;
	}

	@Operation(summary = "删除节点及其子节点", description = "删除节点及其子节点")
	@GetMapping("/deleteNodesBySceneId/{sceneId}/{nodeCode}")
	public OpenResponse deleteNodesBySceneId(@PathVariable("sceneId") String sceneId,
			@PathVariable("nodeCode") String nodeCode) {
		OpenResponse response = new OpenResponse(OpenResponseCode.ERROR);
		try {
			response = configGbomTreeService.deleteNodesBySceneId(sceneId, nodeCode);
		}
		catch (Exception e) {
			response.setMessage("节点删除失败"); // 如果后台不捕获异常，响应消息将由前端\router\axios.js自动捕获处理，系统会显示：未知的错误
		}
		return response;
	}

	/**
	 * 修改GBOM树节点
	 * @param configGbomTree GBOM树
	 * @return R
	 */
	@Operation(summary = "修改GBOM树节点", description = "修改GBOM树节点")
	@SysLog("修改GBOM树节点")
	@PutMapping
	// @PreAuthorize("@pms.hasPermission('configGbomTree_configgbomtree_edit')" )
	public R updateById(@RequestBody ConfigGbomTree configGbomTree) {
		ConfigGbomTreeVo response = configGbomTreeService.swUpdateById(configGbomTree);
		return R.restResult(response, 200, "修改成功");
	}

	@Operation(summary = "根据节点编码复制一颗子树", description = "根据节点编码复制一颗子树")
	@GetMapping("/copyNodeByNodeCode/{nodeCode}")
	public OpenResponse copyNodeByNodeCode(@PathVariable("nodeCode") String nodeCode) {
		OpenResponse<ConfigGbomTreeVo> response = new OpenResponse<>(OpenResponseCode.ERROR);
		if (nodeCode != null) {
			ConfigGbomTreeVo configGbomTreeVo = configGbomTreeService.copyNodeByNodeCode(nodeCode);
			if (configGbomTreeVo == null) {
				response.setCode(4008);
				response.setMessage("复制失败 or 根节点不可复制");
				return response;
			}
			response.setData(configGbomTreeVo);
			response.setCode(200);
			response.setMessage("复制成功");
		}
		return response;
	}

	/**
	 * 移动节点，移动为目标节点的子节点
	 * @param sourceNodeCode 源节点的编码
	 * @param targetNodeCode 目标节点的编码
	 * @return
	 */
	@Operation(summary = "移动节点", description = "移动节点")
	@PostMapping("/moveNode")
	public OpenResponse moveNode(@RequestParam String sourceNodeCode, @RequestParam String targetNodeCode) {
		OpenResponse<ConfigGbomTreeVo> response = new OpenResponse<>(OpenResponseCode.ERROR);
		response.setMessage("移动失败");
		ConfigGbomTreeVo configGbomTreeVo = configGbomTreeService.moveNode(sourceNodeCode, targetNodeCode);
		if (configGbomTreeVo != null) {
			response.setData(configGbomTreeVo);
			response.setCode(200);
			response.setMessage("移动成功");
		}
		return response;
	}

	/**
	 * 新增GBOM根节点
	 * @param configGbomTree GBOM根节点
	 * @return OpenResponse
	 */
	@Operation(summary = "新增GBOM根节点", description = "新增GBOM根节点")
	@SysLog("新增GBOM树")
	@PostMapping("/addRootNode")
	public OpenResponse addRootNode(@RequestBody ConfigGbomTree configGbomTree) {
		OpenResponse<ConfigGbomTreeVo> response = new OpenResponse<>(OpenResponseCode.ERROR);
		response.setMessage("添加根节点失败");
		ConfigGbomTreeVo configGbomTreeVo = configGbomTreeService.addRootNode(configGbomTree);
		if (configGbomTreeVo != null) {
			response.setData(configGbomTreeVo);
			response.setCode(200);
			response.setMessage("添加根节点成功");
		}
		return response;
	}

	/**
	 * 新增gbom树：导入场景元结构树
	 * @param list 结构信息数组-json
	 * @return R
	 */
	@Operation(summary = "新增GBOM树", description = "新增GBOM树")
	@SysLog("新增GBOM树")
	@PostMapping("/createProGbomTree")
	public R createProGbomTree(@RequestBody List<List<JSONObject>> list) {
		return R.ok(configGbomTreeService.createProGbomTree(list));
	}

	/**
	 * 通过sceneId得到所有节点id
	 * @param sceneId 场景Id
	 * @return List<String> ids 节点id
	 */
	@Operation(summary = "通过sceneId得到所有节点id", description = "通过sceneId得到所有节点id")
	@SysLog("通过sceneId得到所有节点id")
	@GetMapping("/getAllNodeIdsBySceneId")
	public R getAllNodeIdsBySceneId(@RequestParam String sceneId) {
		return R.ok(configGbomTreeService.getAllNodeIdsBySceneId(sceneId));
	}

}
