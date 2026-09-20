package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("al_diagnosis_record")
public class AlDiagnosisRecord {

	@TableId(type = IdType.AUTO)
	private Long id;

	private String diagnosisKey;

	private String alarmTaskId;

	private String alarmTime;

	private String requestedTime;

	private String matchedTime;

	private Double timeOffsetSeconds;

	private String farmName;

	private String turbineName;

	private String turbineCode;

	private String nodeId;

	private String nodeCode;

	private String nodeName;

	private String monitorPointId;

	private Long algorithmId;

	private String algorithmShortName;

	private Long algorithmTaskId;

	private Integer diagnosisStatus;

	private Integer isFault;

	private Integer faultCode;

	private String faultName;

	private String dictionaryFaultCode;

	private String dictionaryFaultName;

	private Double confidence;

	private String resultJson;

	private String errorMessage;

	private LocalDateTime createTime;

	private LocalDateTime updateTime;

}
