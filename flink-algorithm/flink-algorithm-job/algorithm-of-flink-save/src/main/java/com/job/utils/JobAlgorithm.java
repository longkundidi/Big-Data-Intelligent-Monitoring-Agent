package com.job.utils;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "job_algorithm")
public class JobAlgorithm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;

    private String jobId;

    private LocalDateTime startTime;

    private String duration;

    private String status;

    private String inputTopic;

    private String outputTopic;

    private String creator;
}
