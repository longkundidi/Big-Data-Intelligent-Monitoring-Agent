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
package sw.model3d.configPerceivedVariable.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author pig code generator
 * @date 2024-03-22 19:24:18
 */
@Data
@TableName("config_perceived_variable")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "")
public class ConfigPerceivedVariable extends Model<ConfigPerceivedVariable> {

	/**
	 * 变量Id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "变量Id")
	private String varId;

	/**
	 * 变量名称
	 */
	@Schema(description = "变量名称")
	private String varName;

	/**
	 * 变量类型
	 */
	@Schema(description = "变量类型")
	private String variableType;

	/**
	 * 数据类型
	 */
	@Schema(description = "数据类型")
	private String dataType;

	/**
	 * 数据单位
	 */
	@Schema(description = "数据单位")
	private String dimension;

	/**
	 * 采集频率
	 */
	@Schema(description = "采集频率")
	private Float collectionFrequency;

	/**
	 * 阈值下限
	 */
	@Schema(description = "阈值下限")
	private Float max;

	/**
	 * 阈值上限
	 */
	@Schema(description = "阈值上限")
	private Float min;

	/**
	 * 采集用途
	 */
	@Schema(description = "采集用途")
	private String purpose;

	/**
	 * 所属表config_failure_mode的节点id
	 */
	@Schema(description = "所属表config_failure_mode的节点id")
	private String nodeId;

}
