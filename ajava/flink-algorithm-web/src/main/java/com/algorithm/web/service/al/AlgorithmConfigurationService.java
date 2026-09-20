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

package com.algorithm.web.service.al;

import com.algorithm.web.model.dto.al.Configuration.AlgorithmConfigurationDto;
import com.algorithm.web.model.dto.al.Configuration.ChildDto;
import com.algorithm.web.model.dto.al.Configuration.ConfigurationDto;

import com.algorithm.web.model.dto.al.Configuration.GroupedConfigDTO;
import com.algorithm.web.model.entity.al.AlgorithmConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.net.MalformedURLException;

/**
 * 领域模型组态管理表
 *
 * @author pig code generator
 * @date 2025-04-29 10:28:19
 */
public interface AlgorithmConfigurationService extends IService<AlgorithmConfiguration> {

	Boolean deleteConfig(String code);

	String saveConfig(ConfigurationDto configurationDto);

	Boolean exitConfigName(String modelName);

	Long startConfig(ChildDto childDto) throws MalformedURLException;

	Object checkConfig(AlgorithmConfigurationDto configurationDto, Boolean isCheck);

	Object getCheckStatus(AlgorithmConfigurationDto configurationDto);

	Page<GroupedConfigDTO> getConfigs(Page page);

	Page<GroupedConfigDTO> getAllConfigs();

	Object updateIspublishedConfig(AlgorithmConfigurationDto configurationDto);

}
