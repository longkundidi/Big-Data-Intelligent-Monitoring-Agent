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
@TableName("al_data_clean")
public class AlDataClean implements Serializable {

	private static final long serialVersionUID = 337361630075002456L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Integer id;

	private String alCode;

	private String alName;

	private String alShortName;

	private String alType;

	private String alBrief;

	private String alSuit;

	private String iconUrl;

	private String alUrl;

	@TableField(value = "create_time", fill = FieldFill.INSERT)
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@TableField(value = "edit_time", fill = FieldFill.UPDATE)
	private LocalDateTime editTime;

	private Integer isDeleted;

	private String creator;

	private String editor;

	private String jobName;

	private String input;

	private String output;

	private Long alNum;

	private Long isPass;

	private Long isDeployed;

	private String programUrl;

	private String deployInput;

	private String deployOutput;

	private String deployRequire;

	private String invocation;

	private Long isCheck;

	private Integer isService;

	private String backupAlUrl;

	private String jarSize;

	private String runSize;

	private String configUrl;

}
