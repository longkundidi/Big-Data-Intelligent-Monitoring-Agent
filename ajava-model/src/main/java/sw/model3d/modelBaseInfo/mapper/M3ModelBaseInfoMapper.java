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

package sw.model3d.modelBaseInfo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import sw.model3d.modelBaseInfo.entity.M3ModelBaseInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * @author pig code generator
 * @date 2023-10-24 10:55:58
 */
@Mapper
public interface M3ModelBaseInfoMapper extends BaseMapper<M3ModelBaseInfo> {

	// 获取顶层节点最大编号
	@Select("SELECT COALESCE(max(node_no),0) FROM m3_model_base_info WHERE node_level = 1")
	Integer getTopMaxNo();

	// 获取子节点最大编号
	@Select("SELECT COALESCE(max(node_no),0) FROM m3_model_base_info WHERE mb_code REGEXP CONCAT('^',#{pCode},'-[0-9]*$')")
	Integer getSonMaxNo(@Param("pCode") String pCode);

	List<M3ModelBaseInfo> getSonNode(@Param("mbCode") String mbCode);

}
