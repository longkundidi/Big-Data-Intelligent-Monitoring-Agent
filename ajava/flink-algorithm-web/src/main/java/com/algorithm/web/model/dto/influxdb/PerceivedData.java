package com.algorithm.web.model.dto.influxdb;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PerceivedData {

	@JsonProperty(value = "measurement")
	private String measurement;

	@JsonProperty(value = "device")
	private String device;

	@JsonProperty(value = "variable")
	private String variable;

	@JsonProperty(value = "mp_time")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private Instant mpTime;

	@JsonProperty(value = "mp_data")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private String mpData;

}
