package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlMaintDecisionRegister1to2;
import com.algorithm.web.service.al.AlMaintDecisionRegister1to2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/maintenanceOne")
public class AlMaintDecisionRegister1to2Controller {

	private final AlMaintDecisionRegister1to2Service alMaintDecisionRegister1to2Service;

	@GetMapping("/page")
	public RestResult getDomainOnePage(Page page, AlMaintDecisionRegister1to2 alMaintDecisionRegister1to2) {
		return RestResult.success(
				alMaintDecisionRegister1to2Service.selectClassPage(page, alMaintDecisionRegister1to2.getModelName()));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlMaintDecisionRegister1to2> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlMaintDecisionRegister1to2.class);
		lambdaQueryWrapper.eq(AlMaintDecisionRegister1to2::getModelName, name);
		return RestResult.success(alMaintDecisionRegister1to2Service.getOne(lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlMaintDecisionRegister1to2 alMaintDecisionRegister1to2) {
		return RestResult.success(alMaintDecisionRegister1to2Service.save(alMaintDecisionRegister1to2));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlMaintDecisionRegister1to2 alMaintDecisionRegister1to2) {
		return RestResult.success(alMaintDecisionRegister1to2Service.updateById(alMaintDecisionRegister1to2));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alMaintDecisionRegister1to2Service.removeById(id));
	}

}
