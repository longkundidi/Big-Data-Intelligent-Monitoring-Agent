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
package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域模型组态管理表
 *
 * @author pig code generator
 * @date 2025-04-29 10:28:19
 */
@Data
@TableName("algorithm_configuration")
@Schema(description = "算法组态管理表")
public class AlgorithmConfiguration implements Serializable {

	private static final long serialVersionUID = 9163947737730502999L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.AUTO)
	@Schema(description = "主键")
	private Long id;

	/**
	 * 编码
	 */
	@Schema(description = "编码")
	private String code;

	/**
	 * 组态名称
	 */
	@Schema(description = "组态名称")
	private String modelName;

	/**
	 * 组态类型
	 */
	@Schema(description = "组态类型")
	private String modelType;

	/**
	 * 任务执行次序
	 */
	@Schema(description = "任务执行次序")
	private Integer sequence;

	/**
	 * 算法类型
	 */
	@Schema(description = "算法类型")
	private String almodelType;

	/**
	 * 算法名称
	 */
	@Schema(description = "算法名称")
	private String almodelName;

	/**
	 * 算法简称
	 */
	@Schema(description = "算法简称")
	private String almodelShortName;

	/**
	 * 针对对象
	 */
	@Schema(description = "针对对象")
	private String modelObject;

	/**
	 * 算法所针对的部套件GBom节点ID
	 */
	@Schema(description = "算法所针对的部套件GBom节点ID")
	private String objectId;

	/**
	 * 预处理流程
	 */
	@Schema(description = "预处理流程")
	private String pretreatment;

	/**
	 * 训练结果
	 */
	@Schema(description = "训练结果")
	private String trainResult;

	/**
	 * 创建时间
	 */
	@Schema(description = "创建时间")
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

	/**
	 * 是否通过测试（0是1已测试未通过2未测试）
	 */
	@Schema(description = "是否通过测试（0是1已测试未通过2未测试）")
	private Long isPass;

	/**
	 * 是否部署（0是1否）
	 */
	@Schema(description = "是否部署（0是1否）")
	private Long isDeployed;

	/**
	 * 是否发布（0是1否）
	 */
	@Schema(description = "是否发布（0是1否）")
	private Long isPublished;

	/**
	 * 被引用次数
	 */
	@Schema(description = "被引用次数")
	private Long modelNum;

	/**
	 * 是否对外服务（0是1否)
	 */
	@Schema(description = "是否对外服务（0是1否)")
	private Integer isService;

	/**
	 * 组态流程图
	 */
	@Schema(description = "组态流程图")
	private String almodelIcon;

	@Schema(description = "训练次数")
	private String trainTimes;

	@Schema(description = "训练批次")
	private String trainBatch;

	@Schema(description = "学习率")
	private String learningRate;

	@Schema(description = "优化器")
	private String optimizer;

	@Schema(description = "运行路径")
	private String configUrl;

	@Schema(description = "小波变换最大分解层数")
	private String maxLevel;

	@Schema(description = "小波变换切片大小")
	private String sliceLength;

	@Schema(description = "小波变换模式")
	private String waveMode;

	@Schema(description = "小波基类型")
	private String waveLet;

	@Schema(description = "提取层级")
	private String n;

}
