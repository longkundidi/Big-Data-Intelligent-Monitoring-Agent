package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.entity.al.AlDataMining;
import com.algorithm.web.service.al.AlDataMiningService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dataMining")
public class AlDataMiningController extends BaseController {

	@Autowired
	private AlDataMiningService alDataMiningService;

	@GetMapping("/page")
	public RestResult getAlAlgorithmPage(Page page, AlDataMining alDataMining) {
		return RestResult.success(alDataMiningService.getPage(page, alDataMining));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Integer id) {
		return RestResult.success(alDataMiningService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlDataMining alDataMining) {
		String programUrl = alDataMining.getProgramUrl();
		alDataMining.setIsService(0);
		alDataMining.setAlShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alDataMiningService.save(alDataMining);
		return RestResult.success(alDataMiningService.updateCodeByName(alDataMining.getAlName()));
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alDataMiningService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlDataMining alDataMining) {
		return RestResult.success(alDataMiningService.updateById(alDataMining));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alDataMiningService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alDataMiningService.list());
	}

	@GetMapping("/getTypeNum/{alType}")
	public RestResult getall(@PathVariable("alType") String alType) {
		return RestResult.success(alDataMiningService.getTypeNum(alType));
	}

}
