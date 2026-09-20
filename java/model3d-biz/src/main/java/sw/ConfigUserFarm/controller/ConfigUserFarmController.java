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

package sw.ConfigUserFarm.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import sw.AlResumeData.service.AlResumeDataService;
import sw.ConfigUserFarm.entity.ConfigUserFarm;
import sw.ConfigUserFarm.entity.ConfigUserMetaModel;
import sw.ConfigUserFarm.entity.dto.UserFarmDTO;
import sw.ConfigUserFarm.service.ConfigUserFarmService;
import sw.ConfigUserFarm.service.ConfigUserMetaModelService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 风场-用户权限对应表
 *
 * @author pig code generator
 * @date 2025-03-10 14:51:27
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configuserfarm")
@Tag(name = "风场-用户权限对应表管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigUserFarmController {

	private final ConfigUserFarmService configUserFarmService;

	private final AlResumeDataService alResumeDataService;

	private final ConfigUserMetaModelService configUserMetaModelService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param configUserFarm 风场-用户权限对应表
	 * @return
	 */
	@Operation(summary = "分页查询", description = "分页查询")
	@GetMapping("/page")
	@PreAuthorize("@pms.hasPermission('demo_configuserfarm_get')")
	public R getConfigUserFarmPage(Page page, ConfigUserFarm configUserFarm) {
		return R.ok(configUserFarmService.page(page, Wrappers.query(configUserFarm)));
	}

	/**
	 * 通过id查询风场-用户权限对应表
	 * @param id id
	 * @return R
	 */
	@Operation(summary = "通过id查询", description = "通过id查询")
	@GetMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('demo_configuserfarm_get')")
	public R getById(@PathVariable("id") Long id) {
		return R.ok(configUserFarmService.getById(id));
	}

	/**
	 * 新增风场-用户权限对应表
	 * @param configUserFarm 风场-用户权限对应表
	 * @return R
	 */
	@Operation(summary = "新增风场-用户权限对应表", description = "新增风场-用户权限对应表")
	@SysLog("新增风场-用户权限对应表")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('demo_configuserfarm_add')")
	public R save(@RequestBody ConfigUserFarm configUserFarm) {
		return R.ok(configUserFarmService.save(configUserFarm));
	}

	/**
	 * 修改风场-用户权限对应表
	 * @param configUserFarm 风场-用户权限对应表
	 * @return R
	 */
	@Operation(summary = "修改风场-用户权限对应表", description = "修改风场-用户权限对应表")
	@SysLog("修改风场-用户权限对应表")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('demo_configuserfarm_edit')")
	public R updateById(@RequestBody ConfigUserFarm configUserFarm) {
		return R.ok(configUserFarmService.updateById(configUserFarm));
	}

	/**
	 * 通过id删除风场-用户权限对应表
	 * @param id id
	 * @return R
	 */
	@Operation(summary = "通过id删除风场-用户权限对应表", description = "通过id删除风场-用户权限对应表")
	@SysLog("通过id删除风场-用户权限对应表")
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('demo_configuserfarm_del')")
	public R removeById(@PathVariable Long id) {
		return R.ok(configUserFarmService.removeById(id));
	}

	/**
	 * 批量新增风场-用户权限对应表
	 * @param userFarmDTO 风场名数组
	 * @return R
	 */
	@Operation(summary = "批量新增风场-用户权限对应表", description = "批量新增风场-用户权限对应表")
	@SysLog("批量新增风场-用户权限对应表")
	@PostMapping("/batchAdd")
	public R batchSave(@RequestBody UserFarmDTO userFarmDTO) {
		if (userFarmDTO.getUserId() == null) {
			return R.failed("参数不能为空");
		}

		// 处理 metaModel
		if (userFarmDTO.getMetaModelId() != null && userFarmDTO.getMetaModelName() != null) {

			ConfigUserMetaModel record = new ConfigUserMetaModel();
			record.setUserId(userFarmDTO.getUserId());
			record.setMetaModelId(userFarmDTO.getMetaModelId());
			record.setMetaModelName(userFarmDTO.getMetaModelName());

			configUserMetaModelService.save(record);
		}

		// 处理风场
		if (userFarmDTO.getFarmNames() != null && !userFarmDTO.getFarmNames().isEmpty()) {

			List<ConfigUserFarm> configUserFarms = userFarmDTO.getFarmNames().stream().map(farmName -> {
				ConfigUserFarm configUserFarm = new ConfigUserFarm();
				configUserFarm.setUserId(userFarmDTO.getUserId());
				configUserFarm.setFarmName(farmName);
				return configUserFarm;
			}).collect(Collectors.toList());

			configUserFarmService.saveBatch(configUserFarms);
		}

		return R.ok("新增成功");
	}

	/**
	 * 批量修改风场-用户权限对应表
	 * @param userFarmDTO 风场名数组
	 * @return R
	 */
	@Operation(summary = "批量修改风场-用户权限对应表", description = "批量修改风场-用户权限对应表")
	@SysLog("批量修改风场-用户权限对应表")
	@PutMapping("/batchUpdate")
	public R batchUpdate(@RequestBody UserFarmDTO userFarmDTO) {
		if (userFarmDTO.getUserId() == null || userFarmDTO.getFarmNames() == null) {
			return R.failed("参数不能为空");
		}

		// 先删除用户已有的风场权限
		configUserFarmService.remove(new QueryWrapper<ConfigUserFarm>().eq("user_id", userFarmDTO.getUserId()));

		// 先删除用户已有的 metaModel 权限
		configUserMetaModelService
			.remove(new QueryWrapper<ConfigUserMetaModel>().eq("user_id", userFarmDTO.getUserId()));

		// 重新插入新的 metaModel 权限
		if (userFarmDTO.getMetaModelId() != null && userFarmDTO.getMetaModelName() != null) {
			ConfigUserMetaModel configUserMetaModel = new ConfigUserMetaModel();
			configUserMetaModel.setUserId(userFarmDTO.getUserId());
			configUserMetaModel.setMetaModelId(userFarmDTO.getMetaModelId());
			configUserMetaModel.setMetaModelName(userFarmDTO.getMetaModelName());
			configUserMetaModelService.save(configUserMetaModel);
		}

		if (userFarmDTO.getFarmNames().isEmpty()) {
			return R.ok("修改成功");
		}

		// 重新插入新的风场权限
		List<ConfigUserFarm> configUserFarms = userFarmDTO.getFarmNames().stream().map(farmName -> {
			ConfigUserFarm configUserFarm = new ConfigUserFarm();
			configUserFarm.setUserId(userFarmDTO.getUserId());
			configUserFarm.setFarmName(farmName);
			return configUserFarm;
		}).collect(Collectors.toList());

		configUserFarmService.saveBatch(configUserFarms);
		return R.ok("修改成功");
	}

	/**
	 * 根据用户ID查询风场列表
	 * @param userId 用户ID
	 * @return R<List < String>> 风场名称列表
	 */
	@Operation(summary = "根据用户ID查询风场列表", description = "根据用户ID查询该用户拥有权限的风场列表")
	@SysLog("根据用户ID查询风场列表")
	@GetMapping("/getFarmsByUserId")
	public R<List<Map<String, String>>> getFarmsByUserId(@RequestParam Long userId) {
		if (userId == null) {
			return R.failed("用户ID不能为空");
		}

		// 查询用户对应的风场名称列表
		List<Map<String, String>> farmList = configUserFarmService
			.list(new QueryWrapper<ConfigUserFarm>().eq("user_id", userId))
			.stream()
			.map(configUserFarm -> {
				Map<String, String> farmMap = new HashMap<>();
				farmMap.put("farmId", alResumeDataService.getIdByFarmName(configUserFarm.getFarmName()).toString());
				farmMap.put("farmName", configUserFarm.getFarmName());
				return farmMap;
			})
			.collect(Collectors.toList());

		ConfigUserMetaModel configUserMetaModel = configUserMetaModelService
			.getOne(new QueryWrapper<ConfigUserMetaModel>().eq("user_id", userId), false);
		if (configUserMetaModel != null && configUserMetaModel.getMetaModelId() != null
				&& configUserMetaModel.getMetaModelName() != null) {
			Map<String, String> metaModelMap = new HashMap<>();
			metaModelMap.put("metaModelId", configUserMetaModel.getMetaModelId().toString());
			metaModelMap.put("metaModelName", configUserMetaModel.getMetaModelName());
			farmList.add(metaModelMap);
		}

		return R.ok(farmList, "查询成功");
	}

	/**
	 * 根据用户ID删除风场-用户权限对应表记录
	 * @param userId 用户ID
	 * @return R 返回结果
	 */
	@Operation(summary = "根据用户ID删除风场-用户权限对应表记录", description = "根据用户ID删除该用户的所有风场权限记录")
	@SysLog("根据用户ID删除风场-用户权限对应表记录")
	@DeleteMapping("/deleteByUserId/{userId}")
	public R removeByUserId(@PathVariable Long userId) {
		if (userId == null) {
			return R.failed("用户ID不能为空");
		}

		// 删除用户对应的风场权限记录
		boolean removed = configUserFarmService.remove(new QueryWrapper<ConfigUserFarm>().eq("user_id", userId));

		// 删除用户对应的 metaModel 权限记录
		boolean metaModelRemoved = configUserMetaModelService
			.remove(new QueryWrapper<ConfigUserMetaModel>().eq("user_id", userId));

		if (removed || metaModelRemoved) {
			return R.ok("删除成功");
		}
		else {
			return R.failed("删除失败");
		}
	}

	/**
	 * 新建场景后增加管理权限
	 */
	@GetMapping("/updatePermission")
	public R updatePermission(@RequestParam String sceneName, @RequestParam Long userId) {
		if (sceneName == null || sceneName.isEmpty()) {
			return R.failed("sceneName 不能为空");
		}
		if (userId == null) {
			return R.failed("userId 不能为空");
		}

		// 检查是否已经存在，避免重复插入
		boolean exists = configUserFarmService
			.count(new LambdaQueryWrapper<ConfigUserFarm>().eq(ConfigUserFarm::getFarmName, sceneName)
				.eq(ConfigUserFarm::getUserId, userId)) > 0;

		if (exists) {
			return R.ok("权限已存在，无需重复添加");
		}

		// 新增权限
		ConfigUserFarm configUserFarm = new ConfigUserFarm();
		configUserFarm.setFarmName(sceneName);
		configUserFarm.setUserId(userId);
		configUserFarmService.save(configUserFarm);

		return R.ok("增加权限成功");
	}

	@Operation(summary = "根据用户id返回元模型id", description = "根据用户id返回元模型id")
	@SysLog("根据用户id返回元模型id")
	@GetMapping("/getMetaModelIdByUserId")
	public R getMetaModelIdByUserId(@RequestParam Long userId, @RequestParam String userRole) {
		return R.ok(configUserMetaModelService.getMetaModelIdByUserId(userId, userRole), "查询成功");
	}

}
