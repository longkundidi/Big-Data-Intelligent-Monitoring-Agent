package com.algorithm.web.model.entity.taskConfig.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskVo implements Serializable {

	private static final long serialVersionUID = -6540058754982717753L;

	private Long taskId;

	private String taskProcess;

}
