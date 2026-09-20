package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlFaultPropagationRegister1to2;
import com.algorithm.web.service.al.AlFaultPropagationRegister1to2Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transmitOne")

public class AlFaultPropagationRegister1to2Controller {

	private final AlFaultPropagationRegister1to2Service alFaultPropagationRegister1To2Service;

	@GetMapping("/page")
	public RestResult getDomainOnePage(Page page, AlFaultPropagationRegister1to2 alFaultPropagationRegister1To2) {
		return RestResult.success(alFaultPropagationRegister1To2Service.selectClassPage(page,
				alFaultPropagationRegister1To2.getModelName()));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlFaultPropagationRegister1to2> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlFaultPropagationRegister1to2.class);
		lambdaQueryWrapper.eq(AlFaultPropagationRegister1to2::getModelName, name);
		return RestResult.success(alFaultPropagationRegister1To2Service.getOne(lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlFaultPropagationRegister1to2 alFaultPropagationRegister1To2) {
		return RestResult.success(alFaultPropagationRegister1To2Service.save(alFaultPropagationRegister1To2));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlFaultPropagationRegister1to2 alFaultPropagationRegister1To2) {
		return RestResult.success(alFaultPropagationRegister1To2Service.updateById(alFaultPropagationRegister1To2));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alFaultPropagationRegister1To2Service.removeById(id));
	}

}
