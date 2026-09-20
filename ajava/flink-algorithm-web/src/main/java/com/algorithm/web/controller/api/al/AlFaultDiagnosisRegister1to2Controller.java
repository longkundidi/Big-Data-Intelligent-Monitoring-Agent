package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister1to2;
import com.algorithm.web.service.al.AlFaultDiagnosisRegister1to2Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/domainone")

public class AlFaultDiagnosisRegister1to2Controller {

	private final AlFaultDiagnosisRegister1to2Service alFaultDiagnosisRegister1To2Service;

	@GetMapping("/page")
	public RestResult getDomainOnePage(Page page, AlFaultDiagnosisRegister1to2 alFaultDiagnosisRegister1To2) {
		return RestResult.success(
				alFaultDiagnosisRegister1To2Service.selectClassPage(page, alFaultDiagnosisRegister1To2.getModelName()));
	}

	@GetMapping("/{name}")
	public RestResult getByName(@PathVariable("name") String name) {
		LambdaQueryWrapper<AlFaultDiagnosisRegister1to2> lambdaQueryWrapper = Wrappers
			.lambdaQuery(AlFaultDiagnosisRegister1to2.class);
		lambdaQueryWrapper.eq(AlFaultDiagnosisRegister1to2::getModelName, name);
		return RestResult.success(alFaultDiagnosisRegister1To2Service.getOne(lambdaQueryWrapper));
	}

	@PostMapping
	public RestResult save(@RequestBody AlFaultDiagnosisRegister1to2 alFaultDiagnosisRegister1To2) {
		return RestResult.success(alFaultDiagnosisRegister1To2Service.save(alFaultDiagnosisRegister1To2));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlFaultDiagnosisRegister1to2 alFaultDiagnosisRegister1To2) {
		return RestResult.success(alFaultDiagnosisRegister1To2Service.updateById(alFaultDiagnosisRegister1To2));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alFaultDiagnosisRegister1To2Service.removeById(id));
	}

}
