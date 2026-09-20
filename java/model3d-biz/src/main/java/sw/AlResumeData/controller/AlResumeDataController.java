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

package sw.AlResumeData.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sw.AlResumeData.entity.AlResumeData;
import sw.AlResumeData.service.AlResumeDataService;

/**
 * 风场信息
 *
 * @author pig code generator
 * @date 2025-03-03 11:54:27
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/alresumedata")
@Tag(name = "风场信息管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class AlResumeDataController {

	private final AlResumeDataService alResumeDataService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param alResumeData 风场信息
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	@PreAuthorize("@pms.hasPermission('demo_alresumedata_get')")
	public R getAlResumeDataPage(Page page, AlResumeData alResumeData) {
		return R.ok(alResumeDataService.page(page, Wrappers.query(alResumeData)));
	}

	/**
	 * 通过id查询风场信息
	 * @param id id
	 * @return R
	 */
	@Operation(summary = "通过id查询", description = "通过id查询")
	@GetMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('demo_alresumedata_get')")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(alResumeDataService.getById(id));
	}

	/**
	 * 新增风场信息
	 * @param alResumeData 风场信息
	 * @return R
	 */
	@Operation(summary = "新增风场信息", description = "新增风场信息")
	@SysLog("新增风场信息")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('demo_alresumedata_add')")
	public R save(@RequestBody AlResumeData alResumeData) {
		return R.ok(alResumeDataService.save(alResumeData));
	}

	/**
	 * 修改风场信息
	 * @param alResumeData 风场信息
	 * @return R
	 */
	@Operation(summary = "修改风场信息", description = "修改风场信息")
	@SysLog("修改风场信息")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('demo_alresumedata_edit')")
	public R updateById(@RequestBody AlResumeData alResumeData) {
		return R.ok(alResumeDataService.updateById(alResumeData));
	}

	/**
	 * 通过id删除风场信息
	 * @param id id
	 * @return R
	 */
	@Operation(summary = "通过id删除风场信息", description = "通过id删除风场信息")
	@SysLog("通过id删除风场信息")
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('demo_alresumedata_del')")
	public R removeById(@PathVariable Long id) {
		return R.ok(alResumeDataService.removeById(id));
	}

	@Operation(summary = "根据风场名返回id", description = "根据风场名返回id")
	@SysLog("根据风场名返回id")
	@GetMapping("/getIdByFarmName/{farmName}")
	public R getIdByFarmName(@PathVariable String farmName) {
		return R.ok(alResumeDataService.getIdByFarmName(farmName));
	}

	@Operation(summary = "根据用户id返回风场列表", description = "根据用户id返回风场列表")
	@SysLog("根据用户id返回风场列表")
	@GetMapping("/getIdFarmsByUserId")
	public R getIdFarmsByUserId(@RequestParam Long userId, @RequestParam String userRole) {
		return R.ok(alResumeDataService.getIdFarmsByUserId(userId, userRole), "查询成功");
	}

}
