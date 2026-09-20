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

import com.baomidou.mybatisplus.extension.service.IService;
import sw.common.response.OpenResponse;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeVo;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTreeVo;

import java.util.List;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-03-07 20:16:44
 */
public interface ConfigGbomTreeService extends IService<ConfigGbomTree> {

	// 获取数据表中，指定层级（nodeLevel）的所有节点
	List<ConfigGbomTreeVo> getTreeNodes(Integer nodeLevel);

	// 根据节点编码，查询它的子节点
	List<ConfigGbomTreeVo> getSonNodes(String nodeCode);

	// 添加子节点
	ConfigGbomTreeVo addSonNode(String parentNodeId, ConfigGbomTree configGbomTree);

	// 删除节点及其模型
	OpenResponse deleteNodesAndModels(String nodeCode);

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

}
