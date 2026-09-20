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
package sw.model3d.configModel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import sw.model3d.configModel.entity.ConfigModel;
import sw.model3d.configModel.entity.ConfigPerceivedTask;
import sw.model3d.configModel.entity.vo.ConfigBomTreeVo;
import sw.model3d.configModel.mapper.ConfigModelMapper;
import sw.model3d.configModel.mapper.ConfigPerceivedTaskMapper;
import sw.model3d.configModel.service.ConfigModelService;
import org.springframework.stereotype.Service;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.List;

/**
 * @author pig code generator
 * @date 2024-04-01 10:43:50
 */
@Service
public class ConfigModelServiceImpl extends ServiceImpl<ConfigModelMapper, ConfigModel> implements ConfigModelService {

	@Autowired
	ConfigPerceivedTaskMapper configPerceivedTaskMapper;

	@Override
	public List<ConfigModel> getByNodeIdAndModelType(String nodeId, String modelType) {
		List<ConfigModel> list = this.list(new QueryWrapper<ConfigModel>().lambda()
			.eq(ConfigModel::getNodeId, nodeId)
			.eq(ConfigModel::getModelType, modelType)
			.eq(ConfigModel::getIsService, 0));
		return list;
	}

	@Override
	public String getNodeId(Long taskId) {
		String modelId = configPerceivedTaskMapper.getModelId(taskId);
		return this.baseMapper.getNodeId(modelId);

	}

}
