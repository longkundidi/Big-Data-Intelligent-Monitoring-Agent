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
package sw.model3d.configFailureMode.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author pig code generator
 * @date 2024-03-15 14:43:38
 */
@Data
public class ConfigFailureModeVo implements Serializable {

	private static final long serialVersionUID = 484098541412950945L;

	/**
	 * 故障记录id
	 */
	@Schema(description = "故障记录id")
	private String failureId;

	/**
	 * 故障名称
	 */
	@Schema(description = "故障名称")
	private String failureName;

	/**
	 * 所属表config_failure_mode的节点id
	 */
	@Schema(description = "所属表config_failure_mode的节点id")
	private String nodeId;

}
