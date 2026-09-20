package com.algorithm.web.model.entity.taskConfig;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author www.javacoder.top
 * @since 2024-07-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("task_config")
public class TaskConfig implements Serializable {

	private static final long serialVersionUID = -4651797283718797831L;

	/**
	 * 任务id
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 任务id
	 */
	private Long taskId;

	/**
	 * 任务执行次序
	 */
	private Integer sequence;

	/**
	 * 算法类型
	 */
	private String alType;

	/**
	 * 算法名称
	 */
	private String alName;

	/**
	 * 算法简称
	 */
	private String alShortName;

}
