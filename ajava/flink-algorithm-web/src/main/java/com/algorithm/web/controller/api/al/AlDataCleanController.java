package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.service.al.AlDataCleanService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.algorithm.web.controller.web.BaseController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dataCleaning")
public class AlDataCleanController extends BaseController {

	@Autowired
	private AlDataCleanService alDataCleanService;

	@GetMapping("/page")
	public RestResult getAlAlgorithmPage(Page page, AlDataClean alDataClean) {
		return RestResult.success(alDataCleanService.getPage(page, alDataClean));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Integer id) {
		return RestResult.success(alDataCleanService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlDataClean alDataClean) {
		String programUrl = alDataClean.getProgramUrl();
		alDataClean.setIsService(0);
		alDataClean.setAlShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alDataCleanService.save(alDataClean);
		return RestResult.success(alDataCleanService.updateCodeByName(alDataClean.getAlName()));
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alDataCleanService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlDataClean alDataClean) {
		return RestResult.success(alDataCleanService.updateById(alDataClean));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alDataCleanService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alDataCleanService.list());
	}

	@GetMapping("/getTypeNum/{alType}")
	public RestResult getall(@PathVariable("alType") String alType) {
		return RestResult.success(alDataCleanService.getTypeNum(alType));
	}

}
