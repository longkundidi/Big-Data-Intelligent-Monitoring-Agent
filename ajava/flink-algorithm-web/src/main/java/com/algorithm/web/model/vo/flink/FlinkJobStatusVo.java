package com.algorithm.web.model.vo.flink;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlinkJobStatusVo {

	private String jobId;

	private String jobName;

	private String state;

	private String startTime;

	private Long durationMillis;

}
