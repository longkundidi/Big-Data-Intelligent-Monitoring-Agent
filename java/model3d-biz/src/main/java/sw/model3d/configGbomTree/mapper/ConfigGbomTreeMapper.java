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
import org.apache.ibatis.annotations.*;
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
@Mapper
public interface ConfigGbomTreeMapper extends BaseMapper<ConfigGbomTree> {

	List<ConfigGbomTreeVo> getTreeNodes(@Param("nodeLevel") Integer nodeLevel);

	List<ConfigGbomTreeVo> getTreeNodesBySceneId(@Param("sceneId") String sceneId,
			@Param("nodeLevel") Integer nodeLevel);

	// 根据节点编码，查询它的下一层子节点
	List<ConfigGbomTreeVo> getSonNodes(@Param("nodeCode") String nodeCode);

	List<ConfigGbomTreeVo> getSonNodesBySceneId(@Param("sceneId") String sceneId, @Param("nodeCode") String nodeCode);

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

	@Select("SELECT node_name FROM config_gbom_tree WHERE node_level != 1")
	List<String> getNodeName();

	@Select("SELECT node_code FROM config_gbom_tree WHERE scene_id = #{sceneId} AND node_level != 1")
	List<String> getNodeCode(@Param("sceneId") String sceneId);

	@Select("SELECT * FROM config_gbom_tree WHERE node_code = #{nodeCode}")
	ConfigGbomTree getNodebyCode(@Param("nodeCode") String nodeCode);

	@Select("SELECT * FROM config_gbom_tree WHERE node_code = #{nodeCode} AND scene_id = #{sceneId}")
	ConfigGbomTree getNodebyCodeAndSceneId(@Param("nodeCode") String nodeCode, @Param("sceneId") String sceneId);

	@Select("SELECT meta_model_id FROM al_resume_data WHERE id = #{projectNum}")
	String getSceneIdByNodeId(@Param("projectNum") String projectNum);

	@Select("SELECT * FROM config_gbom_tree WHERE node_code = #{nodeCode} AND scene_id = #{sceneId}")
	ConfigGbomTree getNodeByCodeAndSceneId(@Param("nodeCode") String nodeCode, @Param("sceneId") String sceneId);

	@Select("SELECT * FROM config_gbom_tree WHERE node_id = #{nodeId}")
	ConfigGbomTree getNodeByNodeId(@Param("nodeId") String nodeId);

	@Select("SELECT node_id FROM config_gbom_tree WHERE node_name = #{nodeName} AND node_type = 'Leaf'")
	String getNodeIdByNodeName(@Param("nodeName") String nodeName);

	@Delete("DELETE FROM config_gbom_tree WHERE scene_id=#{sceneId}")
	void delTempalteBySceneId(@Param("sceneId") String sceneId);

	@Update("UPDATE config_gbom_tree SET used_count = CASE WHEN used_count IS NULL THEN 1 ELSE used_count + 1 END WHERE node_id = #{nodeId}")
	void incrementUsedCountByNodeId(@Param("nodeId") String nodeId);

	@Update("UPDATE config_gbom_tree SET used_count = CASE WHEN used_count > 0 THEN used_count - 1 ELSE 0 END WHERE node_id = #{nodeId}")
	void decrementUsedCountByNodeId(@Param("nodeId") String nodeId);

	@Select("SELECT MAX(swsort) FROM config_gbom_tree WHERE node_type = 'Root'")
	Float selectMaxSwsortOfRoot();

	@Select("SELECT node_id FROM config_gbom_tree WHERE scene_id = #{sceneId}")
	List<String> getNodeIdsBySceneId(@Param("sceneId") String sceneId);

}
