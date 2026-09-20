package com.algorithm.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorOriginalDTO {

    @JsonProperty(value = "monitor_point_id")
    private String monitorPointId;

    @JsonProperty(value = "mp_time")
    private Long mpTime;

    @JsonProperty(value = "mp_data")
    private String mpData;

    @JsonProperty(value = "mp_type")
    private String mpType;



    //8.2适应需求修改
    @JsonProperty(value = "algorithm_name")
    private String algorithmName;

    @JsonProperty(value = "server_id")
    private String serverId;

    @JsonProperty(value = "task_id")
    private String taskId;
}
