package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister3to4;
import com.algorithm.web.service.al.AlFaultDiagnosisRegister3to4Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/domaintwo")
public class AlFaultDiagnosisRegister3to4Controller {

	private final AlFaultDiagnosisRegister3to4Service alFaultDiagnosisRegister3To4Service;

	@GetMapping("/page")
	public RestResult getDomainTwoPage(Page page, AlFaultDiagnosisRegister3to4 alFaultDiagnosisRegister3To4) {
		LambdaQueryWrapper<AlFaultDiagnosisRegister3to4> lambdaQueryWrapper = new LambdaQueryWrapper();
		lambdaQueryWrapper.like(AlFaultDiagnosisRegister3to4::getModelName,
				alFaultDiagnosisRegister3To4.getModelName());
		return RestResult.success(alFaultDiagnosisRegister3To4Service.page(page, lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlFaultDiagnosisRegister3to4 alFaultDiagnosisRegister3To4) {
		return RestResult.success(alFaultDiagnosisRegister3To4Service.save(alFaultDiagnosisRegister3To4));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlFaultDiagnosisRegister3to4 alFaultDiagnosisRegister3To4) {
		return RestResult.success(alFaultDiagnosisRegister3To4Service.updateById(alFaultDiagnosisRegister3To4));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alFaultDiagnosisRegister3To4Service.removeById(id));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlFaultDiagnosisRegister3to4> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlFaultDiagnosisRegister3to4.class);
		lambdaQueryWrapper.eq(AlFaultDiagnosisRegister3to4::getModelName, name);
		return RestResult.success(alFaultDiagnosisRegister3To4Service.getOne(lambdaQueryWrapper));
	}

}
