package com.algorithm.web.model.entity.al;

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
@TableName("al_task")
public class AlTask implements Serializable {

	private static final long serialVersionUID = 337361630075002457L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	/**
	 * 任务编号
	 */
	private Long taskId;

	/**
	 * 任务状态：0未执行，1执行中，2已结束，3执行失败
	 */
	private Integer taskState;

	/**
	 * 算法id
	 */
	private Long alId;

	/**
	 * 任务信息
	 */
	private String taskMsg;

	/**
	 * 任务结果
	 */
	private String taskResult;

	@TableField(value = "edit_time", fill = FieldFill.UPDATE)
	private LocalDateTime editTime;

	private Integer isDeleted;

	@TableField(exist = false)
	private String alName;

	@TableField(exist = false)
	private String alType;

	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;

	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;

	private String creator;

	private String editor;

	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 算法调用路径
	 */
	private String taskUrl;

	/**
	 * 算法回调路径
	 */
	@TableField(value = "task_re_url")
	private String taskReUrl;

	private Long isjson;

	/**
	 * 算法类别
	 */
	private String alClass;

	/**
	 * 使用场景
	 */
	private String useCase;

	/**
	 * 使用的服务器地址
	 */
	private String serverUrl;

}
