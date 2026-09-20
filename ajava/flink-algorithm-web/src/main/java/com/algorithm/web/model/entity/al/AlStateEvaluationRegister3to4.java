package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("al_state_evaluation_register3to4")

public class AlStateEvaluationRegister3to4 implements Serializable {

	private static final long serialVersionUID = 5520817184614213204L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	private Long id;

	private String modelName;

	private Integer ifPretreatment;

	private String pretreatment;

	private String sampleType;

	private String sampleSize;

	private String indicatorRequire;

	private String thresholdType;

	private String thresholdDescription;

	private String overrunTimes;

}
