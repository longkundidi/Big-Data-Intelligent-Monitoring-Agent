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

package sw.model3d.stateAssessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import sw.model3d.stateAssessment.entity.DcAlgorithmHistory;

import java.time.LocalDateTime;

/**
 * 风机实例任务执行结果（任务类型：状态感知任务）
 *
 * @author pig code generator
 * @date 2024-07-16 14:41:16
 */
public interface DcAlgorithmHistoryService extends IService<DcAlgorithmHistory> {

	Object getHistoryDcData(Long taskId, String algoShortname, String startDcTime, String endDcTime)
			throws JsonProcessingException;

}
