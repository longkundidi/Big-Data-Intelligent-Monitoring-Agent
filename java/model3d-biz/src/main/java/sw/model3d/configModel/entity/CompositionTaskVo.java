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

import lombok.Data;
import sw.ai.domain.DomainModelConfiguration;

import java.util.List;

/**
 * @author pig code generator
 * @date 2024-04-01 10:43:50
 */
@Data
public class CompositionTaskVo {

	/**
	 * 任务id
	 */
	private Long taskId;

	/**
	 * 模型id
	 */
	private String modelId;

	/**
	 * 模型名称
	 */
	private String modelName;

	// 调用算法简称
	private String algoShortname;

	/**
	 * 服务算法id
	 */

	private String algoId;

	/**
	 * 所属表config_gbom_tree的节点id
	 */
	private String nodeId;

	// 是否是默认模板
	private Integer isDefaultModel;

	// 是否执行中
	private Integer status;

	// 组态流程
	private List<DomainModelConfiguration> process;

}
