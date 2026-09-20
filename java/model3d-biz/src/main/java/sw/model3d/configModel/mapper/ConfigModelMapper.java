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
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.configModel.entity.ConfigModel;
import org.apache.ibatis.annotations.Mapper;
import sw.model3d.configModel.entity.vo.ConfigBomTreeVo;

import java.util.List;

/**
 * @author pig code generator
 * @date 2024-04-01 10:43:50
 */
@Mapper
public interface ConfigModelMapper extends BaseMapper<ConfigModel> {

	@Select("SELECT node_id  FROM config_model where model_id=#{modelId}")
	String getNodeId(@Param("modelId") String modelId);

}
