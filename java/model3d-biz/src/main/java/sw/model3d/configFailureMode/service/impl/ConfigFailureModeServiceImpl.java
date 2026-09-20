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
package sw.model3d.configFailureMode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import sw.model3d.configFailureMode.entity.ConfigFailureMode;
import sw.model3d.configFailureMode.entity.ConfigFailureModeVo;
import sw.model3d.configFailureMode.mapper.ConfigFailureModeMapper;
import sw.model3d.configFailureMode.service.ConfigFailureModeService;
import org.springframework.stereotype.Service;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.utils.codecTag;

import java.util.List;

/**
 * @author pig code generator
 * @date 2024-03-15 14:43:38
 */
@Service
public class ConfigFailureModeServiceImpl extends ServiceImpl<ConfigFailureModeMapper, ConfigFailureMode>
		implements ConfigFailureModeService {

	@Autowired
	private ConfigGbomTreeMapper configGbomTreeMapper;

	@Override
	public List<ConfigFailureMode> getByNodeId(String nodeId) {
		List<ConfigFailureMode> list = this
			.list(new QueryWrapper<ConfigFailureMode>().lambda().eq(ConfigFailureMode::getNodeId, nodeId));
		return list;
	}

	@Override
	public List<ConfigFailureModeVo> getByBomNodeName(String nodeName) {
		String nodeId = configGbomTreeMapper.getNodeIdByNodeName(nodeName);
		return this.baseMapper.getByNodeId(nodeId);
	}

}
