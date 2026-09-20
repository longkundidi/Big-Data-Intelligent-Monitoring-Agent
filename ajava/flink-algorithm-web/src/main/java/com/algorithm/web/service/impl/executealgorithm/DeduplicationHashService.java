package com.algorithm.web.service.impl.executealgorithm;

import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 哈希去重算法
 */
@Service("DeduplicationHashService")
@Slf4j
public class DeduplicationHashService implements BuildTaskMsgService {

	@Override
	public String buildTaskMsg(String taskMsgStr) {
		TaskMsg taskMsg = JsonUtil.fromJson(taskMsgStr, TaskMsg.class);
		return JsonUtil.toJson(taskMsg);
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	static class TaskMsg {

		String input;

	}

}
