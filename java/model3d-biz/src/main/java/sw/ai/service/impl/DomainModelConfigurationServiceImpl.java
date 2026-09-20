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
package sw.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sw.ai.domain.DomainModelConfiguration;
import sw.ai.mapper.DomainModelConfigurationMapper;
import sw.ai.model.dto.GroupedConfigDTO;
import sw.ai.service.DomainModelConfigurationService;

import java.util.ArrayList;
import java.util.List;

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

	@Override
	public List<GroupedConfigDTO> getAllDomainConfigByBD() {
		// // 1. 查询分组总数（需确保是全局唯一 code 的数量）
		// Long totalGroups = baseMapper.countDistinctGroups();
		//
		// // 2. 查询分组的 code（已全局排序）
		// List<String> groupedCodes = baseMapper.selectGroupedCodesOrdered();
		//
		// // 3. 根据 code 查询详细信息
		// List<DomainModelConfiguration> rawRecords =
		// baseMapper.selectByCodes(groupedCodes);
		//
		// // 4. 分组处理
		// Map<String, GroupedConfigDTO> groupedMap = rawRecords.stream()
		// .collect(Collectors.toMap(
		// DomainModelConfiguration::getCode,
		// item -> {
		// GroupedConfigDTO dto = new GroupedConfigDTO();
		// dto.setCode(item.getCode());
		// dto.setModelName(item.getModelName());
		// dto.setModelObject(item.getModelObject());
		// dto.setModelIcon(item.getAlmodelIcon());
		// dto.setIsService(item.getIsService());
		// dto.setTaskProcess(new ArrayList<>());
		// return dto;
		// },
		// (existing, replacement) -> existing
		// ));
		//
		// // 6. 填充 taskProcess
		// rawRecords.forEach(item -> {
		// GroupedConfigDTO group = groupedMap.get(item.getCode());
		// GroupedConfigDTO.TaskProcessDTO taskDto = new
		// GroupedConfigDTO.TaskProcessDTO();
		// taskDto.setAlmodelName(item.getAlmodelName());
		// taskDto.setSequence(item.getSequence());
		// group.getTaskProcess().add(taskDto);
		// });
		//
		// // 7. 对 taskProcess 按 sequence 排序
		// groupedMap.values().forEach(group -> {
		// // 对每个 group 中的 taskProcess 进行排序
		// group.getTaskProcess().sort(Comparator.comparingInt(GroupedConfigDTO.TaskProcessDTO::getSequence));
		//
		// // 合并相同 sequence 的 taskProcess
		// Map<Integer, StringBuilder> mergedTaskProcesses = new LinkedHashMap<>();
		// group.getTaskProcess().forEach(task -> {
		// mergedTaskProcesses.computeIfAbsent(task.getSequence(), k -> new
		// StringBuilder())
		// .append(task.getAlmodelName()).append("；");
		// });
		//
		// // 清空原 taskProcess 列表，重新添加合并后的数据
		// group.getTaskProcess().clear();
		// mergedTaskProcesses.forEach((sequence, almodelNames) -> {
		// // 去掉末尾的分号
		// GroupedConfigDTO.TaskProcessDTO newTaskDto = new
		// GroupedConfigDTO.TaskProcessDTO();
		// newTaskDto.setSequence(sequence);
		// newTaskDto.setAlmodelName(almodelNames.toString().replaceAll("；$", ""));
		// group.getTaskProcess().add(newTaskDto);
		// });
		// });
		//
		// // 8. 构建分页结果（records 已通过 SQL 全局排序）
		// List<GroupedConfigDTO> sortedRecords = groupedMap.values().stream()
		// .sorted(Comparator.comparingInt(
		// group -> Integer.parseInt(group.getCode().split("-")[1]) // 提取数字部分并转为整数
		// ))
		// .collect(Collectors.toList());
		// return sortedRecords;
		//
		List<GroupedConfigDTO> result = new ArrayList<>();
		List<DomainModelConfiguration> domainModelConfigurations = domainModelConfigurationMapper.getAll();
		for (DomainModelConfiguration config : domainModelConfigurations) {
			GroupedConfigDTO dto = new GroupedConfigDTO();
			BeanUtils.copyProperties(config, dto);
			dto.setTaskProcess(new ArrayList<>()); // 若有默认字段需初始化
			List<GroupedConfigDTO.TaskProcessDTO> taskProcessDTOS = new ArrayList<>();
			ObjectMapper objectMapper = new ObjectMapper();
			result.add(dto);
		}
		return result;
	}

}
