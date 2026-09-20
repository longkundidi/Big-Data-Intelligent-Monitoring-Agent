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
package sw.model3d.configModel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-05-09 11:36:07
 */
@Data
@TableName("config_bom_tree")
@Schema(description = "GBOM树")

public class ConfigBomTree implements Serializable {

	private static final long serialVersionUID = -3770565544695272739L;

	/**
	 * 结构树节点id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "结构树节点id")
	private String nodeId;

	/**
	 * 结构树节点编码
	 */
	@Schema(description = "结构树节点编码")
	private String nodeCode;

	/**
	 * 风机编码
	 */
	@Schema(description = "风机编码")
	private String turbineCode;

	/**
	 * 结构树节点名称
	 */
	@Schema(description = "结构树节点名称")
	private String nodeName;

	/**
	 * 同一层节点的顺序号
	 */
	@Schema(description = "同一层节点的顺序号")
	private Integer nodeNo;

	/**
	 * 排序
	 */
	@Schema(description = "排序")
	private Float swsort;

	/**
	 * 节点层级(1-n)
	 */
	@Schema(description = "节点层级(1-n)")
	private Integer nodeLevel;

	/**
	 * 节点类型 Root-Leaf：根节点、叶子节点；Root：根节点（有叶子节点）；Leaf：叶子节点；Mid：中间节点
	 */
	@Schema(description = "节点类型  Root-Leaf：根节点、叶子节点；Root：根节点（有叶子节点）；Leaf：叶子节点；Mid：中间节点")
	private String nodeType;

	@Schema(description = "结构树更新时间")
	private String updateTime;

	/**
	 * 模型备注说明
	 */
	@Schema(description = "结构树备注说明")
	private String memo;

	/**
	 * 所属项目id
	 */
	@Schema(description = "所属项目id")
	private String proId;

	/**
	 * 风机结构树是否匹配成功(0：未匹配；1：已匹配)
	 */
	@Schema(description = "风机结构树是否匹配成功(0：未匹配；1：已匹配)")
	private Integer isBomPassed;

	/**
	 * 结构树感知变量是否匹配成功(0：未匹配成功；1：已匹配成功)
	 */
	@Schema(description = "结构树感知变量是否匹配成功(0：未匹配成功；1：已匹配成功)")
	private Integer isValPassed;

	/**
	 * 是否生成状态感知任务(0未生成；1：已生成)
	 */
	@Schema(description = "是否生成状态感知任务(0未生成；1：已生成)")
	private Integer isPerceivedTaskCreate;

	// 生产日期
	private LocalDate productionDate;

	// 采购日期
	private LocalDate purchaseDate;

	// 安装日期
	private LocalDate installationDate;

	// 安装负责人
	private String installationLeader;

	// 负责人
	private String responsiblePerson;

	// 零件型号
	private String componentModel;

	// 制造产商
	private String manufacturer;

	// 检修记录
	private String maintenanceRecord;

	// 检修人
	private String maintenancePerson;

	// 故障记录
	private String faultRecord;

	// 故障记录人
	private String faultReporter;

	// 更换记录
	private String replacementRecord;

	// 更换记录人
	private String replacementPerson;

	/**
	 * 制造厂商
	 */
	@Schema(description = "制造者")
	private String producer;

	@Schema(description = "制动器类型")
	private String brakeType;

	@Schema(description = "额定载重")
	private String ratedLoad;

	@Schema(description = "额订速度")
	private String ratedSpeed;

	@Schema(description = "额定功率")
	private String ratedPower;

}
