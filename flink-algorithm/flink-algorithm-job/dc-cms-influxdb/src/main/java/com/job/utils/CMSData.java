package com.job.utils;

import lombok.Data;

@Data
public class CMSData {
    private String dataFloat;
    private long acquisitionTime;
    private long sampleRate; // 单位是Hz
    private String farmName;
    private String turbineName;
    private String location;
}
