package com.algorithm.web.model.dto.al.Configuration;

import cn.hutool.core.date.DateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupedConfigDTO {

	private String code;

	private String modelName;

	private String modelType;

	private String modelObject;

	private String objectId;

	private String modelIcon;

	private Integer isService;

	private Long isDeployed;

	private Long isPublished;

	private String trainMetrics;

	private String pretreatment;

	private String trainEpoch;

	private String trainBatch;

	private String learningRate;

	private String optimizer;

	private String maxLevel;

	private String sliceLength;

	private String waveMode;

	private String waveLet;

	private String n;

	private List<TaskProcessDTO> taskProcess;

	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	@Data
	public static class TaskProcessDTO {

		private String almodelName;

		private String almodelType;

		private String almodelShortName;

		private String configUrl;

		private Integer sequence;

		private String trainResult;

	}

}