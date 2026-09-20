package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlResumeDeviceData;
import com.algorithm.web.service.al.AlResumeDeviceDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alresumedevicedata")

public class AlResumeDeviceDataController {

	private final AlResumeDeviceDataService alResumeDeviceDataService;

	@GetMapping("/page")
	public RestResult getAlResumeFaultDataPage(Page page, AlResumeDeviceData alResumeDeviceData) {
		QueryWrapper<AlResumeDeviceData> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("project_id", alResumeDeviceData.getProjectId());
		return RestResult.success(alResumeDeviceDataService.list(queryWrapper));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success((alResumeDeviceDataService.getById(id)));
	}

	@PostMapping
	public RestResult save(@RequestBody AlResumeDeviceData alResumeDeviceData) {
		return RestResult.success(alResumeDeviceDataService.save(alResumeDeviceData));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlResumeDeviceData alResumeDeviceData) {
		return RestResult.success(alResumeDeviceDataService.updateById(alResumeDeviceData));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alResumeDeviceDataService.removeById(id));
	}

	// 获取BOM树
	@GetMapping("/getFengji/{id}")
	public RestResult getFengji(@PathVariable("id") Long projectId) {

		return RestResult.success(alResumeDeviceDataService.getFengji(projectId));
	}

}
