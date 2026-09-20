package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("al_maint_decision")
public class AlMaintDecision implements Serializable {

	private static final long serialVersionUID = 337361630075002452L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)

	private Long id;

	private String alCode;

	private String modelTypeFirst;

	private String modelName;

	private String modelShortName;

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

	private Long modelNum;

	private String input;

	private String output;

	private Long isjson;

	private Long isCheck;

	private Long isPass;

	private Long isDeployed;

	private Integer isService;

	private String userCode;

	private String backupModelUrl;

	private String jarSize;

	private String runSize;

}
