package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister1to2;
import com.algorithm.web.model.entity.al.AlFeatureExtraction;
import com.algorithm.web.service.al.AlFeatureExtractionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/featureExtraction")
public class AlFeatureExtractionController {

	@Autowired
	private AlFeatureExtractionService alFeatureExtractionService;

	@GetMapping("/page")
	public RestResult getAlAlgorithmPage(Page page, AlFeatureExtraction alFeatureExtraction) {
		return RestResult.success(alFeatureExtractionService.getPage(page, alFeatureExtraction));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Integer id) {
		return RestResult.success(alFeatureExtractionService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlFeatureExtraction alFeatureExtraction) {
		String programUrl = alFeatureExtraction.getProgramUrl();
		alFeatureExtraction.setIsService(0);
		alFeatureExtraction
			.setAlShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alFeatureExtractionService.save(alFeatureExtraction);
		return RestResult.success(alFeatureExtractionService.updateCodeByName(alFeatureExtraction.getAlName()));
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alFeatureExtractionService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlFeatureExtraction alFeatureExtraction) {
		return RestResult.success(alFeatureExtractionService.updateById(alFeatureExtraction));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alFeatureExtractionService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alFeatureExtractionService.list());
	}

	@GetMapping("/getTypeNum/{alType}")
	public RestResult getall(@PathVariable("alType") String alType) {
		return RestResult.success(alFeatureExtractionService.getTypeNum(alType));
	}

	@GetMapping("/getByName")
	public RestResult getByName(@RequestParam String name) {
		LambdaQueryWrapper<AlFeatureExtraction> lambdaQueryWrapper = Wrappers.lambdaQuery(AlFeatureExtraction.class);
		lambdaQueryWrapper.eq(AlFeatureExtraction::getAlName, name);
		return RestResult.success(alFeatureExtractionService.getOne(lambdaQueryWrapper));
	}

}
