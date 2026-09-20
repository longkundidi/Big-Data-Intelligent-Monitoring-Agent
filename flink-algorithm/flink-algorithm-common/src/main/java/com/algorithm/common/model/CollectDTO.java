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
public class CollectDTO {

    @JsonProperty(value = "collect_id")
    private String collectId;

    @JsonProperty(value = "collect_time")
    private Long collectTime;

    @JsonProperty(value = "collect_data")
    private String collectData;

}
