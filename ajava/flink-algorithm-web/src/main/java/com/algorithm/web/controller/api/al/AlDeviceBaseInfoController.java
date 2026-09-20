package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlDeviceBaseInfo;
import com.algorithm.web.service.al.AlDeviceBaseInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设备基本信息Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/devicebaseinfo")
@Tag(name = "设备基本信息管理", description = "设备基本信息的增删改查接口")
public class AlDeviceBaseInfoController {

	private final AlDeviceBaseInfoService alDeviceBaseInfoService;

	private static final Logger log = LoggerFactory.getLogger(AlDeviceBaseInfoController.class);

	/**
	 * 分页查询设备信息
	 * @param page 分页参数
	 * @param alDeviceBaseInfo 查询条件
	 * @return 分页结果
	 */
	@GetMapping("/page")
	@Operation(summary = "分页查询设备信息")
	public RestResult getDeviceBaseInfoPage(Page<AlDeviceBaseInfo> page, AlDeviceBaseInfo alDeviceBaseInfo) {
		LambdaQueryWrapper<AlDeviceBaseInfo> queryWrapper = Wrappers.lambdaQuery();

		// 根据场景ID查询
		if (alDeviceBaseInfo.getSceneId() != null) {
			queryWrapper.eq(AlDeviceBaseInfo::getSceneId, alDeviceBaseInfo.getSceneId());
		}

		// 根据设备编号模糊查询
		if (alDeviceBaseInfo.getDeviceCode() != null && !alDeviceBaseInfo.getDeviceCode().isEmpty()) {
			queryWrapper.like(AlDeviceBaseInfo::getDeviceCode, alDeviceBaseInfo.getDeviceCode());
		}

		// 根据设备名称模糊查询
		if (alDeviceBaseInfo.getDeviceName() != null && !alDeviceBaseInfo.getDeviceName().isEmpty()) {
			queryWrapper.like(AlDeviceBaseInfo::getDeviceName, alDeviceBaseInfo.getDeviceName());
		}

		// 根据制造厂商模糊查询
		if (alDeviceBaseInfo.getManufacturer() != null && !alDeviceBaseInfo.getManufacturer().isEmpty()) {
			queryWrapper.like(AlDeviceBaseInfo::getManufacturer, alDeviceBaseInfo.getManufacturer());
		}

		// 按创建时间降序排列
		queryWrapper.orderByDesc(AlDeviceBaseInfo::getCreateTime);

		return RestResult.success(alDeviceBaseInfoService.page(page, queryWrapper));
	}

	/**
	 * 根据ID查询设备信息
	 * @param id 设备ID
	 * @return 设备信息
	 */
	@GetMapping("/{id:\\d+}")
	@Operation(summary = "根据ID查询设备信息")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(alDeviceBaseInfoService.getById(id));
	}

	/**
	 * 根据场景ID查询设备列表
	 * @param sceneId 场景ID
	 * @return 设备列表
	 */
	@GetMapping("/scene/{sceneId}")
	@Operation(summary = "根据场景ID查询设备列表")
	public RestResult getDevicesBySceneId(@PathVariable Long sceneId) {
		return RestResult.success(alDeviceBaseInfoService.getDevicesBySceneId(sceneId));
	}

	/**
	 * 根据设备编号查询设备列表
	 * @param deviceCode 设备编号
	 * @return 设备列表
	 */
	@GetMapping("/deviceCode/{deviceCode}")
	@Operation(summary = "根据设备编号查询设备列表")
	public RestResult getDevicesByDeviceCode(@PathVariable String deviceCode) {
		return RestResult.success(alDeviceBaseInfoService.getDevicesByDeviceCode(deviceCode));
	}

	/**
	 * 根据设备编号和场景ID查询设备列表
	 * @param deviceCode 设备编号
	 * @param sceneId 场景ID
	 * @return 设备列表
	 */
	@GetMapping("/deviceCode/{deviceCode}/scene/{sceneId}")
	@Operation(summary = "根据设备编号和场景ID查询设备列表")
	public RestResult getDevicesByDeviceCodeAndSceneId(@PathVariable String deviceCode, @PathVariable Long sceneId) {
		System.out.println("进入");
		return RestResult.success(alDeviceBaseInfoService.getDevicesByDeviceCodeAndSceneId(deviceCode, sceneId));
	}

	/**
	 * 根据使用单位和设备编号查询设备列表
	 * @param usageUnit 使用单位
	 * @param deviceCode 设备编号
	 * @return 设备列表
	 */
	@GetMapping("/usageUnit/{usageUnit}/deviceCode/{deviceCode}")
	@Operation(summary = "根据使用单位和设备编号查询设备列表")
	public RestResult getDevicesByUsageUnitAndDeviceCode(@PathVariable String usageUnit,
			@PathVariable String deviceCode) {
		return RestResult.success(alDeviceBaseInfoService.getDevicesByUsageUnitAndDeviceCode(usageUnit, deviceCode));
	}

	/**
	 * 新增设备信息
	 * @param alDeviceBaseInfo 设备信息
	 * @return 是否成功
	 */
	@PostMapping("/save")
	@Operation(summary = "新增设备信息")
	public RestResult save(@RequestBody AlDeviceBaseInfo alDeviceBaseInfo) {
		boolean result = alDeviceBaseInfoService.save(alDeviceBaseInfo);
		if (result) {
			return RestResult.success(alDeviceBaseInfo.getId());
		}
		return RestResult.error("新增设备信息失败");
	}

	/**
	 * 批量新增设备信息
	 * @param deviceList 设备列表
	 * @return 是否成功
	 */
	@PostMapping("/batch")
	@Operation(summary = "批量新增设备信息")
	public RestResult saveBatch(@RequestBody List<AlDeviceBaseInfo> deviceList) {
		boolean result = alDeviceBaseInfoService.saveBatchDevices(deviceList);
		if (result) {
			return RestResult.success("批量新增成功");
		}
		return RestResult.error("批量新增设备信息失败");
	}

	/**
	 * 更新设备信息
	 * @param alDeviceBaseInfo 设备信息
	 * @return 是否成功
	 */
	@PutMapping
	@Operation(summary = "更新设备信息")
	public RestResult updateById(@RequestBody AlDeviceBaseInfo alDeviceBaseInfo) {
		boolean result = alDeviceBaseInfoService.updateById(alDeviceBaseInfo);
		if (result) {
			return RestResult.success("更新成功");
		}
		return RestResult.error("更新设备信息失败");
	}

	/**
	 * 根据ID删除设备信息
	 * @param id 设备ID
	 * @return 是否成功
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "根据ID删除设备信息")
	public RestResult removeById(@PathVariable Long id) {
		boolean result = alDeviceBaseInfoService.removeById(id);
		if (result) {
			return RestResult.success("删除成功");
		}
		return RestResult.error("删除设备信息失败");
	}

	/**
	 * 批量删除设备信息
	 * @param ids 设备ID列表
	 * @return 是否成功
	 */
	@DeleteMapping("/batch")
	@Operation(summary = "批量删除设备信息")
	public RestResult removeBatchByIds(@RequestBody List<Long> ids) {
		boolean result = alDeviceBaseInfoService.removeByIds(ids);
		if (result) {
			return RestResult.success("批量删除成功");
		}
		return RestResult.error("批量删除设备信息失败");
	}

	/**
	 * 查询所有设备信息
	 * @return 设备列表
	 */
	@GetMapping("/list")
	@Operation(summary = "查询所有设备信息")
	public RestResult getDeviceList() {
		return RestResult.success(alDeviceBaseInfoService.list());
	}

}
