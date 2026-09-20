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
package sw.model3d.configAlarmInfo.entity;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 故障预警的报警点信息记录
 *
 * @author pig code generator
 * @date 2024-07-01 22:20:43
 */
@Data
@TableName("dc_alarm")
@Schema(description = "状态感知的报警点信息记录")
public class DcAlarm {

	/**
	 * 主键
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private Long id;

	/**
	 * 所属任务id
	 */
	@Schema(description = "所属任务id")
	private Long taskId;

	/**
	 * 算法结果
	 */
	@Schema(description = "算法结果")
	private String dcData;

	/**
	 * 采集时间
	 */
	@Schema(description = "采集时间")
	private LocalDateTime dcTime;

	/**
	 * 算法名称
	 */
	@Schema(description = "算法名称")
	private String algoShortname;

	/**
	 * 是否报警（1:出现报警;0:未报警）
	 */
	@Schema(description = "是否报警（1:出现报警;0:未报警）")
	private String anomalyFlag;

}
