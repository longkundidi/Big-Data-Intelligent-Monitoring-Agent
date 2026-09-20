package com.algorithm.web.model.dto.al;

import lombok.Data;

@Data
public class ElevatorCmsWaveformRequest {

	private String farmName;

	private String turbineName;

	private String nodeName;

	private String monitorPointId;

	private String targetTime;

}
