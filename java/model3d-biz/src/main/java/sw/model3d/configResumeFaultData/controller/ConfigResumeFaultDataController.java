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

package sw.model3d.configResumeFaultData.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import sw.model3d.configResumeFaultData.entity.ConfigResumeFaultData;
import sw.model3d.configResumeFaultData.service.ConfigResumeFaultDataService;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 风机实例的实际报警信息
 *
 * @author pig code generator
 * @date 2024-07-24 17:21:39
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configresumefaultdata")
@Tag(name = "风机实例的实际报警信息管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigResumeFaultDataController {

	private final ConfigResumeFaultDataService configResumeFaultDataService;

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	public R getConfigResumeFaultDataPage(@RequestParam Integer currentPage, @RequestParam Integer pageSize,
			@RequestParam String nodeId) {
		return R.ok(configResumeFaultDataService.getConfigResumeFaultDataPage(currentPage, pageSize, nodeId));
	}

	/**
	 * 新增风机实例的实际报警信息
	 * @param configResumeFaultData 风机实例的实际报警信息
	 * @return R
	 */
	@Operation(summary = "新增风机实例的实际报警信息", description = "新增风机实例的实际报警信息")
	@SysLog("新增风机实例的实际报警信息")
	@PostMapping
	public R save(@RequestBody ConfigResumeFaultData configResumeFaultData) {
		return R.ok(configResumeFaultDataService.save(configResumeFaultData));
	}

	@Operation(summary = "查询指定时间段内的故障记录", description = "查询指定时间段内的故障记录")
	@GetMapping("/getFaultByTime")
	public R getFaultByTime(@RequestParam String nodeId, @RequestParam String startTime, @RequestParam String endTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime start = LocalDateTime.parse(startTime, formatter);
		LocalDateTime end = LocalDateTime.parse(endTime, formatter);
		return R.ok(configResumeFaultDataService.getFaultByTime(nodeId, start, end));
	}

}
