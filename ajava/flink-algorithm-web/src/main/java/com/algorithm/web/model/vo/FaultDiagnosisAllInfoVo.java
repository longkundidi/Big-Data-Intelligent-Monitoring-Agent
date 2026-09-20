package com.algorithm.web.model.vo;

import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister1to2;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FaultDiagnosisAllInfoVo {

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

	private LocalDateTime editTime;

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

	private String configUrl;

	private String trainUrl;

	private String pretreatment;

}
