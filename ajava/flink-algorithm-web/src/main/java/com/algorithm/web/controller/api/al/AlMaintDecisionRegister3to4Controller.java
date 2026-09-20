package com.algorithm.web.controller.api.al;

import com.algorithm.web.model.entity.al.AlMaintDecisionRegister3to4;
import com.algorithm.web.service.al.AlMaintDecisionRegister3to4Service;
import lombok.RequiredArgsConstructor;
import com.algorithm.web.common.RestResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/maintenanceTwo")
public class AlMaintDecisionRegister3to4Controller {

	private final AlMaintDecisionRegister3to4Service alMaintDecisionRegister3To4Service;

	@GetMapping("/page")
	public RestResult getDomainTwoPage(Page page, AlMaintDecisionRegister3to4 alMaintDecisionRegister3To4) {
		LambdaQueryWrapper<AlMaintDecisionRegister3to4> lambdaQueryWrapper = new LambdaQueryWrapper();
		lambdaQueryWrapper.like(AlMaintDecisionRegister3to4::getModelName, alMaintDecisionRegister3To4.getModelName());
		return RestResult.success(alMaintDecisionRegister3To4Service.page(page, lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlMaintDecisionRegister3to4 alMaintDecisionRegister3To4) {
		return RestResult.success(alMaintDecisionRegister3To4Service.save(alMaintDecisionRegister3To4));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlMaintDecisionRegister3to4 alMaintDecisionRegister3To4) {
		return RestResult.success(alMaintDecisionRegister3To4Service.updateById(alMaintDecisionRegister3To4));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alMaintDecisionRegister3To4Service.removeById(id));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlMaintDecisionRegister3to4> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlMaintDecisionRegister3to4.class);
		lambdaQueryWrapper.eq(AlMaintDecisionRegister3to4::getModelName, name);
		return RestResult.success(alMaintDecisionRegister3To4Service.getOne(lambdaQueryWrapper));
	}

}
