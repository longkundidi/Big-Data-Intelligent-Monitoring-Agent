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

package sw.model3d.configFailureHistory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.model3d.configFailureHistory.entity.ConfigFailureHistory;
import sw.model3d.configFailureHistory.entity.ConfigFailureHistoryVo;

import java.util.List;

/**
 * @author pig code generator
 * @date 2026-03-19 16:15:58
 */
public interface ConfigFailureHistoryService extends IService<ConfigFailureHistory> {

	// 根据节点id查询故障历史
	List<ConfigFailureHistory> getByNodeId(String nodeId);

	// 根据节点id查询故障历史（VO）
	List<ConfigFailureHistoryVo> getVoByNodeId(String nodeId);

}
