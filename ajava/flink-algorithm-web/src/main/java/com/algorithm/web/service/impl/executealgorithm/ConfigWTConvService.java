package com.algorithm.web.service.impl.executealgorithm;

import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service("ConfigWTConvService")
@Slf4j
public class ConfigWTConvService implements BuildTaskMsgService {

	@Override
	public String buildTaskMsg(String taskMsgStr) {
		// 将一个JSON字符串转换为一个taskmsg对象
		TaskMsg taskMsg = JsonUtil.fromJson(taskMsgStr, TaskMsg.class);
		// taskmsg对象再转换回json
		return JsonUtil.toJson(taskMsg);
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	static class TaskMsg {

		String programUrl;

	}

}
