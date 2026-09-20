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

package sw.model3d.configPerceivedVariable.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import sw.model3d.configBomPerceivedVariableTemplate.entity.ConfigBomPerceivedVariableTemplate;
import sw.model3d.configBomPerceivedVariableTemplate.service.ConfigBomPerceivedVariableTemplateService;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeDTO;
import sw.model3d.configGbomTree.service.ConfigGbomTreeService;
import sw.model3d.configModel.entity.ConfigModelVariable;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariableQueryRequest;
import sw.model3d.configPerceivedVariable.mapper.ConfigPerceivedVariableMapper;
import sw.model3d.configPerceivedVariable.service.ConfigPerceivedVariableService;

import java.util.List;
import java.util.Set;

/**
 * @author pig code generator
 * @date 2024-03-22 19:24:18
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configperceivedvariable")
@Tag(name = "感知变量配置")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigPerceivedVariableController {

	private final ConfigPerceivedVariableService configPerceivedVariableService;

	private final ConfigPerceivedVariableMapper configPerceivedVariableMapper;

	private final ConfigGbomTreeService configGbomTreeService;

	private final ConfigBomPerceivedVariableTemplateService configBomPerceivedVariableTemplateService;

	/**
	 * 通过节点id分页查询
	 */
	@Operation(summary = "通过节点id分页查询", description = "通过节点id分页查询")
	@PostMapping("/page")
	// @PreAuthorize("@pms.hasPermission('configPerceivedVariable_configperceivedvariable_get')"
	// )
	public R getConfigPerceivedVariablePage(@RequestBody ConfigPerceivedVariableQueryRequest queryRequest) {
		int current = queryRequest.getCurrent();
		int pageSize = queryRequest.getPageSize();
		String nodeId = queryRequest.getNodeId();
		Page<ConfigPerceivedVariable> page = configPerceivedVariableService.page(new Page<>(current, pageSize),
				new QueryWrapper<ConfigPerceivedVariable>().lambda().eq(ConfigPerceivedVariable::getNodeId, nodeId));
		return R.ok(page, "查询成功");
	}

	/**
	 * 通过节点id分页查询，同时通过nodeType
	 */
	@Operation(summary = "通过节点id分页查询", description = "通过节点id分页查询")
	@PostMapping("/getPageByNodeType")
	// @PreAuthorize("@pms.hasPermission('configPerceivedVariable_configperceivedvariable_get')")
	public R getPageByNodeType(@RequestBody ConfigPerceivedVariableQueryRequest queryRequest) {
		int current = queryRequest.getCurrent();
		int pageSize = queryRequest.getPageSize();
		String nodeId = queryRequest.getNodeId();
		String nodeType = queryRequest.getNodeType();

		IPage<?> page; // 使用泛型通配符，兼容两种不同类型

		if (StringUtils.equals(nodeType, "GBOM")) {
			page = configPerceivedVariableService.page(new Page<>(current, pageSize),
					new QueryWrapper<ConfigPerceivedVariable>().lambda()
						.eq(ConfigPerceivedVariable::getNodeId, nodeId));
		}
		else {
			page = configBomPerceivedVariableTemplateService.page(new Page<>(current, pageSize),
					new QueryWrapper<ConfigBomPerceivedVariableTemplate>().lambda()
						.eq(ConfigBomPerceivedVariableTemplate::getNodeId, nodeId));
		}

		return R.ok(page, "查询成功");
	}

	/**
	 * 查询所有数据
	 */
	@Operation(summary = "查询所有数据", description = "查询所有数据")
	@PostMapping("/getAllByNodeType")
	// @PreAuthorize("@pms.hasPermission('configPerceivedVariable_configperceivedvariable_get')")
	public R getAllByNodeType(@RequestBody ConfigPerceivedVariableQueryRequest queryRequest) {
		String nodeId = queryRequest.getNodeId();
		String nodeType = queryRequest.getNodeType();

		if (StringUtils.equals(nodeType, "GBOM")) {
			List<ConfigPerceivedVariable> list = configPerceivedVariableService
				.list(new QueryWrapper<ConfigPerceivedVariable>().lambda()
					.eq(ConfigPerceivedVariable::getNodeId, nodeId));
			return R.ok(list, "查询成功");
		}
		else {
			List<ConfigBomPerceivedVariableTemplate> list = configBomPerceivedVariableTemplateService
				.list(new QueryWrapper<ConfigBomPerceivedVariableTemplate>().lambda()
					.eq(ConfigBomPerceivedVariableTemplate::getNodeId, nodeId));
			return R.ok(list, "查询成功");
		}
	}

	/**
	 * 获取该节点全部感知变量
	 */
	@Operation(summary = "获取该节点全部感知变量", description = "获取该节点全部感知变量")
	@GetMapping("/getListByNodeId")
	public R getListByNodeId(@RequestParam String nodeId) {
		List<ConfigPerceivedVariable> list = configPerceivedVariableService
			.list(new QueryWrapper<ConfigPerceivedVariable>().lambda().eq(ConfigPerceivedVariable::getNodeId, nodeId));
		return R.ok(list, "查询成功");
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
		return R.ok(configPerceivedVariableService.getByNodeId(nodeId), "查询成功");
	}

	/**
	 * 新增
	 * @param configPerceivedVariable
	 * @return R
	 */
	@Operation(summary = "新增", description = "新增")
	@SysLog("新增")
	@PostMapping
	// @PreAuthorize("@pms.hasPermission('configPerceivedVariable_configperceivedvariable_add')"
	// )
	public R save(@RequestBody ConfigPerceivedVariable configPerceivedVariable) {
		boolean isSaved = configPerceivedVariableService.save(configPerceivedVariable);
		if (isSaved) {
			return R.ok(configPerceivedVariable, "新增成功");
		}
		else {
			return R.failed("新增失败");
		}
	}

	/**
	 * 修改
	 * @param configPerceivedVariable
	 * @return R
	 */
	@Operation(summary = "修改", description = "修改")
	@SysLog("修改")
	@PutMapping
	// @PreAuthorize("@pms.hasPermission('configPerceivedVariable_configperceivedvariable_edit')"
	// )
	public R updateById(@RequestBody ConfigPerceivedVariable configPerceivedVariable) {
		boolean isUpdated = configPerceivedVariableService.updateById(configPerceivedVariable);
		configBomPerceivedVariableTemplateService.updateByVarId(configPerceivedVariable);
		if (isUpdated) {
			return R.ok(configPerceivedVariable, "修改成功");
		}
		return R.failed(null, "修改失败，ID错误");
	}

	/**
	 * 通过id删除gbom感知变量，同时删除机型中对应的感知变量
	 * @param varId id
	 * @return R
	 */
	@Operation(summary = "通过id删除", description = "通过id删除")
	@SysLog("通过id删除")
	@DeleteMapping("/{varId}")
	// @PreAuthorize("@pms.hasPermission('configPerceivedVariable_configperceivedvariable_del')"
	// )
	public R removeById(@PathVariable String varId) {
		boolean isDeleted = configPerceivedVariableService.removeById(varId);
		configBomPerceivedVariableTemplateService.deleteByVarId(varId);
		if (isDeleted) {
			return R.ok(null, "删除成功");
		}
		return R.failed("删除失败");
	}

	@Operation(summary = "通过模型ID查询相关的感知变量-详情信息", description = "通过模型ID查询相关的感知变量-详情信息")
	@SysLog("通过模型ID查询相关的感知变量-详情信息")
	@GetMapping("/getByModelId/{modelId}")
	public R getByModelId(@PathVariable String modelId) {
		List<ConfigPerceivedVariable> res = configPerceivedVariableService.getByModelId(modelId);
		if (!res.isEmpty()) {
			return R.ok(res, "查询成功");
		}
		return R.failed("查询失败！可能是没有相关感知变量");
	}

	@Operation(summary = "通过模型ID查询相关的感知变量-ID", description = "通过模型ID查询相关的感知变量-ID")
	@SysLog("通过模型ID查询相关的感知变量-ID")
	@GetMapping("/getIDByModelId/{modelId}")
	public R getIdByModelId(@PathVariable String modelId) {
		List<ConfigModelVariable> res = configPerceivedVariableService.getIDByModelId(modelId);
		if (!res.isEmpty()) {
			return R.ok(res, "查询成功");
		}
		return R.failed("查询失败！可能是没有相关感知变量");
	}

	@Operation(summary = "获得感知变量总数", description = "获得感知变量总数")
	@SysLog("获得感知变量总数")
	@GetMapping("/all")
	public R getTotalNum() {
		Long count = configPerceivedVariableMapper.selectCount(new QueryWrapper<ConfigPerceivedVariable>());

		return R.ok(count);
	}

	@GetMapping("/getVariebleCount")
	public R getVariebleCount() {
		return R.ok(configPerceivedVariableService.count());
	}

	@Operation(summary = "获得对应元结构树的所有感知变量", description = "获得对应元结构树的所有感知变量")
	@SysLog("获得对应元结构树的所有感知变量")
	@PostMapping("/getAllVariablesByNodes")
	public R getAllVariablesByNodes(@RequestBody List<ConfigGbomTreeDTO> selectNodes) {
		Set<String> nodeIds = configGbomTreeService.getAllNodIdsBySelectNodes(selectNodes);
		return R.ok(configPerceivedVariableService.getAllVariablesByNodeIds(nodeIds));
	}

	@Operation(summary = "gBom感知变量匹配", description = "gBom感知变量匹配")
	@SysLog("gBom感知变量匹配")
	@PostMapping("/gBomVariablesMatch/{sceneId}")
	public R gBomVariablesMatch(@PathVariable String sceneId, @RequestBody List<String> variableNames) {
		return R.ok(configPerceivedVariableService.gBomVariablesMatch(sceneId, variableNames));
	}

}
