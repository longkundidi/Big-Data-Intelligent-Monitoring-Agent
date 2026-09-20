package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.mapper.al.AlFaultDiagnosisMapper;
import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.dto.al.ElevatorCmsWaveformRequest;
import com.algorithm.web.model.dto.al.ModelExampleUpdateRequest;
import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.vo.FaultDiagnosisAllInfoVo;
import com.algorithm.web.service.al.AlFaultDiagnosisService;
import com.algorithm.web.service.impl.al.AlgorithmManagementService;
import com.algorithm.web.service.impl.executealgorithm.ElevatorFaultDiagnosisService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alFaultDiagnosisbase")

public class AlFaultDiagnosisController extends BaseController {

	private final AlFaultDiagnosisService alFaultDiagnosisService;

	private final AlFaultDiagnosisMapper alFaultDiagnosisMapper;

	private final ElevatorFaultDiagnosisService elevatorFaultDiagnosisService;

	private final AlgorithmManagementService algorithmManagementService;

	private final ObjectMapper objectMapper;

	@GetMapping("/page")
	public RestResult getAlFaultDiagnosisBasePage(Page page, AlFaultDiagnosis alFaultDiagnosis) {
		return RestResult.success(alFaultDiagnosisService.getPage(page, alFaultDiagnosis));
	}

	/* 模糊查询对象 */
	@GetMapping("/page1")
	public RestResult getAlFaultDiagnosisBasePage1(Page page, AlFaultDiagnosis alFaultDiagnosis) {
		return RestResult.success(alFaultDiagnosisService.getPageObj(page, alFaultDiagnosis));
	}

	/* 模糊查询模型 */
	@GetMapping("/page2")
	public RestResult getAlFaultDiagnosisBasePage2(Page page, AlFaultDiagnosis alFaultDiagnosis) {
		return RestResult.success(alFaultDiagnosisService.getPageName(page, alFaultDiagnosis));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(algorithmManagementService.get(AlgorithmManagementService.TYPE_DIAGNOSIS, id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlFaultDiagnosis alFaultDiagnosis) {
		return RestResult.success(algorithmManagementService.create(AlgorithmManagementService.TYPE_DIAGNOSIS,
				objectMapper.valueToTree(alFaultDiagnosis)));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlFaultDiagnosis alFaultDiagnosis) {
		return RestResult.success(algorithmManagementService.update(AlgorithmManagementService.TYPE_DIAGNOSIS,
				alFaultDiagnosis.getId(), objectMapper.valueToTree(alFaultDiagnosis)));
	}

	@PutMapping("/{id}/examples")
	public RestResult updateExamples(@PathVariable Long id, @RequestBody ModelExampleUpdateRequest request) {
		boolean updated = algorithmManagementService.updateExamples(AlgorithmManagementService.TYPE_DIAGNOSIS, id,
				request.getInput(), request.getOutput());
		return updated ? RestResult.success() : RestResult.error("模型输入输出保存失败");
	}

	@DeleteMapping("/{id}")
	public RestResult removeById1(@PathVariable Long id) {
		return RestResult.success(algorithmManagementService.delete(AlgorithmManagementService.TYPE_DIAGNOSIS, id));
	}

	@GetMapping("/list")
	public RestResult getlist() {
		List<FaultDiagnosisAllInfoVo> faultDiagnosisAllInfoVo = alFaultDiagnosisMapper.selectAllInfo();
		return RestResult.success(faultDiagnosisAllInfoVo);
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		if (!algorithmManagementService.nameExists(AlgorithmManagementService.TYPE_DIAGNOSIS, name, null)) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	// 执行风机故障诊断
	@PostMapping("/operate")
	public RestResult operate(@RequestBody DiagnoseInfoDto diagnoseInfoDto) {
		return RestResult.success(alFaultDiagnosisService.operate(diagnoseInfoDto));
	}

	@PostMapping("/cmsWaveform/latest")
	public RestResult getLatestCmsWaveform(@RequestBody ElevatorCmsWaveformRequest request) {
		return RestResult.success(elevatorFaultDiagnosisService.queryLatestWaveform(request));
	}

	@PostMapping("/cmsWaveform/nearest")
	public RestResult getNearestCmsWaveform(@RequestBody ElevatorCmsWaveformRequest request) {
		return RestResult.success(elevatorFaultDiagnosisService.queryNearestWaveform(request));
	}

	@GetMapping("/diagnosisRecords")
	public RestResult getDiagnosisRecords(@RequestParam String turbineCode,
			@RequestParam(required = false) String startTime) {
		return RestResult.success(alFaultDiagnosisService.listDiagnosisRecords(turbineCode, startTime));
	}

	@GetMapping("/diagnosisRecords/{id}")
	public RestResult getDiagnosisRecord(@PathVariable Long id) {
		return RestResult.success(alFaultDiagnosisService.getDiagnosisRecord(id));
	}

	// 执行信号分析
	@PostMapping("/signalAnalysis")
	public RestResult signalAnalysis(@RequestBody DiagnoseInfoDto diagnoseInfoDto) {
		return RestResult.success(alFaultDiagnosisService.signalAnalysis(diagnoseInfoDto));
	}

}
