package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("al_condition_category_register1to2")
public class AlConditionCategoryRegister1to2 implements Serializable {

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

}
