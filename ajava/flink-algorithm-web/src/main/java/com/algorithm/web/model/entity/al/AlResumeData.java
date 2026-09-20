package com.algorithm.web.model.entity.al;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@TableName("al_resume_data")
@NoArgsConstructor
@AllArgsConstructor

public class AlResumeData implements Serializable {

	private static final long serialVersionUID = 337361630075102452L;

	@TableId(type = IdType.AUTO)
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
