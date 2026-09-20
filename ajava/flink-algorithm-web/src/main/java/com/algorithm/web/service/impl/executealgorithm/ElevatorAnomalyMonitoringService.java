package com.algorithm.web.service.impl.executealgorithm;

import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.dto.influxdb.SensorData;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.service.al.influxdb.InfluxDBService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service("ElevatorAnomalyMonitoringService")
public class ElevatorAnomalyMonitoringService implements BuildTaskMsgService {

	private static final int WINDOW_SIZE = 1024;

	@Autowired
	private InfluxDBService influxDBService;

	@Override
	public String buildTaskMsg(String taskMsgStr) {
		TaskMsg taskMsg = JsonUtil.fromJson(taskMsgStr, TaskMsg.class);
		validateTaskMsg(taskMsg);

		Instant startTime = Instant.parse(taskMsg.getStartTime());
		Instant endTime = Instant.parse(taskMsg.getEndTime());
		if (!startTime.isBefore(endTime)) {
			throw new BizException("startTime必须早于endTime");
		}

		List<SensorData> sensorDataList = influxDBService.getElevatorSensorData(taskMsg.getMonitorPointId(), startTime,
				endTime);
		sensorDataList
			.sort(Comparator.comparing(SensorData::getMpTime, Comparator.nullsLast(Comparator.naturalOrder())));

		List<Double> values = sensorDataList.isEmpty() ? new ArrayList<>()
				: flattenValues(sensorDataList.subList(sensorDataList.size() - 1, sensorDataList.size()));
		if (values.size() != WINDOW_SIZE) {
			throw new BizException(String.format("监测算法需要1024个采样点，当前时间范围读取到%d个", values.size()));
		}

		taskMsg.setValues(values);
		taskMsg.setSampleCount(WINDOW_SIZE);
		return JsonUtil.toJson(taskMsg);
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
					throw new BizException("InfluxDB监测数据包含非数值内容");
				}
				if (!Double.isFinite(value)) {
					throw new BizException("InfluxDB监测数据包含NaN或无穷值");
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
		if (taskMsg == null || isBlank(taskMsg.getMonitorPointId()) || isBlank(taskMsg.getStartTime())
				|| isBlank(taskMsg.getEndTime())) {
			throw new BizException("taskMsg必须包含monitorPointId、startTime和endTime");
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

		private String monitorPointId;

		private String startTime;

		private String endTime;

		private Integer sampleCount;

		private List<Double> values;

	}

}
