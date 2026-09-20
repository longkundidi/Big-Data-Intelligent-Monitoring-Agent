package com.job.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SensorDTO {

    @JsonProperty(value = "monitor_point_id")
    private String monitorPointId;

    @JsonProperty(value = "mp_time")
    private Long mpTime;

    @JsonProperty(value = "mp_data")
    private String mpData;

}
