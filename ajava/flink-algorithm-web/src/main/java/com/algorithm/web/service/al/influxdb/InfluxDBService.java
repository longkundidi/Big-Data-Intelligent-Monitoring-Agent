package com.algorithm.web.service.al.influxdb;

import com.algorithm.web.model.dto.influxdb.PerceivedData;
import com.algorithm.web.model.dto.influxdb.SensorData;

import java.time.Instant;
import java.util.List;

public interface InfluxDBService {

	/**
	 * 查询传感器原始数据-
	 * @param monitorPointId 监控点ID
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @param limit 限制条数
	 * @return 监控点原始数据
	 */
	List<SensorData> getSensorData(String monitorPointId, Instant startTime, Instant endTime, Integer limit);

	/**
	 * 查询传感器原始数据-
	 * @param monitorPointId 监控点ID
	 * @param startTime 开始时间
	 * @param endTime 结束时间
	 * @return 监控点原始数据
	 */
	List<SensorData> getSensorData(String monitorPointId, Instant startTime, Instant endTime);

	/**
	 * 按电梯实例和 GBOM 测点查询一条记录包含一个完整采样窗口的数据。
	 */
	List<SensorData> getElevatorSensorData(String farmName, String turbineName, String nodeName, String monitorPointId,
			Instant startTime, Instant endTime);

	List<SensorData> getElevatorSensorData(String monitorPointId, Instant startTime, Instant endTime);

	List<PerceivedData> getPerceivedData(String measurement, String device, Instant startTime, Instant endTime,
			Integer limit);

}
