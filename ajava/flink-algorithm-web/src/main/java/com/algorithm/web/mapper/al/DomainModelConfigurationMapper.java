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

package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.DomainModelConfiguration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 状态感知模型信息
 *
 * @author pig code generator
 * @date 2025-04-08 15:53:44
 */
@Mapper
public interface DomainModelConfigurationMapper extends BaseMapper<DomainModelConfiguration> {

	@Delete("DELETE FROM domain_model_configuration WHERE code = #{ code }")
	Boolean deleteConfig(@Param("code") String code);

	@Select("SELECT COUNT(1) > 0 FROM domain_model_configuration WHERE model_name = #{modelName}")
	Boolean exitConfigName(@Param("modelName") String modelName);

	// 统计分组总数
	@Select("SELECT COUNT(DISTINCT code) FROM domain_model_configuration")
	Long countDistinctGroups();

	// 查询分组的 code（支持分页）
	@Select("SELECT code FROM domain_model_configuration GROUP BY code ORDER BY code LIMIT #{pageSize} OFFSET #{offset}")
	List<String> selectGroupedCodes(@Param("pageSize") int pageSize, @Param("offset") int offset);

	// 根据分组的 code 查询详细信息
	@Select({ "<script>", "SELECT * FROM domain_model_configuration", "WHERE code IN ",
			"<foreach collection='codes' item='code' open='(' separator=',' close=')'>", "#{code}", "</foreach>",
			"</script>" })
	List<DomainModelConfiguration> selectByCodes(@Param("codes") List<String> codes);

	@Select(" SELECT code FROM domain_model_configuration GROUP BY code ORDER BY CAST(SUBSTRING(code, LOCATE('-', code) + 1) AS SIGNED)LIMIT #{pageSize} OFFSET #{offset} ")
	List<String> selectGroupedCodesOrdered(@Param("pageSize") int pageSize, @Param("offset") int offset);

}
