package com.algorithm.web.controller.api.al;

import com.algorithm.web.model.entity.al.AlConditionCategoryRegister3to4;
import com.algorithm.web.service.al.AlConditionCategoryRegister3to4Service;
import lombok.RequiredArgsConstructor;
import com.algorithm.web.common.RestResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conditionsTwo")
public class AlConditionCategoryRegister3to4Controller {

	private final AlConditionCategoryRegister3to4Service alConditionCategoryRegister3To4Service;

	@GetMapping("/page")
	public RestResult getDomainTwoPage(Page page, AlConditionCategoryRegister3to4 alConditionCategoryRegister3To4) {
		LambdaQueryWrapper<AlConditionCategoryRegister3to4> lambdaQueryWrapper = new LambdaQueryWrapper();
		lambdaQueryWrapper.like(AlConditionCategoryRegister3to4::getModelName,
				alConditionCategoryRegister3To4.getModelName());
		return RestResult.success(alConditionCategoryRegister3To4Service.page(page, lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlConditionCategoryRegister3to4 alConditionCategoryRegister3To4) {
		return RestResult.success(alConditionCategoryRegister3To4Service.save(alConditionCategoryRegister3To4));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlConditionCategoryRegister3to4 alConditionCategoryRegister3To4) {
		return RestResult.success(alConditionCategoryRegister3To4Service.updateById(alConditionCategoryRegister3To4));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alConditionCategoryRegister3To4Service.removeById(id));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlConditionCategoryRegister3to4> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlConditionCategoryRegister3to4.class);
		lambdaQueryWrapper.eq(AlConditionCategoryRegister3to4::getModelName, name);
		return RestResult.success(alConditionCategoryRegister3To4Service.getOne(lambdaQueryWrapper));
	}

}
