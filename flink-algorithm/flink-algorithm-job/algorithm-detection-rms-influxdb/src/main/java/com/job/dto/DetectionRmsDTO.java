package com.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetectionRmsDTO {

    @JsonProperty(value = "monitor_point_id")
    private String monitorPointId;

    @JsonProperty(value = "server_id")
    private String serverId;

    @JsonProperty(value = "mp_time")
    private Long mpTime;

    @JsonProperty(value = "mp_data")
    private DetectionRms mpData;

}
