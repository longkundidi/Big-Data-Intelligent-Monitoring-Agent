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
package sw.model3d.ConfigBomPerceivedVariable.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sw.model3d.ConfigBomPerceivedVariable.entity.ConfigBomPerceivedVariable;
import sw.model3d.ConfigBomPerceivedVariable.mapper.ConfigBomPerceivedVariableMapper;
import sw.model3d.ConfigBomPerceivedVariable.service.ConfigBomPerceivedVariableService;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.model3d.configModel.entity.vo.VariableNotMatch;
import sw.model3d.configModel.entity.ConfigModelVariable;
import sw.model3d.configModel.mapper.ConfigBomTreeMapper;
import sw.model3d.configModel.mapper.ConfigModelVariableMapper;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.model3d.configPerceivedVariable.mapper.ConfigPerceivedVariableMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pig code generator
 * @date 2024-05-09 11:37:07
 */
@Service
public class ConfigBomPerceivedVariableServiceImpl
		extends ServiceImpl<ConfigBomPerceivedVariableMapper, ConfigBomPerceivedVariable>
		implements ConfigBomPerceivedVariableService {

	@Autowired
	private ConfigBomPerceivedVariableMapper configBomPerceivedVariableMapper;

	@Autowired
	private ConfigPerceivedVariableMapper configPerceivedVariableMapper;

	@Autowired
	private ConfigGbomTreeMapper configGbomTreeMapper;

	@Autowired
	private ConfigBomTreeMapper configBomTreeMapper;

	@Autowired
	private ConfigModelVariableMapper configModelVariableMapper;

	@Override
	public List<String> findVariables(String turbineCode) {
		return configBomPerceivedVariableMapper.findVariables(turbineCode);
	}

	@Override
	public List<VariableNotMatch> variableMatch(List<String> variables, String sceneId) {

		List<VariableNotMatch> variableNotMatches = new ArrayList<>();
		Set<String> inputVariables = variables == null ? Collections.emptySet()
				: variables.stream().filter(Objects::nonNull).collect(Collectors.toSet());

		List<String> gbomNodeIds = configGbomTreeMapper.getNodeIdsBySceneId(sceneId);
		if (gbomNodeIds == null || gbomNodeIds.isEmpty()) {
			return variableNotMatches;
		}

		// 仅校验“有感知变量的测点节点”是否存在：
		// 某节点只要至少有一个变量命中即判定该节点存在。
		List<ConfigPerceivedVariable> gbomPerceivedVariables = configPerceivedVariableMapper
			.selectList(new LambdaQueryWrapper<ConfigPerceivedVariable>()
				.select(ConfigPerceivedVariable::getNodeId, ConfigPerceivedVariable::getVarName)
				.in(ConfigPerceivedVariable::getNodeId, gbomNodeIds));

		Map<String, Set<String>> nodeVariableMap = new LinkedHashMap<>();
		for (ConfigPerceivedVariable perceivedVariable : gbomPerceivedVariables) {
			String nodeId = perceivedVariable.getNodeId();
			String varName = perceivedVariable.getVarName();
			if (nodeId == null || varName == null || varName.isEmpty()) {
				continue;
			}
			nodeVariableMap.computeIfAbsent(nodeId, key -> new LinkedHashSet<>()).add(varName);
		}

		for (Map.Entry<String, Set<String>> entry : nodeVariableMap.entrySet()) {
			String nodeId = entry.getKey();
			Set<String> nodeVariables = entry.getValue();
			boolean nodeMatched = nodeVariables.stream().anyMatch(inputVariables::contains);
			if (nodeMatched) {
				continue;
			}

			VariableNotMatch variableNotMatch = new VariableNotMatch();
			variableNotMatch.setNodeId(nodeId);

			ConfigGbomTree node = configGbomTreeMapper.getNodeByNodeId(nodeId);
			if (node == null) {
				continue;
			}
			String nodeCode = node.getNodeCode();
			String sceneId2 = node.getSceneId();
			String nodeName = node.getNodeName();

			String parentName = "";
			String grandParentName = "";

			// =============================
			// 安全计算 Parent
			// =============================
			int lastDash = nodeCode.lastIndexOf("-");
			if (lastDash != -1) {
				// 存在父节点
				String parentCode = nodeCode.substring(0, lastDash);
				ConfigGbomTree parentNode = configGbomTreeMapper.getNodebyCodeAndSceneId(parentCode, sceneId2);

				if (parentNode != null) {
					parentName = parentNode.getNodeName();
				}

				// =============================
				// 安全计算 Grand Parent
				// =============================
				int grandDash = parentCode.lastIndexOf("-");
				if (grandDash != -1) {
					String grandCode = parentCode.substring(0, grandDash);
					ConfigGbomTree grandParentNode = configGbomTreeMapper.getNodebyCodeAndSceneId(grandCode, sceneId2);
					if (grandParentNode != null) {
						grandParentName = grandParentNode.getNodeName();
					}
				}
			}

			// =============================
			// 拼接最终 nodeParent，不报错
			// =============================
			String nodeParent = String.join("-",
					Arrays.asList(grandParentName, parentName, nodeName)
						.stream()
						.filter(s -> s != null && !s.isEmpty()) // 过滤空字符串
						.collect(Collectors.toList()));

			variableNotMatch.setNodeParent(nodeParent);
			variableNotMatch.setNodeName(nodeName);
			variableNotMatch.setVariables(new ArrayList<>(nodeVariables));
			variableNotMatches.add(variableNotMatch);
		}

		return variableNotMatches;
	}

	@Override
	public List<ConfigBomPerceivedVariable> getByNodeId(String nodeId) {
		String turbineCode = configBomTreeMapper.getTurbineCodeByNodeId(nodeId);
		return this.list(new QueryWrapper<ConfigBomPerceivedVariable>().lambda()
			.eq(ConfigBomPerceivedVariable::getTurbineCode, turbineCode));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<ConfigBomPerceivedVariable> prepareTaskVariables(String nodeId, String modelId) {
		if (!StringUtils.hasText(nodeId) || !StringUtils.hasText(modelId)) {
			throw new IllegalArgumentException("实例节点和模型不能为空");
		}

		String turbineCode = configBomTreeMapper.getTurbineCodeByNodeId(nodeId);
		if (!StringUtils.hasText(turbineCode)) {
			throw new IllegalArgumentException("未找到实例节点对应的设备编码");
		}

		List<ConfigModelVariable> modelVariables = configModelVariableMapper
			.selectList(new LambdaQueryWrapper<ConfigModelVariable>().eq(ConfigModelVariable::getModelId, modelId));
		if (modelVariables.isEmpty()) {
			throw new IllegalArgumentException("当前模型未配置感知变量");
		}

		LinkedHashSet<String> modelVariableIds = modelVariables.stream()
			.map(ConfigModelVariable::getVarId)
			.filter(StringUtils::hasText)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		List<ConfigPerceivedVariable> variableDefinitions = configPerceivedVariableMapper
			.selectBatchIds(modelVariableIds);
		Map<String, ConfigPerceivedVariable> definitionById = variableDefinitions.stream()
			.collect(Collectors.toMap(ConfigPerceivedVariable::getVarId, value -> value, (left, right) -> left));

		List<ConfigBomPerceivedVariable> instanceVariables = this
			.list(new LambdaQueryWrapper<ConfigBomPerceivedVariable>().eq(ConfigBomPerceivedVariable::getTurbineCode,
					turbineCode));
		Set<String> existingVariableIds = instanceVariables.stream()
			.map(ConfigBomPerceivedVariable::getVarId)
			.filter(StringUtils::hasText)
			.collect(Collectors.toSet());

		for (String variableId : modelVariableIds) {
			if (existingVariableIds.contains(variableId)) {
				continue;
			}
			ConfigPerceivedVariable definition = definitionById.get(variableId);
			if (definition == null) {
				throw new IllegalArgumentException("模型关联的感知变量不存在：" + variableId);
			}

			ConfigBomPerceivedVariable instanceVariable = new ConfigBomPerceivedVariable();
			BeanUtils.copyProperties(definition, instanceVariable);
			instanceVariable.setId(null);
			instanceVariable.setNodeId(nodeId);
			instanceVariable.setTurbineCode(turbineCode);
			this.save(instanceVariable);
			existingVariableIds.add(variableId);
		}

		return this.list(new LambdaQueryWrapper<ConfigBomPerceivedVariable>()
			.eq(ConfigBomPerceivedVariable::getTurbineCode, turbineCode)
			.orderByAsc(ConfigBomPerceivedVariable::getVarName));
	}

}
