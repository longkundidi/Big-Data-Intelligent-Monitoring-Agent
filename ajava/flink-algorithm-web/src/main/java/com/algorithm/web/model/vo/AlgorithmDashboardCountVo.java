package com.algorithm.web.model.vo;

import lombok.Data;

@Data
public class AlgorithmDashboardCountVo {

	private Long total;

	private Long pendingReview;

	private Long pendingTest;

	private Long testFailed;

	private Long pendingDeploy;

	private Long deployed;

}
