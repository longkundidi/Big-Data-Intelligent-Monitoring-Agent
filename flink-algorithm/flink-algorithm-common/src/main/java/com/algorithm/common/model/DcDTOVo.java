package com.algorithm.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DcDTOVo {
    private String dc_data;
    private String dc_time;
    private Long id;
}
