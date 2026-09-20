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
package sw.model3d.configResumeFaultData.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 风机实例的实际报警信息
 *
 * @author pig code generator
 * @date 2024-07-24 17:21:39
 */
@Data
@TableName("config_resume_fault_data")
@Schema(description = "风机实例的实际报警信息")
public class ConfigResumeFaultData implements Serializable {

	private static final long serialVersionUID = 595371293953627624L;

	/**
	 * 主键
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private Long id;

	/**
	 * 风机实例节点唯一标识id
	 */
	@Schema(description = "风机实例节点唯一标识id")
	private String nodeId;

	@Schema(description = "故障部件")
	private String component;

	/**
	 * 故障名称
	 */
	@Schema(description = "故障名称")
	private String faultName;

	/**
	 * 故障发生时间
	 */
	@Schema(description = "故障发生时间")
	private LocalDateTime startTime;

	/**
	 * 故障结束时间
	 */
	@Schema(description = "故障结束时间")
	private LocalDateTime endTime;

}
