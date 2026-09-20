package com.algorithm.web.model.vo.flink;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FlinkServiceStatusVo {

	private String serviceKey;

	private String displayName;

	private String componentType;

	private String sourceType;

	private String description;

	private String inputName;

	private String outputName;

	private String healthStatus;

	private String state;

	private Integer runningInstances;

	private Integer expectedInstances;

	private String detail;

	private List<FlinkJobStatusVo> jobs;

}
