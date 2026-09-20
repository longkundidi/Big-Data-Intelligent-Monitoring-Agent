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

package sw.model3d.configPerceivedVariable.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import sw.model3d.configFailureMode.entity.ConfigFailureMode;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeDTO;
import sw.model3d.configModel.entity.ConfigBomTree;
import sw.model3d.configModel.entity.ConfigModelVariable;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pig code generator
 * @date 2024-03-22 19:24:18
 */
public interface ConfigPerceivedVariableService extends IService<ConfigPerceivedVariable> {

	// 根据节点id查询
	List<ConfigPerceivedVariable> getByNodeId(String nodeId);

	// 通过模型ID查询相关的感知变量-详情信息
	List<ConfigPerceivedVariable> getByModelId(String modelId);

	// 通过模型ID查询相关的感知变量-详情信息-page
	Page<ConfigPerceivedVariable> getPageByModelId(int current, int pageSize, String modelId);

	// 通过模型ID查询相关的感知变量-ID
	List<ConfigModelVariable> getIDByModelId(String modelId);

	/**
	 * 获得对应元结构树的所有感知变量
	 * @param nodeIds 机型选中所有的结构树节点id
	 * @return Map<String, List<ConfigModelVariable>> 返回元结构每个节点下的变量
	 */
	Map<String, List<ConfigPerceivedVariable>> getAllVariablesByNodeIds(Set<String> nodeIds);

	/**
	 * 将variableNames与gbom中的感知变量进行匹配，返回未匹配成功的模糊匹配
	 * @param sceneId gbomId
	 * @param variableNames 需要被匹配的变量名
	 * @return map：key是未匹配成功的变量，value是gbom感知变量中可能的匹配
	 */
	Map<String, List<String>> gBomVariablesMatch(String sceneId, List<String> variableNames);

}
