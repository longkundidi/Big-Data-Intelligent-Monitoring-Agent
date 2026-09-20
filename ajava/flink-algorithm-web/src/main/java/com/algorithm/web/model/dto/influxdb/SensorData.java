package com.algorithm.web.model.dto.influxdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SensorData {

	@JsonProperty(value = "monitor_point_id")
	private String monitorPointId;

	@JsonProperty(value = "mp_time")
	private Instant mpTime;

	@JsonProperty(value = "mp_data")
	private String mpData;

}
