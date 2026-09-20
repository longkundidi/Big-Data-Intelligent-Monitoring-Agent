package com.algorithm.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DcDTO {
    private String dc_data;
    private Long dc_time;
    private Long id;
}
