package com.algorithm.web.controller.api.al;

import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.service.al.AlTaskService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/algorithm/job")
public class AlgorithmJobController {

	@Autowired
	private AlTaskService alTaskService;

	@RequestMapping(value = "/algorithmJobCallback", method = { RequestMethod.POST })
	public void algorithmJobCallback(@RequestBody AlTask alTask) {
		System.out.println(alTask.getTaskResult());
		log.info("algorithmJobCallback taskId={} taskResult={} taskState={}", alTask.getTaskId(),
				alTask.getTaskResult(), alTask.getTaskState());
		LambdaUpdateWrapper<AlTask> lambdaUpdate = Wrappers.lambdaUpdate(AlTask.class);
		lambdaUpdate.eq(AlTask::getId, alTask.getTaskId());
		alTaskService.update(AlTask.builder()
			.taskState(alTask.getTaskState())
			.taskResult(alTask.getTaskResult())
			.endTime(LocalDateTime.now())
			.build(), lambdaUpdate);
	}

}
