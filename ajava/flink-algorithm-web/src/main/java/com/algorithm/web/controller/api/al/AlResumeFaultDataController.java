package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlResumeFaultData;
import com.algorithm.web.service.al.AlResumeFaultDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alresumefaultdata")

public class AlResumeFaultDataController {

	private final AlResumeFaultDataService alResumeFaultDataService;

	@GetMapping("/page")
	public RestResult getAlResumeFaultDataPage(Page page, AlResumeFaultData alResumeFaultData) {
		QueryWrapper<AlResumeFaultData> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("project_id", alResumeFaultData.getProjectId());
		return RestResult.success(alResumeFaultDataService.list(queryWrapper));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success((alResumeFaultDataService.getById(id)));
	}

	@PostMapping
	public RestResult save(@RequestBody AlResumeFaultData alResumeFaultData) {
		return RestResult.success(alResumeFaultDataService.save(alResumeFaultData));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlResumeFaultData alResumeFaultData) {
		return RestResult.success(alResumeFaultDataService.updateById(alResumeFaultData));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alResumeFaultDataService.removeById(id));
	}

	// 由项目和风机查询故障信息
	@GetMapping("/getFaultInfoByProjectByFengji/{projectId}/{deviceId}")
	public RestResult getFaultInfoByProjectByFengji(@PathVariable("projectId") Long projectId,
			@PathVariable("deviceId") Long deviceId) {

		return RestResult.success(alResumeFaultDataService.getFaultInfo(projectId, deviceId));
	}

}
