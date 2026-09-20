package com.algorithm.web.service.taskConfig;

import cn.hutool.json.JSONObject;
import com.algorithm.web.model.entity.taskConfig.TaskConfig;
import com.algorithm.web.model.entity.taskConfig.dto.TaskDto;
import com.algorithm.web.model.entity.taskConfig.vo.TaskVo;
import com.algorithm.web.model.entity.taskConfigResult.TaskConfigResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author www.javacoder.top
 * @since 2024-07-22
 */
public interface TaskConfigService extends IService<TaskConfig> {

	List<TaskVo> getTasks();

	boolean addTask(List<TaskDto> taskDTOs);

	List<String> getAlNameByType(String alType);

	List<String> getModelNameByType(String modelType);

	boolean delTaskByTaskId(Long taskId);

	Object getTasksInfo(Long taskId);

	boolean editTask(Long taskId, List<TaskDto> taskDTOs);

	boolean operateTask(String taskData, Long taskId);

	List<TaskConfigResult> getTaskOutputs(Integer curPage, Integer size, Long taskId);

}
