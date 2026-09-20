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

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-05-09 11:36:07
 */
@Data
@AllArgsConstructor
public class ConfigBomTreeWithStatus implements Serializable {

	private static final long serialVersionUID = 9156065586418349320L;

	/**
	 * 结构树节点名称
	 */
	private String name;

	private String turbineCode;

	private String status;

	private String producer;

}
