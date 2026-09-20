package com.algorithm.web.service.impl.executealgorithm;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("JsonForwardTaskMsgService")
@RequiredArgsConstructor
public class JsonForwardTaskMsgService implements BuildTaskMsgService {

	private final ObjectMapper objectMapper;

	@Override
	public String buildTaskMsg(String taskMsgStr) {
		if (taskMsgStr == null || taskMsgStr.trim().isEmpty()) {
			throw new BizException("模型输入不能为空");
		}
		try {
			objectMapper.readTree(taskMsgStr);
			return taskMsgStr;
		}
		catch (Exception exception) {
			throw new BizException("HTTP JSON模型输入必须是合法JSON");
		}
	}

}
