package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlFaultPropagationRegister3to4;
import com.algorithm.web.service.al.AlFaultPropagationRegister3to4Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transmitTwo")
public class AlFaultPropagationRegister3to4Controller {

	private final AlFaultPropagationRegister3to4Service alFaultPropagationRegister3To4Service;

	@GetMapping("/page")
	public RestResult getDomainTwoPage(Page page, AlFaultPropagationRegister3to4 alFaultPropagationRegister3To4) {
		LambdaQueryWrapper<AlFaultPropagationRegister3to4> lambdaQueryWrapper = new LambdaQueryWrapper();
		lambdaQueryWrapper.like(AlFaultPropagationRegister3to4::getModelName,
				alFaultPropagationRegister3To4.getModelName());
		return RestResult.success(alFaultPropagationRegister3To4Service.page(page, lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlFaultPropagationRegister3to4 alFaultPropagationRegister3To4) {
		return RestResult.success(alFaultPropagationRegister3To4Service.save(alFaultPropagationRegister3To4));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlFaultPropagationRegister3to4 alFaultPropagationRegister3To4) {
		return RestResult.success(alFaultPropagationRegister3To4Service.updateById(alFaultPropagationRegister3To4));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alFaultPropagationRegister3To4Service.removeById(id));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlFaultPropagationRegister3to4> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlFaultPropagationRegister3to4.class);
		lambdaQueryWrapper.eq(AlFaultPropagationRegister3to4::getModelName, name);
		return RestResult.success(alFaultPropagationRegister3To4Service.getOne(lambdaQueryWrapper));
	}

}
