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

package sw.model3d.configFailureMode.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.model3d.configFailureMode.entity.ConfigFailureMode;
import sw.model3d.configFailureMode.entity.ConfigFailureModeVo;

import java.util.List;

/**
 * @author pig code generator
 * @date 2024-03-15 14:43:38
 */
public interface ConfigFailureModeService extends IService<ConfigFailureMode> {

	// 根据节点id查询故障记录
	List<ConfigFailureMode> getByNodeId(String nodeId);

	List<ConfigFailureModeVo> getByBomNodeName(String nodeName);

}
