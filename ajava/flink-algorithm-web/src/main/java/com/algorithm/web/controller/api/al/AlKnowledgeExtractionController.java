package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.entity.al.AlKnowledgeExtraction;
import com.algorithm.web.service.al.AlKnowledgeExtractionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alKnowledgeExtraction")
public class AlKnowledgeExtractionController extends BaseController {

	@Autowired
	private AlKnowledgeExtractionService alKnowledgeExtractionService;

	@GetMapping("/page")
	public RestResult getAlAlgorithmPage(Page page, AlKnowledgeExtraction alKnowledgeExtraction) {
		return RestResult.success(alKnowledgeExtractionService.getPage(page, alKnowledgeExtraction));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Integer id) {
		return RestResult.success(alKnowledgeExtractionService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlKnowledgeExtraction alKnowledgeExtraction) {
		String programUrl = alKnowledgeExtraction.getProgramUrl();
		alKnowledgeExtraction.setIsService(0);
		alKnowledgeExtraction
			.setAlShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alKnowledgeExtractionService.save(alKnowledgeExtraction);
		return RestResult.success(alKnowledgeExtractionService.updateCodeByName(alKnowledgeExtraction.getAlName()));
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alKnowledgeExtractionService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlKnowledgeExtraction alKnowledgeExtraction) {
		return RestResult.success(alKnowledgeExtractionService.updateById(alKnowledgeExtraction));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alKnowledgeExtractionService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alKnowledgeExtractionService.list());
	}

	@GetMapping("/getTypeNum/{alType}")
	public RestResult getall(@PathVariable("alType") String alType) {
		return RestResult.success(alKnowledgeExtractionService.getTypeNum(alType));
	}

}
