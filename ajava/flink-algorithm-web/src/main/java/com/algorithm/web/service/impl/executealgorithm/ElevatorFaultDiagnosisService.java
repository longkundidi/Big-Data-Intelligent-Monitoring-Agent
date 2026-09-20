package com.algorithm.web.service.impl.executealgorithm;

import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.dto.al.ElevatorCmsWaveformRequest;
import com.algorithm.web.model.dto.influxdb.SensorData;
import com.algorithm.web.model.vo.ElevatorCmsWaveformVo;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.service.al.influxdb.InfluxDBService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;

@Service("ElevatorFaultDiagnosisService")
public class ElevatorFaultDiagnosisService implements BuildTaskMsgService {

	private static final int WINDOW_SIZE = 1024;

	private static final int WINDOW_SECONDS = 30;

	private static final int SAMPLE_RATE_HZ = 45;

	private final InfluxDBService influxDBService;

	@Value("${elevator.diagnosis.max-time-difference-minutes:3}")
	private long maxTimeDifferenceMinutes;

	@Value("${elevator.cms.latest-max-age-seconds:45}")
	private long latestMaxAgeSeconds;

	public ElevatorFaultDiagnosisService(InfluxDBService influxDBService) {
		this.influxDBService = influxDBService;
	}

	@Override
	public String buildTaskMsg(String taskMsgStr) {
		TaskMsg taskMsg = JsonUtil.fromJson(taskMsgStr, TaskMsg.class);
		validateTaskMsg(taskMsg);

		Instant requestedTime = parseRequestedTime(taskMsg);
		Duration tolerance = Duration.ofMinutes(maxTimeDifferenceMinutes);
		List<SensorData> sensorDataList = influxDBService.getElevatorSensorData(taskMsg.getFarmName(),
				taskMsg.getTurbineName(), taskMsg.getPart(), taskMsg.getMonitorPointId(),
				requestedTime.minus(tolerance), requestedTime.plus(tolerance).plusMillis(1));
		SensorData nearest = findNearestSensorData(sensorDataList, requestedTime, tolerance);

		List<Double> values = flattenValues(Collections.singletonList(nearest));
		if (values.size() != WINDOW_SIZE) {
			throw new BizException(String.format("距离报警时间最近的电梯数据应包含1024个采样点，实际为%d个", values.size()));
		}

		Instant matchedTime = nearest.getMpTime();
		long timeOffsetMillis = absoluteDurationMillis(requestedTime, matchedTime);
		taskMsg.setMonitorPointId(nearest.getMonitorPointId());
		taskMsg.setRequestedTime(requestedTime.toString());
		taskMsg.setMatchedTime(matchedTime.toString());
		taskMsg.setTimeOffsetSeconds(timeOffsetMillis / 1000.0d);
		taskMsg.setStartTime(matchedTime.minusSeconds(WINDOW_SECONDS).toString());
		taskMsg.setEndTime(matchedTime.toString());
		taskMsg.setValues(values);
		taskMsg.setSampleCount(WINDOW_SIZE);
		return JsonUtil.toJson(taskMsg);
	}

	public ElevatorCmsWaveformVo queryLatestWaveform(ElevatorCmsWaveformRequest request) {
		validateWaveformRequest(request, false);
		if (latestMaxAgeSeconds <= 0) {
			throw new BizException("CMS实时数据最大时效必须大于0秒");
		}

		Instant requestedTime = Instant.now();
		List<SensorData> sensorDataList = influxDBService.getElevatorSensorData(request.getFarmName(),
				request.getTurbineName(), request.getNodeName(), request.getMonitorPointId(),
				requestedTime.minusSeconds(latestMaxAgeSeconds), requestedTime.plusSeconds(1));
		SensorData latest = sensorDataList == null ? null
				: sensorDataList.stream()
					.filter(sensorData -> sensorData != null && sensorData.getMpTime() != null)
					.max(Comparator.comparing(SensorData::getMpTime))
					.orElse(null);
		if (latest == null) {
			return unavailableWaveform(request, requestedTime, String.format("最近%d秒内没有新的CMS振动数据", latestMaxAgeSeconds));
		}
		return buildWaveform(request, requestedTime, latest);
	}

	public ElevatorCmsWaveformVo queryNearestWaveform(ElevatorCmsWaveformRequest request) {
		validateWaveformRequest(request, true);
		Instant requestedTime = parseTargetTime(request.getTargetTime());
		Duration tolerance = Duration.ofMinutes(maxTimeDifferenceMinutes);
		List<SensorData> sensorDataList = influxDBService.getElevatorSensorData(request.getFarmName(),
				request.getTurbineName(), request.getNodeName(), request.getMonitorPointId(),
				requestedTime.minus(tolerance), requestedTime.plus(tolerance).plusMillis(1));
		try {
			return buildWaveform(request, requestedTime,
					findNearestSensorData(sensorDataList, requestedTime, tolerance));
		}
		catch (BizException exception) {
			return unavailableWaveform(request, requestedTime, exception.getMessage());
		}
	}

	private ElevatorCmsWaveformVo buildWaveform(ElevatorCmsWaveformRequest request, Instant requestedTime,
			SensorData sensorData) {
		List<Double> values = flattenValues(Collections.singletonList(sensorData));
		if (values.size() != WINDOW_SIZE) {
			throw new BizException(String.format("CMS振动数据应包含1024个采样点，实际为%d个", values.size()));
		}
		long timeOffsetMillis = absoluteDurationMillis(requestedTime, sensorData.getMpTime());
		return ElevatorCmsWaveformVo.builder()
			.available(true)
			.message("查询成功")
			.farmName(request.getFarmName())
			.turbineName(request.getTurbineName())
			.nodeName(request.getNodeName())
			.monitorPointId(sensorData.getMonitorPointId())
			.requestedTime(requestedTime.toString())
			.matchedTime(sensorData.getMpTime().toString())
			.timeOffsetSeconds(timeOffsetMillis / 1000.0d)
			.sampleCount(WINDOW_SIZE)
			.sampleRateHz(SAMPLE_RATE_HZ)
			.durationSeconds(WINDOW_SIZE / (double) SAMPLE_RATE_HZ)
			.values(values)
			.build();
	}

	private ElevatorCmsWaveformVo unavailableWaveform(ElevatorCmsWaveformRequest request, Instant requestedTime,
			String message) {
		return ElevatorCmsWaveformVo.builder()
			.available(false)
			.message(message)
			.farmName(request.getFarmName())
			.turbineName(request.getTurbineName())
			.nodeName(request.getNodeName())
			.monitorPointId(request.getMonitorPointId())
			.requestedTime(requestedTime.toString())
			.sampleCount(0)
			.sampleRateHz(SAMPLE_RATE_HZ)
			.values(Collections.emptyList())
			.build();
	}

	private Instant parseTargetTime(String targetTime) {
		try {
			return Instant.parse(targetTime);
		}
		catch (Exception exception) {
			throw new BizException("CMS历史数据查询时间必须是带时区的ISO-8601格式");
		}
	}

	private void validateWaveformRequest(ElevatorCmsWaveformRequest request, boolean requireTargetTime) {
		if (request == null || isBlank(request.getFarmName()) || isBlank(request.getTurbineName())
				|| isBlank(request.getNodeName())) {
			throw new BizException("CMS波形查询必须包含farmName、turbineName和nodeName");
		}
		if (requireTargetTime && isBlank(request.getTargetTime())) {
			throw new BizException("CMS历史波形查询必须包含targetTime");
		}
	}

	private Instant parseRequestedTime(TaskMsg taskMsg) {
		String requestedTime = isBlank(taskMsg.getAlarmTime()) ? taskMsg.getEndTime() : taskMsg.getAlarmTime();
		try {
			return Instant.parse(requestedTime);
		}
		catch (Exception exception) {
			throw new BizException("报警时间必须是带时区的ISO-8601格式");
		}
	}

	SensorData findNearestSensorData(List<SensorData> sensorDataList, Instant requestedTime, Duration tolerance) {
		if (sensorDataList == null || sensorDataList.isEmpty()) {
			throw new BizException(String.format("报警时间前后%d分钟内未找到当前电梯测点数据", maxTimeDifferenceMinutes));
		}
		SensorData nearest = sensorDataList.stream()
			.filter(sensorData -> sensorData != null && sensorData.getMpTime() != null)
			.min(Comparator.comparingLong(sensorData -> absoluteDurationMillis(requestedTime, sensorData.getMpTime())))
			.orElseThrow(() -> new BizException("InfluxDB返回的电梯数据缺少采集时间"));
		long timeOffsetMillis = absoluteDurationMillis(requestedTime, nearest.getMpTime());
		if (timeOffsetMillis > tolerance.toMillis()) {
			throw new BizException(
					String.format("最近电梯数据与报警时间相差%.1f秒，超过%d分钟限制", timeOffsetMillis / 1000.0d, maxTimeDifferenceMinutes));
		}
		return nearest;
	}

	private static long absoluteDurationMillis(Instant first, Instant second) {
		return Math.abs(Duration.between(first, second).toMillis());
	}

	private List<Double> flattenValues(List<SensorData> sensorDataList) {
		List<Double> values = new ArrayList<>(WINDOW_SIZE);
		for (SensorData sensorData : sensorDataList) {
			String raw = sensorData.getMpData();
			if (raw == null || raw.trim().isEmpty()) {
				continue;
			}
			String cleaned = raw.trim().replace("[", "").replace("]", "");
			for (String token : cleaned.split("[,\\s]+")) {
				if (token.isEmpty()) {
					continue;
				}
				double value;
				try {
					value = Double.parseDouble(token);
				}
				catch (NumberFormatException exception) {
					throw new BizException("InfluxDB故障诊断数据包含非数值内容");
				}
				if (!Double.isFinite(value)) {
					throw new BizException("InfluxDB故障诊断数据包含NaN或无穷值");
				}
				values.add(value);
				if (values.size() > WINDOW_SIZE) {
					throw new BizException("所选时间范围超过1024个采样点，请缩小时间范围");
				}
			}
		}
		return values;
	}

	private void validateTaskMsg(TaskMsg taskMsg) {
		if (taskMsg == null || isBlank(taskMsg.getFarmName()) || isBlank(taskMsg.getTurbineName())
				|| isBlank(taskMsg.getPart()) || (isBlank(taskMsg.getAlarmTime()) && isBlank(taskMsg.getEndTime()))) {
			throw new BizException("电梯故障诊断必须包含farmName、turbineName、part和报警时间");
		}
		if (maxTimeDifferenceMinutes <= 0) {
			throw new BizException("电梯故障诊断最大时间差配置必须大于0分钟");
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	static class TaskMsg {

		private String farmName;

		private String turbineName;

		private String part;

		private String location;

		private String taskId;

		private String nodeId;

		private String nodeCode;

		private String monitorPointId;

		private String alarmTime;

		private String startTime;

		private String endTime;

		private Integer sampleCount;

		private List<Double> values;

		private String requestedTime;

		private String matchedTime;

		private Double timeOffsetSeconds;

	}

}
