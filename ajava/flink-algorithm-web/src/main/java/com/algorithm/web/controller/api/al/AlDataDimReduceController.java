package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.entity.al.AlDataDimReduce;
import com.algorithm.web.service.al.AlDataDimReduceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dimensionalityReduction")
public class AlDataDimReduceController extends BaseController {

	@Autowired
	private AlDataDimReduceService alDataDimReduceService;

	@GetMapping("/page")
	public RestResult getAlAlgorithmPage(Page page, AlDataDimReduce alDataDimReduce) {
		return RestResult.success(alDataDimReduceService.getPage(page, alDataDimReduce));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Integer id) {
		return RestResult.success(alDataDimReduceService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlDataDimReduce alDataDimReduce) {
		String programUrl = alDataDimReduce.getProgramUrl();
		alDataDimReduce.setIsService(0);
		alDataDimReduce.setAlShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alDataDimReduceService.save(alDataDimReduce);
		return RestResult.success(alDataDimReduceService.updateCodeByName(alDataDimReduce.getAlName()));
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alDataDimReduceService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlDataDimReduce alDataDimReduce) {
		return RestResult.success(alDataDimReduceService.updateById(alDataDimReduce));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alDataDimReduceService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alDataDimReduceService.list());
	}

	@GetMapping("/getTypeNum/{alType}")
	public RestResult getall(@PathVariable("alType") String alType) {
		return RestResult.success(alDataDimReduceService.getTypeNum(alType));
	}

}
