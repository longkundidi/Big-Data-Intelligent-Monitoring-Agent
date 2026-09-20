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
package sw.model3d.stateAssessment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import sw.model3d.stateAssessment.entity.DcAlgorithmHistory;
import sw.model3d.stateAssessment.mapper.DcAlgorithmHistoryMapper;
import sw.model3d.stateAssessment.service.DcAlgorithmHistoryService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 风机实例任务执行结果（任务类型：状态感知任务）
 *
 * @author pig code generator
 * @date 2024-07-16 14:41:16
 */
@Service
public class DcAlgorithmHistoryServiceImpl extends ServiceImpl<DcAlgorithmHistoryMapper, DcAlgorithmHistory>
		implements DcAlgorithmHistoryService {

	@Override
	public Object getHistoryDcData(Long taskId, String algoShortname, String startDcTime, String endDcTime)
			throws JsonProcessingException {
		LambdaQueryWrapper<DcAlgorithmHistory> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DcAlgorithmHistory::getTaskId, taskId)
			.eq(DcAlgorithmHistory::getAlgoShortname, algoShortname)
			.ge(DcAlgorithmHistory::getDcTime, startDcTime)
			.le(DcAlgorithmHistory::getDcTime, endDcTime);

		List<DcAlgorithmHistory> DcAlgorithmHistoryList = this.getBaseMapper().selectList(queryWrapper);
		List<Double> dataList = new ArrayList<>();
		ArrayList<Double> thresholdList = new ArrayList<>();
		ArrayList<Double> anomaly_flagList = new ArrayList<>();
		ArrayList<String> dcTimeList = new ArrayList<>();

		for (DcAlgorithmHistory DcAlgorithmHistory : DcAlgorithmHistoryList) {
			JSONObject dcDataObject = JSON.parseObject(DcAlgorithmHistory.getDcData());

			// 创建 ObjectMapper 实例
			ObjectMapper objectMapper = new ObjectMapper();
			// 解析JSON字符串为JsonNode对象
			JsonNode jsonNode = objectMapper.readTree(DcAlgorithmHistory.getDcData());
			if (DcAlgorithmHistory.getAlgoShortname().equals("CAE")) {

				JSONArray rcoHi = (JSONArray) dcDataObject.get("rco_hi");
				dataList.add((rcoHi.getDoubleValue(0)));

			}
			if (DcAlgorithmHistory.getAlgoShortname().equals("MultiFeatureIndexFusionAE")
					|| DcAlgorithmHistory.getAlgoShortname().equals("LSTMAE")) {
				// 从JsonNode中提取rms_hi的值
				double rmsHi = jsonNode.get("rms_hi").asDouble();
				dataList.add(rmsHi);

			}

			double threshold = jsonNode.get("threshold").asDouble();
			thresholdList.add(threshold);

			double anomaly_flag = jsonNode.get("anomaly_flag").asDouble();
			anomaly_flagList.add(anomaly_flag);

			String dcTime = DcAlgorithmHistory.getDcTime().toString();

			dcTimeList.add(dcTime);

		}
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode jsonNodes = objectMapper.createObjectNode();

		jsonNodes.set("dc_dataList", objectMapper.valueToTree(dataList));
		jsonNodes.set("thresholdList", objectMapper.valueToTree(thresholdList));
		jsonNodes.set("anomaly_flagList", objectMapper.valueToTree(anomaly_flagList));
		jsonNodes.set("dcTimeList", objectMapper.valueToTree(dcTimeList));

		return Collections.singletonList(jsonNodes);
	}

}
