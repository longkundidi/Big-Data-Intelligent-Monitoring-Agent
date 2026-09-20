package com.algorithm.web.service.impl.al.influxdb;

import com.algorithm.web.model.dto.influxdb.PerceivedData;
import com.algorithm.web.model.dto.influxdb.SensorData;
import com.algorithm.web.service.al.influxdb.InfluxDBService;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.influxdb.query.dsl.Flux;
import com.influxdb.query.dsl.functions.restriction.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InfluxDBServiceImpl implements InfluxDBService {

	@Autowired
	private InfluxDBClient influxDBClient;

	@Value("${influxdb.bucket}")
	private String bucket;

	@Override
	public List<SensorData> getSensorData(String monitorPointId, Instant startTime, Instant endTime, Integer limit) {

		Restrictions restrictions = Restrictions.and(Restrictions.measurement().equal("sensor_data"),
				Restrictions.tag("monitor_point_id").equal(monitorPointId), Restrictions.value().exists());

		Flux flux = Flux.from(bucket).range(startTime, endTime).filter(restrictions);

		if (limit != null) {
			flux = flux.limit(limit);
		}

		QueryApi queryApi = influxDBClient.getQueryApi();

		List<FluxTable> tables = queryApi.query(flux.toString());

		List<SensorData> sensorDataList = new ArrayList<>();

		for (FluxTable fluxTable : tables) {
			List<FluxRecord> records = fluxTable.getRecords();
			for (FluxRecord fluxRecord : records) {
				Optional<Object> field = Optional.ofNullable(fluxRecord.getValueByKey("_field"));
				Optional<Object> value = Optional.ofNullable(fluxRecord.getValueByKey("_value"));
				if (field.isPresent() && value.isPresent()) {
					if (field.get().toString().startsWith("mp_data")) {
						sensorDataList.add(SensorData.builder()
							.monitorPointId(monitorPointId)
							.mpTime(fluxRecord.getTime())
							.mpData(value.get().toString())
							.build());
					}
				}

			}
		}

		return sensorDataList;
	}

	@Override
	public List<SensorData> getSensorData(String monitorPointId, Instant startTime, Instant endTime) {
		return getSensorData(monitorPointId, startTime, endTime, null);
	}

	@Override
	public List<SensorData> getElevatorSensorData(String monitorPointId, Instant startTime, Instant endTime) {
		Restrictions restrictions = Restrictions.and(Restrictions.measurement().equal("智慧家园小区"),
				Restrictions.tag("monitor_point_id").equal(monitorPointId), Restrictions.value().exists());
		return queryElevatorSensorData(restrictions, monitorPointId, startTime, endTime);
	}

	@Override
	public List<SensorData> getElevatorSensorData(String farmName, String turbineName, String nodeName,
			String monitorPointId, Instant startTime, Instant endTime) {
		Restrictions restrictions = Restrictions.and(Restrictions.measurement().equal(farmName),
				Restrictions.tag("elevator_instance").equal(turbineName),
				Restrictions.tag("gbom_level_3").equal(nodeName), Restrictions.value().exists());
		if (monitorPointId != null && !monitorPointId.trim().isEmpty()) {
			restrictions = Restrictions.and(restrictions, Restrictions.tag("monitor_point_id").equal(monitorPointId));
		}

		return queryElevatorSensorData(restrictions, monitorPointId, startTime, endTime);
	}

	private List<SensorData> queryElevatorSensorData(Restrictions restrictions, String monitorPointId,
			Instant startTime, Instant endTime) {
		Flux flux = Flux.from(bucket).range(startTime, endTime).filter(restrictions);
		List<FluxTable> tables = influxDBClient.getQueryApi().query(flux.toString());
		List<SensorData> sensorDataList = new ArrayList<>();

		for (FluxTable fluxTable : tables) {
			for (FluxRecord fluxRecord : fluxTable.getRecords()) {
				Object value = fluxRecord.getValueByKey("_value");
				if (value == null || fluxRecord.getTime() == null) {
					continue;
				}
				Object pointIdValue = fluxRecord.getValueByKey("monitor_point_id");
				sensorDataList.add(SensorData.builder()
					.monitorPointId(pointIdValue == null ? monitorPointId : pointIdValue.toString())
					.mpTime(fluxRecord.getTime())
					.mpData(value.toString())
					.build());
			}
		}
		return sensorDataList;
	}

	@Override
	public List<PerceivedData> getPerceivedData(String measurement, String device, Instant startTime, Instant endTime,
			Integer limit) {

		Restrictions restrictions = Restrictions.and(Restrictions.measurement().equal(measurement),
				Restrictions.tag("device").equal(device), Restrictions.value().exists());

		Flux flux = Flux.from(bucket).range(startTime, endTime).filter(restrictions);

		if (limit != null) {
			flux = flux.limit(limit);
		}

		QueryApi queryApi = influxDBClient.getQueryApi();

		List<FluxTable> tables = queryApi.query(flux.toString());

		List<PerceivedData> perceivedData = new ArrayList<>();

		for (FluxTable fluxTable : tables) {
			List<FluxRecord> records = fluxTable.getRecords();
			for (FluxRecord fluxRecord : records) {
				Optional<Object> field = Optional.ofNullable(fluxRecord.getValueByKey("_field"));
				Optional<Object> value = Optional.ofNullable(fluxRecord.getValueByKey("_value"));
				if (field.isPresent() && value.isPresent()) {
					perceivedData.add(PerceivedData.builder()
						.measurement(measurement)
						.device(device)
						.variable(field.get().toString())
						.mpTime(fluxRecord.getTime())
						.mpData(value.get().toString())
						.build());
				}

			}
		}

		return perceivedData;
	}

}
