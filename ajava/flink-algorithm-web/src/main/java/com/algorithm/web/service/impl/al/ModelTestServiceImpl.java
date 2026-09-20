package com.algorithm.web.service.impl.al;

import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.model.dto.al.ModelTestRequest;
import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.entity.al.AlStateEvaluation;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.service.al.AlFaultDiagnosisService;
import com.algorithm.web.service.al.AlStateEvaluationService;
import com.algorithm.web.service.al.AlTaskService;
import com.algorithm.web.service.al.AlgorithmJobService;
import com.algorithm.web.service.al.ModelTestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class ModelTestServiceImpl implements ModelTestService {

	private final AlTaskService alTaskService;

	private final AlgorithmJobService algorithmJobService;

	private final AlStateEvaluationService alStateEvaluationService;

	private final AlFaultDiagnosisService alFaultDiagnosisService;

	public ModelTestServiceImpl(AlTaskService alTaskService, AlgorithmJobService algorithmJobService,
			AlStateEvaluationService alStateEvaluationService, AlFaultDiagnosisService alFaultDiagnosisService) {
		this.alTaskService = alTaskService;
		this.algorithmJobService = algorithmJobService;
		this.alStateEvaluationService = alStateEvaluationService;
		this.alFaultDiagnosisService = alFaultDiagnosisService;
	}

	@Override
	public AlTask execute(ModelTestRequest request) {
		ModelEndpoint endpoint = resolveEndpoint(request);
		AlTask task = AlTask.builder()
			.alId(request.getAlId())
			.alClass(toTaskClass(request.getAlgorithmType()))
			.useCase("模型测试")
			.taskMsg(request.getTaskMsg())
			.taskResult("{}")
			.taskState(0)
			.isDeleted(0)
			.isjson(0L)
			.startTime(LocalDateTime.now())
			.build();
		alTaskService.save(task);
		task.setTaskId(task.getId());

		try {
			AlTask result = algorithmJobService.startConfiguredJob(task, endpoint.backupUrl, endpoint.modelUrl,
					endpoint.executorType, endpoint.executorConfig);
			copyResult(task, result);
		}
		catch (Exception exception) {
			task.setTaskState(3);
			task.setTaskResult(JsonUtil.toJson(Collections.singletonMap("message", exception.getMessage())));
		}

		task.setEndTime(LocalDateTime.now());
		alTaskService.updateById(task);
		if (Integer.valueOf(2).equals(task.getTaskState())) {
			endpoint.incrementInvocationCount();
		}
		return task;
	}

	private ModelEndpoint resolveEndpoint(ModelTestRequest request) {
		if (request == null || request.getAlId() == null || isBlank(request.getAlgorithmType())
				|| isBlank(request.getTaskMsg())) {
			throw new IllegalArgumentException("模型测试必须包含alId、algorithmType和taskMsg");
		}

		switch (request.getAlgorithmType()) {
			case "evaluation":
				AlStateEvaluation evaluation = alStateEvaluationService.getById(request.getAlId());
				if (evaluation == null) {
					throw new IllegalArgumentException("状态评估模型不存在: " + request.getAlId());
				}
				return new ModelEndpoint(evaluation.getModelUrl(), evaluation.getBackupModelUrl(),
						evaluation.getExecutorType(), evaluation.getExecutorConfig(), () -> {
							evaluation.setModelNum(defaultCount(evaluation.getModelNum()) + 1);
							alStateEvaluationService.updateById(evaluation);
						});
			case "diagnosis":
				AlFaultDiagnosis diagnosis = alFaultDiagnosisService.getById(request.getAlId());
				if (diagnosis == null) {
					throw new IllegalArgumentException("故障诊断模型不存在: " + request.getAlId());
				}
				return new ModelEndpoint(diagnosis.getModelUrl(), diagnosis.getBackupModelUrl(),
						diagnosis.getExecutorType(), diagnosis.getExecutorConfig(), () -> {
							diagnosis.setModelNum(defaultCount(diagnosis.getModelNum()) + 1);
							alFaultDiagnosisService.updateById(diagnosis);
						});
			default:
				throw new IllegalArgumentException("暂不支持的模型类型: " + request.getAlgorithmType());
		}
	}

	private String toTaskClass(String algorithmType) {
		return "evaluation".equals(algorithmType) ? "alStateEvaluation" : "alFaultDiagnosis";
	}

	private void copyResult(AlTask target, AlTask source) {
		target.setTaskState(source.getTaskState() == null ? 3 : source.getTaskState());
		target.setTaskMsg(source.getTaskMsg() == null ? target.getTaskMsg() : source.getTaskMsg());
		target.setTaskResult(source.getTaskResult() == null ? "{}" : source.getTaskResult());
		target.setTaskUrl(source.getTaskUrl());
		target.setTaskReUrl(source.getTaskReUrl());
		target.setServerUrl(source.getServerUrl());
	}

	private long defaultCount(Long value) {
		return value == null ? 0 : value;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private static final class ModelEndpoint {

		private final String modelUrl;

		private final String backupUrl;

		private final String executorType;

		private final String executorConfig;

		private final Runnable invocationCounter;

		private ModelEndpoint(String modelUrl, String backupUrl, String executorType, String executorConfig,
				Runnable invocationCounter) {
			this.modelUrl = modelUrl;
			this.backupUrl = backupUrl;
			this.executorType = executorType;
			this.executorConfig = executorConfig;
			this.invocationCounter = invocationCounter;
		}

		private void incrementInvocationCount() {
			invocationCounter.run();
		}

	}

}
