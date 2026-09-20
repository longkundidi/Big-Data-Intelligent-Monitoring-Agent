package com.algorithm.web.controller.api.al;

import cn.hutool.core.util.ObjectUtil;
import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.mapper.al.AuditsManagementMapper;
import com.algorithm.web.model.entity.al.*;
import com.algorithm.web.model.vo.AlgorithmDashboardCountVo;
import com.algorithm.web.service.al.*;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/AuditsManagement")
public class AuditsManagementController extends BaseController {

	private static final long REVIEW_PENDING = 1L;

	private static final long REVIEW_APPROVED = 0L;

	private static final long TEST_PASSED = 0L;

	private static final long TEST_FAILED = 1L;

	private static final long TEST_PENDING = 2L;

	private static final long DEPLOYED = 0L;

	private static final long NOT_DEPLOYED = 1L;

	@Autowired
	private final AuditsManagementService auditsManagementService;

	@Autowired
	private AlDataCleanService alDataCleanService;

	@Autowired
	private AlKnowledgeExtractionService alKnowledgeExtractionService;

	@Autowired
	private AlFaultDiagnosisService alFaultDiagnosisService;

	@Autowired
	private AlStateEvaluationService alStateEvaluationService;

	@Autowired
	private AuditsManagementMapper auditsManagementMapper;

	@GetMapping("/page")
	public RestResult getStagePage(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
			@RequestParam String stage, @RequestParam(defaultValue = "") String alModelName,
			@RequestParam(defaultValue = "") String alModelType) {
		return RestResult.success(
				auditsManagementService.getStagePage(new Page<>(pageNum, pageSize), stage, alModelName, alModelType));
	}

	@GetMapping("/detail")
	public RestResult getDetail(@RequestParam String sourceType, @RequestParam Long algorithmId) {
		Object detail;
		switch (sourceType) {
			case "state_evaluation":
				detail = alStateEvaluationService.getById(algorithmId);
				break;
			case "fault_diagnosis":
				detail = alFaultDiagnosisService.getById(algorithmId);
				break;
			default:
				return RestResult.error("不支持的算法来源");
		}
		return detail == null ? RestResult.error("未找到对应的算法记录") : RestResult.success(detail);
	}

	@GetMapping("/dashboardCounts")
	public RestResult getDashboardCounts() {
		Map<String, AlgorithmDashboardCountVo> counts = new LinkedHashMap<>();
		counts.put("stateEvaluation", auditsManagementMapper.getStateEvaluationDashboardCounts());
		counts.put("faultDiagnosis", auditsManagementMapper.getFaultDiagnosisDashboardCounts());
		return RestResult.success(counts);
	}

	@PostMapping("/transition")
	public RestResult transition(@RequestBody AuditsManagement request) {
		if (request.getAlgorithmId() == null || request.getSourceType() == null || request.getAction() == null) {
			return RestResult.error("算法来源、算法ID和操作不能为空");
		}

		AuditsManagement current = auditsManagementMapper.getBySourceAndId(request.getSourceType(),
				request.getAlgorithmId());
		if (current == null) {
			return RestResult.error("未找到对应的算法记录");
		}

		String validationError = applyTransition(current, request);
		if (validationError != null) {
			return RestResult.error(validationError);
		}

		return auditsManagementMapper.updateWorkflowStatus(current) == 1 ? RestResult.success(current)
				: RestResult.error("状态更新失败");
	}

	private String applyTransition(AuditsManagement current, AuditsManagement request) {
		switch (request.getAction()) {
			case "APPROVE":
				if (!hasStatus(current.getIsCheck(), REVIEW_PENDING)) {
					return "只有待审核算法可以执行审核通过";
				}
				current.setIsCheck(REVIEW_APPROVED);
				current.setIsPass(TEST_PENDING);
				current.setIsDeployed(NOT_DEPLOYED);
				break;
			case "TEST_PASS":
			case "TEST_FAIL":
				if (!hasStatus(current.getIsCheck(), REVIEW_APPROVED)
						|| !hasStatus(current.getIsDeployed(), NOT_DEPLOYED)
						|| (!hasStatus(current.getIsPass(), TEST_PENDING)
								&& !hasStatus(current.getIsPass(), TEST_FAILED))) {
					return "只有待测试或测试失败的算法可以更新测试结果";
				}
				current.setIsPass("TEST_PASS".equals(request.getAction()) ? TEST_PASSED : TEST_FAILED);
				current.setIsDeployed(NOT_DEPLOYED);
				break;
			case "RESET_TEST":
				if (!hasStatus(current.getIsCheck(), REVIEW_APPROVED) || !hasStatus(current.getIsPass(), TEST_PASSED)) {
					return "只有测试通过的算法可以重新测试";
				}
				current.setIsPass(TEST_PENDING);
				current.setIsDeployed(NOT_DEPLOYED);
				break;
			case "DEPLOY":
				if (!hasStatus(current.getIsCheck(), REVIEW_APPROVED) || !hasStatus(current.getIsPass(), TEST_PASSED)
						|| !hasStatus(current.getIsDeployed(), NOT_DEPLOYED)) {
					return "只有审核通过且测试通过的算法可以部署";
				}
				if (request.getAlModelUrl() == null || request.getAlModelUrl().trim().isEmpty()) {
					return "部署时必须填写算法调用地址";
				}
				current.setIsDeployed(DEPLOYED);
				current.setAlModelUrl(request.getAlModelUrl().trim());
				break;
			case "UNDEPLOY":
				if (!hasStatus(current.getIsCheck(), REVIEW_APPROVED) || !hasStatus(current.getIsPass(), TEST_PASSED)
						|| !hasStatus(current.getIsDeployed(), DEPLOYED)) {
					return "该算法当前未部署";
				}
				current.setIsDeployed(NOT_DEPLOYED);
				current.setAlModelUrl(null);
				break;
			case "REVOKE_REVIEW":
				if (!hasStatus(current.getIsCheck(), REVIEW_APPROVED) || hasStatus(current.getIsDeployed(), DEPLOYED)) {
					return "只有未部署的已审核算法可以撤回审核";
				}
				current.setIsCheck(REVIEW_PENDING);
				current.setIsPass(TEST_PENDING);
				current.setIsDeployed(NOT_DEPLOYED);
				current.setAlModelUrl(null);
				break;
			default:
				return "不支持的状态操作";
		}
		return null;
	}

	private boolean hasStatus(Long actual, long expected) {
		return actual != null && actual == expected;
	}

	// 分页查询已测试未通过算法和模型
	@GetMapping("/getTestNoPassPage")
	public RestResult getTestNoPassPage(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
			@RequestParam(defaultValue = "") String alModelName, @RequestParam(defaultValue = "") String alModelType) {
		return RestResult.success(
				auditsManagementService.getTestNoPassPage(new Page<>(pageNum, pageSize), alModelName, alModelType));
	}

	// 分页查询已测试待部署算法和模型
	@GetMapping("/getTestNoDeployPage")
	public RestResult getTestNoDeployPage(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
			@RequestParam(defaultValue = "") String alModelName, @RequestParam(defaultValue = "") String alModelType) {
		return RestResult.success(
				auditsManagementService.getTestNoDeployPage(new Page<>(pageNum, pageSize), alModelName, alModelType));
	}

	// 分页查询未测试算法和模型
	@GetMapping("/getNoTestPage")
	public RestResult getNoTestPage(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
			@RequestParam(defaultValue = "") String alModelName, @RequestParam(defaultValue = "") String alModelType) {
		return RestResult
			.success(auditsManagementService.getNoTestPage(new Page<>(pageNum, pageSize), alModelName, alModelType));
	}

	// 分页查询已部署算法和模型
	@GetMapping("/getDeployedPage")
	public RestResult getDeployedPage(@RequestParam Integer pageNum, @RequestParam Integer pageSize,
			@RequestParam(defaultValue = "") String alModelName, @RequestParam(defaultValue = "") String alModelType) {
		return RestResult
			.success(auditsManagementService.getDeployedPage(new Page<>(pageNum, pageSize), alModelName, alModelType));
	}

	// 更新算法或模型的测试状态和部署状态&&0417审核（启用）状态
	@PostMapping("/updateTestStatus")
	public RestResult updateTestOrQuote(@RequestBody AuditsManagement auditsManagement) {
		QueryWrapper<AlDataClean> alDataCleanQueryWrapper = new QueryWrapper<>();
		alDataCleanQueryWrapper.eq("al_name", auditsManagement.getAlModelName());
		AlDataClean alDataClean = alDataCleanService.getOne(alDataCleanQueryWrapper);
		if (ObjectUtil.isEmpty(alDataClean)) {

			QueryWrapper<AlKnowledgeExtraction> alKnowledgeExtractionQueryWrapper = new QueryWrapper<>();
			alKnowledgeExtractionQueryWrapper.eq("al_name", auditsManagement.getAlModelName());
			AlKnowledgeExtraction alKnowledgeExtraction = alKnowledgeExtractionService
				.getOne(alKnowledgeExtractionQueryWrapper);

			if (ObjectUtil.isEmpty(alKnowledgeExtraction)) {

				QueryWrapper<AlFaultDiagnosis> alFaultDiagnosisQueryWrapper = new QueryWrapper<>();
				alFaultDiagnosisQueryWrapper.eq("model_name", auditsManagement.getAlModelName());
				AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisService.getOne(alFaultDiagnosisQueryWrapper);
				if (ObjectUtil.isEmpty(alFaultDiagnosis)) {

					QueryWrapper<AlStateEvaluation> alStateEvaluationQueryWrapper = new QueryWrapper<>();
					alStateEvaluationQueryWrapper.eq("model_name", auditsManagement.getAlModelName());
					AlStateEvaluation alStateEvaluation = alStateEvaluationService
						.getOne(alStateEvaluationQueryWrapper);
					alStateEvaluation.setIsPass(auditsManagement.getIsPass());
					return RestResult.success(alStateEvaluationService.updateById(alStateEvaluation));

				}
				else {
					alFaultDiagnosis.setIsPass(auditsManagement.getIsPass());
					return RestResult.success(alFaultDiagnosisService.updateById(alFaultDiagnosis));
				}
			}
			else {
				alKnowledgeExtraction.setIsPass(auditsManagement.getIsPass());
				return RestResult.success(alKnowledgeExtractionService.updateById(alKnowledgeExtraction));
			}
		}
		else {
			alDataClean.setIsPass(auditsManagement.getIsPass());
			return RestResult.success(alDataCleanService.updateById(alDataClean));
		}
	}

	@PostMapping("/updateDeployStatus")
	public RestResult updateDeployStatus(@RequestBody AuditsManagement auditsManagement) {
		QueryWrapper<AlDataClean> alDataCleanQueryWrapper = new QueryWrapper<>();
		alDataCleanQueryWrapper.eq("al_name", auditsManagement.getAlModelName());
		AlDataClean alDataClean = alDataCleanService.getOne(alDataCleanQueryWrapper);
		if (ObjectUtil.isEmpty(alDataClean)) {

			QueryWrapper<AlKnowledgeExtraction> alKnowledgeExtractionQueryWrapper = new QueryWrapper<>();
			alKnowledgeExtractionQueryWrapper.eq("al_name", auditsManagement.getAlModelName());
			AlKnowledgeExtraction alKnowledgeExtraction = alKnowledgeExtractionService
				.getOne(alKnowledgeExtractionQueryWrapper);

			if (ObjectUtil.isEmpty(alKnowledgeExtraction)) {

				QueryWrapper<AlFaultDiagnosis> alFaultDiagnosisQueryWrapper = new QueryWrapper<>();
				alFaultDiagnosisQueryWrapper.eq("model_name", auditsManagement.getAlModelName());
				AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisService.getOne(alFaultDiagnosisQueryWrapper);
				if (ObjectUtil.isEmpty(alFaultDiagnosis)) {

					QueryWrapper<AlStateEvaluation> alStateEvaluationQueryWrapper = new QueryWrapper<>();
					alStateEvaluationQueryWrapper.eq("model_name", auditsManagement.getAlModelName());
					AlStateEvaluation alStateEvaluation = alStateEvaluationService
						.getOne(alStateEvaluationQueryWrapper);
					alStateEvaluation.setIsDeployed(auditsManagement.getIsDeployed());
					alStateEvaluation.setModelUrl(auditsManagement.getAlModelUrl());
					return RestResult.success(alStateEvaluationService.updateById(alStateEvaluation));

				}
				else {
					alFaultDiagnosis.setIsDeployed(auditsManagement.getIsDeployed());
					alFaultDiagnosis.setModelUrl(auditsManagement.getAlModelUrl());
					return RestResult.success(alFaultDiagnosisService.updateById(alFaultDiagnosis));
				}
			}
			else {
				alKnowledgeExtraction.setIsDeployed(auditsManagement.getIsDeployed());
				alKnowledgeExtraction.setAlUrl(auditsManagement.getAlModelUrl());
				return RestResult.success(alKnowledgeExtractionService.updateById(alKnowledgeExtraction));
			}
		}
		else {
			alDataClean.setIsDeployed(auditsManagement.getIsDeployed());
			alDataClean.setAlUrl(auditsManagement.getAlModelUrl());
			return RestResult.success(alDataCleanService.updateById(alDataClean));
		}
	}

	@PostMapping("/updateCheckStatus")
	public RestResult updateCheckStatus(@RequestBody AuditsManagement auditsManagement) {
		QueryWrapper<AlDataClean> alDataCleanQueryWrapper = new QueryWrapper<>();
		alDataCleanQueryWrapper.eq("al_name", auditsManagement.getAlModelName());
		AlDataClean alDataClean = alDataCleanService.getOne(alDataCleanQueryWrapper);
		if (ObjectUtil.isEmpty(alDataClean)) {

			QueryWrapper<AlKnowledgeExtraction> alKnowledgeExtractionQueryWrapper = new QueryWrapper<>();
			alKnowledgeExtractionQueryWrapper.eq("al_name", auditsManagement.getAlModelName());
			AlKnowledgeExtraction alKnowledgeExtraction = alKnowledgeExtractionService
				.getOne(alKnowledgeExtractionQueryWrapper);

			if (ObjectUtil.isEmpty(alKnowledgeExtraction)) {

				QueryWrapper<AlFaultDiagnosis> alFaultDiagnosisQueryWrapper = new QueryWrapper<>();
				alFaultDiagnosisQueryWrapper.eq("model_name", auditsManagement.getAlModelName());
				AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisService.getOne(alFaultDiagnosisQueryWrapper);
				if (ObjectUtil.isEmpty(alFaultDiagnosis)) {

					QueryWrapper<AlStateEvaluation> alStateEvaluationQueryWrapper = new QueryWrapper<>();
					alStateEvaluationQueryWrapper.eq("model_name", auditsManagement.getAlModelName());
					AlStateEvaluation alStateEvaluation = alStateEvaluationService
						.getOne(alStateEvaluationQueryWrapper);
					alStateEvaluation.setIsCheck(auditsManagement.getIsCheck());
					return RestResult.success(alStateEvaluationService.updateById(alStateEvaluation));

				}
				else {
					alFaultDiagnosis.setIsCheck(auditsManagement.getIsCheck());
					return RestResult.success(alFaultDiagnosisService.updateById(alFaultDiagnosis));
				}
			}
			else {
				alKnowledgeExtraction.setIsCheck(auditsManagement.getIsCheck());
				return RestResult.success(alKnowledgeExtractionService.updateById(alKnowledgeExtraction));
			}
		}
		else {
			alDataClean.setIsCheck(auditsManagement.getIsCheck());
			return RestResult.success(alDataCleanService.updateById(alDataClean));
		}
	}

	// 更新算法包
	@PostMapping("/updateProgramUrl")
	public RestResult updateProgramUrl(@RequestBody AuditsManagement auditsManagement) {
		QueryWrapper<AlDataClean> alDataCleanQueryWrapper = new QueryWrapper<>();
		alDataCleanQueryWrapper.eq("al_name", auditsManagement.getAlModelName());
		AlDataClean alDataClean = alDataCleanService.getOne(alDataCleanQueryWrapper);
		if (ObjectUtil.isEmpty(alDataClean)) {

			QueryWrapper<AlKnowledgeExtraction> alKnowledgeExtractionQueryWrapper = new QueryWrapper<>();
			alKnowledgeExtractionQueryWrapper.eq("al_name", auditsManagement.getAlModelName());
			AlKnowledgeExtraction alKnowledgeExtraction = alKnowledgeExtractionService
				.getOne(alKnowledgeExtractionQueryWrapper);

			if (ObjectUtil.isEmpty(alKnowledgeExtraction)) {

				QueryWrapper<AlFaultDiagnosis> alFaultDiagnosisQueryWrapper = new QueryWrapper<>();
				alFaultDiagnosisQueryWrapper.eq("model_name", auditsManagement.getAlModelName());
				AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisService.getOne(alFaultDiagnosisQueryWrapper);
				if (ObjectUtil.isEmpty(alFaultDiagnosis)) {

					QueryWrapper<AlStateEvaluation> alStateEvaluationQueryWrapper = new QueryWrapper<>();
					alStateEvaluationQueryWrapper.eq("model_name", auditsManagement.getAlModelName());
					AlStateEvaluation alStateEvaluation = alStateEvaluationService
						.getOne(alStateEvaluationQueryWrapper);
					alStateEvaluation.setProgramUrl(auditsManagement.getProgramUrl());
					return RestResult.success(alStateEvaluationService.updateById(alStateEvaluation));

				}
				else {
					alFaultDiagnosis.setProgramUrl(auditsManagement.getProgramUrl());
					return RestResult.success(alFaultDiagnosisService.updateById(alFaultDiagnosis));
				}
			}
			else {
				alKnowledgeExtraction.setProgramUrl(auditsManagement.getProgramUrl());
				return RestResult.success(alKnowledgeExtractionService.updateById(alKnowledgeExtraction));
			}
		}
		else {
			alDataClean.setProgramUrl(auditsManagement.getProgramUrl());
			return RestResult.success(alDataCleanService.updateById(alDataClean));
		}
	}

	// 获取详情
	@GetMapping("/getByName")
	public RestResult getByName(@RequestParam String alModelName, @RequestParam String modelFunction,
			@RequestParam String alModelType) {
		if (modelFunction.equals("故障诊断")) {
			List<Object> list = new ArrayList<>();
			QueryWrapper<AlFaultDiagnosis> alDigitalTwinQueryWrapper = new QueryWrapper<>();
			alDigitalTwinQueryWrapper.eq("model_name", alModelName);
			AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisService.getOne(alDigitalTwinQueryWrapper);
			list.add(alFaultDiagnosis);
			return RestResult.success(list);
		}
		else if (modelFunction.equals("实时状态评估")) {
			List<Object> list = new ArrayList<>();
			QueryWrapper<AlStateEvaluation> alStateEvaluationQueryWrapper = new QueryWrapper<>();
			alStateEvaluationQueryWrapper.eq("model_name", alModelName);
			list.add(alStateEvaluationService.getOne(alStateEvaluationQueryWrapper));
			return RestResult.success(list);
		}
		else if (alModelType.equals("数据去重") || alModelType.equals("缺失值填充") || alModelType.equals("异常值检测")
				|| alModelType.equals("数据标准化")) {
			List<Object> list = new ArrayList<>();
			QueryWrapper<AlDataClean> alDataCleanQueryWrapper = new QueryWrapper<>();
			alDataCleanQueryWrapper.eq("al_name", alModelName);
			list.add(alDataCleanService.getOne(alDataCleanQueryWrapper));
			return RestResult.success(list);
		}
		else {
			List<Object> list = new ArrayList<>();
			QueryWrapper<AlKnowledgeExtraction> alKnowledgeExtractionQueryWrapper = new QueryWrapper<>();
			alKnowledgeExtractionQueryWrapper.eq("al_name", alModelName);
			list.add(alKnowledgeExtractionService.getOne(alKnowledgeExtractionQueryWrapper));
			return RestResult.success(list);
		}
	}

	@GetMapping("/getCount")
	public RestResult getTestandNoTestCount(@RequestParam String altype, @RequestParam(required = false) Boolean isTest,
			@RequestParam(required = false) Boolean isDeployed) {
		Long count = 0L;

		if (altype.equals("数据清洗算法")) {
			if (isTest != null) {
				if (isTest == true && isDeployed == null) {
					count = auditsManagementMapper.getTestCountByDataClean();
				}
				if (isTest == false && isDeployed == null) {
					count = auditsManagementMapper.getNoTestCountByDataClean();
				}

			}
			if (isDeployed != null) {

				if (isTest == null && isDeployed == true) {
					count = auditsManagementMapper.getDeployedCountByDataClean();
				}
				if (isTest == null && isDeployed == false) {
					count = auditsManagementMapper.getNoDeployedCountByDataClean();
				}
			}

		}
		if (altype.equals("知识抽取推荐模型")) {

			if (isTest != null) {
				if (isTest == true && isDeployed == null) {
					count = auditsManagementMapper.getTestCountByKnowledgeExtraction();
				}
				if (isTest == false && isDeployed == null) {
					count = auditsManagementMapper.getNoTestCountByKnowledgeExtraction();
				}
			}
			if (isDeployed != null) {
				if (isTest == null && isDeployed == true) {
					count = auditsManagementMapper.getDeployedCountByKnowledgeExtraction();
				}
				if (isTest == null && isDeployed == false) {
					count = auditsManagementMapper.getNoDeployedCountByKnowledgeExtraction();
				}
			}

		}
		if (altype.equals("故障诊断模型")) {
			if (isTest != null) {
				if (isTest == true && isDeployed == null) {
					count = auditsManagementMapper.getTestCountByFaultDiagnose();
				}
				if (isTest == false && isDeployed == null) {
					count = auditsManagementMapper.getNoTestCountByFaultDiagnose();
				}
			}
			if (isDeployed != null) {
				if (isTest == null && isDeployed == true) {
					count = auditsManagementMapper.getDeployedCountByFaultDiagnose();
				}
				if (isTest == null && isDeployed == false) {
					count = auditsManagementMapper.getNoDeployedCountByFaultDiagnose();
				}
			}
		}
		if (altype.equals("实时状态评估模型")) {
			if (isTest != null) {
				if (isTest == true && isDeployed == null) {
					count = auditsManagementMapper.getTestCountByStateEvaluation();
				}
				if (isTest == false && isDeployed == null) {
					count = auditsManagementMapper.getNoTestCountByStateEvaluation();
				}
			}
			if (isDeployed != null) {
				if (isTest == null && isDeployed == true) {
					count = auditsManagementMapper.getDeployedCountByStateEvaluation();
				}
				if (isTest == null && isDeployed == false) {
					count = auditsManagementMapper.getNoDeployedCountByStateEvaluation();
				}
			}

		}

		return RestResult.success(count);
	}

}
