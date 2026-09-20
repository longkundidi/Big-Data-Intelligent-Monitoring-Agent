package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.mapper.al.AlStateEvaluationRegister1to2Mapper;
import com.algorithm.web.model.entity.al.AlStateEvaluationRegister1to2;
import com.algorithm.web.service.al.AlStateEvaluationRegister1to2Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stateOne")

public class AlStateEvaluationRegister1to2Controller {

	private final AlStateEvaluationRegister1to2Service alStateEvaluationRegister1to2Service;

	@GetMapping("/page")
	public RestResult getDomainOnePage(Page page, AlStateEvaluationRegister1to2 alStateEvaluationRegister1to2) {
		return RestResult.success(alStateEvaluationRegister1to2Service.selectClassPage(page,
				alStateEvaluationRegister1to2.getModelName()));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlStateEvaluationRegister1to2> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlStateEvaluationRegister1to2.class);
		lambdaQueryWrapper.eq(AlStateEvaluationRegister1to2::getModelName, name);
		return RestResult.success(alStateEvaluationRegister1to2Service.getOne(lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlStateEvaluationRegister1to2 alStateEvaluationRegister1to2) {
		return RestResult.success(alStateEvaluationRegister1to2Service.save(alStateEvaluationRegister1to2));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlStateEvaluationRegister1to2 alStateEvaluationRegister1to2) {
		return RestResult.success(alStateEvaluationRegister1to2Service.updateById(alStateEvaluationRegister1to2));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alStateEvaluationRegister1to2Service.removeById(id));
	}

}
