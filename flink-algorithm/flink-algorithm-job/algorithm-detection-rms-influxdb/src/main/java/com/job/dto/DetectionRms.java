package com.job.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

//RMS故障报警
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetectionRms {

    @JsonProperty(value = "rms_hi")
    private Double rmsHi;

    @JsonProperty(value = "threshold")
    private Float threshold;

    @JsonProperty(value = "anomaly_flag")
    private Integer anomalyFlag;

}
