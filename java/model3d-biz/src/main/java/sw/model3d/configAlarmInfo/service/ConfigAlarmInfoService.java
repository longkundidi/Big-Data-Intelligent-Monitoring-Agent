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

package sw.model3d.configAlarmInfo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.model3d.configAlarmInfo.entity.DcAlarm;
import sw.model3d.configAlarmInfo.entity.vo.ConfigAlarmInfoVo;

import java.util.ArrayList;

/**
 * 故障预警的报警点信息记录
 *
 * @author pig code generator
 * @date 2024-07-01 22:20:43
 */
public interface ConfigAlarmInfoService extends IService<DcAlarm> {

	ArrayList<ConfigAlarmInfoVo> getConfigAlarmInfoPage(String proId);

	ArrayList<ConfigAlarmInfoVo> getConfigAlarmInfoSevenDayPage(String proId);

}
