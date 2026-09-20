package com.algorithm.web.model.entity.taskConfigResult;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * (TaskConfigResult)表实体类
 *
 * @author makejava
 * @since 2024-07-24 11:50:29
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("task_config_result")
public class TaskConfigResult implements Serializable {

	private static final long serialVersionUID = 5273543465307948112L;

	// 主键
	@TableId
	private String id;

	// 任务id
	private Long taskId;

	// 任务执行结果
	private String taskResult;

	// 任务执行时间
	private LocalDateTime taskTime;

}
