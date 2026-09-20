package com.algorithm.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDTO {
    private String task_data;
    private Long task_time;
    private Long task_id;
    private Integer task_sequence;
}
