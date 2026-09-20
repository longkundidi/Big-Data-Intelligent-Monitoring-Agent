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

package sw.model3d.modelBaseTree.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.common.response.OpenResponse;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTreeVo;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTree;
import java.util.List;

/**
 * @author pig code generator
 * @date 2023-10-28 09:20:35
 */
public interface M3ModelBaseTreeService extends IService<M3ModelBaseTree> {

	// 根据模板Id，查询顶层节点
	M3ModelBaseTreeVo getRootNodeByMbId(String mbId);

	// 添加根节点
	OpenResponse addRootNode(String nodeName, String mbId);

	// 根据节点编码，查询它的子节点
	List<M3ModelBaseTreeVo> getSonNodes(String nodeCode);

	// 添加子节点
	OpenResponse addSonNode(String parentNodeId, M3ModelBaseTree m3ModelBaseTree);

	OpenResponse deleteNodesAndModels(String nodeCode);

}
