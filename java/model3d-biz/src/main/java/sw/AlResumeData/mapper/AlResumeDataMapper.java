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

package sw.AlResumeData.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.AlResumeData.entity.AlResumeData;

/**
 * 风场信息
 *
 * @author pig code generator
 * @date 2025-03-03 11:54:27
 */
@Mapper
public interface AlResumeDataMapper extends BaseMapper<AlResumeData> {

	@Select("SELECT id FROM al_resume_data WHERE project = #{farmName} LIMIT 1")
	Long getIdByFarmName(@Param("farmName") String farmName);

	/**
	 * 通过turbineCode前缀id查project，再查sceneId
	 */
	@Select("SELECT id FROM al_resume_data WHERE project = (SELECT project FROM al_resume_data WHERE id = #{projectNum}) AND bom_model = 'GBOM' LIMIT 1")
	String getSceneIdByProjectNum(@Param("projectNum") String projectNum);

}
