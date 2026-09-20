package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("al_fault_propagation_register3to4")

public class AlFaultPropagationRegister3to4 implements Serializable {

	private static final long serialVersionUID = 1206388297227265522L;

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
