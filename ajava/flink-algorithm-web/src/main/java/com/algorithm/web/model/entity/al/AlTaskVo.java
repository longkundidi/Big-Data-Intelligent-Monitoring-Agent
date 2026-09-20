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
public class AlTaskVo implements Serializable {

	private static final long serialVersionUID = 337361630075002457L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	private Long taskId;

	private Integer taskState;

	private Long alId;

	private String taskMsg;

	private String taskResult;

	@TableField(value = "edit_time", fill = FieldFill.UPDATE)
	private LocalDateTime editTime;

	private Integer isDeleted;

	private String alName;

	private String alType;

	private Long alNum;

	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;

	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;

	private String creator;

	private String editor;

	@TableField(value = "create_time", fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	private String taskUrl;

	@TableField(value = "task_re_url")
	private String taskReUrl;

	private Long isjson;

	private String alClass;

	private String useCase;

	private Integer isService;

	private String serverUrl;

}
