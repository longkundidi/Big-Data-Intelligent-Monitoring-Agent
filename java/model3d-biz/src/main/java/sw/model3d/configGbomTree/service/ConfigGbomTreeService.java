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

package sw.model3d.configGbomTree.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import sw.common.response.OpenResponse;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeDTO;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeVo;

import java.util.List;
import java.util.Set;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-03-07 20:16:44
 */
public interface ConfigGbomTreeService extends IService<ConfigGbomTree> {

	// 获取数据表中，指定层级（nodeLevel）的所有节点
	List<ConfigGbomTreeVo> getTreeNodes(Integer nodeLevel);

	/**
	 * 获取数据表中，指定场景的元结构树
	 * @param sceneId 场景id
	 * @param nodeLevel 指定层级
	 * @return List<ConfigGbomTreeVo> 结构树
	 */
	List<ConfigGbomTreeVo> getTreeNodesBySceneId(String sceneId, Integer nodeLevel);

	List<ConfigGbomTreeVo> getGBomTreeBySceneIdAndProId(String sceneId, String proId, Integer nodeLevel);

	// 根据节点编码，查询它的子节点
	List<ConfigGbomTreeVo> getSonNodes(String nodeCode);

	List<ConfigGbomTreeVo> getSonNodesBySceneId(String sceneId, String nodeCode);

	List<ConfigGbomTreeVo> getGBomSonTreeBySceneIdAndProId(String sceneId, String proId, String nodeCode);

	// 添加子节点
	ConfigGbomTreeVo addSonNode(String parentNodeId, ConfigGbomTree configGbomTree);

	// 删除节点及其模型
	OpenResponse deleteNodesAndModels(String nodeCode);

	OpenResponse deleteNodesBySceneId(String sceneId, String nodeCode);

	// 根据Id更新节点
	ConfigGbomTreeVo swUpdateById(ConfigGbomTree configGbomTree);

	// 根据节点编码复制子树
	ConfigGbomTreeVo copyNodeByNodeCode(String nodeCode);

	/**
	 * 节点移动
	 * @param sourceNodeCode: 待移动的节点
	 * @param targetNodeCode：目标节点
	 * @return 移动后的节点
	 */
	ConfigGbomTreeVo moveNode(String sourceNodeCode, String targetNodeCode);

	// 添加Gbom根节点
	ConfigGbomTreeVo addRootNode(ConfigGbomTree configGbomTree);

	/**
	 * 导入元结构树
	 * @param list
	 * @return boolean 是否导入成功
	 */
	boolean createProGbomTree(List<List<JSONObject>> list);

	/**
	 * 通过sceneId得到所有节点id
	 * @param sceneId 场景Id
	 * @return List<String> ids 节点id
	 */
	List<String> getAllNodeIdsBySceneId(String sceneId);

	/**
	 * 通过已选择的中间节点扩展到叶子节点
	 * @param selectNodes 在元结构树上选择的节点（根节点，中间节点，部分叶子节点）
	 * @return List<ConfigGbomTree> 扩展过后的节点
	 */
	Set<String> getAllNodIdsBySelectNodes(List<ConfigGbomTreeDTO> selectNodes);

}
