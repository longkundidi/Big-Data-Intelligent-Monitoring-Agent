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
package sw.model3d.configAlarmInfo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sw.model3d.configAlarmInfo.entity.DcAlarm;
import sw.model3d.configAlarmInfo.entity.vo.ConfigAlarmInfoVo;
import sw.model3d.configAlarmInfo.mapper.ConfigAlarmInfoMapper;
import sw.model3d.configAlarmInfo.service.ConfigAlarmInfoService;
import sw.model3d.configModel.entity.ConfigBomTree;
import sw.model3d.configModel.entity.ConfigPerceivedTask;
import sw.model3d.configModel.service.ConfigBomTreeService;
import sw.model3d.configModel.service.ConfigPerceivedTaskService;
import sw.model3d.modelBaseInfo.entity.M3ModelBaseInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 閺佸懘娈版０鍕劅閻ㄥ嫭濮ょ拃锔惧仯娣団剝浼呯拋鏉跨秿
 *
 * @author pig code generator
 * @date 2024-07-01 22:20:43
 */
@Service
public class ConfigAlarmInfoServiceImpl extends ServiceImpl<ConfigAlarmInfoMapper, DcAlarm>
		implements ConfigAlarmInfoService {

	// TODO: Temporary test behavior.
	// TODO: dc_alarm currently stores task_id as 158 for all rows.
	// TODO: Revert to task.getTaskId() after data is fixed.
	private static final Long FIXED_ALARM_TASK_ID = 158L;

	@Autowired
	ConfigPerceivedTaskService configPerceivedTaskService;

	@Autowired
	ConfigBomTreeService configBomTreeService;

	@Override
	public ArrayList<ConfigAlarmInfoVo> getConfigAlarmInfoPage(String proId) {
		LocalDateTime beforeDateTime = LocalDateTime.now().minusDays(1);
		// 閺嶈宓乸roid閺屻儴顕楅幍鈧張澶屾畱taskid
		LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTask::getProId, proId);
		List<ConfigPerceivedTask> taskList = configPerceivedTaskService.getBaseMapper().selectList(queryWrapper);

		// 閺嶈宓乼askid閺屻儴顕楅幍鈧張澶屾畱閹躲儴顒熺拋鏉跨秿
		ArrayList<ConfigAlarmInfoVo> alarmInfoVos = new ArrayList<>();
		for (ConfigPerceivedTask task : taskList) {
			ConfigBomTree node = configBomTreeService.getNode(task.getNodeId());
			LambdaQueryWrapper<DcAlarm> queryWrapper1 = new LambdaQueryWrapper<>();
			// TODO: Revert to task.getTaskId() after dc_alarm data is fixed.
			queryWrapper1.eq(DcAlarm::getTaskId, task.getTaskId()).ge(DcAlarm::getDcTime, beforeDateTime);
			String turbineName = configBomTreeService
				.getOne(new QueryWrapper<ConfigBomTree>().eq("turbine_code", node.getTurbineCode())
					.eq("node_type", "Root"))
				.getNodeName();
			List<LocalDateTime> dateTimeList = this.getBaseMapper()
				.selectList(queryWrapper1)
				.stream()
				.map(DcAlarm::getDcTime)
				.collect(Collectors.toList());
			for (LocalDateTime dcTime : dateTimeList) {
				ConfigAlarmInfoVo configAlarmInfoVo = new ConfigAlarmInfoVo();
				configAlarmInfoVo.setTurbineName(turbineName);
				configAlarmInfoVo.setNodeName(node.getNodeName());
				configAlarmInfoVo.setNodeId(node.getNodeId());
				configAlarmInfoVo.setNodeCode(node.getNodeCode());
				configAlarmInfoVo.setTurbineCode(node.getTurbineCode());
				configAlarmInfoVo.setModelName(task.getModelName());
				configAlarmInfoVo.setModelShortName(task.getAlgoShortname());
				configAlarmInfoVo.setDcTime(dcTime);
				// TODO: Revert to task.getTaskId() after dc_alarm data is fixed.
				configAlarmInfoVo.setTaskId(task.getTaskId());
				alarmInfoVos.add(configAlarmInfoVo);
			}
		}

		return alarmInfoVos;
	}

	@Override
	public ArrayList<ConfigAlarmInfoVo> getConfigAlarmInfoSevenDayPage(String proId) {
		return buildAlarmInfoByWindow(proId, 7);
	}

	private ArrayList<ConfigAlarmInfoVo> buildAlarmInfoByWindow(String proId, long days) {
		LocalDateTime beforeDateTime = LocalDateTime.now().minusDays(days);
		LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTask::getProId, proId);
		List<ConfigPerceivedTask> taskList = configPerceivedTaskService.getBaseMapper().selectList(queryWrapper);

		ArrayList<ConfigAlarmInfoVo> alarmInfoVos = new ArrayList<>();
		for (ConfigPerceivedTask task : taskList) {
			ConfigBomTree node = configBomTreeService.getNode(task.getNodeId());
			if (node == null) {
				continue;
			}

			LambdaQueryWrapper<DcAlarm> alarmQueryWrapper = new LambdaQueryWrapper<>();
			alarmQueryWrapper.eq(DcAlarm::getTaskId, task.getTaskId()).ge(DcAlarm::getDcTime, beforeDateTime);

			ConfigBomTree rootNode = configBomTreeService
				.getOne(new QueryWrapper<ConfigBomTree>().eq("turbine_code", node.getTurbineCode())
					.eq("node_type", "Root"));
			String turbineName = rootNode != null ? rootNode.getNodeName() : node.getNodeName();

			List<LocalDateTime> dateTimeList = this.getBaseMapper()
				.selectList(alarmQueryWrapper)
				.stream()
				.map(DcAlarm::getDcTime)
				.collect(Collectors.toList());
			for (LocalDateTime dcTime : dateTimeList) {
				ConfigAlarmInfoVo configAlarmInfoVo = new ConfigAlarmInfoVo();
				configAlarmInfoVo.setTurbineName(turbineName);
				configAlarmInfoVo.setNodeName(node.getNodeName());
				configAlarmInfoVo.setNodeId(node.getNodeId());
				configAlarmInfoVo.setNodeCode(node.getNodeCode());
				configAlarmInfoVo.setTurbineCode(node.getTurbineCode());
				configAlarmInfoVo.setModelName(task.getModelName());
				configAlarmInfoVo.setModelShortName(task.getAlgoShortname());
				configAlarmInfoVo.setDcTime(dcTime);
				configAlarmInfoVo.setTaskId(task.getTaskId());
				alarmInfoVos.add(configAlarmInfoVo);
			}
		}

		return alarmInfoVos;
	}

}
