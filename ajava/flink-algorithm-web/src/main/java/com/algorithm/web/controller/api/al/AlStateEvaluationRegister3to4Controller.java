package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlStateEvaluationRegister3to4;
import com.algorithm.web.service.al.AlStateEvaluationRegister3to4Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stateTwo")
public class AlStateEvaluationRegister3to4Controller {

	private final AlStateEvaluationRegister3to4Service alStateEvaluationRegister3to4Service;

	@GetMapping("/page")
	public RestResult getDomainTwoPage(Page page, AlStateEvaluationRegister3to4 alStateEvaluationRegister3to4) {
		LambdaQueryWrapper<AlStateEvaluationRegister3to4> lambdaQueryWrapper = new LambdaQueryWrapper();
		lambdaQueryWrapper.like(AlStateEvaluationRegister3to4::getModelName,
				alStateEvaluationRegister3to4.getModelName());
		return RestResult.success(alStateEvaluationRegister3to4Service.page(page, lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlStateEvaluationRegister3to4 alStateEvaluationRegister3to4) {
		return RestResult.success(alStateEvaluationRegister3to4Service.save(alStateEvaluationRegister3to4));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlStateEvaluationRegister3to4 alStateEvaluationRegister3to4) {
		return RestResult.success(alStateEvaluationRegister3to4Service.updateById(alStateEvaluationRegister3to4));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alStateEvaluationRegister3to4Service.removeById(id));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlStateEvaluationRegister3to4> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlStateEvaluationRegister3to4.class);
		lambdaQueryWrapper.eq(AlStateEvaluationRegister3to4::getModelName, name);
		return RestResult.success(alStateEvaluationRegister3to4Service.getOne(lambdaQueryWrapper));
	}

}
