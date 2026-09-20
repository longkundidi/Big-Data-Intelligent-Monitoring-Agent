package com.algorithm.web.model.entity.taskConfig.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TaskDto implements Serializable {

	private static final long serialVersionUID = -7030192742770039578L;

	/**
	 * 任务执行次序
	 */
	private Integer sequence;

	/**
	 * 算法类型
	 */
	private String alType;

	/**
	 * 算法名称
	 */
	private String alName;

}
