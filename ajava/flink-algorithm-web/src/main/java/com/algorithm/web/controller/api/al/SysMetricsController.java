package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.SysMetrics;
import com.algorithm.web.service.al.SysMetricsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储系统的评价指标(SysMetrics)表控制层
 *
 * @author makejava
 * @since 2024-11-06 15:28:31
 */
@RestController
@RequestMapping("/api/sysMetrics")
@CrossOrigin
public class SysMetricsController {

	/**
	 * 服务对象
	 */
	@Autowired
	private SysMetricsService sysMetricsService;

	@GetMapping("/list")
	public RestResult getList(String alType) {
		LambdaQueryWrapper<SysMetrics> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysMetrics::getAlType, alType);
		List<SysMetrics> list = sysMetricsService.list(queryWrapper);
		List<String> metircs_list = list.stream().map(SysMetrics::getName).collect(Collectors.toList());
		return RestResult.success(metircs_list);
	}

	// 获取所有评价指标及详情
	@GetMapping("/page")
	public RestResult getPage(@RequestParam Integer currentPage, @RequestParam Integer pageSize,
			@RequestParam String alType) {
		Page<SysMetrics> page = new Page<>(currentPage, pageSize);
		LambdaQueryWrapper<SysMetrics> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(SysMetrics::getAlType, alType);
		Page<SysMetrics> lists = sysMetricsService.page(page, queryWrapper);
		return RestResult.success(lists);
	}

	// 获取指定id的详情
	@GetMapping("/{id}")
	public RestResult getById(@PathVariable Long id) {
		return RestResult.success(sysMetricsService.getById(id));
	}

	// 模糊查询评价指标
	@GetMapping("/searchByAltypeOrName")
	public RestResult searchByAltypeOrName(@RequestParam String content) {
		LambdaQueryWrapper<SysMetrics> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.like(SysMetrics::getName, content).or().like(SysMetrics::getAlType, content);
		List<SysMetrics> sysMetrics = sysMetricsService.getBaseMapper().selectList(queryWrapper);
		return RestResult.success(sysMetrics);
	}

	// 新增评价指标
	@PostMapping("/add")
	public RestResult addMetrics(@RequestBody SysMetrics sysMetrics) {
		return RestResult.success(sysMetricsService.save(sysMetrics));
	}

	// 删除评价指标
	@DeleteMapping("/delete/{id}")
	public RestResult deleteMetrics(@PathVariable String id) {
		return RestResult.success(sysMetricsService.removeById(id));
	}

	// 编辑评价指标
	@PutMapping("/edit")
	public RestResult editMetrics(@RequestBody SysMetrics sysMetrics) {
		return RestResult.success(sysMetricsService.updateById(sysMetrics));
	}

}
