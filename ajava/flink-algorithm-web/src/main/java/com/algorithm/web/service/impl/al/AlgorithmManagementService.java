package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.entity.al.AlAlgorithmMenu;
import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister1to2;
import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister3to4;
import com.algorithm.web.model.entity.al.AlStateEvaluation;
import com.algorithm.web.model.entity.al.AlStateEvaluationRegister1to2;
import com.algorithm.web.model.entity.al.AlStateEvaluationRegister3to4;
import com.algorithm.web.service.al.AlAlgorithmMenuService;
import com.algorithm.web.service.al.AlFaultDiagnosisRegister1to2Service;
import com.algorithm.web.service.al.AlFaultDiagnosisRegister3to4Service;
import com.algorithm.web.service.al.AlFaultDiagnosisService;
import com.algorithm.web.service.al.AlStateEvaluationRegister1to2Service;
import com.algorithm.web.service.al.AlStateEvaluationRegister3to4Service;
import com.algorithm.web.service.al.AlStateEvaluationService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AlgorithmManagementService {

	public static final String TYPE_EVALUATION = "evaluation";

	public static final String TYPE_DIAGNOSIS = "diagnosis";

	private static final Set<String> EXECUTOR_TYPES = new HashSet<>(
			Arrays.asList("HTTP_JSON", "ELEVATOR_INFLUX_MONITOR", "ELEVATOR_INFLUX_DIAGNOSIS", "SPRING_BEAN"));

	private final AlStateEvaluationService stateEvaluationService;

	private final AlFaultDiagnosisService faultDiagnosisService;

	private final AlStateEvaluationRegister1to2Service stateDetailOneService;

	private final AlStateEvaluationRegister3to4Service stateDetailTwoService;

	private final AlFaultDiagnosisRegister1to2Service diagnosisDetailOneService;

	private final AlFaultDiagnosisRegister3to4Service diagnosisDetailTwoService;

	private final AlAlgorithmMenuService algorithmMenuService;

	private final JdbcTemplate jdbcTemplate;

	private final ObjectMapper objectMapper;

	public Object get(String type, Long id) {
		String normalizedType = normalizeType(type);
		Object algorithm = TYPE_EVALUATION.equals(normalizedType) ? stateEvaluationService.getById(id)
				: faultDiagnosisService.getById(id);
		if (algorithm == null) {
			throw new BizException("算法不存在: " + id);
		}
		return algorithm;
	}

	public boolean nameExists(String type, String name, Long excludedId) {
		String normalizedType = normalizeType(type);
		if (TYPE_EVALUATION.equals(normalizedType)) {
			return stateEvaluationService.count(Wrappers.<AlStateEvaluation>lambdaQuery()
				.eq(AlStateEvaluation::getModelName, name)
				.ne(excludedId != null, AlStateEvaluation::getId, excludedId)) > 0;
		}
		return faultDiagnosisService.count(Wrappers.<AlFaultDiagnosis>lambdaQuery()
			.eq(AlFaultDiagnosis::getModelName, name)
			.ne(excludedId != null, AlFaultDiagnosis::getId, excludedId)) > 0;
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean updateExamples(String type, Long id, String input, String output) {
		if (input == null || output == null) {
			throw new BizException("模型输入举例和输出举例不能为空");
		}
		String normalizedType = normalizeType(type);
		if (TYPE_EVALUATION.equals(normalizedType)) {
			return stateEvaluationService.lambdaUpdate()
				.eq(AlStateEvaluation::getId, id)
				.set(AlStateEvaluation::getInput, input)
				.set(AlStateEvaluation::getOutput, output)
				.update();
		}
		return faultDiagnosisService.lambdaUpdate()
			.eq(AlFaultDiagnosis::getId, id)
			.set(AlFaultDiagnosis::getInput, input)
			.set(AlFaultDiagnosis::getOutput, output)
			.update();
	}

	@Transactional(rollbackFor = Exception.class)
	public Object create(String type, JsonNode payload) {
		String normalizedType = normalizeType(type);
		if (TYPE_EVALUATION.equals(normalizedType)) {
			AlStateEvaluation algorithm = objectMapper.convertValue(payload, AlStateEvaluation.class);
			prepareNewState(algorithm);
			stateEvaluationService.save(algorithm);
			algorithm.setAlCode("se-" + algorithm.getId());
			stateEvaluationService.updateById(algorithm);
			ensureDetailRecord(normalizedType, algorithm.getModelName(), algorithm.getModelType());
			return algorithm;
		}

		AlFaultDiagnosis algorithm = objectMapper.convertValue(payload, AlFaultDiagnosis.class);
		prepareNewDiagnosis(algorithm);
		faultDiagnosisService.save(algorithm);
		algorithm.setAlCode("fd-" + algorithm.getId());
		faultDiagnosisService.updateById(algorithm);
		ensureDetailRecord(normalizedType, algorithm.getModelName(), algorithm.getModelType());
		return algorithm;
	}

	@Transactional(rollbackFor = Exception.class)
	public Object update(String type, Long id, JsonNode payload) {
		String normalizedType = normalizeType(type);
		if (TYPE_EVALUATION.equals(normalizedType)) {
			AlStateEvaluation current = stateEvaluationService.getById(id);
			if (current == null) {
				throw new BizException("算法不存在: " + id);
			}
			AlStateEvaluation updated = objectMapper.convertValue(payload, AlStateEvaluation.class);
			updated.setId(id);
			updated.setAlCode(current.getAlCode());
			preserveStateWorkflow(current, updated);
			validateShortNameChange(current.getModelShortName(), updated.getModelShortName());
			validateState(updated, id);
			stateEvaluationService.updateById(updated);
			synchronizeRename(normalizedType, id, current.getModelName(), updated.getModelName(),
					current.getModelShortName(), updated.getModelShortName());
			ensureDetailRecord(normalizedType, updated.getModelName(), updated.getModelType());
			return stateEvaluationService.getById(id);
		}

		AlFaultDiagnosis current = faultDiagnosisService.getById(id);
		if (current == null) {
			throw new BizException("算法不存在: " + id);
		}
		AlFaultDiagnosis updated = objectMapper.convertValue(payload, AlFaultDiagnosis.class);
		updated.setId(id);
		updated.setAlCode(current.getAlCode());
		preserveDiagnosisWorkflow(current, updated);
		validateShortNameChange(current.getModelShortName(), updated.getModelShortName());
		validateDiagnosis(updated, id);
		faultDiagnosisService.updateById(updated);
		synchronizeRename(normalizedType, id, current.getModelName(), updated.getModelName(),
				current.getModelShortName(), updated.getModelShortName());
		ensureDetailRecord(normalizedType, updated.getModelName(), updated.getModelType());
		return faultDiagnosisService.getById(id);
	}

	@Transactional(rollbackFor = Exception.class)
	public boolean delete(String type, Long id) {
		String normalizedType = normalizeType(type);
		if (TYPE_EVALUATION.equals(normalizedType)) {
			AlStateEvaluation current = stateEvaluationService.getById(id);
			if (current == null) {
				return false;
			}
			ensureShortNameNotUsedByTask(current.getModelShortName(), "删除");
			stateDetailOneService.remove(Wrappers.<AlStateEvaluationRegister1to2>lambdaQuery()
				.eq(AlStateEvaluationRegister1to2::getModelName, current.getModelName()));
			stateDetailTwoService.remove(Wrappers.<AlStateEvaluationRegister3to4>lambdaQuery()
				.eq(AlStateEvaluationRegister3to4::getModelName, current.getModelName()));
			removeMenu(normalizedType, id);
			return stateEvaluationService.removeById(id);
		}

		AlFaultDiagnosis current = faultDiagnosisService.getById(id);
		if (current == null) {
			return false;
		}
		ensureShortNameNotUsedByTask(current.getModelShortName(), "删除");
		diagnosisDetailOneService.remove(Wrappers.<AlFaultDiagnosisRegister1to2>lambdaQuery()
			.eq(AlFaultDiagnosisRegister1to2::getModelName, current.getModelName()));
		diagnosisDetailTwoService.remove(Wrappers.<AlFaultDiagnosisRegister3to4>lambdaQuery()
			.eq(AlFaultDiagnosisRegister3to4::getModelName, current.getModelName()));
		removeMenu(normalizedType, id);
		return faultDiagnosisService.removeById(id);
	}

	private void prepareNewState(AlStateEvaluation algorithm) {
		algorithm.setId(null);
		algorithm.setAlCode(null);
		algorithm.setIsCheck(1L);
		algorithm.setIsPass(2L);
		algorithm.setIsDeployed(1L);
		algorithm.setIsService(0);
		algorithm.setModelNum(0L);
		algorithm.setExecutorType(normalizeExecutor(algorithm.getExecutorType(), null));
		validateState(algorithm, null);
	}

	private void prepareNewDiagnosis(AlFaultDiagnosis algorithm) {
		algorithm.setId(null);
		algorithm.setAlCode(null);
		algorithm.setIsCheck(1L);
		algorithm.setIsPass(2L);
		algorithm.setIsDeployed(1L);
		algorithm.setIsService(0);
		algorithm.setModelNum(0L);
		algorithm.setExecutorType(normalizeExecutor(algorithm.getExecutorType(), null));
		validateDiagnosis(algorithm, null);
	}

	private void preserveStateWorkflow(AlStateEvaluation current, AlStateEvaluation updated) {
		updated.setIsCheck(updated.getIsCheck() == null ? current.getIsCheck() : updated.getIsCheck());
		updated.setIsPass(updated.getIsPass() == null ? current.getIsPass() : updated.getIsPass());
		updated.setIsDeployed(updated.getIsDeployed() == null ? current.getIsDeployed() : updated.getIsDeployed());
		updated.setIsService(updated.getIsService() == null ? current.getIsService() : updated.getIsService());
		updated.setModelNum(updated.getModelNum() == null ? current.getModelNum() : updated.getModelNum());
		updated.setExecutorType(normalizeExecutor(updated.getExecutorType(), current.getExecutorType()));
	}

	private void preserveDiagnosisWorkflow(AlFaultDiagnosis current, AlFaultDiagnosis updated) {
		updated.setIsCheck(updated.getIsCheck() == null ? current.getIsCheck() : updated.getIsCheck());
		updated.setIsPass(updated.getIsPass() == null ? current.getIsPass() : updated.getIsPass());
		updated.setIsDeployed(updated.getIsDeployed() == null ? current.getIsDeployed() : updated.getIsDeployed());
		updated.setIsService(updated.getIsService() == null ? current.getIsService() : updated.getIsService());
		updated.setModelNum(updated.getModelNum() == null ? current.getModelNum() : updated.getModelNum());
		updated.setExecutorType(normalizeExecutor(updated.getExecutorType(), current.getExecutorType()));
	}

	private void validateState(AlStateEvaluation algorithm, Long excludedId) {
		validateCommon(algorithm.getModelName(), algorithm.getModelShortName(), algorithm.getExecutorType(),
				algorithm.getExecutorConfig());
		if ("ELEVATOR_INFLUX_DIAGNOSIS".equals(algorithm.getExecutorType())) {
			throw new BizException("状态评估算法不能使用故障诊断执行器");
		}
		long count = stateEvaluationService.count(Wrappers.<AlStateEvaluation>lambdaQuery()
			.eq(AlStateEvaluation::getModelName, algorithm.getModelName())
			.ne(excludedId != null, AlStateEvaluation::getId, excludedId));
		if (count > 0) {
			throw new BizException("状态评估算法名称已存在");
		}
		validateShortNameUnique(TYPE_EVALUATION, algorithm.getModelShortName(), excludedId);
	}

	private void validateDiagnosis(AlFaultDiagnosis algorithm, Long excludedId) {
		validateCommon(algorithm.getModelName(), algorithm.getModelShortName(), algorithm.getExecutorType(),
				algorithm.getExecutorConfig());
		if ("ELEVATOR_INFLUX_MONITOR".equals(algorithm.getExecutorType())) {
			throw new BizException("故障诊断算法不能使用状态监测执行器");
		}
		long count = faultDiagnosisService.count(Wrappers.<AlFaultDiagnosis>lambdaQuery()
			.eq(AlFaultDiagnosis::getModelName, algorithm.getModelName())
			.ne(excludedId != null, AlFaultDiagnosis::getId, excludedId));
		if (count > 0) {
			throw new BizException("故障诊断算法名称已存在");
		}
		validateShortNameUnique(TYPE_DIAGNOSIS, algorithm.getModelShortName(), excludedId);
	}

	private void validateShortNameUnique(String type, String shortName, Long excludedId) {
		long stateCount = stateEvaluationService.count(Wrappers.<AlStateEvaluation>lambdaQuery()
			.eq(AlStateEvaluation::getModelShortName, shortName)
			.ne(TYPE_EVALUATION.equals(type) && excludedId != null, AlStateEvaluation::getId, excludedId));
		long diagnosisCount = faultDiagnosisService.count(Wrappers.<AlFaultDiagnosis>lambdaQuery()
			.eq(AlFaultDiagnosis::getModelShortName, shortName)
			.ne(TYPE_DIAGNOSIS.equals(type) && excludedId != null, AlFaultDiagnosis::getId, excludedId));
		if (stateCount + diagnosisCount > 0) {
			throw new BizException("算法短名已被其他算法使用");
		}
	}

	private void validateShortNameChange(String oldShortName, String newShortName) {
		if (!Objects.equals(oldShortName, newShortName)) {
			ensureShortNameNotUsedByTask(oldShortName, "修改");
		}
	}

	private void ensureShortNameNotUsedByTask(String shortName, String operation) {
		if (isBlank(shortName)) {
			return;
		}
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM config_perceived_task WHERE algo_shortname = ?", Integer.class, shortName);
		if (count != null && count > 0) {
			throw new BizException("算法短名 " + shortName + " 已被感知任务使用，不能" + operation);
		}
	}

	private void validateCommon(String name, String shortName, String executorType, String executorConfig) {
		if (isBlank(name)) {
			throw new BizException("算法名称不能为空");
		}
		if (isBlank(shortName)) {
			throw new BizException("算法短名不能为空");
		}
		if (!shortName.matches("[A-Za-z0-9_-]+")) {
			throw new BizException("算法短名只允许字母、数字、下划线和短横线");
		}
		if (!EXECUTOR_TYPES.contains(executorType)) {
			throw new BizException("不支持的执行器类型: " + executorType);
		}
		if ("SPRING_BEAN".equals(executorType)) {
			try {
				JsonNode beanName = objectMapper.readTree(executorConfig).get("beanName");
				if (beanName == null || !beanName.isTextual() || beanName.asText().trim().isEmpty()) {
					throw new BizException("内置执行器必须配置beanName");
				}
			}
			catch (BizException exception) {
				throw exception;
			}
			catch (Exception exception) {
				throw new BizException("内置执行器配置不是合法JSON");
			}
		}
	}

	private void ensureDetailRecord(String type, String modelName, String modelType) {
		boolean learningModel = "深度学习模型".equals(modelType) || "传统机器学习模型".equals(modelType);
		if (TYPE_EVALUATION.equals(type) && learningModel
				&& stateDetailOneService.count(Wrappers.<AlStateEvaluationRegister1to2>lambdaQuery()
					.eq(AlStateEvaluationRegister1to2::getModelName, modelName)) == 0) {
			AlStateEvaluationRegister1to2 detail = new AlStateEvaluationRegister1to2();
			detail.setModelName(modelName);
			stateDetailOneService.save(detail);
		}
		else if (TYPE_EVALUATION.equals(type) && !learningModel
				&& stateDetailTwoService.count(Wrappers.<AlStateEvaluationRegister3to4>lambdaQuery()
					.eq(AlStateEvaluationRegister3to4::getModelName, modelName)) == 0) {
			AlStateEvaluationRegister3to4 detail = new AlStateEvaluationRegister3to4();
			detail.setModelName(modelName);
			stateDetailTwoService.save(detail);
		}
		else if (TYPE_DIAGNOSIS.equals(type) && learningModel
				&& diagnosisDetailOneService.count(Wrappers.<AlFaultDiagnosisRegister1to2>lambdaQuery()
					.eq(AlFaultDiagnosisRegister1to2::getModelName, modelName)) == 0) {
			AlFaultDiagnosisRegister1to2 detail = new AlFaultDiagnosisRegister1to2();
			detail.setModelName(modelName);
			diagnosisDetailOneService.save(detail);
		}
		else if (TYPE_DIAGNOSIS.equals(type) && !learningModel
				&& diagnosisDetailTwoService.count(Wrappers.<AlFaultDiagnosisRegister3to4>lambdaQuery()
					.eq(AlFaultDiagnosisRegister3to4::getModelName, modelName)) == 0) {
			AlFaultDiagnosisRegister3to4 detail = new AlFaultDiagnosisRegister3to4();
			detail.setModelName(modelName);
			diagnosisDetailTwoService.save(detail);
		}
	}

	private void synchronizeRename(String type, Long id, String oldName, String newName, String oldShortName,
			String newShortName) {
		if (!Objects.equals(oldName, newName)) {
			if (TYPE_EVALUATION.equals(type)) {
				stateDetailOneService.update(Wrappers.<AlStateEvaluationRegister1to2>lambdaUpdate()
					.eq(AlStateEvaluationRegister1to2::getModelName, oldName)
					.set(AlStateEvaluationRegister1to2::getModelName, newName));
				stateDetailTwoService.update(Wrappers.<AlStateEvaluationRegister3to4>lambdaUpdate()
					.eq(AlStateEvaluationRegister3to4::getModelName, oldName)
					.set(AlStateEvaluationRegister3to4::getModelName, newName));
			}
			else {
				diagnosisDetailOneService.update(Wrappers.<AlFaultDiagnosisRegister1to2>lambdaUpdate()
					.eq(AlFaultDiagnosisRegister1to2::getModelName, oldName)
					.set(AlFaultDiagnosisRegister1to2::getModelName, newName));
				diagnosisDetailTwoService.update(Wrappers.<AlFaultDiagnosisRegister3to4>lambdaUpdate()
					.eq(AlFaultDiagnosisRegister3to4::getModelName, oldName)
					.set(AlFaultDiagnosisRegister3to4::getModelName, newName));
			}
			jdbcTemplate.update("UPDATE domain_model SET basic_algorithm = ? WHERE basic_algorithm = ?", newName,
					oldName);
		}

		algorithmMenuService.update(Wrappers.<AlAlgorithmMenu>lambdaUpdate()
			.eq(AlAlgorithmMenu::getAlgorithmType, type)
			.eq(AlAlgorithmMenu::getAlgorithmId, id)
			.set(AlAlgorithmMenu::getMenuName, newName));

		if ((!Objects.equals(oldName, newName) || !Objects.equals(oldShortName, newShortName))
				&& !isBlank(oldShortName)) {
			jdbcTemplate.update(
					"UPDATE algorithm_configuration SET almodel_name = ?, almodel_short_name = ? WHERE almodel_name = ? AND almodel_short_name = ?",
					newName, newShortName, oldName, oldShortName);
		}
	}

	private void removeMenu(String type, Long id) {
		algorithmMenuService.remove(Wrappers.<AlAlgorithmMenu>lambdaQuery()
			.eq(AlAlgorithmMenu::getAlgorithmType, type)
			.eq(AlAlgorithmMenu::getAlgorithmId, id));
	}

	private String normalizeType(String type) {
		String normalized = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
		if (!TYPE_EVALUATION.equals(normalized) && !TYPE_DIAGNOSIS.equals(normalized)) {
			throw new BizException("只支持状态评估和故障诊断算法");
		}
		return normalized;
	}

	private String normalizeExecutor(String executorType, String fallback) {
		String value = isBlank(executorType) ? fallback : executorType;
		if (isBlank(value)) {
			throw new BizException("算法未配置执行方式");
		}
		return value.trim().toUpperCase(Locale.ROOT);
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}
