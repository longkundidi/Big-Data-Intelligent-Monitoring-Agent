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

package sw.model3d.stateAssessment.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import sw.model3d.stateAssessment.entity.DcAlgorithmHistory;
import sw.model3d.stateAssessment.service.DcAlgorithmHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 风机实例任务执行结果（任务类型：状态感知任务）
 *
 * @author pig code generator
 * @date 2024-07-16 14:41:16
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/dcalgorithmhistory")
@Tag(name = "风机实例任务执行结果（任务类型：状态感知任务）管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DcAlgorithmHistoryController {

	private final DcAlgorithmHistoryService dcAlgorithmHistoryService;

	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/getHistoryDcData")
	public R getDcAlgorithmHistoryPage(@RequestParam Long taskId, @RequestParam String algoShortname,
			@RequestParam String startDcTime, @RequestParam String endDcTime) throws JsonProcessingException {
		return R.ok(dcAlgorithmHistoryService.getHistoryDcData(taskId, algoShortname, startDcTime, endDcTime));
	}

}
