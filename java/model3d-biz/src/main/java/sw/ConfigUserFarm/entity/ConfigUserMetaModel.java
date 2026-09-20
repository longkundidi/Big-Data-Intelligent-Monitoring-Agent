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
package sw.ConfigUserFarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 元模型-用户权限对应表
 *
 *
 */
@Data
@TableName("config_user_meta_model")
@Schema(description = "元模型-用户权限对应表")
public class ConfigUserMetaModel implements Serializable {

	private static final long serialVersionUID = 8264232471387866894L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private Long id;

	/**
	 * 元模型id
	 */
	@Schema(description = "元模型id")
	private Long metaModelId;

	/**
	 * 元模型名
	 */
	@Schema(description = "元模型名")
	private String metaModelName;

	/**
	 * 用户id
	 */
	@Schema(description = "用户id")
	private Long userId;

}
