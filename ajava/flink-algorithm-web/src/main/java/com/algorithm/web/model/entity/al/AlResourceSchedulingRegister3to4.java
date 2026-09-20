package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("al_resource_scheduling_register3to4")

public class AlResourceSchedulingRegister3to4 implements Serializable {

	private static final long serialVersionUID = 138187976248819273L;

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
