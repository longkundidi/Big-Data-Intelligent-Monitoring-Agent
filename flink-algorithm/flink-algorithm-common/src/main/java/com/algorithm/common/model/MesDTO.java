package com.algorithm.common.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MesDTO {
    @JsonProperty("time")
    private Long time;

    @JsonProperty("property")
    private List<PropertyItem> property;
}

