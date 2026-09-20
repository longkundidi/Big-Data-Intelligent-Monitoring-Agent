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

package sw.model3d.modelBaseInfo.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.model3d.modelBaseInfo.entity.M3ModelBaseInfo;
import sw.model3d.modelBaseInfo.service.M3ModelBaseInfoService;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * @author pig code generator
 * @date 2023-10-24 10:55:58
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/m3modelbaseinfo")
@Tag(name = "管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class M3ModelBaseInfoController {

	private final M3ModelBaseInfoService m3ModelBaseInfoService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param m3ModelBaseInfo
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	@PreAuthorize("@pms.hasPermission('modelBaseInfo_m3modelbaseinfo_get')")
	public R getM3ModelBaseInfoPage(Page page, M3ModelBaseInfo m3ModelBaseInfo) {
		return R.ok(m3ModelBaseInfoService.page(page, Wrappers.query(m3ModelBaseInfo)));
	}

	/**
	 * 通过id查询
	 * @param mbId id
	 * @return R
	 */
	@Operation(summary = "通过id查询", description = "通过id查询")
	@GetMapping("/{mbId}")
	@PreAuthorize("@pms.hasPermission('modelBaseInfo_m3modelbaseinfo_get')")
	public R getById(@PathVariable("mbId") String mbId) {
		return R.ok(m3ModelBaseInfoService.getById(mbId));
	}

	/**
	 * 新增
	 * @param m3ModelBaseInfo
	 * @return R
	 */
	@Operation(summary = "新增", description = "新增")
	@SysLog("新增")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('modelBaseInfo_m3modelbaseinfo_add')")
	public OpenResponse save(@RequestBody M3ModelBaseInfo m3ModelBaseInfo) {
		OpenResponse<M3ModelBaseInfo> response = new OpenResponse<>(OpenResponseCode.SUCCESS);
		try {
			m3ModelBaseInfoService.addNode(m3ModelBaseInfo);
			response.setData(m3ModelBaseInfo);
		}
		catch (Exception e) {
			response.setCode(OpenResponseCode.ERROR);
		}
		return response;
	}

	/**
	 * 修改
	 * @param m3ModelBaseInfo
	 * @return R
	 */
	@Operation(summary = "修改", description = "修改")
	@SysLog("修改")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('modelBaseInfo_m3modelbaseinfo_edit')")
	public R updateById(@RequestBody M3ModelBaseInfo m3ModelBaseInfo) {
		return R.ok(m3ModelBaseInfoService.updateById(m3ModelBaseInfo));
	}

	/**
	 * 通过id删除
	 * @param mbId id
	 * @return R
	 */
	@Operation(summary = "通过id删除", description = "通过id删除")
	@SysLog("通过id删除")
	@DeleteMapping("/{mbId}")
	@PreAuthorize("@pms.hasPermission('modelBaseInfo_m3modelbaseinfo_del')")
	public OpenResponse deleteModelBaseInfo(@PathVariable String mbId) {
		return m3ModelBaseInfoService.deleteModelBaseInfo(mbId);
	}

	@Operation(summary = "分页查询根节点", description = "分页查询根节点")
	@GetMapping("/getRootNodePage")
	public R getRootNodePage(@RequestParam(defaultValue = "1", required = false) long current,
			@RequestParam(defaultValue = "20", required = false) long size) {
		Page<M3ModelBaseInfo> page = new Page<>(current, size);
		return R.ok(m3ModelBaseInfoService.getRootNodePage(page));
	}

	// 查询下一级子节点
	@Operation(summary = "查询下一级子节点", description = "查询下一级子节点")
	@GetMapping("/getSonNode/{mbCode}")
	public R getSonNode(@PathVariable(value = "mbCode") String mbCode) {
		return R.ok(m3ModelBaseInfoService.getSonNode(mbCode));
	}

}
