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
package sw.model3d.modelBaseTree.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.model3d.minio.MinioServiceImpl;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTreeVo;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTree;
import sw.model3d.modelBaseTree.mapper.M3ModelBaseTreeMapper;
import sw.model3d.modelBaseTree.service.M3ModelBaseTreeService;
import org.springframework.stereotype.Service;

import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import java.text.SimpleDateFormat;
import sw.utils.codecTag;
import sw.utils.entity.TreeEntity;
import sw.common.exception.MyException;
import sw.common.exception.MyExceptionEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author pig code generator
 * @date 2023-10-28 09:20:35
 */
@Service
public class M3ModelBaseTreeServiceImpl extends ServiceImpl<M3ModelBaseTreeMapper, M3ModelBaseTree>
		implements M3ModelBaseTreeService {

	@Autowired
	private MinioServiceImpl minioServiceImpl;

	@Override
	public M3ModelBaseTreeVo getRootNodeByMbId(String mbId) {
		return baseMapper.getRootNodeByMbId(mbId);
	}

	@Override
	public List<M3ModelBaseTreeVo> getSonNodes(String nodeCode) {
		return baseMapper.getSonNodes(nodeCode);
	}

	// 生成表m3_model_base_tree的根节点编码
	private String rootNodeCode(Integer nodeNo) {
		return codecTag.modelBaseTreeCode + nodeNo; // 根节点编码 = MT + 当前根节点的最大编号
	}

	@Override
	public OpenResponse addRootNode(String nodeName, String mbId) {
		OpenResponse response = new OpenResponse<>(OpenResponseCode.ERROR);
		if (!(nodeName == null || nodeName.equals("") || (mbId == null) || mbId.equals(""))) {
			M3ModelBaseTree m3ModelBaseTree = new M3ModelBaseTree();
			Integer nodeNo = baseMapper.getTopMaxNo() + 1;
			m3ModelBaseTree.setNodeNo(nodeNo);
			m3ModelBaseTree.setNodeCode(rootNodeCode(nodeNo));
			m3ModelBaseTree.setNodeName(nodeName);
			m3ModelBaseTree.setNodeSort((float) nodeNo);
			m3ModelBaseTree.setNodeLevel(1);
			m3ModelBaseTree.setNodeType("Root-Leaf");
			m3ModelBaseTree.setModelFileurl("noUrl");
			m3ModelBaseTree.setModelType("noType");
			m3ModelBaseTree.setMbId(mbId);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateStr = formatter.format(new Date()); // 设置时间戳
			m3ModelBaseTree.setUpdateTime(dateStr);
			this.save(m3ModelBaseTree);
			response.setData(m3ModelBaseTree);
			response.setCode(OpenResponseCode.SUCCESS);
		}
		return response;
	}

	@Override
	@Transactional
	public OpenResponse addSonNode(String parentNodeId, M3ModelBaseTree m3ModelBaseTree) {
		OpenResponse response = new OpenResponse<>(OpenResponseCode.ERROR);
		M3ModelBaseTree parentMoMetatree = this.getById(parentNodeId);
		if (parentMoMetatree != null) {
			Integer nodeNo = baseMapper.getSonMaxNo(parentMoMetatree.getNodeCode()) + 1;
			m3ModelBaseTree.setNodeNo(nodeNo);
			m3ModelBaseTree.setNodeCode(sonNodeCode(parentMoMetatree.getNodeCode(), nodeNo));
			if (m3ModelBaseTree.getNodeSort() == null)
				m3ModelBaseTree.setNodeSort(nodeNo.floatValue());
			m3ModelBaseTree.setNodeLevel(parentMoMetatree.getNodeLevel() + 1);
			m3ModelBaseTree.setNodeType("Leaf");
			m3ModelBaseTree.setMbId(parentMoMetatree.getMbId());
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			m3ModelBaseTree.setUpdateTime(formatter.format(new Date())); // 设置时间戳
			try {
				this.save(m3ModelBaseTree);
				parentMoMetatree.setNodeType(TreeEntity.setParentNodeTypeAfterAdd(parentMoMetatree.getNodeType())); // 更改父节点的node_type属性
				this.updateById(parentMoMetatree);
				response.setCode(OpenResponseCode.SUCCESS);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new MyException(MyExceptionEnum.DATABASE_EXCEPTION); // 子节点添加失败，事务回滚
			}
		}
		return response;
	}

	// 根据父节点编码，生成表m3_model_base_tree的子节点编码
	private String sonNodeCode(String pNodeCode, Integer nodeNo) {
		return pNodeCode + codecTag.joinStr + nodeNo; // 子节点编码 = 父节点编码 + "-" + 当前节点的最大编号
	}

	@Override
	@Transactional
	public OpenResponse deleteNodesAndModels(String nodeCode) {
		OpenResponse response = new OpenResponse(OpenResponseCode.SC_DELETE);
		List<M3ModelBaseTree> delList = baseMapper.selectList(new QueryWrapper<M3ModelBaseTree>().lambda()
			.eq(M3ModelBaseTree::getNodeCode, nodeCode)
			.or()
			.likeRight(M3ModelBaseTree::getNodeCode, nodeCode + codecTag.joinStr)); // 查询待删除列表
		try {
			List<String> dels = new ArrayList<>();
			List<String> delCodes = new ArrayList<>();
			for (M3ModelBaseTree node : delList) {
				String modelUrl = node.getModelFileurl();
				if ("noUrl".equals(modelUrl))
					modelUrl = "";
				int res = 0;
				if (!("".equals(modelUrl)))
					res = minioServiceImpl.removeModelAndImage(modelUrl, ""); // 如果模型、图片有一个不空，调用minio服务，删除minio服务器上的模型和图片
				if (res == 0) {
					dels.add(node.getNodeId());
					delCodes.add(node.getNodeCode());
				}
			}
			// m3RoamPathService.batchRemoveByNodeIds(dels); // 首先删除节点关联的漫游路径
			// m3SpritemarkService.batchRemoveByNodeCodes(delCodes); // 删除节点关联的标注
			removeBatchByIds(dels); // 批量删除节点
			updatePNodeTypeWhileDel(nodeCode); // 更新它的父节点类型
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new MyException(MyExceptionEnum.DATABASE_EXCEPTION); // 节点删除失败，抛出异常，事务回滚
		}
		return response;
	};

	// 更新被删除节点的父节点类型
	private void updatePNodeTypeWhileDel(String nodeCode) {
		// 截取父节点
		String pCode = nodeCode.substring(0, nodeCode.lastIndexOf(codecTag.joinStr));
		M3ModelBaseTree pMoMetatree = baseMapper
			.selectOne(new QueryWrapper<M3ModelBaseTree>().lambda().eq(M3ModelBaseTree::getNodeCode, pCode));
		List<M3ModelBaseTreeVo> list = baseMapper.getSonNodes(pCode);
		if (list.size() == 0) { // 删除子节点后，没有其他子节点,则更新父节点type
			if (pMoMetatree.getNodeLevel() == 1) {
				pMoMetatree.setNodeType("Root-Leaf");
				this.updateById(pMoMetatree);
			}
			else {
				pMoMetatree.setNodeType("Leaf");
				this.updateById(pMoMetatree);
			}
		}
	}

}
