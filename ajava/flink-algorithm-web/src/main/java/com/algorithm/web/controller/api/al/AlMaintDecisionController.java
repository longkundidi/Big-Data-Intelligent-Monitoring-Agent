package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.entity.al.AlMaintDecision;
import com.algorithm.web.service.al.AlMaintDecisionRegister1to2Service;
import com.algorithm.web.service.al.AlMaintDecisionRegister3to4Service;
import com.algorithm.web.service.al.AlMaintDecisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/maintenanceDecisions")
public class AlMaintDecisionController extends BaseController {

	private final AlMaintDecisionService alMaintDecisionService;

	private final AlMaintDecisionRegister1to2Service alMaintDecisionRegister1to2Service;

	private final AlMaintDecisionRegister3to4Service alMaintDecisionRegister3to4Service;

	@GetMapping("/page")
	public RestResult getDomainBasePage(Page page, AlMaintDecision alMaintDecision) {
		return RestResult.success(alMaintDecisionService.getPage(page, alMaintDecision));
	}

	/* 模糊查询对象 */
	@GetMapping("/page1")
	public RestResult getDomainBasePage1(Page page, AlMaintDecision alMaintDecision) {
		return RestResult.success(alMaintDecisionService.getPageObj(page, alMaintDecision));
	}

	/* 模糊查询模型 */
	@GetMapping("/page2")
	public RestResult getDomainBasePage2(Page page, AlMaintDecision alMaintDecision) {
		return RestResult.success(alMaintDecisionService.getPageName(page, alMaintDecision));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(alMaintDecisionService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlMaintDecision alMaintDecision) {
		String programUrl = alMaintDecision.getProgramUrl();
		alMaintDecision.setIsService(0);
		alMaintDecision
			.setModelShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alMaintDecisionService.save(alMaintDecision);
		return RestResult.success(alMaintDecisionService.updateCodeByName(alMaintDecision.getModelName()));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlMaintDecision alMaintDecision) {
		// alMaintDecision.setEditor(this.getUserName());
		return RestResult.success(alMaintDecisionService.updateById(alMaintDecision));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById1(@PathVariable Long id) {
		String modelName = alMaintDecisionService.getnamebyid(id);
		alMaintDecisionRegister1to2Service.deletebyname(modelName);
		alMaintDecisionRegister3to4Service.deletebyname(modelName);
		return RestResult.success(alMaintDecisionService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alMaintDecisionService.list());
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alMaintDecisionService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

}
