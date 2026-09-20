package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.entity.al.AlConditionCategory;
import com.algorithm.web.service.al.AlConditionCategoryRegister1to2Service;
import com.algorithm.web.service.al.AlConditionCategoryRegister3to4Service;
import com.algorithm.web.service.al.AlConditionCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/conditionsClassification")
public class AlConditionCategoryController extends BaseController {

	private final AlConditionCategoryService alConditionCategoryService;

	private final AlConditionCategoryRegister1to2Service alConditionCategoryRegister1to2Service;

	private final AlConditionCategoryRegister3to4Service alConditionCategoryRegister3to4Service;

	@GetMapping("/page")
	public RestResult getDomainBasePage(Page page, AlConditionCategory alConditionCategory) {
		return RestResult.success(alConditionCategoryService.getPage(page, alConditionCategory));
	}

	/* 模糊查询对象 */
	@GetMapping("/page1")
	public RestResult getDomainBasePage1(Page page, AlConditionCategory alConditionCategory) {
		return RestResult.success(alConditionCategoryService.getPageObj(page, alConditionCategory));
	}

	/* 模糊查询模型 */
	@GetMapping("/page2")
	public RestResult getDomainBasePage2(Page page, AlConditionCategory alConditionCategory) {
		return RestResult.success(alConditionCategoryService.getPageName(page, alConditionCategory));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(alConditionCategoryService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlConditionCategory alConditionCategory) {
		String programUrl = alConditionCategory.getProgramUrl();
		alConditionCategory.setIsService(0);
		alConditionCategory
			.setModelShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alConditionCategoryService.save(alConditionCategory);
		return RestResult.success(alConditionCategoryService.updateCodeByName(alConditionCategory.getModelName()));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlConditionCategory alConditionCategory) {
		// alConditionCategory.setEditor(this.getUserName());
		return RestResult.success(alConditionCategoryService.updateById(alConditionCategory));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById1(@PathVariable Long id) {
		String modelName = alConditionCategoryService.getnamebyid(id);
		alConditionCategoryRegister1to2Service.deletebyname(modelName);
		alConditionCategoryRegister3to4Service.deletebyname(modelName);
		return RestResult.success(alConditionCategoryService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alConditionCategoryService.list());
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alConditionCategoryService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

}
