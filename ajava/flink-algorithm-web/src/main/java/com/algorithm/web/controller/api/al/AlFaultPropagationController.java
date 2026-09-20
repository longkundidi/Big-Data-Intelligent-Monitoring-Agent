package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlFaultPropagation;
import com.algorithm.web.service.al.AlFaultPropagationRegister1to2Service;
import com.algorithm.web.service.al.AlFaultPropagationRegister3to4Service;
import com.algorithm.web.service.al.AlFaultPropagationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faultTransmit")

public class AlFaultPropagationController extends BaseController {

	private final AlFaultPropagationService alFaultPropagationService;

	private final AlFaultPropagationRegister1to2Service alFaultPropagationRegister1To2Service;

	private final AlFaultPropagationRegister3to4Service alFaultPropagationRegister3To4Service;

	@GetMapping("/page")
	public RestResult getAlFaultPropagationBasePage(Page page, AlFaultPropagation alFaultPropagation) {
		return RestResult.success(alFaultPropagationService.getPage(page, alFaultPropagation));
	}

	/* 模糊查询对象 */
	@GetMapping("/page1")
	public RestResult getAlFaultPropagationBasePage1(Page page, AlFaultPropagation alFaultPropagation) {
		return RestResult.success(alFaultPropagationService.getPageObj(page, alFaultPropagation));
	}

	/* 模糊查询模型 */
	@GetMapping("/page2")
	public RestResult getAlFaultPropagationBasePage2(Page page, AlFaultPropagation alFaultPropagation) {
		return RestResult.success(alFaultPropagationService.getPageName(page, alFaultPropagation));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(alFaultPropagationService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlFaultPropagation alFaultPropagation) {
		String programUrl = alFaultPropagation.getProgramUrl();
		alFaultPropagation.setIsService(0);
		alFaultPropagation
			.setModelShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alFaultPropagationService.save(alFaultPropagation);
		return RestResult.success(alFaultPropagationService.updateCodeByName(alFaultPropagation.getModelName()));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlFaultPropagation alFaultPropagation) {
		return RestResult.success(alFaultPropagationService.updateById(alFaultPropagation));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById1(@PathVariable Long id) {
		String modelName = alFaultPropagationService.getnamebyid(id);
		alFaultPropagationRegister1To2Service.deletebyname(modelName);
		alFaultPropagationRegister3To4Service.deletebyname(modelName);
		return RestResult.success(alFaultPropagationService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alFaultPropagationService.list());
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alFaultPropagationService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	// 执行风机故障诊断
	@PostMapping("/operate/{useCase}")
	public RestResult operate(@PathVariable String useCase, @RequestBody DiagnoseInfoDto diagnoseInfoDto) {
		return RestResult.success(alFaultPropagationService.operate(useCase, diagnoseInfoDto));
	}

}
