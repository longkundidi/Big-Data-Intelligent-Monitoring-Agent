package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlResourceSchedulingRegister3to4;
import com.algorithm.web.service.al.AlResourceSchedulingRegister3to4Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedulingTwo")
public class AlResourceSchedulingRegister3to4Controller {

	private final AlResourceSchedulingRegister3to4Service alResourceSchedulingRegister3To4Service;

	@GetMapping("/page")
	public RestResult getDomainTwoPage(Page page, AlResourceSchedulingRegister3to4 alResourceSchedulingRegister3To4) {
		LambdaQueryWrapper<AlResourceSchedulingRegister3to4> lambdaQueryWrapper = new LambdaQueryWrapper();
		lambdaQueryWrapper.like(AlResourceSchedulingRegister3to4::getModelName,
				alResourceSchedulingRegister3To4.getModelName());
		return RestResult.success(alResourceSchedulingRegister3To4Service.page(page, lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlResourceSchedulingRegister3to4 alResourceSchedulingRegister3To4) {
		return RestResult.success(alResourceSchedulingRegister3To4Service.save(alResourceSchedulingRegister3To4));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlResourceSchedulingRegister3to4 alResourceSchedulingRegister3To4) {
		return RestResult.success(alResourceSchedulingRegister3To4Service.updateById(alResourceSchedulingRegister3To4));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alResourceSchedulingRegister3To4Service.removeById(id));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlResourceSchedulingRegister3to4> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlResourceSchedulingRegister3to4.class);
		lambdaQueryWrapper.eq(AlResourceSchedulingRegister3to4::getModelName, name);
		return RestResult.success(alResourceSchedulingRegister3To4Service.getOne(lambdaQueryWrapper));
	}

}
