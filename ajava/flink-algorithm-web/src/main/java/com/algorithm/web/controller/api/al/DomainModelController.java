package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.DomainModel;
import com.algorithm.web.service.al.DomainModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 领域模型基本信息表(DomainModel)表控制层
 *
 * @author makejava
 * @since 2024-11-12 17:46:13
 */
@RestController
@RequestMapping("/api/domainModel")
@CrossOrigin

public class DomainModelController {

	/**
	 * 服务对象
	 */
	@Autowired
	private DomainModelService domainModelService;

	@PostMapping
	public RestResult save(@RequestBody DomainModel domainModel) {
		domainModel.setIsService(0);
		return RestResult.success(domainModelService.save(domainModel));
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = domainModelService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	// 根据基础算法返回领域模型列表
	@GetMapping("/getlistByBasicAlgorithm")
	public RestResult getlistByBasicAlgorithm(@RequestParam String basicAlgorithm, @RequestParam String modelType) {
		return RestResult.success(domainModelService.getlistByBasicAlgorithm(basicAlgorithm, modelType));
	}

	// 查询全部领域模型，供算法总览大类统计和树节点使用
	@GetMapping("/list")
	public RestResult list() {
		return RestResult.success(domainModelService.list());
	}

	// 根据id查询领域模型信息
	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Integer id) {
		return RestResult.success(domainModelService.getById(id));
	}

	@PutMapping
	public RestResult updateById(@RequestBody DomainModel domainModel) {
		return RestResult.success(domainModelService.updateById(domainModel));
	}

	// 根据id删除领域模型信息
	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(domainModelService.removeById(id));
	}

	@GetMapping("/getCurrentObject")
	public RestResult getCurrentObject(@RequestParam("alId") Long alId, @RequestParam("type") String type) {
		return RestResult.success(domainModelService.getCurrentObject(alId, type));
	}

}
