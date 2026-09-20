package sw.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pig4cloud.pig.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import sw.ai.domain.AlFaultDiagnosis;
import sw.ai.domain.AlStateEvaluation;
import sw.ai.model.dto.GroupedConfigDTO;
import sw.ai.service.AlFaultDiagnosisService;
import sw.ai.service.AlStateEvaluationService;
import sw.ai.service.DomainModelConfigurationService;
import sw.model3d.configModel.entity.ConfigBomTree;

import java.util.List;

/**
 * @author pig code generator
 * @date 2023-09-19 15:40:51
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
@Tag(name = "管理2")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class AIController {

	private final AlStateEvaluationService alStateEvaluationService;

	private final AlFaultDiagnosisService alFaultDiagnosisService;

	private final DomainModelConfigurationService domainModelConfigurationService;

	@Operation(summary = "查询所有状态感知算法", description = "查询所有状态感知算法")
	@GetMapping("/getAllByBD")
	public R getAllByBD() {
		List<AlStateEvaluation> algoList = alStateEvaluationService.getAllByBD();
		if (!CollectionUtils.isEmpty(algoList)) {
			for (AlStateEvaluation item : algoList) {
				item.setLabel(item.getModelName());
				item.setValue(item.getId());
			}
		}
		return R.ok(algoList, "查询成功");
	}

	@Operation(summary = "查询所有故障诊断算法", description = "查询所有故障诊断算法")
	@GetMapping("/getAllFaultDiagnosisByBD")
	public R getAllFaultDiagnosisByBD() {
		List<AlFaultDiagnosis> algoList = alFaultDiagnosisService.getAllByBD();
		if (!CollectionUtils.isEmpty(algoList)) {
			for (AlFaultDiagnosis item : algoList) {
				item.setLabel(item.getModelName());
				item.setValue(item.getId());
			}
		}
		return R.ok(algoList, "查询成功");
	}

	@Operation(summary = "查询所有组态算法", description = "查询所有组态算法")
	@GetMapping("/getAllDomainConfigByBD")
	public R getAllConfigByBD() {
		List<GroupedConfigDTO> algoList = domainModelConfigurationService.getAllDomainConfigByBD();
		if (!CollectionUtils.isEmpty(algoList)) {
			for (GroupedConfigDTO item : algoList) {
				item.setLabel(item.getModelName());
				item.setValue(item.getCode());
			}
		}
		return R.ok(algoList, "查询成功");
	}

	@Operation(summary = "查询id对应的状态评估算法简称", description = "查询id对应的状态评估算法简称")
	@GetMapping("/getShortName")
	public R getShortName(@RequestParam String algoId) {
		return R.ok(alStateEvaluationService
			.getOne(new QueryWrapper<AlStateEvaluation>().lambda().eq(AlStateEvaluation::getId, algoId)), "查询成功");
	}

}
