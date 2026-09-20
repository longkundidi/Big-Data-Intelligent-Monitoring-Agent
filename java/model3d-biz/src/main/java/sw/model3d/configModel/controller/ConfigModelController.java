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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sw.AlResumeData.service.AlResumeDataService;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.model3d.ConfigBomPerceivedVariable.service.ConfigBomPerceivedVariableService;
import sw.model3d.configModel.entity.*;
import sw.model3d.configModel.entity.vo.ConfigBomTreeVo;
import sw.model3d.configModel.entity.vo.ConfigComponentMatchVo;
import sw.model3d.configModel.entity.vo.ConfigVariableMatchVo;
import sw.model3d.configModel.service.ConfigBomTreeService;
import sw.model3d.configModel.service.ConfigModelService;
import sw.model3d.configModel.service.ConfigModelVariableService;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.model3d.configPerceivedVariable.service.ConfigPerceivedVariableService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author pig code generator
 * @date 2024-04-01 10:43:50
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configmodel")
@Tag(name = "管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigModelController {

	private final ConfigModelService configModelService;

	private final ConfigModelVariableService configModelVariableService;

	private final ConfigPerceivedVariableService configPerceivedVariableService;

	private final ConfigBomTreeService configBomTreeService;

	private final ConfigBomPerceivedVariableService configBomPerceivedVariableService;

	private final AlResumeDataService alResumeDataService;

	/**
	 * 通过节点id分页查询
	 */
	@Operation(summary = "通过节点id分页查询", description = "通过节点id分页查询")
	@PostMapping("/page")
	// @PreAuthorize("@pms.hasPermission('configModel_configmodel_get')" )
	public R getConfigModelPage(@RequestBody ConfigModelPageQueryRequest queryRequest) {
		int current = queryRequest.getCurrent();
		int pageSize = queryRequest.getPageSize();
		String nodeId = queryRequest.getNodeId();
		String modelType = queryRequest.getModelType();
		Page<ConfigModel> page = configModelService.page(new Page<>(current, pageSize),
				new QueryWrapper<ConfigModel>().lambda()
					.eq(ConfigModel::getNodeId, nodeId)
					.eq(ConfigModel::getModelType, modelType)
					.eq(ConfigModel::getIsService, 0));
		return R.ok(page, "查询成功");
	}

	/**
	 * 通过modelId查询相关感知变量
	 */
	@Operation(summary = "通过modelId查询相关感知变量", description = "通过modelId查询相关感知变量")
	@PostMapping("/pageByModelId")
	// @PreAuthorize("@pms.hasPermission('configModel_configmodel_get')" )
	public R getPVByModelId(@RequestBody ConfigModelPageQueryRequestByModelId queryRequest) {
		int current = queryRequest.getCurrent();
		int pageSize = queryRequest.getPageSize();
		String modelId = queryRequest.getModelId();
		Page<ConfigPerceivedVariable> pageByModelId = configPerceivedVariableService.getPageByModelId(current, pageSize,
				modelId);
		return R.ok(pageByModelId, "查询成功");
	}

	/**
	 * 通过节点id和类型查询
	 */
	@Operation(summary = "通过节点id和类型查询", description = "通过节点id和类型查询")
	@PostMapping("/query")
	// @PreAuthorize("@pms.hasPermission('configModel_configmodel_get')" )
	public R getByNodeId(@RequestBody ConfigModelQueryRequest queryRequest) {
		String nodeId = queryRequest.getNodeId();
		String modelType = queryRequest.getModelType();
		return R.ok(configModelService.getByNodeIdAndModelType(nodeId, modelType), "查询成功");
	}

	/**
	 * 通过状态感知任务id查询到对应节点的故障诊断任务
	 */
	@Operation(summary = "通过状态感知任务id查询到对应节点的故障诊断任务", description = "通过状态感知任务id查询到对应节点的故障诊断任务")
	@GetMapping("/getDiagnosis")
	// @PreAuthorize("@pms.hasPermission('configModel_configmodel_get')" )
	public R getDiagnosis(@RequestParam Long taskId) {

		String nodeId = configModelService.getNodeId(taskId);
		return R.ok(configModelService.getByNodeIdAndModelType(nodeId, "failure"), "查询成功");
	}

	/**
	 * 通过id查询
	 */
	@Operation(summary = "通过id查询", description = "通过id查询")
	@GetMapping("/{modelId}")
	// @PreAuthorize("@pms.hasPermission('configModel_configmodel_get')" )
	public R getById(@PathVariable String modelId) {
		ConfigModel res = configModelService.getById(modelId);
		if (res != null) {
			return R.ok(res, "查询成功");
		}
		return R.failed("查询失败！");
	}

	/**
	 * 新增故障诊断模板
	 * @param configModel
	 * @return R
	 */
	@Operation(summary = "新增", description = "新增")
	@SysLog("新增")
	@PostMapping
	// @PreAuthorize("@pms.hasPermission('configModel_configmodel_add')" )
	public R save(@RequestBody ConfigModel configModel) {
		configModel.setIsService(0);
		boolean isSaved = configModelService.save(configModel);
		if (isSaved) {
			return R.ok(configModel, "新增成功");
		}
		else {
			return R.failed("新增失败");
		}
	}

	/**
	 * 修改
	 * @param configModel
	 * @return R
	 */
	@Operation(summary = "修改", description = "修改")
	@SysLog("修改")
	@PutMapping
	// @PreAuthorize("@pms.hasPermission('configModel_configmodel_edit')")
	public R updateById(@RequestBody ConfigModel configModel) {
		boolean isUpdated = configModelService.updateById(configModel);
		if (isUpdated) {
			return R.ok(configModel, "修改成功");
		}
		return R.failed(null, "修改失败，ID错误");
	}

	/**
	 * 通过id删除
	 * @param modelId id
	 * @return R
	 */
	@Operation(summary = "通过id删除", description = "通过id删除")
	@SysLog("通过id删除")
	@DeleteMapping("/delete")
	// @PreAuthorize("@pms.hasPermission('configModel_configmodel_del')")
	public R removeById(@RequestParam String modelId, @RequestParam String modelType) {
		boolean isDeleted = configModelService.removeById(modelId);
		if (modelType.equals("perceived") || modelType.equals("composition")) {
			// 删除感知变量
			LambdaQueryWrapper<ConfigModelVariable> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(ConfigModelVariable::getModelId, modelId);
			int delete = configModelVariableService.getBaseMapper().delete(queryWrapper);
			if (isDeleted && delete > 0) {
				return R.ok(null, "删除成功");
			}
			else {
				return R.failed("删除失败");
			}
		}
		else {
			if (isDeleted) {
				return R.ok(null, "删除成功");
			}
			else {
				return R.failed("删除失败");
			}
		}

	}

	@Operation(summary = "新增状态感知模型", description = "新增状态感知模型")
	@PostMapping("/addService")
	@Transactional
	public R addService(@RequestBody List<Object> objList) {
		ConfigModel configModel = JSON.parseObject(JSON.toJSONString(objList.get(0)), ConfigModel.class);// 取对象数组的第一个元素，转换为Java对象
		configModel.setIsService(0);
		configModelService.save(configModel); // 新增服务

		if (configModel.getModelType().equals("perceived") || configModel.getModelType().equals("composition")) {
			int size = configModelVariableService.batchAdd(configModel.getModelId(),
					(List<LinkedHashMap>) objList.get(1));// 填写关联表
			if (size > 0) {
				return R.ok(null, "新增成功");
			}
			return R.ok(null, "可能没有选择感知变量");
		}

		return R.ok(null, "新增成功");

	}

	@Operation(summary = "编辑模型", description = "编辑模型")
	@PostMapping("/editService")
	@Transactional
	public R editService(@RequestBody List<Object> objList) {
		ConfigModel configModel = JSON.parseObject(JSON.toJSONString(objList.get(0)), ConfigModel.class);// 取对象数组的第一个元素，转换为Java对象
		boolean modelIsUpdated = configModelService.updateById(configModel);// 编辑服务

		if (configModel.getModelType().equals("perceived") || configModel.getModelType().equals("composition")) {
			boolean varIsUpdate = configModelVariableService.batchEdit(configModel.getModelId(),
					(List<LinkedHashMap>) objList.get(1));// 填写关联表
			if (modelIsUpdated && varIsUpdate) {
				return R.ok(null, "编辑成功");
			}
			return R.ok(null, "编辑失败");
		}

		return R.ok(null, "编辑成功");

	}

	@Operation(summary = "机组零部件匹配", description = "机组零部件匹配")
	@GetMapping("/componentMatch")
	@Transactional
	public R componentMatch(@RequestParam String proName, @RequestParam String productModel,
			@RequestParam String sceneId) {

		// 1. 获取当前项目的所有结构树未进行匹配或匹配未成功机组
		List<ConfigBomTree> turbines = configBomTreeService.findAllTurbine(proName, productModel);
		// 2. 获取每个机组的零部件并进行匹配
		List<ConfigComponentMatchVo> configComponentMatchVos = new ArrayList<>();

		for (ConfigBomTree turbine : turbines) {
			List<String> componentCodes = configBomTreeService.findComponentCodes(turbine.getTurbineCode());
			ConfigComponentMatchVo configComponentMatchVo = configBomTreeService.componentMatch(componentCodes, turbine,
					sceneId);

			turbine.setIsBomPassed(configComponentMatchVo.getIsBomPassed());
			configBomTreeService.updateById(turbine);

			configComponentMatchVo.setTurbineCode(turbine.getTurbineCode());
			configComponentMatchVo.setTurbineName(turbine.getNodeName());
			configComponentMatchVos.add(configComponentMatchVo);
		}
		return R.ok(configComponentMatchVos);
	}

	@Operation(summary = "节点感知变量匹配", description = "节点感知变量匹配")
	@GetMapping("/variableMatch")
	@Transactional
	public R variableMatch(@RequestParam String proName, @RequestParam String productModel,
			@RequestParam String sceneId) {

		// 1. 获取当前项目的所有通过结构树匹配的机组
		List<ConfigBomTree> turbinesMatched = configBomTreeService.findAllBomMatchedTurbine(proName, productModel);
		// 2. 获取每个机组的实例感知变量并进行感知变量匹配
		List<ConfigVariableMatchVo> configComponentMatchVos = new ArrayList<>();

		for (ConfigBomTree turbine : turbinesMatched) {

			ConfigVariableMatchVo configVariableMatchVo = new ConfigVariableMatchVo();
			configVariableMatchVo.setTurbineCode(turbine.getTurbineCode());
			configVariableMatchVo.setTurbineName(turbine.getNodeName());

			List<String> variables = configBomPerceivedVariableService.findVariables(turbine.getTurbineCode());
			configVariableMatchVo
				.setVariableNotMatches(configBomPerceivedVariableService.variableMatch(variables, sceneId));

			if (configVariableMatchVo.getVariableNotMatches().isEmpty()) {
				configVariableMatchVo.setIsValPassed(1);
				turbine.setIsValPassed(1);
			}
			else {
				configVariableMatchVo.setIsValPassed(0);
				turbine.setIsValPassed(0);
			}
			configBomTreeService.updateById(turbine);

			configVariableMatchVo.setIsBomPassed(turbine.getIsBomPassed());

			configComponentMatchVos.add(configVariableMatchVo);
		}
		return R.ok(configComponentMatchVos);
	}

	@Operation(summary = "获取当前项目当前机型所有通过匹配的风机节点", description = "获取当前项目当前机型所有通过匹配的风机节点")
	@GetMapping("/getComponentNodes")
	public OpenResponse getComponentNodes(@RequestParam String nodeLevel, @RequestParam Long proId) {
		OpenResponse<List<ConfigBomTreeVo>> response = new OpenResponse<>(OpenResponseCode.ERROR);
		response.setMessage("查询失败");
		List<ConfigBomTreeVo> configBomTreeList = configBomTreeService.getComponentNodes(proId, nodeLevel);
		response.setData(configBomTreeList);
		response.setCode(200);
		response.setMessage("查询成功");
		return response;
	}

	@Operation(summary = "根据项目节点编码和turbineCode查询所有通过匹配的子节点", description = "根据项目节点编码和turbineCode查询所有通过匹配的子节点")
	@GetMapping("/getBomSons")
	public OpenResponse getBomSons(@RequestParam String nodeCode, @RequestParam String turbineCode) {

		OpenResponse<List<ConfigBomTreeVo>> response = new OpenResponse<>(OpenResponseCode.ERROR);
		response.setMessage("查询失败");
		List<ConfigBomTreeVo> configBomTreeList = configBomTreeService.getBomSons(nodeCode, turbineCode);
		response.setData(configBomTreeList);
		response.setCode(200);
		response.setMessage("查询成功");
		return response;
	}

	/* 以下接口均与模板template相同 */
	@Operation(summary = "根据项目节点编码和turbinecode查询子节点", description = "根据项目名、机型和节点编码查询子节点")
	@GetMapping("/getSonNodes")
	public R getSonNodes(@RequestParam String nodeCode, @RequestParam String turbineCode) {
		return R.ok(configBomTreeService.getSonNodes(nodeCode, turbineCode), "查询成功");
	}

	@Operation(summary = "根据项目proid节点级别获取所有树节点", description = "根据项目名、机型和节点级别获取所有树节点")
	@GetMapping("/getTreeNodes/{proId}")
	public R getTreeNodes(@PathVariable Long proId, @RequestParam String nodeLevel) {
		return R.ok(configBomTreeService.getTreeNodes(proId, nodeLevel), "查询成功");
	}

	/**
	 * 导入机组数据
	 */
	@Operation(summary = "导入机组数据", description = "导入机组数据")
	@PostMapping("/uploadUnitData")
	public R uploadUnitData(@RequestBody List<List<JSONObject>> list) {
		return R.ok(configBomTreeService.uploadUnitData(list), "导入机组数据成功");
	}

	@Operation(summary = "更新机组数据", description = "导入机组数据")
	@PostMapping("/updateUnitData")
	public R updateUnitData(@RequestBody ConfigBomTreeVo configBomTreeVo) {
		if (configBomTreeVo.getId() == null) {
			return R.failed("id不能为空");
		}
		if (configBomTreeService.updateUnitData(configBomTreeVo)) {
			return R.ok("修改机组数据成功");
		}
		return R.failed("修改机组数据失败");

	}

	@Operation(summary = "根据父节点id (parentNodeId)、子节点的节点信息（ConfigBomTreeTemplate）、添加子节点",
			description = "根据项目名、机型和节点编码查询子节点")
	@PostMapping("/addSonNode/{parentNodeId}")
	public R addSonNode(@PathVariable String parentNodeId, @RequestBody ConfigBomTree configBomTree) {

		return R.ok(configBomTreeService.addSonNode(parentNodeId, configBomTree), "新增成功");
	}

	@Operation(summary = "根据节点id获取模板对象", description = "根据节点id获取模板对象")
	@GetMapping("/getConfigBomTree/{nodeId}")
	public R getConfigBomTree(@PathVariable String nodeId) {
		return R.ok(configBomTreeService.getNode(nodeId), "查询成功");
	}

	@Operation(summary = "根据节点id编辑节点信息", description = "根据节点id编辑节点信息")
	@PutMapping("/editNodeInfo")
	public R editNodeInfo(@RequestBody ConfigBomTree nodeForm) {
		return R.ok(configBomTreeService.editNodeInfo(nodeForm), "编辑成功");
	}

	@Operation(summary = "根据项目号proId节点nodeId删除节点及其子节点", description = "根据节点nodeId删除节点及其子节点")
	@GetMapping("/delNode/{proId}")
	public R delNode(@PathVariable Long proId, @RequestParam String turbineCode, @RequestParam String nodeCode) {
		configBomTreeService.delNode(proId, turbineCode, nodeCode);
		return R.ok("删除成功");
	}

	@Operation(summary = "根据turbineCode和节点编码nodeCode复制一棵子树", description = "根据nodeId和节点编码nodeCode复制一棵子树")
	@PutMapping("/copySonNode")
	public OpenResponse copySonNode(@RequestParam String turbineCode, @RequestParam String nodeCode) {
		return configBomTreeService.copySonNode(turbineCode, nodeCode);
	}

	@Operation(summary = "根据proId、原节点编码nodeCode和目标节点编码nodeCode移动节点",
			description = "根据proId、原节点编码nodeCode和目标节点编码nodeCode移动节点")
	@PostMapping("/moveNode")
	public OpenResponse moveNode(@RequestParam String turbineCode, @RequestParam String sourceNodeCode,
			@RequestParam String targetNodeCode) {
		OpenResponse<ConfigBomTreeVo> response = new OpenResponse<>(OpenResponseCode.ERROR);
		response.setMessage("移动失败");
		ConfigBomTreeVo configBomTreeTemplateVo = configBomTreeService.moveNode(turbineCode, sourceNodeCode,
				targetNodeCode);
		if (configBomTreeTemplateVo != null) {
			response.setData(configBomTreeTemplateVo);
			response.setCode(200);
			response.setMessage("移动成功");
		}
		return response;

	}

	// 查找部件对应的测点
	@PostMapping("/getLocation")
	public R getLocation(@RequestParam String turbineCode, @RequestParam String nodeCode) {
		return R.ok(configBomTreeService.getLocation(turbineCode, nodeCode));
	}

	@Operation(summary = "根据风场和机型获取实例风机列表（含运行状态）", description = "根据风场和机型获取实例风机列表（含运行状态）")
	@GetMapping("/getStateOfTree")
	public R getStateOfTree(@RequestParam String farmName, @RequestParam String turbineModel) {
		return R.ok(configBomTreeService.getStateOfTree(farmName, turbineModel), "查询成功");
	}

	@Operation(summary = "根据风场获取所有实例风机列表（含运行状态）", description = "根据风场获取所有实例风机列表（含运行状态）")
	@GetMapping("/getStateOfTreeByFarm")
	public R getStateOfTreeByFarm(@RequestParam String farmName) {
		return R.ok(configBomTreeService.getStateOfTreeByFarm(farmName), "查询成功");
	}

	@Operation(summary = "根据风场获取所有实例风机列表（按运行中任务判断接入状态）", description = "根据风场获取所有实例风机列表（按运行中任务判断接入状态）")
	@GetMapping("/getAccessStateOfTreeByFarm")
	public R getAccessStateOfTreeByFarm(@RequestParam String farmName) {
		return R.ok(configBomTreeService.getAccessStateOfTreeByFarm(farmName), "查询成功");
	}

	@Operation(summary = "获取风场", description = "根据风场和机型获取实例风机列表（含运行状态）")
	@GetMapping("/getOverviewOfFarms")
	public R getOverviewOfFarms(@RequestParam Long userId) {
		return R.ok(configBomTreeService.getOverviewOfFarms(userId), "查询成功");
	}

	@GetMapping("/searchNode")
	public R searchNode(@RequestParam String searchKey) {
		return R.ok(configBomTreeService.searchNode(searchKey));
	}

}
