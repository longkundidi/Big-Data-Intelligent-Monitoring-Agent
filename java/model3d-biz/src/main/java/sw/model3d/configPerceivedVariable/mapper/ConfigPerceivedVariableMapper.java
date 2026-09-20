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

package sw.model3d.configPerceivedVariable.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author pig code generator
 * @date 2024-03-22 19:24:18
 */
@Mapper
public interface ConfigPerceivedVariableMapper extends BaseMapper<ConfigPerceivedVariable> {

	@Select("SELECT var_name FROM config_perceived_variable")
	List<String> findVariableNames();

	/*
	 * @Select("SELECT DISTINCT var_name FROM config_perceived_variable WHERE scene_id = #{sceneId}"
	 * ) List<String> findVariableNamesBySceneId(@Param("sceneId") String sceneId);
	 */

	@Select("SELECT node_id FROM config_perceived_variable where var_name = #{varName}")
	List<String> getVariableNodeIds(@Param("varName") String varName);

	@Select("SELECT var_name FROM config_perceived_variable where var_id = #{varId}")
	String findVariableNameById(@Param("varId") String varId);

	@Select("SELECT var_id, var_name FROM config_perceived_variable WHERE node_id = #{nodeId}")
	List<Map<String, Object>> findVariableNamesByNodeId(@Param("nodeId") String nodeId);

	@Select("SELECT var_id FROM config_perceived_variable where var_name = #{variable} LIMIT 1")
	String getVarId(@Param("variable") String variable);

}
