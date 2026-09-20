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

package sw.model3d.modelBaseTree.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTreeVo;
import sw.model3d.modelBaseTree.entity.M3ModelBaseTree;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author pig code generator
 * @date 2023-10-28 09:20:35
 */
@Mapper
public interface M3ModelBaseTreeMapper extends BaseMapper<M3ModelBaseTree> {

	// 获取最大顶层节点编号
	@Select("SELECT COALESCE(max(node_no),0) FROM m3_model_base_tree WHERE node_level = 1")
	Integer getTopMaxNo();

	M3ModelBaseTreeVo getRootNodeByMbId(@Param("mbId") String mbId);

	// 获取最大子节点编号
	@Select("SELECT COALESCE(max(node_no),0) FROM m3_model_base_tree WHERE node_code REGEXP CONCAT('^',#{pNodeCode},'-[0-9]*$')")
	Integer getSonMaxNo(@Param("pNodeCode") String pNodeCode);

	// 根据节点编码，查询它的下一层子节点
	List<M3ModelBaseTreeVo> getSonNodes(@Param("nodeCode") String nodeCode);

	// 查询所有顶层节点
	List<M3ModelBaseTreeVo> getRootNodes();

}
