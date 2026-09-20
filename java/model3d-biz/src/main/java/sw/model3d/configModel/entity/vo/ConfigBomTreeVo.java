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

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-05-09 11:36:07
 */
@Data
public class ConfigBomTreeVo implements Serializable {

	private static final long serialVersionUID = -3770565544695272739L;

	/**
	 * 结构树节点id
	 */
	private String id;

	/**
	 * 结构树节点编码
	 */
	private String nodeCode;

	/**
	 * 结构树节点名称
	 */
	private String name;

	private String turbineCode;

	/**
	 * 结构树节点层级(1-n)
	 */
	private String nodeType;

	/**
	 * 结构树节点类型 Root-Leaf：根节点、叶子节点；Root：根节点（有叶子节点）
	 */
	private String nodeLevel;

	private boolean Leaf;

	private Date productionDate;

	// 采购日期
	private Date purchaseDate;

	// 安装日期
	private Date installationDate;

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

	// 制动器类型
	private String brakeType;

	// 额定载重
	private String ratedLoad;

	// 额定速度
	private String ratedSpeed;

	// 额定功率
	private String ratedPower;

}
