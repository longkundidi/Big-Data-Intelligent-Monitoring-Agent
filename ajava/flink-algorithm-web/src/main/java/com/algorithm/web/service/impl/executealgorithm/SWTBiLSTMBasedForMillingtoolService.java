package com.algorithm.web.service.impl.executealgorithm;

import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.model.dto.influxdb.SensorData;
import com.algorithm.web.service.al.MinioService;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.service.al.influxdb.InfluxDBService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service("SWTBiLSTMBasedForMillingtoolService")
@Slf4j
public class SWTBiLSTMBasedForMillingtoolService implements BuildTaskMsgService {

	@Autowired
	private InfluxDBService influxDBService;

	@Autowired
	private MinioService minioService;

	@Override
	public String buildTaskMsg(String taskMsgStr) {
		TaskMsg taskMsg = JsonUtil.fromJson(taskMsgStr, TaskMsg.class);
		Instant startTime = Instant.parse(taskMsg.getStartTime());
		Instant endTime = Instant.parse(taskMsg.getEndTime());
		String monitorPointId = taskMsg.getMonitorPointId();
		// String path = PathUtil.buildPath(endTime, "history", monitorPointId + "_" +
		// endTime + ".csv");
		String path = taskMsg.getFilePath();

		if (!minioService.objectExist(path)) {
			List<SensorData> sensorDataList = influxDBService.getSensorData(monitorPointId, startTime, endTime);
			StringBuilder stringBuilder = new StringBuilder();
			for (SensorData s : sensorDataList) {
				stringBuilder.append(s.getMpData()).append("\n");
			}
			byte[] bytes = stringBuilder.toString().getBytes();
			InputStream stream = new ByteArrayInputStream(bytes);
			minioService.upload(stream, path, (long) bytes.length);
		}
		taskMsg.setFilePath(path);
		return JsonUtil.toJson(taskMsg);
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	static class TaskMsg {

		String monitorPointId;

		String startTime;

		String endTime;

		String filePath;

	}

}
