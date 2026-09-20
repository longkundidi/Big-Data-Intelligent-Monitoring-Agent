package com.algorithm.web.mapper.taskConfig;

import com.algorithm.web.model.entity.taskConfig.TaskConfig;
import com.algorithm.web.model.entity.taskConfigResult.TaskConfigResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author www.javacoder.top
 * @since 2024-07-22
 */
@Repository
public interface TaskConfigMapper extends BaseMapper<TaskConfig> {

	@Select(("SELECT MAX(task_id) FROM task_config"))
	Optional<Long> findMaxTaskId();

	@Select(("SELECT al_name FROM al_data_clean where al_type = #{alType}"))
	List<String> getAlNameByType(@Param("alType") String alType);

	@Select(("SELECT model_name FROM al_state_evaluation"))
	List<String> getModelNameByType(String modelType);

	@Delete("DELETE FROM task_config WHERE task_id=#{taskId}")
	boolean delTaskByTaskId(Long taskId);

	List<TaskConfigResult> getTaskOutputs(@Param("offsetValue") Integer offsetValue, @Param("size") Integer size,
			@Param("taskId") Long taskId);

}
