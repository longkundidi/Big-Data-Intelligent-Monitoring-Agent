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
package sw.model3d.configPerceivedVariable.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.model3d.configModel.entity.ConfigModelVariable;
import sw.model3d.configModel.service.ConfigModelVariableService;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.model3d.configPerceivedVariable.mapper.ConfigPerceivedVariableMapper;
import sw.model3d.configPerceivedVariable.service.ConfigPerceivedVariableService;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pig code generator
 * @date 2024-03-22 19:24:18
 */
@Service
public class ConfigPerceivedVariableServiceImpl extends
		ServiceImpl<ConfigPerceivedVariableMapper, ConfigPerceivedVariable> implements ConfigPerceivedVariableService {

	@Resource
	private ConfigModelVariableService configModelVariableService;

	@Autowired
	private ConfigGbomTreeMapper configGbomTreeMapper;

	@Autowired
	private ConfigPerceivedVariableMapper configPerceivedVariableMapper;

	@Override
	public List<ConfigPerceivedVariable> getByNodeId(String nodeId) {
		List<ConfigPerceivedVariable> list = this
			.list(new QueryWrapper<ConfigPerceivedVariable>().lambda().eq(ConfigPerceivedVariable::getNodeId, nodeId));
		return list;
	}

	@Override
	public List<ConfigPerceivedVariable> getByModelId(String modelId) {
		// 通过模型ID查询相关的感知变量 待实现
		// 1. 在关联表中查询modeId对应的感知变量ID列表
		List<ConfigModelVariable> configModelVariables = getIDByModelId(modelId);
		// 2. 通过得到的感知变量ID列表查询到感知变量详情，并存入list中
		List<ConfigPerceivedVariable> res = new LinkedList<>();
		for (ConfigModelVariable configModelVariable : configModelVariables) {
			String varId = configModelVariable.getVarId();
			ConfigPerceivedVariable pv = this.getById(varId);
			res.add(pv);
		}
		// 3. 返回list
		return res;
	}

	@Override
	public Page<ConfigPerceivedVariable> getPageByModelId(int current, int pageSize, String modelId) {
		// 通过模型ID查询相关的感知变量 待实现
		// 1. 在关联表中查询modeId对应的感知变量ID列表
		List<ConfigModelVariable> configModelVariables = getIDByModelId(modelId);
		// 2. 通过得到的感知变量ID列表查询到感知变量详情，并存入list中
		List<String> varIds = new LinkedList<>();
		for (ConfigModelVariable configModelVariable : configModelVariables) {
			String varId = configModelVariable.getVarId();
			varIds.add(varId);
		}
		Page<ConfigPerceivedVariable> res = this.page(new Page<>(current, pageSize),
				new QueryWrapper<ConfigPerceivedVariable>().lambda().in(ConfigPerceivedVariable::getVarId, varIds));
		// 3. 返回list
		return res;
	}

	@Override
	public List<ConfigModelVariable> getIDByModelId(String modelId) {
		List<ConfigModelVariable> configModelVariableList = configModelVariableService
			.list(new QueryWrapper<ConfigModelVariable>().lambda().eq(ConfigModelVariable::getModelId, modelId));

		// LinkedList<String> res = new LinkedList<>();
		// for (ConfigModelVariable s : list) {
		// res.add(s.getVarId().toString());
		// }
		return configModelVariableList;
	}

	@Override
	public Map<String, List<ConfigPerceivedVariable>> getAllVariablesByNodeIds(Set<String> nodeIds) {
		// 查找感知变量
		List<ConfigPerceivedVariable> allVariables = this.list();
		return allVariables.stream()
			.filter(e -> nodeIds.contains(e.getNodeId()))
			.collect(Collectors.groupingBy(ConfigPerceivedVariable::getNodeId));
	}

	// 安全提取父级编码
	private String getParentCode(String code) {
		int idx = code.lastIndexOf("-");
		return idx != -1 ? code.substring(0, idx) : "";
	}

	@Override
	public Map<String, List<String>> gBomVariablesMatch(String sceneId, List<String> variableNames) {
		Map<String, List<String>> fuzzyMatchMap = new HashMap<>();

		// 1. 拿到元感知变量列表
		List<String> nodeIds = configGbomTreeMapper.getNodeIdsBySceneId(sceneId);
		List<ConfigPerceivedVariable> gBomTreeVariable = configPerceivedVariableMapper
			.selectList(new LambdaQueryWrapper<ConfigPerceivedVariable>().select(ConfigPerceivedVariable::getVarName)
				.in(ConfigPerceivedVariable::getNodeId, nodeIds));

		// 提取唯一变量名
		Set<String> gBomTreeVariableNames = gBomTreeVariable.stream()
			.map(ConfigPerceivedVariable::getVarName)
			.collect(Collectors.toSet());

		// 提取精确匹配的变量（交集）
		Set<String> exactMatches = new HashSet<>(variableNames);
		exactMatches.retainAll(gBomTreeVariableNames); // exactMatches = 精确匹配上的变量

		// 过滤掉这些精确匹配变量，剩下的才能参与模糊匹配
		Set<String> fuzzyCandidates = gBomTreeVariableNames.stream()
			.filter(name -> !exactMatches.contains(name))
			.collect(Collectors.toSet());

		// 2. 遍历 variableNames，找那些没精确匹配的
		for (String var : variableNames) {
			if (!exactMatches.contains(var)) {
				List<String> fuzzyMatches = fuzzyCandidates.stream()
					.filter(candidate -> isFuzzyMatch(var, candidate))
					.collect(Collectors.toList());

				fuzzyMatchMap.put(var, fuzzyMatches);
			}
		}

		return fuzzyMatchMap;
	}

	private boolean isFuzzyMatch(String src, String target) {
		if (src == null || target == null)
			return false;
		src = src.toLowerCase();
		target = target.toLowerCase();

		// 快速包含判断
		if (src.contains(target) || target.contains(src)) {
			return true;
		}

		// 相似度判断
		LevenshteinDistance distance = new LevenshteinDistance();
		int editDist = distance.apply(src, target);
		int maxLen = Math.max(src.length(), target.length());

		double similarity = 1 - ((double) editDist / maxLen);
		return similarity >= 0.4;
	}

}
