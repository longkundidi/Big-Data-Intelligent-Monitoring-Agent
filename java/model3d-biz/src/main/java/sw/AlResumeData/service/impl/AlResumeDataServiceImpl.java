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
package sw.AlResumeData.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sw.AlResumeData.entity.AlResumeData;
import sw.AlResumeData.mapper.AlResumeDataMapper;
import sw.AlResumeData.service.AlResumeDataService;
import sw.ConfigUserFarm.mapper.ConfigUserFarmMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 风场信息
 *
 * @author pig code generator
 * @date 2025-03-03 11:54:27
 */
@Service
public class AlResumeDataServiceImpl extends ServiceImpl<AlResumeDataMapper, AlResumeData>
		implements AlResumeDataService {

	@Autowired
	private AlResumeDataMapper alResumeDataMapper;

	@Autowired
	private ConfigUserFarmMapper configUserFarmMapper;

	@Override
	public Long getIdByFarmName(String farmName) {
		return alResumeDataMapper.getIdByFarmName(farmName);
	}

	@Override
	public List<AlResumeData> getIdFarmsByUserId(Long userId, String userRole) {

		if ("ROLE_SYS".equals(userRole)) {
			// 当 userRole 为 ROLE_SYS 时，查询所有记录
			return alResumeDataMapper.selectList(new QueryWrapper<>());
		}

		// 当 userRole 不是 ROLE_SYS 时，获取用户关联的风场名称
		List<String> farmNames = configUserFarmMapper.getFarmNamesByUserId(userId);
		if (farmNames.isEmpty()) {
			return Collections.emptyList(); // 如果用户没有关联的风场，返回空列表
		}

		// 查询符合风场名的 AlResumeData 记录
		return alResumeDataMapper
			.selectList(new QueryWrapper<AlResumeData>().in("project", farmNames).eq("bom_model", "SBOM"));
	}

}
