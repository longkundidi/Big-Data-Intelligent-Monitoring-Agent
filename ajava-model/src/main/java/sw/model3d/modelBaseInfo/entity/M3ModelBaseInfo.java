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
package sw.model3d.modelBaseInfo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableField;

/**
 * @author pig code generator
 * @date 2023-10-24 10:55:58
 */
@Data
@TableName("m3_model_base_info")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "")
public class M3ModelBaseInfo extends Model<M3ModelBaseInfo> {

	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "模型库id")
	private String mbId;

	@Schema(description = "记录编码，顶层节点编码：MT1，MT2，3，子节点编码：MT1-1,MT1-2等")
	private String mbCode;

	@Schema(description = "3D场景名称")
	private String mbName;

	@Schema(description = "场景参数")
	private String scenePara;

	@Schema(description = "场景图片URL地址")
	private String imageUrl;

	@Schema(description = "包含的模型个数")
	private Integer modelNumber;

	@Schema(description = "包含模型的总计字节数，单位：字节")
	private Integer totalSize;

	@Schema(description = "同一层节点的顺序号")
	private Integer nodeNo;

	@Schema(description = "排序")
	private Float nodeSort;

	@Schema(description = "节点层级(1-n)")
	private Integer nodeLevel;

	@Schema(description = "节点类型  RL：根节点（无叶子节点）；R：根节点（有叶子节点）；L：叶子节点；M：中间节点")
	private String nodeType;

	@Schema(description = "创建者")
	private String creator;

	@Schema(description = "创建时间")
	private Date createTime;

	@Schema(description = "父节点编码")
	@TableField(exist = false)
	private String pmbCode; // 注意：第二个字母不能大写，否则传递参数会失败

}
