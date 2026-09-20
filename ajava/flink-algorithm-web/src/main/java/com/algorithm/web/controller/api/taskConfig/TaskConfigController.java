package com.algorithm.web.controller.api.taskConfig;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.taskConfig.dto.TaskDto;
import com.algorithm.web.service.taskConfig.TaskConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author www.javacoder.top
 * @since 2024-07-22
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/taskConfig")
public class TaskConfigController {

	@Autowired
	private TaskConfigService taskConfigService;

	@GetMapping()
	public RestResult getTasks() {
		return RestResult.success(taskConfigService.getTasks());
	}

	@GetMapping("/info/{taskId}")
	public RestResult getTasksInfo(@PathVariable Long taskId) {
		return RestResult.success(taskConfigService.getTasksInfo(taskId));
	}

	@PostMapping
	public RestResult addTask(@RequestBody List<TaskDto> taskDTOs) {
		return RestResult.success(taskConfigService.addTask(taskDTOs));
	}

	@PutMapping("/editTask/{taskId}")
	public RestResult editTask(@PathVariable Long taskId, @RequestBody List<TaskDto> taskDTOs) {
		return RestResult.success(taskConfigService.editTask(taskId, taskDTOs));
	}

	@GetMapping("/getAlNameByType")
	public RestResult getAlNameByType(@RequestParam String alType) {
		return RestResult.success(taskConfigService.getAlNameByType(alType));
	}

	@GetMapping("/getModelNameByType")
	public RestResult getModelNameByType(@RequestParam String alType) {
		return RestResult.success(taskConfigService.getModelNameByType(alType));
	}

	@DeleteMapping("/delTaskByTaskId/{taskId}")
	public RestResult delTaskByTaskId(@PathVariable Long taskId) {
		return RestResult.success(taskConfigService.delTaskByTaskId(taskId));
	}

	@GetMapping("/operateTask")
	public RestResult operateTask(@RequestParam String taskData, @RequestParam Long taskId) {
		return RestResult.success(taskConfigService.operateTask(taskData, taskId));
	}

	@GetMapping("/getTaskOutputs")
	public RestResult getTaskOutputs(@RequestParam Integer curPage, @RequestParam Integer size,
			@RequestParam Long taskId) {
		return RestResult.success(taskConfigService.getTaskOutputs(curPage, size, taskId));
	}

}
