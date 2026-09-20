package com.algorithm.web.controller.api.taskConfigResult;

import com.algorithm.web.service.taskConfigResult.TaskConfigResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * (TaskConfigResult)表控制层
 *
 * @author makejava
 * @since 2024-07-24 11:50:01
 */
@RestController
@RequestMapping("/taskConfigResult")
@CrossOrigin
public class TaskConfigResultController {

	/**
	 * 服务对象
	 */
	@Autowired
	private TaskConfigResultService taskConfigResultService;

}
