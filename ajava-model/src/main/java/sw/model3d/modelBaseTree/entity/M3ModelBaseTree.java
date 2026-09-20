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
package sw.model3d.modelBaseTree.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author pig code generator
 * @date 2023-10-28 09:20:35
 */
@Data
@TableName("m3_model_base_tree")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "")
public class M3ModelBaseTree extends Model<M3ModelBaseTree> {

	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "结构树节点id")
	private String nodeId;

	@Schema(description = "结构树节点编码")
	private String nodeCode;

	@Schema(description = "结构树节点名称")
	private String nodeName;

	@Schema(description = "同一层节点的顺序号")
	private Integer nodeNo;

	@Schema(description = "排序")
	private Float nodeSort;

	@Schema(description = "节点层级(1-n)")
	private Integer nodeLevel;

	@Schema(description = "节点类型  Root-Leaf：根节点、叶子节点；Root：根节点（有叶子节点）；Leaf：叶子节点；Mid：中间节点")
	private String nodeType;

	@Schema(description = "模型文件Url地址")
	private String modelFileurl;

	@Schema(description = "模型文件的缩略图地址")
	private String urlImg;

	@Schema(description = "模型文件大小(字节数)")
	private Integer modelSize;

	@Schema(description = "模型文件类型，glb、obj")
	private String modelType;

	@Schema(description = "xcoordinate")
	private Double xcoordinate;

	@Schema(description = "ycoordinate")
	private Double ycoordinate;

	@Schema(description = "zcoordinate")
	private Double zcoordinate;

	@Schema(description = "xrotationAngle")
	private Double xrotationAngle;

	@Schema(description = "yrotationAngle")
	private Double yrotationAngle;

	@Schema(description = "zrotationAngle")
	private Double zrotationAngle;

	@Schema(description = "场景参数，加载模型的相机参数、灯光、背景等参数")
	private String scenePara;

	@Schema(description = "更新时间")
	private String updateTime;

	@Schema(description = "模型备注说明")
	private String memo;

	@Schema(description = "所属模型库id")
	private String mbId;

}
