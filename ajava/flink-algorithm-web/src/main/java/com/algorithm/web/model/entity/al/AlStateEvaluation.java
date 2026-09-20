package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("al_state_evaluation")

public class AlStateEvaluation implements Serializable {

	private static final long serialVersionUID = 1178645928543232800L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)

	private Long id;

	private String alCode;

	private String modelTypeFirst;

	private String modelName;

	private String modelShortName;

	/** Execution adapter selected independently from the display name and short name. */
	private String executorType;

	/** Reserved JSON configuration for configurable execution adapters. */
	private String executorConfig;

	private String modelIcon;

	private String programUrl;

	private String modelUrl;

	private String modelProvider;

	private String modelObject;

	private String objectId;

	private String modelType;

	private String modelInvoke;

	private String modelFunction;

	private String modelLibrary;

	private String modelCondition;

	private String modelAdvantage;

	private String modelDisadvantage;

	private String deployInput;

	private String deployOutput;

	private String deployRequire;

	@TableField(value = "edit_time", fill = FieldFill.UPDATE)
	private LocalDateTime editTime;

	@TableField(value = "create_time", fill = FieldFill.INSERT)
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	private String creator;

	private String editor;

	private Long isCheck;

	private Long isPass;

	private Long isDeployed;

	private Integer isService;

	private Long modelNum;

	private String input;

	private String output;

	private Long isjson;

	private String userCode;

	private String backupModelUrl;

	private String jarSize;

	private String runSize;

	private String configUrl;

	private String trainUrl;

}
