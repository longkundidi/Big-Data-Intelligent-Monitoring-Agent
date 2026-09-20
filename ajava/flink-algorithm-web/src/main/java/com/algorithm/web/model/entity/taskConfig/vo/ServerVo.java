package com.algorithm.web.model.entity.taskConfig.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ServerVo implements Serializable {

	private static final long serialVersionUID = -3560941903065153098L;

	private Integer id;

	// 服务器名称
	private String name;

	// 服务器地址
	private String url;

	private Double totalMemory;

	private Double freeMemory;

	private Double memoryUsage;

	private Double cpuUsage;

	private Boolean isRunning;

	private Long microservicesCount;

}
