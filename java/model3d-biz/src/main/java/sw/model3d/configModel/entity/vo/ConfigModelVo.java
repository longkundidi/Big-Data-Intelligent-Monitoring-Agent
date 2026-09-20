/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */
package sw.model3d.configModel.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author pig code generator
 * @date 2024-04-01 10:43:50
 */
@Data
public class ConfigModelVo {

	/*
	 * 任务id
	 */

	private Long taskId;

	/**
	 * 模型id
	 */

	private String modelId;

	/**
	 * 模型名称
	 */

	private String modelName;

	// 调用算法简称
	private String algoShortname;

	/**
	 * 已关联的感知变量数目
	 */
	private Long variableNum;

	/**
	 * 单次模型输入的数据维度
	 */
	private Long dataDimension;

	/**
	 * 关联变量
	 */

	private String relatedVar;

	/**
	 * 时间窗口
	 */

	private LocalDateTime timeWindow;

	/**
	 * 模型类别
	 */

	private String modelType;

	/**
	 * 服务算法id
	 */

	private String algoId;

	/**
	 * 服务类型，1：故障报警:2：故障预警，3：加速退化提示
	 */

	private String serviceType;

	/**
	 * 评估方法，1：状态阈值评估；2：服务阶段评估
	 */

	private String assessType;

	/**
	 * 指标要求,1：原始信号；2：单统计学指标；3：多统计学指标
	 */

	private String indexRequirement;

	/**
	 * 评估指标，1：原始残差；2：自编码器残差；3：BiLSTM残差
	 */

	private String assessIndex;

	/**
	 * 阈值类型，1：专家阈值，2：数据阈值
	 */

	private String thresholdType;

	/**
	 * 连续超限次数
	 */

	private Integer limitNumber;

	/**
	 * 预测故障失效判据, 0: 温度残差超过80
	 */

	private String failureCriterion;

	/**
	 * 趋势预测算法, 0: BLSTM
	 */

	private String trendPrediction;

	/**
	 * 预测结果连续超限次数
	 */

	private Integer resultLimitNum;

	/**
	 * 服务简介
	 */

	private String intro;

	/**
	 * 所属表config_gbom_tree的节点id
	 */

	private String nodeId;

	// 是否是默认模板
	private Integer isDefaultModel;

	// 是否执行中
	private Integer status;

}
