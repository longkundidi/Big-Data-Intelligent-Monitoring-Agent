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

package sw.model3d.configModel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.model3d.configModel.entity.ConfigBomTree;
import sw.model3d.configModel.entity.vo.ConfigBomTreeVo;
import sw.utils.entity.TreeEntity;

import java.util.List;
import java.util.Map;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-05-09 11:36:07
 */
@Mapper
public interface ConfigBomTreeMapper extends BaseMapper<ConfigBomTree> {

	@Select("SELECT * FROM config_bom_tree WHERE (turbine_code = #{turbineCode} AND node_type = 'Leaf')")
	List<ConfigBomTree> findComponents(String turbineCode);

	List<ConfigBomTreeVo> getComponentNodes(@Param("proId") Long proId, @Param("nodeLevel") String nodeLevel,
			@Param("turbineCodes") List<String> turbineCodes);

	@Select("SELECT node_code FROM config_bom_tree WHERE (turbine_code = #{turbineCode} AND node_level != 1)")
	List<String> findComponentCodes(@Param("turbineCode") String turbineCode);

	TreeEntity getNodeVobyCode(@Param("nodeCode") String nodeCode, @Param("turbineCode") String turbineCode,
			@Param("proId") String proId);

	List<TreeEntity> getBomList(@Param("turbineCode") String turbineCode, @Param("proId") String proId);

	List<ConfigBomTreeVo> getSonNodes(@Param("nodeCode") String nodeCode, @Param("turbineCode") String turbineCode);

	List<ConfigBomTreeVo> getTreeNodes(@Param("proId") Long proId, @Param("nodeLevel") String nodeLevel);

	@Select("SELECT COALESCE(max(node_no),0) FROM config_bom_tree WHERE node_code REGEXP CONCAT('^',#{nodeCode},'-[0-9]*$') and turbine_code=#{turbineCode}")
	Integer getSonMaxNo(@Param("nodeCode") String nodeCode, @Param("turbineCode") String turbineCode);

	List<ConfigBomTree> getAllSubNodesByNodeCode(@Param("turbineCode") String turbineCode,
			@Param("nodeCode") String nodeCode);

	// 根据节点编码，找到当前节点及其所有下属节点的Id，不包括本节点
	@Select("SELECT node_id FROM config_bom_tree WHERE (node_level = #{nodeLevel} and node_code REGEXP CONCAT('^',#{nodeCode},'-')) and turbine_code=#{turbineCode}")
	List<String> getNextLevelNodeIdsByNodeCode(@Param("nodeCode") String nodeCode,
			@Param("nodeLevel") Integer nodeLevel, @Param("turbineCode") String turbineCode);

	@Select("SELECT turbine_code FROM config_bom_tree WHERE (pro_id = #{proId} AND is_bom_passed = 1 AND node_level = 1)")
	List<String> getAllMatchedTurbineCode(@Param("proId") Long proId);

	List<ConfigBomTreeVo> getBomSons(@Param("nodeCode") String nodeCode, @Param("turbineCode") String turbineCode);

	@Select("SELECT * FROM config_bom_tree WHERE node_code = #{nodeCode} AND turbine_code = #{turbineCode}")
	ConfigBomTree getNodebyCode(@Param("nodeCode") String nodeCode, @Param("turbineCode") String turbineCode);

	@Select("SELECT turbine_code FROM config_bom_tree WHERE node_id = #{nodeId}")
	String getTurbineCodeByNodeId(@Param("nodeId") String nodeId);

	@Select("SELECT EXISTS (\n" + "    SELECT 1 \n" + "    FROM config_bom_tree \n"
			+ "    WHERE node_name = #{nodeName} AND node_level = 1\n" + ") AS is_exists")
	boolean exitNodeName(@Param("nodeName") String nodeName);

	List<String> selectTurbineCodesByNodeIds(@Param("nodeIds") List<String> nodeIds);

	List<Map<String, Object>> getRootNodes(@Param("proIds") List<Long> proIds);

	@Select({ "<script>", "SELECT node_id FROM config_bom_tree_template", "WHERE pro_id = #{proId}", "AND node_code IN",
			"<foreach collection='nodeCodes' item='code' open='(' separator=',' close=')'>", "#{code}", "</foreach>",
			"</script>" })
	List<String> getNodeIdsByProIdAndNodeCodes(@Param("proId") String proId,
			@Param("nodeCodes") List<String> nodeCodes);

}
