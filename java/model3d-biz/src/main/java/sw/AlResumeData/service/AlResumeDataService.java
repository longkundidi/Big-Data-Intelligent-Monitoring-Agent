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

package sw.AlResumeData.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.AlResumeData.entity.AlResumeData;

import java.util.List;

/**
 * 风场信息
 *
 * @author pig code generator
 * @date 2025-03-03 11:54:27
 */
public interface AlResumeDataService extends IService<AlResumeData> {

	Long getIdByFarmName(String farmName);

	List<AlResumeData> getIdFarmsByUserId(Long userId, String userRole);

}
