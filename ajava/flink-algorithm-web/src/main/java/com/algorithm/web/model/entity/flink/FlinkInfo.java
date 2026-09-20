package com.algorithm.web.model.entity.flink;

import lombok.Data;

import java.io.Serializable;

@Data
public class FlinkInfo implements Serializable {

	private static final long serialVersionUID = 2008304341679346823L;

	private Long running;

	private Long finished;

	private Long canceled;

	private Long failed;

	private Long totalSlots;

	private Long availableSlots;

	private Long taskManagers;

}
