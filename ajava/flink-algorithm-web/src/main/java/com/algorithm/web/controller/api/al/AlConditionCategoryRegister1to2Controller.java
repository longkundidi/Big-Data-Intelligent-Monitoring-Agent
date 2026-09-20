package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlConditionCategoryRegister1to2;
import com.algorithm.web.service.al.AlConditionCategoryRegister1to2Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conditionsOne")
public class AlConditionCategoryRegister1to2Controller {

	private final AlConditionCategoryRegister1to2Service alConditionCategoryRegister1to2Service;

	@GetMapping("/page")
	public RestResult getDomainOnePage(Page page, AlConditionCategoryRegister1to2 alConditionCategoryRegister1to2) {
		return RestResult.success(alConditionCategoryRegister1to2Service.selectClassPage(page,
				alConditionCategoryRegister1to2.getModelName()));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlConditionCategoryRegister1to2> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlConditionCategoryRegister1to2.class);
		lambdaQueryWrapper.eq(AlConditionCategoryRegister1to2::getModelName, name);
		return RestResult.success(alConditionCategoryRegister1to2Service.getOne(lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlConditionCategoryRegister1to2 alConditionCategoryRegister1to2) {
		return RestResult.success(alConditionCategoryRegister1to2Service.save(alConditionCategoryRegister1to2));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlConditionCategoryRegister1to2 alConditionCategoryRegister1to2) {
		return RestResult.success(alConditionCategoryRegister1to2Service.updateById(alConditionCategoryRegister1to2));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alConditionCategoryRegister1to2Service.removeById(id));
	}

}
