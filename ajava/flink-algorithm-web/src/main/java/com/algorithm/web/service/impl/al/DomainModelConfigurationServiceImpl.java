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
package com.algorithm.web.service.impl.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.enums.SysConfigEnum;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlgorithmConfigurationMapper;
import com.algorithm.web.mapper.al.DomainModelConfigurationMapper;
import com.algorithm.web.model.dto.al.Configuration.ChildDto;
import com.algorithm.web.model.dto.al.Configuration.ConfigurationDto;
import com.algorithm.web.model.dto.al.Configuration.DomainModelConfigurationDto;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.model.entity.al.AlgorithmConfiguration;
import com.algorithm.web.model.entity.al.DomainModelConfiguration;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.service.al.AlTaskService;
import com.algorithm.web.service.al.DomainModelConfigurationService;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.utils.RestTemplateUtil;
import com.algorithm.web.utils.TaskMsgServices;
import com.alibaba.nacos.shaded.com.google.protobuf.ServiceException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 状态感知模型信息
 *
 * @author pig code generator
 * @date 2025-04-08 15:53:44
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DomainModelConfigurationServiceImpl
		extends ServiceImpl<DomainModelConfigurationMapper, DomainModelConfiguration>
		implements DomainModelConfigurationService {

	@Autowired
	DomainModelConfigurationMapper domainModelConfigurationMapper;

	@Autowired
	private Map<String, BuildTaskMsgService> algorithmServiceMap;

	@Autowired
	private AlTaskService alTaskService;

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	AlgorithmConfigurationMapper algorithmConfigurationMapper;

	@Override
	public Long saveConfig(ConfigurationDto dto) throws ServiceException {
		// 创建新的配置对象
		DomainModelConfiguration cfg = new DomainModelConfiguration();
		cfg.setModelName(dto.getModelName());
		cfg.setModelObject(dto.getModelObject());
		cfg.setObjectId(dto.getObjectId());
		cfg.setSequence(dto.getModelSequence());
		cfg.setAlmodelType(dto.getModelType());
		cfg.setCreateTime(LocalDateTime.now());
		cfg.setAlmodelIcon(dto.getImageUrl());
		cfg.setVote(dto.getModelVote());

		if (!this.save(cfg)) { // 返回值检查
			throw new ServiceException("保存配置失败");
		}

		cfg.setCode("dac-" + cfg.getId());
		if (!this.updateById(cfg)) {
			throw new ServiceException("写入 code 失败");
		}

		return cfg.getId();
	}

	@Override
	public Boolean deleteConfig(String code) {
		return domainModelConfigurationMapper.deleteConfig(code);
	}

	@Override
	public Boolean exitConfigName(String modelName) {
		return domainModelConfigurationMapper.exitConfigName(modelName);
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	static class TaskMsg {

		String programUrl;

	}

	@Override
	public Long startConfig(ChildDto childDto) throws MalformedURLException {
		AlTask alTask = new AlTask();

		// 依次执行算法
		String configUrl = childDto.getConfigUrl();

		Optional<BuildTaskMsgService> optional = Optional.ofNullable(
				algorithmServiceMap.get(TaskMsgServices.getTaskMsgService("config-" + childDto.getShortName())));
		if (optional.isPresent()) {
			BuildTaskMsgService buildTaskMsgService = optional.get();
			alTask.setTaskUrl(configUrl);
			alTask.setTaskState(0);
			alTask.setTaskReUrl(systemConfigService.getSystemConfigByKey(SysConfigEnum.ALGORITHM_CALLBACK_URL.getKey())
					+ "algorithm/job/algorithmJobCallback");
			alTask.setTaskMsg(buildTaskMsgService.buildTaskMsg(childDto.getTaskMsg()));
			alTask.setUseCase("算法组态");
			alTask.setStartTime(LocalDateTime.now());
			alTask.setAlClass("modelConfiguration");
			alTaskService.save(alTask);
			alTask.setTaskId(alTask.getId());

			String body = JsonUtil.toJson(alTask);
			log.info("开始执行任务 taskId={} algorithmUrl={} body={}", alTask.getTaskId(), configUrl, body);

			executeAlgorithmUrls(alTask, configUrl, body);

		}
		else {
			throw new BizException("组态算法 " + childDto.getShortName() + " 未注册");
		}

		return alTask.getId();

	}

	@Override
	public Object checkConfig(DomainModelConfigurationDto configurationDto, Boolean isCheck) {

		if (configurationDto == null || configurationDto.getCode() == null || configurationDto.getModelName() == null) {
			return RestResult.error("配置数据不能为空");
		}
		// 1. 查询符合条件的记录
		LambdaQueryWrapper<DomainModelConfiguration> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DomainModelConfiguration::getCode, configurationDto.getCode())
			.eq(DomainModelConfiguration::getModelName, configurationDto.getModelName());
		List<DomainModelConfiguration> existingConfigs = this.list(queryWrapper);
		if (existingConfigs.isEmpty()) {
			return RestResult.error("未找到匹配的配置记录");
		}
		// 2. 批量更新记录
		existingConfigs.forEach(config -> {
			// 反转 isService 字段
			config.setIsService(config.getIsService() == 1 ? 0 : 1);
			// 根据 isCheck 决定是否更新 isDeployed
			if (isCheck) {
				config.setIsDeployed(config.getIsDeployed() == 1L ? 0L : 1L);
			}
		});
		// 3. 执行批量更新
		this.updateBatchById(existingConfigs);
		if (isCheck) {
			return RestResult.success("通过审核");
		}
		return RestResult.success("更新启用状态");
	}

	@Override
	public Object getCheckStatus(DomainModelConfigurationDto configurationDto) {
		LambdaQueryWrapper<DomainModelConfiguration> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DomainModelConfiguration::getCode, configurationDto.getCode())
			.eq(DomainModelConfiguration::getModelName, configurationDto.getModelName())
			.last("LIMIT 1"); // 优化：只查询一条记录
		DomainModelConfiguration config = this.getOne(queryWrapper);
		return config != null ? config.getIsDeployed() : null;
	}

	@Override
	public Page<DomainModelConfigurationDto> getConfigs(Page<DomainModelConfiguration> page) {
		Page<DomainModelConfiguration> configPage = this.page(page);

		List<DomainModelConfigurationDto> dtoList = new ArrayList<>();

		for (DomainModelConfiguration config : configPage.getRecords()) {

			DomainModelConfigurationDto dto = new DomainModelConfigurationDto();
			BeanUtils.copyProperties(config, dto);

			// 处理 sequence 字段
			List<String> codeList = new ArrayList<>();
			try {
				codeList = new ObjectMapper().readValue(config.getSequence(), new TypeReference<List<String>>() {
				});
			}
			catch (Exception e) {
				log.error("解析 groupList 失败: {}", config.getSequence(), e);
			}

			if (!codeList.isEmpty()) {
				// 查询 AlgorithmConfiguration 中的 model_name
				List<AlgorithmConfiguration> algoList = algorithmConfigurationMapper.selectList(
						Wrappers.<AlgorithmConfiguration>lambdaQuery().in(AlgorithmConfiguration::getCode, codeList));

				// 根据 code 去重，仅保留第一个 model_name
				Map<String, String> codeToModelName = new LinkedHashMap<>();
				for (AlgorithmConfiguration algo : algoList) {
					codeToModelName.putIfAbsent(algo.getCode(), algo.getModelName());
				}

				// 保持顺序拼接 taskProcess
				List<String> taskProcess = new ArrayList<>();
				for (String code : codeList) {
					String name = codeToModelName.get(code);
					if (name != null) {
						taskProcess.add(name);
					}
				}

				dto.setTaskProcess(taskProcess);
			}
			dtoList.add(dto);
		}

		// 返回新的分页结果
		Page<DomainModelConfigurationDto> dtoPage = new Page<>();
		dtoPage.setRecords(dtoList);
		dtoPage.setCurrent(configPage.getCurrent());
		dtoPage.setSize(configPage.getSize());
		dtoPage.setTotal(configPage.getTotal());
		dtoPage.setPages(configPage.getPages());

		return dtoPage;

	}

	private void executeAlgorithmUrls(AlTask alTask, String configUrl, String body) throws MalformedURLException {
		ObjectMapper objectMapper = new ObjectMapper();
		URL url = new URL(configUrl);
		String base_url = url.getProtocol() + "://" + url.getHost();
		try {
			String res = RestTemplateUtil.post(configUrl, body); // 尝试请求
			log.info("任务 taskId={} 返回={}", alTask.getTaskId(), res);
			if (isValidJson(res)) {
				JsonNode rootNode = objectMapper.readTree(res);
				String content = rootNode.get("taskState").toString();

				if (content.equals("3") || res == null) {
					alTask.setTaskResult(null);
					alTask.setId(rootNode.get("taskId").asLong());
					alTask.setTaskState(3);
					alTask.setServerUrl(base_url);
				}
				else {
					alTask = JsonUtil.fromJson(res, AlTask.class);
					alTask.setId(rootNode.get("taskId").asLong());
					alTask.setTaskUrl(configUrl); // 记录实际使用的算法 URL
					alTask.setTaskState(2);
					alTask.setServerUrl(base_url);
				}
				alTask.setEndTime(LocalDateTime.now());
				alTaskService.updateById(alTask);
			}
		}
		catch (Exception e) {
			log.warn("请求算法服务 {} 失败，错误信息: {}", configUrl, e.getMessage());
			alTask.setTaskUrl(configUrl); // 记录实际使用的算法 URL
			alTask.setTaskState(3);
			alTask.setServerUrl(base_url);
		}

	}

	private boolean isValidJson(String json) {
		try {
			JsonUtil.fromJson(json, Object.class); // 尝试将其解析为 Object
			return true; // 如果没有抛出异常，说明是有效的 JSON
		}
		catch (Exception e) {
			return false; // 如果抛出异常，则不是有效的 JSON
		}
	}

}
