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
package sw.model3d.stateAssessment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pig4cloud.pig.common.mybatis.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 风机实例任务执行结果（任务类型：状态感知任务）
 *
 * @author pig code generator
 * @date 2024-07-16 14:41:16
 */
@Data
@TableName("dc_algorithm_history")
@Schema(description = "风机实例任务执行结果（任务类型：状态感知任务）")
public class DcAlgorithmHistory {

	/**
	 * 主键
	 */
	@TableId(type = IdType.ASSIGN_ID)
	@Schema(description = "主键")
	private Integer id;

	/**
	 * 算法结果
	 */
	@Schema(description = "算法结果")
	private String dcData;

	/**
	 * 任务id
	 */
	@Schema(description = "任务id")
	private String taskId;

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

}
