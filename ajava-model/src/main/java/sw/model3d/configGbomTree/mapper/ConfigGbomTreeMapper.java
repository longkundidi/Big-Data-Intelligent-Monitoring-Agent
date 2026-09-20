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

package sw.model3d.configGbomTree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import org.apache.ibatis.annotations.Mapper;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeVo;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTreeVo;

import java.util.List;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-03-07 20:16:44
 */
@Mapper
public interface ConfigGbomTreeMapper extends BaseMapper<ConfigGbomTree> {

	List<ConfigGbomTreeVo> getTreeNodes(@Param("nodeLevel") Integer nodeLevel);

	// 根据节点编码，查询它的下一层子节点
	List<ConfigGbomTreeVo> getSonNodes(@Param("nodeCode") String nodeCode);

	// 获取最大子节点编号
	@Select("SELECT COALESCE(max(node_no),0) FROM config_gbom_tree WHERE node_code REGEXP CONCAT('^',#{pNodeCode},'-[0-9]*$')")
	Integer getSonMaxNo(@Param("pNodeCode") String pNodeCode);

	// 根据节点编码，找到其所有下属节点(子节点、孙子节点)，不包括本节点
	List<ConfigGbomTree> getAllSubNodesByNodeCode(
			@Param("nodeCode") String nodeCode/* , @Param("projCode") String projCode */);

	// 根据节点编码，找到当前节点及其所有下属节点的Id，不包括本节点
	@Select("SELECT node_id FROM config_gbom_tree WHERE (node_level = #{nodeLevel} and node_code REGEXP CONCAT('^',#{nodeCode},'-'))")
	List<String> getNextLevelNodeIdsByNodeCode(@Param("nodeCode") String nodeCode,
			@Param("nodeLevel") Integer nodeLevel/* ,@Param("projCode") String projCode */);

}
