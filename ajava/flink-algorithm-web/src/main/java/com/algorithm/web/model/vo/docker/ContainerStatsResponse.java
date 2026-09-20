package com.algorithm.web.model.vo.docker;

import lombok.Data;

@Data
public class ContainerStatsResponse {

	private double cpuPercent;

	private double memoryPercent;

	private int cpuTotal;

	private String memoryTotal;

}
