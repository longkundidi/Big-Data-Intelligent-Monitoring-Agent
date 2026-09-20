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
package sw.AlResumeData.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 风场信息
 *
 * @author pig code generator
 * @date 2025-03-03 11:54:27
 */
@Data
@TableName("al_resume_data")
@Schema(description = "场景信息")
public class AlResumeData implements Serializable {

	private static final long serialVersionUID = -4631847346503992179L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private Long id;

	/**
	 * 项目名称
	 */
	@Schema(description = "项目名称")
	private String project;

	/**
	 * 描述
	 */
	@Schema(description = "描述")
	private String description;

	/**
	 * 图标地址
	 */
	@Schema(description = "图标地址")
	private String iconUrl;

	/**
	 * 产品机型
	 */
	@Schema(description = "产品机型")
	private String productModel;

	/**
	 * BOM信息
	 */
	@Schema(description = "BOM信息")
	private String bomModel;

	@Schema(description = "机型id")
	private Long productModelId;

	@Schema(description = "元模型id")
	private Long metaModelId;

}
