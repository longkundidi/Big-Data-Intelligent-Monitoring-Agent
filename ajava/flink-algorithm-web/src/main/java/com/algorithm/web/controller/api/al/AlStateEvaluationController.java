package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.dto.al.ModelExampleUpdateRequest;
import com.algorithm.web.model.entity.al.AlStateEvaluation;
import com.algorithm.web.service.al.AlStateEvaluationService;
import com.algorithm.web.service.impl.al.AlgorithmManagementService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stateEvaluation")

public class AlStateEvaluationController extends BaseController {

	private final AlStateEvaluationService alStateEvaluationService;

	private final AlgorithmManagementService algorithmManagementService;

	private final ObjectMapper objectMapper;

	@GetMapping("/page")
	public RestResult getDomainBasePage(Page page, AlStateEvaluation alStateEvaluation) {
		return RestResult.success(alStateEvaluationService.getPage(page, alStateEvaluation));
	}

	/* 模糊查询对象 */
	@GetMapping("/page1")
	public RestResult getDomainBasePage1(Page page, AlStateEvaluation alStateEvaluation) {
		return RestResult.success(alStateEvaluationService.getPageObj(page, alStateEvaluation));
	}

	/* 模糊查询模型 */
	@GetMapping("/page2")
	public RestResult getDomainBasePage2(Page page, AlStateEvaluation alStateEvaluation) {
		return RestResult.success(alStateEvaluationService.getPageName(page, alStateEvaluation));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(algorithmManagementService.get(AlgorithmManagementService.TYPE_EVALUATION, id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlStateEvaluation alStateEvaluation) {
		return RestResult.success(algorithmManagementService.create(AlgorithmManagementService.TYPE_EVALUATION,
				objectMapper.valueToTree(alStateEvaluation)));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlStateEvaluation alStateEvaluation) {
		return RestResult.success(algorithmManagementService.update(AlgorithmManagementService.TYPE_EVALUATION,
				alStateEvaluation.getId(), objectMapper.valueToTree(alStateEvaluation)));
	}

	@PutMapping("/{id}/examples")
	public RestResult updateExamples(@PathVariable Long id, @RequestBody ModelExampleUpdateRequest request) {
		boolean updated = algorithmManagementService.updateExamples(AlgorithmManagementService.TYPE_EVALUATION, id,
				request.getInput(), request.getOutput());
		return updated ? RestResult.success() : RestResult.error("模型输入输出保存失败");
	}

	@DeleteMapping("/{id}")
	public RestResult removeById1(@PathVariable Long id) {
		return RestResult.success(algorithmManagementService.delete(AlgorithmManagementService.TYPE_EVALUATION, id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alStateEvaluationService.list());
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		if (!algorithmManagementService.nameExists(AlgorithmManagementService.TYPE_EVALUATION, name, null)) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

}
