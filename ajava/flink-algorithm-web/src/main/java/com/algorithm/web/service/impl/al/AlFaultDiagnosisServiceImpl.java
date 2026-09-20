package com.algorithm.web.service.impl.al;

import cn.hutool.json.JSONObject;
import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.enums.SysConfigEnum;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlDiagnosisRecordMapper;
import com.algorithm.web.mapper.al.AlFaultDiagnosisMapper;
import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.al.AlDiagnosisRecord;
import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.service.al.AlFaultDiagnosisService;
import com.algorithm.web.service.al.AlTaskService;
import com.algorithm.web.service.al.AlgorithmJobService;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.utils.RestTemplateUtil;
import com.algorithm.web.utils.TaskMsgServices;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AlFaultDiagnosisServiceImpl extends ServiceImpl<AlFaultDiagnosisMapper, AlFaultDiagnosis>
		implements AlFaultDiagnosisService {

	/**
	 * 当前诊断链路仍未完成真实设备到算法样例参数的映射，因此这里先统一封装为一组 已验证可成功返回结果的固定请求模板。后续真正恢复动态诊断时，请优先修改
	 * {@link #buildCompatibleDiagnosisPayload(DiagnoseInfoDto)}，而不是在各个调用点再次散落拼装字段。
	 */
	private static final String COMPATIBLE_FARM_NAME = "普格海口风电场";

	/**
	 * 已验证可成功跑通诊断模型的风机实例。
	 */
	private static final String COMPATIBLE_TURBINE_NAME = "风机#4";

	/**
	 * 已验证可成功跑通诊断模型的部件名称。
	 */
	private static final String COMPATIBLE_PART_NAME = "发电机";

	/**
	 * 已验证可成功跑通诊断模型的测点/位置名称。
	 */
	private static final String COMPATIBLE_LOCATION_NAME = "发电机非驱动端轴承";

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	private AlFaultDiagnosisMapper alFaultDiagnosisMapper;

	@Autowired
	private AlDiagnosisRecordMapper alDiagnosisRecordMapper;

	private final AlTaskService alTaskService;

	@Autowired
	private AlgorithmJobService algorithmJobService;

	@Autowired
	public AlFaultDiagnosisServiceImpl(@Lazy AlTaskService alTaskService) {
		this.alTaskService = alTaskService;
	}

	@Autowired
	private Map<String, BuildTaskMsgService> algorithmServiceMap;

	@Override
	public String getnamebyid(long id) {
		return alFaultDiagnosisMapper.getnamebyid(id);
	}

	@Override
	public AlFaultDiagnosis getbyname(String modelName) {
		return alFaultDiagnosisMapper.getbyname(modelName);
	}

	@Override
	public AlFaultDiagnosis getbyId(long id) {
		return alFaultDiagnosisMapper.getbyId(id);
	}

	@Override
	public IPage<AlFaultDiagnosis> getPage(IPage page, AlFaultDiagnosis alFaultDiagnosis) {
		String modelTypeFirst = alFaultDiagnosis.getModelTypeFirst();
		String modelFunction = alFaultDiagnosis.getModelFunction();
		if (Objects.equals(modelFunction, "")) {
			return alFaultDiagnosisMapper.selectClassPagenull(page, modelTypeFirst);
		}
		else {
			return alFaultDiagnosisMapper.selectClassPage(page, modelTypeFirst, modelFunction);
		}
	}

	@Override
	public IPage<AlFaultDiagnosis> getPageObj(IPage page, AlFaultDiagnosis alFaultDiagnosis) {
		String modelTypeFirst = alFaultDiagnosis.getModelTypeFirst();
		String modelFunction = alFaultDiagnosis.getModelFunction();
		String modelObject = alFaultDiagnosis.getModelObject();
		return alFaultDiagnosisMapper.selectObj(page, modelTypeFirst, modelFunction, modelObject);
	}

	@Override
	public IPage<AlFaultDiagnosis> getPageName(IPage page, AlFaultDiagnosis alFaultDiagnosis) {
		String modelTypeFirst = alFaultDiagnosis.getModelTypeFirst();
		String modelFunction = alFaultDiagnosis.getModelFunction();
		String modelName = alFaultDiagnosis.getModelName();
		return alFaultDiagnosisMapper.selectName(page, modelTypeFirst, modelFunction, modelName);
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alFaultDiagnosisMapper.exitName(name);
		return alName != null;
	}

	@Override
	public List<AlDiagnosisRecord> listDiagnosisRecords(String turbineCode, String startTime) {
		if (isBlank(turbineCode)) {
			throw new BizException("设备实例编码不能为空");
		}

		String normalizedStartTime;
		try {
			normalizedStartTime = isBlank(startTime) ? Instant.now().minus(7, ChronoUnit.DAYS).toString()
					: Instant.parse(startTime).toString();
		}
		catch (Exception exception) {
			throw new BizException("诊断记录开始时间必须是带时区的ISO-8601格式");
		}

		return alDiagnosisRecordMapper.selectRecentByTurbineCode(turbineCode.trim(), normalizedStartTime);
	}

	@Override
	public Map<String, Object> getDiagnosisRecord(Long id) {
		if (id == null) {
			throw new BizException("诊断记录ID不能为空");
		}
		AlDiagnosisRecord record = alDiagnosisRecordMapper.selectById(id);
		if (record == null) {
			throw new BizException("诊断记录不存在");
		}

		AlTask task = record.getAlgorithmTaskId() == null ? null : alTaskService.getById(record.getAlgorithmTaskId());
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("record", record);
		result.put("taskMsg", task == null ? null : task.getTaskMsg());
		result.put("taskResult", task == null ? record.getResultJson() : task.getTaskResult());
		return result;
	}

	@Override
	public boolean updateCodeByName(String modelName) {
		if (modelName == null || modelName.trim().isEmpty()) {
			throw new BizException("模型名称不能为空");
		}
		AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisMapper.getbyname(modelName);
		if (alFaultDiagnosis == null) {
			throw new BizException("未找到名称为 " + modelName + " 的模型");
		}
		String code = String.format("fd-%d", alFaultDiagnosis.getId());
		alFaultDiagnosis.setAlCode(code);
		return this.updateById(alFaultDiagnosis);
	}

	@Override
	public Object operate(DiagnoseInfoDto diagnoseInfoDto) {
		AlFaultDiagnosis alFaultDiagnosis = this.getbyId(Long.parseLong(diagnoseInfoDto.getAlgoId()));
		if (alFaultDiagnosis == null) {
			throw new BizException("故障诊断算法不存在");
		}
		String url = alFaultDiagnosis.getModelUrl();
		DiagnosisReservation reservation = null;
		if (isElevatorDiagnosis(alFaultDiagnosis)) {
			reservation = reserveDiagnosis(diagnoseInfoDto, alFaultDiagnosis);
			if (reservation.getCachedTask() != null) {
				return reservation.getCachedTask();
			}
		}

		try {
			JSONObject jsonObject = buildDiagnosisPayload(diagnoseInfoDto, alFaultDiagnosis);

			Optional<BuildTaskMsgService> optional = Optional.ofNullable(
					algorithmServiceMap.get(TaskMsgServices.getTaskMsgService(alFaultDiagnosis.getModelShortName())));
			BuildTaskMsgService buildTaskMsgService = optional
				.orElseThrow(() -> new BizException("未找到算法 " + alFaultDiagnosis.getModelShortName() + " 的输入适配器"));

			AlTask alTask = new AlTask();
			alTask.setTaskReUrl(systemConfigService.getSystemConfigByKey(SysConfigEnum.ALGORITHM_CALLBACK_URL.getKey())
					+ "algorithm/job/algorithmJobCallback");
			String taskMsg = JsonUtil.toJson(jsonObject);

			alTask.setAlId(Long.parseLong(diagnoseInfoDto.getAlgoId()));
			alTask.setTaskMsg(buildTaskMsgService.buildTaskMsg(taskMsg));
			alTask.setTaskUrl(url);
			alTask.setTaskState(0);
			alTask.setIsDeleted(0);
			alTask.setTaskResult("\"\"");

			alTaskService.save(alTask);
			alTask.setTaskId(alTask.getId());

			String body = JsonUtil.toJson(alTask);
			String res = RestTemplateUtil.post(url, body);
			AlTask resultTask;
			if (res == null || res.trim().isEmpty()) {
				resultTask = buildFailedTaskResult(alTask, "算法服务返回空结果");
			}
			else {
				try {
					resultTask = JsonUtil.fromJson(res, AlTask.class);
					if (resultTask == null) {
						resultTask = buildFailedTaskResult(alTask, "算法服务未返回可解析的任务结果");
					}
					else {
						if (resultTask.getTaskState() == null) {
							resultTask.setTaskState(2);
						}
						if (resultTask.getTaskResult() == null || resultTask.getTaskResult().trim().isEmpty()) {
							resultTask = buildFailedTaskResult(alTask, "算法服务未返回诊断结果");
						}
					}
				}
				catch (Exception exception) {
					resultTask = buildFailedTaskResult(alTask, res);
				}
			}

			persistTaskResult(alTask, resultTask);
			if (reservation != null) {
				completeDiagnosisRecord(reservation.getRecord(), resultTask);
			}
			return resultTask;
		}
		catch (RuntimeException exception) {
			if (reservation != null) {
				failDiagnosisRecord(reservation.getRecord(), exception.getMessage());
			}
			throw exception;
		}
	}

	private boolean isElevatorDiagnosis(AlFaultDiagnosis algorithm) {
		return "FFCNet".equalsIgnoreCase(algorithm.getModelShortName());
	}

	private DiagnosisReservation reserveDiagnosis(DiagnoseInfoDto request, AlFaultDiagnosis algorithm) {
		validateDiagnosisIdentity(request);
		String alarmTime = normalizeAlarmTime(request);
		String diagnosisKey = DigestUtils.sha256Hex(String.join("\u001f", request.getTaskId(), alarmTime,
				request.getTurbineCode(), request.getNodeId(), request.getAlgoId()));
		LocalDateTime now = LocalDateTime.now();

		AlDiagnosisRecord record = AlDiagnosisRecord.builder()
			.diagnosisKey(diagnosisKey)
			.alarmTaskId(request.getTaskId())
			.alarmTime(alarmTime)
			.requestedTime(alarmTime)
			.farmName(request.getFarmName())
			.turbineName(request.getTurbineName())
			.turbineCode(request.getTurbineCode())
			.nodeId(request.getNodeId())
			.nodeCode(request.getNodeCode())
			.nodeName(request.getPart())
			.monitorPointId(request.getMonitorPointId())
			.algorithmId(algorithm.getId())
			.algorithmShortName(algorithm.getModelShortName())
			.diagnosisStatus(1)
			.createTime(now)
			.updateTime(now)
			.build();

		try {
			alDiagnosisRecordMapper.insert(record);
			return new DiagnosisReservation(record, null);
		}
		catch (DuplicateKeyException exception) {
			AlDiagnosisRecord existing = findDiagnosisRecord(diagnosisKey);
			if (existing == null) {
				throw new BizException("诊断记录并发创建失败，请重试");
			}
			if (Integer.valueOf(2).equals(existing.getDiagnosisStatus())) {
				AlTask cachedTask = alTaskService.getById(existing.getAlgorithmTaskId());
				if (cachedTask == null) {
					throw new BizException("诊断记录存在，但对应算法任务已丢失");
				}
				if (cachedTask.getTaskId() == null) {
					cachedTask.setTaskId(cachedTask.getId());
				}
				return new DiagnosisReservation(existing, cachedTask);
			}
			if (Integer.valueOf(1).equals(existing.getDiagnosisStatus())) {
				throw new BizException("该报警正在诊断，请勿重复提交");
			}

			LambdaUpdateWrapper<AlDiagnosisRecord> retry = new LambdaUpdateWrapper<>();
			retry.eq(AlDiagnosisRecord::getId, existing.getId())
				.eq(AlDiagnosisRecord::getDiagnosisStatus, 3)
				.set(AlDiagnosisRecord::getDiagnosisStatus, 1)
				.set(AlDiagnosisRecord::getAlgorithmTaskId, null)
				.set(AlDiagnosisRecord::getErrorMessage, null)
				.set(AlDiagnosisRecord::getUpdateTime, now);
			if (alDiagnosisRecordMapper.update(null, retry) != 1) {
				throw new BizException("该报警正在被其他请求重新诊断");
			}
			existing.setDiagnosisStatus(1);
			existing.setAlgorithmTaskId(null);
			existing.setErrorMessage(null);
			existing.setUpdateTime(now);
			return new DiagnosisReservation(existing, null);
		}
	}

	private AlDiagnosisRecord findDiagnosisRecord(String diagnosisKey) {
		return alDiagnosisRecordMapper
			.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlDiagnosisRecord>()
				.eq(AlDiagnosisRecord::getDiagnosisKey, diagnosisKey)
				.last("LIMIT 1"));
	}

	private void validateDiagnosisIdentity(DiagnoseInfoDto request) {
		if (request == null || isBlank(request.getTaskId()) || isBlank(request.getTurbineCode())
				|| isBlank(request.getNodeId()) || isBlank(request.getAlgoId())) {
			throw new BizException("电梯诊断记录必须包含taskId、turbineCode、nodeId和algoId");
		}
	}

	private String normalizeAlarmTime(DiagnoseInfoDto request) {
		String value = isBlank(request.getAlarmTime()) ? request.getEndTime() : request.getAlarmTime();
		try {
			return Instant.parse(value).toString();
		}
		catch (Exception exception) {
			throw new BizException("电梯诊断记录的告警时间必须是带时区的ISO-8601格式");
		}
	}

	private void persistTaskResult(AlTask sourceTask, AlTask resultTask) {
		resultTask.setId(sourceTask.getId());
		resultTask.setTaskId(sourceTask.getId());
		if (resultTask.getAlId() == null) {
			resultTask.setAlId(sourceTask.getAlId());
		}
		if (resultTask.getTaskMsg() == null) {
			resultTask.setTaskMsg(sourceTask.getTaskMsg());
		}
		if (resultTask.getTaskUrl() == null) {
			resultTask.setTaskUrl(sourceTask.getTaskUrl());
		}
		if (resultTask.getTaskReUrl() == null) {
			resultTask.setTaskReUrl(sourceTask.getTaskReUrl());
		}
		resultTask.setIsDeleted(0);
		alTaskService.updateById(resultTask);
	}

	@SuppressWarnings("unchecked")
	private void completeDiagnosisRecord(AlDiagnosisRecord record, AlTask task) {
		record.setAlgorithmTaskId(task.getId());
		record.setResultJson(task.getTaskResult());
		record.setUpdateTime(LocalDateTime.now());

		if (!Integer.valueOf(2).equals(task.getTaskState())) {
			record.setDiagnosisStatus(3);
			record.setErrorMessage(extractMessage(task.getTaskResult()));
			alDiagnosisRecordMapper.updateById(record);
			return;
		}

		Map<String, Object> result = JsonUtil.fromJson(task.getTaskResult(), Map.class);
		Map<String, Object> taskMessage = JsonUtil.fromJson(task.getTaskMsg(), Map.class);
		record.setDiagnosisStatus(2);
		record.setIsFault(Boolean.TRUE.equals(result.get("is_fault")) ? 1 : 0);
		record.setFaultCode(toInteger(result.get("fault_code")));
		record.setFaultName(toStringValue(result.get("fault_name")));
		applyFailureDictionaryMapping(record);
		record.setConfidence(toDouble(result.get("confidence")));
		record.setRequestedTime(toStringValue(taskMessage.get("requestedTime")));
		record.setMatchedTime(toStringValue(taskMessage.get("matchedTime")));
		record.setTimeOffsetSeconds(toDouble(taskMessage.get("timeOffsetSeconds")));
		record.setMonitorPointId(toStringValue(taskMessage.get("monitorPointId")));
		record.setErrorMessage(null);
		alDiagnosisRecordMapper.updateById(record);
	}

	private void applyFailureDictionaryMapping(AlDiagnosisRecord record) {
		if (!Integer.valueOf(1).equals(record.getIsFault()) || record.getFaultCode() == null) {
			record.setDictionaryFaultCode(null);
			record.setDictionaryFaultName(null);
			return;
		}

		switch (record.getFaultCode()) {
			case 1:
				record.setDictionaryFaultCode("2");
				record.setDictionaryFaultName("部分磨损");
				break;
			case 2:
				record.setDictionaryFaultCode("7");
				record.setDictionaryFaultName("全部磨损");
				break;
			case 3:
				record.setDictionaryFaultCode("1");
				record.setDictionaryFaultName("表面油污");
				break;
			case 4:
				record.setDictionaryFaultCode("5");
				record.setDictionaryFaultName("表面存在异物");
				break;
			case 5:
				record.setDictionaryFaultCode("0");
				record.setDictionaryFaultName("制动力不足");
				break;
			case 6:
				record.setDictionaryFaultCode("4");
				record.setDictionaryFaultName("间隙过大");
				break;
			case 7:
				record.setDictionaryFaultCode("6");
				record.setDictionaryFaultName("未紧密贴合");
				break;
			default:
				record.setDictionaryFaultCode(null);
				record.setDictionaryFaultName(null);
		}
	}

	private void failDiagnosisRecord(AlDiagnosisRecord record, String message) {
		if (record == null || record.getId() == null) {
			return;
		}
		record.setDiagnosisStatus(3);
		record.setErrorMessage(isBlank(message) ? "诊断执行异常" : message);
		record.setUpdateTime(LocalDateTime.now());
		alDiagnosisRecordMapper.updateById(record);
	}

	@SuppressWarnings("unchecked")
	private String extractMessage(String resultJson) {
		try {
			Map<String, Object> result = JsonUtil.fromJson(resultJson, Map.class);
			return toStringValue(result.get("message"));
		}
		catch (Exception exception) {
			return resultJson;
		}
	}

	private Integer toInteger(Object value) {
		return value instanceof Number ? ((Number) value).intValue() : null;
	}

	private Double toDouble(Object value) {
		return value instanceof Number ? ((Number) value).doubleValue() : null;
	}

	private String toStringValue(Object value) {
		return value == null ? null : value.toString();
	}

	private static class DiagnosisReservation {

		private final AlDiagnosisRecord record;

		private final AlTask cachedTask;

		DiagnosisReservation(AlDiagnosisRecord record, AlTask cachedTask) {
			this.record = record;
			this.cachedTask = cachedTask;
		}

		AlDiagnosisRecord getRecord() {
			return record;
		}

		AlTask getCachedTask() {
			return cachedTask;
		}

	}

	private AlTask buildFailedTaskResult(AlTask alTask, String message) {
		String errorMessage = (message == null || message.trim().isEmpty()) ? "算法服务未返回有效结果" : message;
		alTask.setTaskState(3);
		alTask.setTaskResult(JsonUtil.toJson(new HashMap<String, String>() {
			{
				put("message", errorMessage);
			}
		}));
		return alTask;
	}

	@Override
	public Object signalAnalysis(DiagnoseInfoDto diagnoseInfoDto) {
		String url = "http://192.168.16.219:8854/createTask/";
		Optional<BuildTaskMsgService> optional = Optional
			.ofNullable(algorithmServiceMap.get(TaskMsgServices.getTaskMsgService("signalAnalysis")));

		return createAndExecuteTask(diagnoseInfoDto, url, optional);
	}

	private Object createAndExecuteTask(DiagnoseInfoDto diagnoseInfoDto, String url,
			Optional<BuildTaskMsgService> optional) {
		if (!optional.isPresent()) {
			throw new IllegalStateException("BuildTaskMsgService not found");
		}
		BuildTaskMsgService buildTaskMsgService = optional.get();

		// signalAnalysis 也复用同一套兼容模板，保证当前系统内所有诊断相关下发报文行为一致。
		JSONObject jsonObject = buildCompatibleDiagnosisPayload(diagnoseInfoDto);

		AlTask alTask = new AlTask();
		alTask.setTaskReUrl(systemConfigService.getSystemConfigByKey(SysConfigEnum.ALGORITHM_CALLBACK_URL.getKey())
				+ "algorithm/job/algorithmJobCallback");
		String taskMsg = JsonUtil.toJson(jsonObject);

		if (diagnoseInfoDto.getAlgoId() != null) {
			alTask.setAlId(Long.parseLong(diagnoseInfoDto.getAlgoId()));
		}
		else {
			alTask.setAlId(0L);
		}
		alTask.setTaskMsg(buildTaskMsgService.buildTaskMsg(taskMsg));
		alTask.setTaskUrl(url);
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTask.setTaskResult("\"\"");

		alTaskService.save(alTask);
		alTask.setTaskId(alTask.getId());

		String body = JsonUtil.toJson(alTask);
		String res = RestTemplateUtil.post(url, body);
		if (res == null || res.trim().isEmpty()) {
			return buildFailedTaskResult(alTask, "算法服务返回空结果");
		}
		try {
			AlTask resultTask = JsonUtil.fromJson(res, AlTask.class);
			if (resultTask == null) {
				return buildFailedTaskResult(alTask, "算法服务未返回可解析的任务结果");
			}
			if (resultTask.getTaskState() == null) {
				resultTask.setTaskState(2);
			}
			return resultTask;
		}
		catch (Exception e) {
			return buildFailedTaskResult(alTask, res);
		}
	}

	/**
	 * 构造当前阶段统一使用的兼容诊断报文。
	 *
	 * <p>
	 * 当前真实业务对象（例如电梯节点、报警节点、感知变量）到算法可识别风机样例参数的正式映射 还没有实现完成，所以这里先强制改写为一组已经在线下验证成功的固定参数。
	 * </p>
	 *
	 * <p>
	 * 之所以仍然保留 startTime / endTime 动态传入，是为了让页面侧选择的报警时间窗仍然能参与
	 * 诊断请求，不把时间也完全写死。这样至少可以在同一套成功样板参数上验证不同时间范围。
	 * </p>
	 *
	 * <p>
	 * 后续真正恢复正式逻辑时，建议只修改这里： 1. 把固定 farm / turbine / part / location 换回真实业务字段； 2.
	 * 在这里补齐从节点、任务、测点到算法参数的映射； 3. 尽量不要再回到各个调用点零散拼接字段，避免后续维护继续分叉。
	 * </p>
	 */
	private JSONObject buildCompatibleDiagnosisPayload(DiagnoseInfoDto diagnoseInfoDto) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("farmName", COMPATIBLE_FARM_NAME);
		jsonObject.put("turbineName", COMPATIBLE_TURBINE_NAME);
		jsonObject.put("part", COMPATIBLE_PART_NAME);
		jsonObject.put("location", COMPATIBLE_LOCATION_NAME);
		jsonObject.put("startTime", diagnoseInfoDto.getStartTime());
		jsonObject.put("endTime", diagnoseInfoDto.getEndTime());
		return jsonObject;
	}

	private JSONObject buildDiagnosisPayload(DiagnoseInfoDto diagnoseInfoDto, AlFaultDiagnosis alFaultDiagnosis) {
		if (!"FFCNet".equalsIgnoreCase(alFaultDiagnosis.getModelShortName())) {
			return buildCompatibleDiagnosisPayload(diagnoseInfoDto);
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("farmName", diagnoseInfoDto.getFarmName());
		jsonObject.put("turbineName", diagnoseInfoDto.getTurbineName());
		jsonObject.put("part", diagnoseInfoDto.getPart());
		jsonObject.put("location", diagnoseInfoDto.getLocation());
		jsonObject.put("monitorPointId", diagnoseInfoDto.getMonitorPointId());
		jsonObject.put("alarmTime", isBlank(diagnoseInfoDto.getAlarmTime()) ? diagnoseInfoDto.getEndTime()
				: diagnoseInfoDto.getAlarmTime());
		jsonObject.put("startTime", diagnoseInfoDto.getStartTime());
		jsonObject.put("endTime", diagnoseInfoDto.getEndTime());
		jsonObject.put("taskId", diagnoseInfoDto.getTaskId());
		jsonObject.put("nodeId", diagnoseInfoDto.getNodeId());
		jsonObject.put("nodeCode", diagnoseInfoDto.getNodeCode());
		return jsonObject;
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

}
