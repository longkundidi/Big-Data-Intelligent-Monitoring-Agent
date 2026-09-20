package com.algorithm.web.model.entity.flink;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("job_algorithm")
public class FlinkAlgorithm implements Serializable {

	private static final long serialVersionUID = 5012595695001870882L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	private String jobName;

	private String jobId;

	@TableField(value = "start_time", fill = FieldFill.INSERT)
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;

	private String duration;

	private String status;

	private String inputTopic;

	private String outputTopic;

}
