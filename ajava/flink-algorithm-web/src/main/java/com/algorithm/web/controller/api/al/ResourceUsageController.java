package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.service.al.*;
import com.algorithm.web.utils.AlModelContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resourceUsage")
public class ResourceUsageController {

	private final ResourceUsageService resourceUsageService;

	private final AlDataCleanService alDataCleanService;

	private final AlKnowledgeExtractionService alKnowledgeExtractionService;

	private final AlStateEvaluationService alStateEvaluationService;

	private final AlFaultDiagnosisService alFaultDiagnosisService;

	@GetMapping("/containerStats")
	public RestResult containerStats(@RequestParam String serverName, @RequestParam(defaultValue = "") String alId,
			@RequestParam String alClass) {
		String shortName = "";
		switch (alClass) {
			case "alDataCleaning":
				shortName = alDataCleanService.getById(alId).getAlShortName();
				break;
			case "alKnowledgeExtraction":
				shortName = alKnowledgeExtractionService.getById(alId).getAlShortName();
				break;
			case "alStateEvaluation":
				shortName = alStateEvaluationService.getById(alId).getModelShortName();
				break;
			case "alFaultDiagnosis":
				shortName = alFaultDiagnosisService.getById(alId).getModelShortName();
				break;
			case "flink":
				shortName = alClass;
				break;
			default:
				System.out.println("Invalid alClass");
				break;
		}
		return RestResult.success(resourceUsageService.containerStats(serverName,
				AlModelContainerService.getAlModelContainerName(shortName)));
	}

}
