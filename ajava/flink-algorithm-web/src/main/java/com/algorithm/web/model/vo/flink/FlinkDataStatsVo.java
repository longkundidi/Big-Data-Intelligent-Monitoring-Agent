package com.algorithm.web.model.vo.flink;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlinkDataStatsVo {

	private Long taskId;

	private Long processedLastHour;

	private Long alarmsLastSevenDays;

	private String latestResultTime;

}
