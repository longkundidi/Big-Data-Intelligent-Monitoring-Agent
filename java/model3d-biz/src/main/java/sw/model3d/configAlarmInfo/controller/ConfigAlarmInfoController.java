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

package sw.model3d.configAlarmInfo.controller;

import com.pig4cloud.pig.common.core.util.R;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import sw.model3d.configAlarmInfo.entity.DcAlarm;
import sw.model3d.configAlarmInfo.entity.ConfigAlarmInfoQueryRequest;
import sw.model3d.configAlarmInfo.service.ConfigAlarmInfoService;
import sw.model3d.configModel.service.ConfigPerceivedTaskService;

/**
 * 故障预警的报警点信息记录
 *
 * @author pig code generator
 * @date 2024-07-01 22:20:43
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configalarminfo")
@Tag(name = "故障预警的报警点信息记录管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigAlarmInfoController {

	private final ConfigAlarmInfoService configAlarmInfoService;

	@Operation(summary = "分页查询", description = "分页查询")
	@PostMapping("/page")
	public R getConfigAlarmInfoPage(@RequestParam Long proId) {
		return R.ok(configAlarmInfoService.getConfigAlarmInfoPage(proId.toString()), "查询成功");
	}

	@Operation(summary = "查询近7天全部报警", description = "返回项目下近7天的全部报警记录，不按日期聚合")
	@PostMapping("/page7d")
	public R getConfigAlarmInfoSevenDayPage(@RequestParam Long proId) {
		return R.ok(configAlarmInfoService.getConfigAlarmInfoSevenDayPage(proId.toString()), "查询成功");
	}

}
