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

package sw.ai.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sw.ai.service.AlgorithmConfigurationService;

/**
 * 领域模型组态管理表
 *
 * @author pig code generator
 * @date 2025-04-29 10:28:19
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/algorithmconfiguration")
@Tag(name = "领域模型组态管理表管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class AlgorithmConfigurationController {

	private final AlgorithmConfigurationService algorithmConfigurationService;

}
