package com.algorithm.web.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ElevatorCmsWaveformVo {

	private Boolean available;

	private String message;

	private String farmName;

	private String turbineName;

	private String nodeName;

	private String monitorPointId;

	private String requestedTime;

	private String matchedTime;

	private Double timeOffsetSeconds;

	private Integer sampleCount;

	private Integer sampleRateHz;

	private Double durationSeconds;

	private List<Double> values;

}
