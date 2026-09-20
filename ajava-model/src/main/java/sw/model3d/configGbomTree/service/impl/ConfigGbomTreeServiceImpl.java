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
package sw.model3d.configGbomTree.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.core.util.R;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;
import sw.common.exception.MyException;
import sw.common.exception.MyExceptionEnum;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.common.strUtils;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeVo;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.model3d.configGbomTree.service.ConfigGbomTreeService;
import org.springframework.stereotype.Service;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTreeVo;
import sw.utils.codecTag;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-03-07 20:16:44
 */
@Service
public class ConfigGbomTreeServiceImpl extends ServiceImpl<ConfigGbomTreeMapper, ConfigGbomTree>
		implements ConfigGbomTreeService {

	// @Value("${minioController}")
	// private String minioController;
	// @Resource
	// private RestTemplate restTemplate;
	@Override
	public List<ConfigGbomTreeVo> getTreeNodes(Integer nodeLevel) {
		return baseMapper.getTreeNodes(nodeLevel);
	}

	@Override
	public List<ConfigGbomTreeVo> getSonNodes(String nodeCode) {
		return baseMapper.getSonNodes(nodeCode);
	}

	// 根据父节点编码，生成表mo_metatree的子节点编码
	private String sonNodeCode(String pNodeCode, Integer nodeNo) {
		return pNodeCode + codecTag.joinStr + nodeNo; // 子节点编码 = 父节点编码 + "-" + 当前节点的最大编号
	}

	@Override
	public ConfigGbomTreeVo addSonNode(String parentNodeId, ConfigGbomTree configGbomTree) {
		ConfigGbomTreeVo res = new ConfigGbomTreeVo();
		ConfigGbomTree parentGbomTree = this.getById(parentNodeId);
		if (parentGbomTree != null) {
			Integer nodeNo = baseMapper.getSonMaxNo(parentGbomTree.getNodeCode()) + 1;
			/*
			 * configGbomTree.setNodeId("1623157004696940777");
			 * configGbomTree.setNodeCode("MT3-1-4-2");
			 * configGbomTree.setNodeName("构架子节点"); configGbomTree.setNodeNo(nodeNo);
			 * configGbomTree.setSwsort(5f); configGbomTree.setNodeLevel(5);
			 * configGbomTree.setNodeType("Leaf");
			 */
			configGbomTree.setNodeNo(nodeNo);
			configGbomTree.setNodeCode(sonNodeCode(parentGbomTree.getNodeCode(), nodeNo));
			if (configGbomTree.getSwsort() == null || configGbomTree.getSwsort().equals(0f))
				configGbomTree.setSwsort(nodeNo.floatValue());
			configGbomTree.setNodeLevel(parentGbomTree.getNodeLevel() + 1);
			configGbomTree.setNodeType("Leaf");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			configGbomTree.setUpdateTime(formatter.format(new Date())); // 设置时间戳
			// configGbomTree.setUserCode(genUserCode(GbomTree.getNodeLevel(),
			// GbomTree.getNodeNo(), parentGbomTree.getUserCode())); // 生成用户编码
			try {
				this.save(configGbomTree);
				parentGbomTree.setNodeType("Mid"); // 更改父节点的node_type属性
				this.updateById(parentGbomTree);
				BeanUtils.copyProperties(configGbomTree, res);
				res.setId(configGbomTree.getNodeId());
				res.setName(configGbomTree.getNodeName());
				if (Objects.equals(configGbomTree.getNodeType(), "Leaf"))
					res.setLeaf(true);
				BeanUtils.copyProperties(configGbomTree, res);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new MyException(MyExceptionEnum.DATABASE_EXCEPTION); // 子节点添加失败，事务回滚
			}
		}
		return res;
	}

	@Override
	@Transactional
	public OpenResponse deleteNodesAndModels(String nodeCode) {
		List<ConfigGbomTree> delList = baseMapper.selectList(new QueryWrapper<ConfigGbomTree>().lambda()
			.eq(ConfigGbomTree::getNodeCode, nodeCode)
			.or()
			.likeRight(ConfigGbomTree::getNodeCode, nodeCode + codecTag.joinStr)); // 查询待删除列表
		try {
			List<String> dels = new ArrayList<>();
			for (ConfigGbomTree node : delList) {
				// String modelUrl = node.getModelFileurl();
				// if ("noUrl".equals(modelUrl))
				// modelUrl = "";
				// String imgUrl = node.getUrlImg();
				// if (("".equals(imgUrl))||(imgUrl == null))
				// imgUrl = "";
				// if ((!"".equals(modelUrl))||(!"".equals(imgUrl))){
				// String url = minioController + "/minio/removeObject?modelUrl=" +
				// modelUrl + "&imgUrl=" + imgUrl;
				// restTemplate.getForEntity(url, R.class); // 调用第三方微服务，删除minio服务器上的模型和图片
				// }
				dels.add(node.getNodeId());
			}
			this.removeBatchByIds(dels); // 批量删除节点
			updatePNodeTypeWhileDel(nodeCode); // 更新它的父节点类型
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new MyException(MyExceptionEnum.DATABASE_EXCEPTION); // 节点删除失败，抛出异常，事务回滚
		}
		OpenResponse response = new OpenResponse(OpenResponseCode.SC_DELETE);
		return response;
	}

	// 更新被删除节点的父节点类型
	private void updatePNodeTypeWhileDel(String nodeCode) {
		// 截取父节点
		String pCode = nodeCode.substring(0, nodeCode.lastIndexOf(codecTag.joinStr));
		ConfigGbomTree pGbomTree = baseMapper
			.selectOne(new QueryWrapper<ConfigGbomTree>().lambda().eq(ConfigGbomTree::getNodeCode, pCode));
		List<ConfigGbomTreeVo> list = baseMapper.getSonNodes(pCode);
		if (list.isEmpty()) { // 删除子节点后，没有其他子节点,则更新父节点type
			if (pGbomTree.getNodeLevel() == 1) {
				pGbomTree.setNodeType("Root-Leaf");
				this.updateById(pGbomTree);
			}
			else {
				pGbomTree.setNodeType("Leaf");
				this.updateById(pGbomTree);
			}
		}
	}

	@Override
	public ConfigGbomTreeVo swUpdateById(ConfigGbomTree configGbomTree) {
		// OpenResponse response = new OpenResponse(OpenResponseCode.ERROR);
		this.updateById(configGbomTree);
		// if ("noUrl".equals(configGbomTree.getModelFileurl()))
		// this.lambdaUpdate().eq(ConfigGbomTree::getNodeId, configGbomTree.getNodeId())
		// .set(ConfigGbomTree::getModelType, "noType")
		// .set(ConfigGbomTree::getModelSize, null)
		// .update(); // 如果是删除模型，将model_size字段设置为null
		// if ("".equals(configGbomTree.getUrlImg()))
		// this.lambdaUpdate().eq(ConfigGbomTree::getNodeId, configGbomTree.getNodeId())
		// .set(ConfigGbomTree::getUrlImg, null)
		// .update(); // 如果是删除图片，将url_img字段设置为null
		// response.setCode(OpenResponseCode.SC_ACCEPTED);
		// response.setMessage("更新成功");
		ConfigGbomTree updatedConfigGbomTree = this.getById(configGbomTree.getNodeId());
		ConfigGbomTreeVo res = new ConfigGbomTreeVo();
		BeanUtils.copyProperties(updatedConfigGbomTree, res);
		res.setId(updatedConfigGbomTree.getNodeId());
		res.setName(updatedConfigGbomTree.getNodeName());
		if (updatedConfigGbomTree.getNodeType().equals("Leaf"))
			res.setLeaf(true);
		return res;
	}

	@Override
	public ConfigGbomTreeVo copyNodeByNodeCode(String nodeCode) {
		if (hasParentNode(nodeCode)) { // 如果存在父节点，就执行节点复制操作
			List<ConfigGbomTree> addNodes = new ArrayList<>();
			String projCode = getProjCode(nodeCode);// 提取项目编码
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 格式化日期
			String strDate = formatter.format(new Date());
			Pair<ConfigGbomTree, ConfigGbomTree> pair = buildParentNodeForCopyTree(nodeCode, projCode, strDate);
			ConfigGbomTree parentNode = pair.getRight();
			ConfigGbomTree parentOriginalNode = pair.getLeft(); // 原始节点
			addNodes.add(parentNode);
			List<ConfigGbomTree> sonNodes = buildSonNodesForCopyTree(nodeCode, parentNode.getNodeCode(),
					parentOriginalNode.getUserCode(), parentNode.getUserCode(), projCode, strDate);
			addNodes.addAll(sonNodes);
			if (!addNodes.isEmpty())
				this.saveBatch(addNodes);

			ConfigGbomTree newPNode = addNodes.get(0);
			ConfigGbomTreeVo res = new ConfigGbomTreeVo();
			BeanUtils.copyProperties(newPNode, res);
			res.setId(newPNode.getNodeId());
			res.setName(newPNode.getNodeName());
			return res;
		}
		return null;
	}

	// 判断一个节点是否存在父节点
	private boolean hasParentNode(String nodeCode) {
		if (!nodeCode.contains("-"))
			return false;
		String pCode = nodeCode.substring(0, nodeCode.lastIndexOf(codecTag.joinStr));
		ConfigGbomTree pGbomTree = baseMapper
			.selectOne(new QueryWrapper<ConfigGbomTree>().lambda().eq(ConfigGbomTree::getNodeCode, pCode));
		if (pGbomTree != null)// 存在父节点
			return true;
		else
			return false;
	}

	// 从节点编码中提取项目编码
	private String getProjCode(String nodeCode) {
		// TODO 暂定为 "MT"
		return "MT";
	}

	/**
	 * 根据节点编码，构造子树的根节点
	 * @param nodeCode: 被复制节点的编码
	 * @param projCode： 项目编码
	 * @param strUpdateTime： 时间戳
	 * @return：返回2个对象，originalNode：被复制节点对象；gbomTreeNode：待添加的新节点对象
	 */
	private Pair<ConfigGbomTree, ConfigGbomTree> buildParentNodeForCopyTree(String nodeCode, String projCode,
			String strUpdateTime) {
		ConfigGbomTree gbomTreeNode = this.getOne(new QueryWrapper<ConfigGbomTree>().eq("node_code", nodeCode)); // 查询待复制的节点
		ConfigGbomTree originalNode = new ConfigGbomTree();
		BeanUtil.copyProperties(gbomTreeNode, originalNode); // 复制一个拷贝
		gbomTreeNode.setNodeId(null);
		ConfigGbomTree parentNode = this
			.getOne(new QueryWrapper<ConfigGbomTree>().eq("node_code", getParentNodeCode(nodeCode))); // 获取父节点

		Integer nodeNo = baseMapper.getSonMaxNo(parentNode.getNodeCode()) + 1; // 计算子树根节点的编号
		gbomTreeNode.setNodeNo(nodeNo);
		gbomTreeNode.setNodeCode(sonNodeCode(parentNode.getNodeCode(), nodeNo)); // 生成新节点的编码
		gbomTreeNode.setNodeName(gbomTreeNode.getNodeName() + gbomTreeNode.getNodeNo()); // 修改节点名称
		gbomTreeNode.setSwsort((float) nodeNo);
		gbomTreeNode.setUpdateTime(strUpdateTime); // 设置日期日期
		// TODO 用户自定义编码
		// gbomTreeNode.setUserCode(genUserCode(gbomTreeNode.getNodeLevel(),
		// gbomTreeNode.getNodeNo(), parentNode.getUserCode())); // 生成子节点的用户编码
		Pair<ConfigGbomTree, ConfigGbomTree> pair = Pair.of(originalNode, gbomTreeNode);
		return pair;
	}

	// 从节点编码中提取父节点编码
	private String getParentNodeCode(String nodeCode) {
		return nodeCode.substring(0, nodeCode.lastIndexOf("-"));
	}

	// 生成结构树节点的用户自定义编码(根节点不编码)
	private String genUserCode(Integer nodeLevel, Integer nodeNo, String pUserCode) {
		/*
		 * if (nodeLevel == 2) return pUserCode + codecTag.joinStr + new
		 * DecimalFormat("000").format(nodeNo); // 按3位输出编码 else if (nodeLevel == 3) return
		 * pUserCode + codecTag.joinStr + codecTag.userObj + nodeNo; // 输出第一级构件编码 else if
		 * ((nodeLevel > 3)&&(pUserCode != null)&&(!"".equals(pUserCode))) return
		 * pUserCode + codecTag.joinStr + nodeNo; // 输出子构件编码
		 */
		return null; // 其他情况不生成编码
	}

	/**
	 * 根据节点编码，构造子树的子节点
	 * @param nodeCode: 被复制节点的编码
	 * @param newParentNodeCode： 待添加的新节点编码
	 * @param userCode： 被复制节点的用户编码
	 * @param newParentUserCode： 待添加的新节点用户编码
	 * @param projCode; 项目编码
	 * @param strUpdateTime： 时间戳
	 * @return
	 */
	private List<ConfigGbomTree> buildSonNodesForCopyTree(String nodeCode, String newParentNodeCode, String userCode,
			String newParentUserCode, String projCode, String strUpdateTime) {
		List<ConfigGbomTree> sonNodes = baseMapper
			.getAllSubNodesByNodeCode(nodeCode/* , projCode */);
		for (ConfigGbomTree node : sonNodes) {
			node.setNodeId(null);
			String newNodeCode = updateSonNodeCode(node.getNodeCode(), nodeCode, newParentNodeCode);
			node.setNodeCode(newNodeCode); // 修改节点编码
			// 修改用户编码
			// String newCode = updateSonNodeUserCode(node.getUserCode(), userCode,
			// newParentUserCode);
			// node.setUserCode(newCode);
			node.setUpdateTime(strUpdateTime); // 设置日期日期
		}
		return sonNodes;
	}

	/**
	 * 根据父节点的最新编码（pNodeNewCode），更新子节点编码
	 * @param sonNodeCode: 子节点的原始编码
	 * @param pNodeCode: 父节点的原始编码
	 * @param pNodeNewCode： 父节点的最新编码
	 * @return：子节点最新编码
	 */
	private String updateSonNodeCode(String sonNodeCode, String pNodeCode, String pNodeNewCode) {
		return sonNodeCode.replace(pNodeCode, pNodeNewCode);
	}

	/**
	 * 根据父节点的最新编码（parentNodeNewUserCode），更新子节点用户编码
	 * @param sonNodeUserCode： 子节点的原始用户编码
	 * @param parentNodeUserCode：父节点原始用户编码
	 * @param parentNodeNewUserCode：父节点最新编码
	 * @return：子节点最新编码
	 */
	private String updateSonNodeUserCode(String sonNodeUserCode, String parentNodeUserCode,
			String parentNodeNewUserCode) {
		/*
		 * if
		 * ((sonNodeUserCode.contains(codecTag.userObj))&&(parentNodeNewUserCode.contains(
		 * codecTag.userObj))) sonNodeUserCode =
		 * sonNodeUserCode.replace(codecTag.userObj,""); // 如果存在2个obj，则去掉后面一个obj String
		 * newUserCode =
		 * sonNodeUserCode.replace(parentNodeUserCode,parentNodeNewUserCode); if
		 * ((strUtils.getCount(newUserCode,codecTag.joinStr) >=
		 * 2)&&(!newUserCode.contains(codecTag.userObj))){ // 如果第三级节点编码中没有obj,则插入obj int
		 * index = strUtils.getIndexOf(newUserCode,codecTag.joinStr,2) + 1; newUserCode =
		 * newUserCode.substring(0,index) + codecTag.userObj +
		 * newUserCode.substring(index); } return newUserCode;
		 */
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ConfigGbomTreeVo moveNode(String sourceNodeCode, String targetNodeCode) {
		OpenResponse response = new OpenResponse(OpenResponseCode.SUCCESS);
		String projCode = getProjCode(sourceNodeCode);
		ConfigGbomTree gbomTreeNode = baseMapper.selectOne(new QueryWrapper<ConfigGbomTree>().lambda()
			.eq(ConfigGbomTree::getNodeCode,
					sourceNodeCode)/*
									 * . eq(ConfigGbomTree::getProjCode,projCode)
									 */); // 找到待移动的节点对象
		String pUserCode = gbomTreeNode.getUserCode(); // 保存父节点的原始用户编码
		ConfigGbomTree PgbomTreeNode = baseMapper
			.selectOne(new QueryWrapper<ConfigGbomTree>().lambda().eq(ConfigGbomTree::getNodeCode, targetNodeCode)); // 找到目标节点对象
		List<ConfigGbomTree> nodeList = baseMapper.selectList(new QueryWrapper<ConfigGbomTree>().lambda()
			.likeRight(ConfigGbomTree::getNodeCode, sourceNodeCode
					+ codecTag.joinStr)/*
										 * . eq(ConfigGbomTree::getProjCode,projCode)
										 */); // 找到待移动节点的子节点
		Integer oldNodeLevel = gbomTreeNode.getNodeLevel();
		// 1、生成（待移动节点）新的节点编码nodeCode
		gbomTreeNode.setNodeNo(baseMapper.getSonMaxNo(targetNodeCode) + 1);
		gbomTreeNode.setNodeLevel(PgbomTreeNode.getNodeLevel() + 1);
		gbomTreeNode.setNodeType((nodeList.size() == 0) ? "Leaf" : "Mid");
		gbomTreeNode.setNodeCode(sonNodeCode(targetNodeCode, gbomTreeNode.getNodeNo()));
		gbomTreeNode.setUserCode(
				genUserCode(gbomTreeNode.getNodeLevel(), gbomTreeNode.getNodeNo(), PgbomTreeNode.getUserCode())); // 生成子节点的用户编码
		try {
			// 2、更新目标节点的nodeType属性
			if ("Leaf".equals(PgbomTreeNode.getNodeType()) || "Root-Leaf".equals(PgbomTreeNode.getNodeType())) {
				PgbomTreeNode.setNodeType("Mid");
				this.update(PgbomTreeNode, new QueryWrapper<ConfigGbomTree>().eq("node_id", PgbomTreeNode.getNodeId())
				/* .eq("proj_code",getProjCode(PgbomTreeNode.getNodeCode())) */);
			}
			// 3、原节点的父亲节点类型更新
			if (sourceNodeCode.contains(codecTag.joinStr)) {
				String pNodeCode = getParentNodeCode(sourceNodeCode); // 根据当前节点编码，提取父节点编码
				List<String> sonNodeIds = baseMapper.getNextLevelNodeIdsByNodeCode(pNodeCode,
						strUtils.getCount(pNodeCode, "-")
								+ 2/* , getProjCode(pNodeCode) */); // 查询父节点下一层级是否存在其他子节点
				if (sonNodeIds.size() == 1) { // 如果父节点下，没有其他子节点，则更改父节点node_type属性.注意：这里采用了事务处理，因此这里仅剩的2个节点分别是父节点和它的一个子节点
					ConfigGbomTree parentProjTree = this
						.getOne(new QueryWrapper<ConfigGbomTree>().eq("node_code", pNodeCode)
						/* .eq("proj_code",getProjCode(sourceNodeCode)) */); // 根据节点编码，查询父节点
					parentProjTree.setNodeType(setParentNodeTypeAfterDel(parentProjTree.getNodeType())); // 设置父节点的node_type属性
					this.update(parentProjTree,
							new QueryWrapper<ConfigGbomTree>().eq("node_id", parentProjTree.getNodeId())
					/* .eq("proj_code",getProjCode(parentProjTree.getNodeCode())) */); // 更新源节点的父节点node_type属性
				}
			}
			// 4、更新子树根节点
			this.update(gbomTreeNode, new QueryWrapper<ConfigGbomTree>().eq("node_id", gbomTreeNode.getNodeId())
			/* .eq("proj_code",getProjCode(gbomTreeNode.getNodeCode())) */);
			// 5、批量更新树的子节点
			if (nodeList.size() > 0) {
				int ind = strUtils.getIndexOf(nodeList.get(0).getNodeCode(), codecTag.joinStr, oldNodeLevel);
				String pNodeCode = gbomTreeNode.getNodeCode();
				for (ConfigGbomTree sonNode : nodeList) {
					sonNode.setNodeCode(pNodeCode + sonNode.getNodeCode().substring(ind));
					sonNode.setNodeLevel(strUtils.getCount(sonNode.getNodeCode(), "-") + 1);
					String newUserCode = updateSonNodeUserCode(sonNode.getUserCode(), pUserCode,
							gbomTreeNode.getUserCode());
					sonNode.setUserCode(newUserCode);
					this.update(sonNode, new QueryWrapper<ConfigGbomTree>().eq("node_id", sonNode.getNodeId())
					/* .eq("proj_code", getProjCode(sonNode.getNodeCode())) */);
				}
			}
		}
		catch (Exception e) {
			response.setCode(OpenResponseCode.ERROR);
			response.setMessage("移动失败");
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); // 事务回滚
		}
		ConfigGbomTree newNode = this.getById(gbomTreeNode.getNodeId());

		ConfigGbomTreeVo res = new ConfigGbomTreeVo();
		BeanUtils.copyProperties(newNode, res);
		res.setId(newNode.getNodeId());
		res.setName(newNode.getNodeName());
		return res;
	}

	// 节点删除后，更新父节点的node_type属性
	private String setParentNodeTypeAfterDel(String pNodeType) {
		String nodeType = null;
		if (!(pNodeType == null || pNodeType.equals(""))) {
			if (pNodeType.equals("Root"))
				nodeType = "Root-Leaf";
			else if (pNodeType.equals("Mid"))
				nodeType = "Leaf";
		}
		return nodeType;
	}

}
