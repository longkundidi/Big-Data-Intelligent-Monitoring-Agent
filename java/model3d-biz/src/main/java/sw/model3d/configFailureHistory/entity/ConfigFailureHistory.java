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
package sw.model3d.configFailureHistory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author pig code generator
 * @date 2026-03-19 16:15:58
 */
@Data
@TableName("config_failure_history")
@EqualsAndHashCode
@Schema(description = "故障历史配置类")
public class ConfigFailureHistory extends Model<ConfigFailureHistory> {

	/**
	 * 故障记录id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "故障记录id")
	private String failureId;

	/**
	 * 故障名称
	 */
	@Schema(description = "故障名称")
	private String failureName;

	/**
	 * 故障编码
	 */
	@Schema(description = "故障编码")
	private String failureCode;

	/**
	 * 故障说明
	 */
	@Schema(description = "故障说明")
	private String memo;

	/**
	 * 所属表config_gbom_tree的节点id
	 */
	@Schema(description = "所属表config_gbom_tree的节点id")
	private String nodeId;

	/**
	 * 场景id，如：1、23
	 */
	@Schema(description = "场景id，如：1、23")
	private String sceneId;

	/**
	 * 设备名称，如：电梯#1
	 */
	@Schema(description = "设备名称，如：电梯#1")
	private String deviceName;

}