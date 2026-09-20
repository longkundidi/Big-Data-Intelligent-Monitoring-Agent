package com.algorithm.web.model.entity.al;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuditsManagement implements Serializable {

	private static final long serialVersionUID = -2327179622526133085L;

	private Long algorithmId;

	private String sourceType;

	private String alModelName;

	private String alModelType;

	private String modelFunction;

	private String programUrl;

	private String alModelUrl;

	private String createTime;

	private Long isDeployed;

	private Long isPass;

	private String creator;

	private Long isCheck;

	private String action;

}
