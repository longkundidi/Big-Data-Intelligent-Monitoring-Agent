package com.algorithm.web.model.vo.flink;

import com.algorithm.web.model.entity.flink.FlinkInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FlinkOperationsOverviewVo {

	private String clusterStatus;

	private String flinkVersion;

	private String refreshedAt;

	private FlinkInfo cluster;

	private FlinkDataStatsVo dataStats;

	private List<FlinkServiceStatusVo> services;

	private List<FlinkJobStatusVo> otherJobs;

}
