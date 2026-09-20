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

package sw.model3d.configResumeFaultData.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import sw.model3d.configResumeFaultData.entity.ConfigResumeFaultData;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风机实例的实际报警信息
 *
 * @author pig code generator
 * @date 2024-07-24 17:21:39
 */
@Mapper
public interface ConfigResumeFaultDataMapper extends BaseMapper<ConfigResumeFaultData> {

	List<ConfigResumeFaultData> getFaultByTime(@Param("nodeId") String nodeId,
			@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

}
