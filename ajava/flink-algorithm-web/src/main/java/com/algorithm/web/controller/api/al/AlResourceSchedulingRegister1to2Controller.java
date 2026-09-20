package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlResourceSchedulingRegister1to2;
import com.algorithm.web.service.al.AlResourceSchedulingRegister1to2Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedulingOne")

public class AlResourceSchedulingRegister1to2Controller {

	private final AlResourceSchedulingRegister1to2Service alResourceSchedulingRegister1To2Service;

	@GetMapping("/page")
	public RestResult getDomainOnePage(Page page, AlResourceSchedulingRegister1to2 alResourceSchedulingRegister1To2) {
		return RestResult.success(alResourceSchedulingRegister1To2Service.selectClassPage(page,
				alResourceSchedulingRegister1To2.getModelName()));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlResourceSchedulingRegister1to2> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlResourceSchedulingRegister1to2.class);
		lambdaQueryWrapper.eq(AlResourceSchedulingRegister1to2::getModelName, name);
		return RestResult.success(alResourceSchedulingRegister1To2Service.getOne(lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlResourceSchedulingRegister1to2 alResourceSchedulingRegister1To2) {
		return RestResult.success(alResourceSchedulingRegister1To2Service.save(alResourceSchedulingRegister1To2));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlResourceSchedulingRegister1to2 alResourceSchedulingRegister1To2) {
		return RestResult.success(alResourceSchedulingRegister1To2Service.updateById(alResourceSchedulingRegister1To2));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alResourceSchedulingRegister1To2Service.removeById(id));
	}

}
