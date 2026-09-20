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

import com.algorithm.web.model.dto.al.Configuration.ChildDto;
import com.algorithm.web.model.dto.al.Configuration.ConfigurationDto;
import com.algorithm.web.model.dto.al.Configuration.DomainModelConfigurationDto;
import com.algorithm.web.model.dto.al.Configuration.GroupedConfigDTO;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.model.entity.al.DomainModelConfiguration;
import com.alibaba.nacos.shaded.com.google.protobuf.ServiceException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.flink.table.planner.expressions.In;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

/**
 * 状态感知模型信息
 *
 * @author pig code generator
 * @date 2025-04-08 15:53:44
 */
public interface DomainModelConfigurationService extends IService<DomainModelConfiguration> {

	Object saveConfig(ConfigurationDto configurationDto) throws ServiceException;

	Boolean deleteConfig(String code);

	Boolean exitConfigName(String modelName);

	Long startConfig(ChildDto childDto) throws MalformedURLException;

	Object checkConfig(DomainModelConfigurationDto configurationDto, Boolean isCheck);

	Object getCheckStatus(DomainModelConfigurationDto configurationDto);

	Page<DomainModelConfigurationDto> getConfigs(Page<DomainModelConfiguration> page);

}
