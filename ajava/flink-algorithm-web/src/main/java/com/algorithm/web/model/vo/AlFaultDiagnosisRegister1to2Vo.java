package com.algorithm.web.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("al_fault_diagnosis_register1to2")

public class AlFaultDiagnosisRegister1to2Vo implements Serializable {

	private static final long serialVersionUID = 337361630075002451L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	private String modelName;

	private String modelFramework;

	private String device;

	private Integer ifTrain;

	private Integer ifTest;

	private String trainDataset;

	private String testDataset;

	private Integer ifPretreatment;

	private String pretreatment;

	private String sampleType;

	private String sampleSize;

	private String trainTimes;

	private String trainBatch;

	private String lossFunction;

	private String regularization;

	private String learningRate;

	private String optimizer;

	private String trainResults;

	private String testResults;

	private String metrics;

	private String modelUrl;

	private String jarSize;

	private String runSize;

}
