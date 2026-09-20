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
package sw.model3d.modelBaseInfo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import sw.common.exception.MyException;
import sw.common.exception.MyExceptionEnum;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.common.strUtils;
import sw.model3d.modelBaseInfo.entity.M3ModelBaseInfo;
import sw.model3d.modelBaseInfo.mapper.M3ModelBaseInfoMapper;
import sw.model3d.modelBaseInfo.service.M3ModelBaseInfoService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTree;
import sw.model3d.modelBaseTree.service.M3ModelBaseTreeService;
import sw.utils.codecTag;
import java.util.Date;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author pig code generator
 * @date 2023-10-24 10:55:58
 */
@Service
public class M3ModelBaseInfoServiceImpl extends ServiceImpl<M3ModelBaseInfoMapper, M3ModelBaseInfo>
		implements M3ModelBaseInfoService {

	@Autowired
	private M3ModelBaseTreeService m3ModelBaseTreeService;

	@Override
	public IPage<M3ModelBaseInfo> getRootNodePage(Page page) {
		IPage<M3ModelBaseInfo> m3CasetreeInfoPage = this.page(page,
				new QueryWrapper<M3ModelBaseInfo>().lambda()
					.eq(M3ModelBaseInfo::getNodeLevel, 1)
					.orderByAsc(M3ModelBaseInfo::getNodeSort));
		return m3CasetreeInfoPage;
	}

	@Override
	public List<M3ModelBaseInfo> getSonNode(String mbCode) {
		return baseMapper.getSonNode(mbCode);
	}

	@Override
	@Transactional
	public M3ModelBaseInfo addNode(M3ModelBaseInfo m3ModelBaseInfo) {
		try {
			String pCode = m3ModelBaseInfo.getPmbCode(); // 获取父节点编码
			// 更新父节点类型
			if (pCode != null && !pCode.equals("")) {
				M3ModelBaseInfo pMoMetatreeInfo = baseMapper
					.selectOne(new QueryWrapper<M3ModelBaseInfo>().lambda().eq(M3ModelBaseInfo::getMbCode, pCode));
				String pNodeType = setParentNodeTypeAfterAdd(pMoMetatreeInfo.getNodeType());
				if (pNodeType != pMoMetatreeInfo.getNodeType()) {
					pMoMetatreeInfo.setNodeType(pNodeType);
					this.updateById(pMoMetatreeInfo); // 更改父节点的node_type属性
				}
			}
			else {
				pCode = "";
			}
			M3ModelBaseInfo saveM3CasetreeInfo = this.codeMoMetatreeInfo(m3ModelBaseInfo, pCode); // 生成节点
			this.save(saveM3CasetreeInfo);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new MyException(MyExceptionEnum.DATABASE_EXCEPTION); // 子节点添加失败，事务回滚
		}
		return m3ModelBaseInfo;
	}

	// 节点添加后，更新父节点的node_type属性
	private String setParentNodeTypeAfterAdd(String pNodeType) {
		String nodeType = null;
		if (!(pNodeType == null || pNodeType.equals(""))) {
			if (pNodeType.equals("Root-Leaf"))
				nodeType = "Root";
			else if (pNodeType.equals("Leaf"))
				nodeType = "Mid";
		}
		return nodeType;
	}

	// 生成节点的编码
	private String genMbCode(String pCode, String subCode) {
		String code;
		if ("".equals(pCode)) {
			code = codecTag.modelBaseCode + subCode; // 模板根节点编码 = TP + 最大编号(case_no)
		}
		else {
			code = pCode + codecTag.joinStr + subCode; // 子模板编号 = 父模板编码 + "-" +
														// 最大编号(case_no)
		}
		return code;
	}

	// 编码MoMetatreeInfo对象
	private M3ModelBaseInfo codeMoMetatreeInfo(M3ModelBaseInfo m3ModelBaseInfo, String pCode) {
		// 节点编号顺序自增
		if (pCode == null || pCode.equals("")) {
			m3ModelBaseInfo.setNodeNo(baseMapper.getTopMaxNo() + 1);
			m3ModelBaseInfo.setNodeType("Root-Leaf");
		}
		else {
			m3ModelBaseInfo.setNodeNo(baseMapper.getSonMaxNo(pCode) + 1);
			m3ModelBaseInfo.setNodeType("Leaf");
		}
		// 节点编号(no)生成节点编码(code)
		m3ModelBaseInfo.setMbCode(genMbCode(pCode, m3ModelBaseInfo.getNodeNo().toString()));
		m3ModelBaseInfo.setNodeLevel(strUtils.getCount(m3ModelBaseInfo.getMbCode(), codecTag.joinStr) + 1);
		if (m3ModelBaseInfo.getNodeSort() == null)
			m3ModelBaseInfo.setNodeSort(m3ModelBaseInfo.getNodeNo().floatValue());
		else
			m3ModelBaseInfo.setNodeSort(m3ModelBaseInfo.getNodeSort());
		m3ModelBaseInfo.setCreateTime(new Date());
		m3ModelBaseInfo.setCreator(SecurityUtils.getUser().getUsername());
		return m3ModelBaseInfo;
	}

	@Override
	@Transactional
	public OpenResponse deleteModelBaseInfo(String mbId) {
		OpenResponse response = new OpenResponse<>(OpenResponseCode.ERROR);
		try {
			List<M3ModelBaseTree> treeNodes = m3ModelBaseTreeService
				.list(new QueryWrapper<M3ModelBaseTree>().eq("mb_id", mbId));
			if (treeNodes.size() > 1) {
				response.setCode(OpenResponseCode.SC_UNAUTHORIZED);
				response.setMessage("请先删除根节点下的所有节点，再删除该记录！");
			}
			else { // 如果该项目只包含一个根节点，允许删除
				if (treeNodes.size() == 1)
					m3ModelBaseTreeService.removeById(treeNodes.get(0)); // 删除表m3_model_base_tree中的根节点
				updataParentNodeType(mbId); // 更新父节点node_type属性
				this.removeById(mbId); // 删除表m3_model_base_info中的记录
				response.setCode(OpenResponseCode.SUCCESS);
				response.setMessage("记录删除成功！");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new MyException(MyExceptionEnum.DATABASE_EXCEPTION); // 删除失败，事务回滚
		}
		return response;
	}

	// 根据模板ID，查询它的父节点，更新父节点的temp_type属性
	private void updataParentNodeType(String mbId) {
		M3ModelBaseInfo curNode = this.getById(mbId); // 根据节点Id查询当前对象
		String pNodeCode = getParentNodeCode(curNode.getMbCode());
		if (pNodeCode != null) { // 如果当前节点存在一个父节点
			List<M3ModelBaseInfo> sonNodes = baseMapper.getSonNode(pNodeCode); // 查询子节点
			if (sonNodes.size() <= 1) { // 如果父节点下的子节点数=1，更改父节点属性
				M3ModelBaseInfo parentNode = this.getOne(new QueryWrapper<M3ModelBaseInfo>().eq("mb_code", pNodeCode)); // 根据节点编码，查询父节点
				parentNode.setNodeType(setParentNodeTypeAfterDel(parentNode.getNodeType())); // 设置父节点的node_type属性
				this.updateById(parentNode); // 更新源节点的父节点node_type属性
			}
		}
	}

	// 从节点编码中提取父节点编码
	private String getParentNodeCode(String nodeCode) {
		if (nodeCode.contains(codecTag.joinStr))
			return nodeCode.substring(0, nodeCode.lastIndexOf("-"));
		else
			return null;
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
