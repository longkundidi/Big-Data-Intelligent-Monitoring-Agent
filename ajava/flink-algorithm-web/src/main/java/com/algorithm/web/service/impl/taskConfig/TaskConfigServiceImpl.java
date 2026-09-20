package com.algorithm.web.service.impl.taskConfig;

import cn.hutool.json.JSONObject;
import com.algorithm.web.mapper.al.AlDataCleanMapper;
import com.algorithm.web.mapper.al.AlStateEvaluationMapper;
import com.algorithm.web.mapper.taskConfig.TaskConfigMapper;
import com.algorithm.web.model.entity.taskConfig.TaskConfig;
import com.algorithm.web.model.entity.taskConfig.dto.TaskDto;
import com.algorithm.web.model.entity.taskConfig.vo.TaskVo;
import com.algorithm.web.model.entity.taskConfigResult.TaskConfigResult;
import com.algorithm.web.service.taskConfig.TaskConfigService;
import com.algorithm.web.service.taskConfigResult.TaskConfigResultService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.kafka.clients.producer.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author www.javacoder.top
 * @since 2024-07-22
 */
@Service
public class TaskConfigServiceImpl extends ServiceImpl<TaskConfigMapper, TaskConfig> implements TaskConfigService {

	@Autowired
	private AlDataCleanMapper alDataCleanMapper;

	@Autowired
	private AlStateEvaluationMapper alStateEvaluationMapper;

	@Autowired
	private TaskConfigResultService taskConfigResultService;

	@Override
	public List<TaskVo> getTasks() {

		List<TaskVo> tasks = new ArrayList<>();

		// 获取task_config中的所有记录
		List<TaskConfig> taskConfigs = this.list();
		if (taskConfigs.isEmpty()) {
			return null;
		}

		// 将具有相同taskId的对象分组
		Map<Long, List<TaskConfig>> groupedTasks = taskConfigs.stream()
			.collect(Collectors.groupingBy(TaskConfig::getTaskId));

		// 处理分组后的任务
		for (Map.Entry<Long, List<TaskConfig>> entry : groupedTasks.entrySet()) {
			Long taskId = entry.getKey();
			List<TaskConfig> configs = entry.getValue();
			// 将每个task的流程处理成字符串
			String taskProcess = processTaskConfigs(configs);
			TaskVo taskVo = new TaskVo();
			taskVo.setTaskId(taskId);
			taskVo.setTaskProcess(taskProcess);
			tasks.add(taskVo);
		}
		return tasks;
	}

	@Override
	public boolean addTask(List<TaskDto> taskDTOs) {
		// 查找最大的task_id
		Optional<Long> maxTaskId = this.getBaseMapper().findMaxTaskId();
		for (TaskDto taskDto : taskDTOs) {
			if (!taskDto.getAlType().isEmpty()) {
				TaskConfig taskConfig = new TaskConfig();
				taskConfig.setTaskId(maxTaskId.orElse(0L) + 1);
				taskConfig.setSequence(taskDto.getSequence());
				taskConfig.setAlType(taskDto.getAlType());
				taskConfig.setAlName(taskDto.getAlName());
				// 分算法类型查找算法简称
				if (taskConfig.getAlType().equals("实时状态评估")) {
					taskConfig.setAlShortName(alStateEvaluationMapper.getShortNameByName(taskDto.getAlName()));
				}
				else {
					taskConfig.setAlShortName(alDataCleanMapper.getShortNameByName(taskDto.getAlName()));
				}

				try {
					this.save(taskConfig);
				}
				catch (Exception e) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public List<String> getAlNameByType(String alType) {
		return this.getBaseMapper().getAlNameByType(alType);
	}

	@Override
	public List<String> getModelNameByType(String modelType) {
		return this.getBaseMapper().getModelNameByType(modelType);
	}

	@Override
	public boolean delTaskByTaskId(Long taskId) {

		return this.getBaseMapper().delTaskByTaskId(taskId);
	}

	@Override
	public Object getTasksInfo(Long taskId) {
		LambdaQueryWrapper<TaskConfig> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(TaskConfig::getTaskId, taskId);
		List<TaskConfig> taskConfigs = this.getBaseMapper().selectList(queryWrapper);

		ArrayList<TaskConfig> deduplications = new ArrayList<>();
		ArrayList<TaskConfig> imputations = new ArrayList<>();
		ArrayList<TaskConfig> anomalyDetections = new ArrayList<>();
		ArrayList<TaskConfig> normalizations = new ArrayList<>();
		ArrayList<TaskConfig> stateEvaluations = new ArrayList<>();

		for (TaskConfig taskConfig : taskConfigs) {
			// 分算法类型装配对象
			if ("数据去重".equals(taskConfig.getAlType())) {
				deduplications.add(taskConfig);
			}
			else if ("缺失值填充".equals(taskConfig.getAlType())) {
				imputations.add(taskConfig);
			}
			else if ("异常值检测".equals(taskConfig.getAlType())) {
				anomalyDetections.add(taskConfig);
			}
			else if ("数据标准化".equals(taskConfig.getAlType())) {
				normalizations.add(taskConfig);
			}
			else if ("实时状态评估".equals(taskConfig.getAlType())) {
				stateEvaluations.add(taskConfig);
			}
		}

		Map<String, Object> dataForm = new LinkedHashMap<>();
		dataForm.put("deduplications", deduplications);
		dataForm.put("imputations", imputations);
		dataForm.put("anomalyDetections", anomalyDetections);
		dataForm.put("normalizations", normalizations);
		dataForm.put("stateEvaluations", stateEvaluations);

		return dataForm;
	}

	@Override
	public boolean editTask(Long taskId, List<TaskDto> taskDTOs) {
		// 删除原来的任务配置信息
		this.delTaskByTaskId(taskId);

		// 新增修改后的任务配置信息
		for (TaskDto taskDto : taskDTOs) {
			TaskConfig taskConfig = new TaskConfig();
			taskConfig.setTaskId(taskId);
			taskConfig.setSequence(taskDto.getSequence());
			taskConfig.setAlType(taskDto.getAlType());
			taskConfig.setAlName(taskDto.getAlName());
			if (taskConfig.getAlType().equals("实时状态评估")) {
				taskConfig.setAlShortName(alStateEvaluationMapper.getShortNameByName(taskDto.getAlName()));
			}
			else {
				taskConfig.setAlShortName(alDataCleanMapper.getShortNameByName(taskDto.getAlName()));
			}
			this.save(taskConfig);
		}

		return true;
	}

	@Override
	public boolean operateTask(String taskData, Long taskId) {
		// 获取当前时间作为 task_time
		Long taskTime = Instant.now().toEpochMilli();
		Integer taskSequence = 1;

		// 创建 JSON 对象并填充数据
		JSONObject taskJson = new JSONObject();
		taskJson.put("task_data", taskData);
		taskJson.put("task_id", taskId);
		taskJson.put("task_time", taskTime);
		taskJson.put("task_sequence", taskSequence);

		// 发送数据到 Kafka topic
		return sendToKafka(taskJson.toString());
	}

	@Override
	public List<TaskConfigResult> getTaskOutputs(Integer curPage, Integer size, Long taskId) {
		Integer offsetValue = (curPage - 1) * size;
		Page<TaskConfigResult> page = new Page<>(curPage, size);
		LambdaQueryWrapper<TaskConfigResult> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(TaskConfigResult::getTaskId, taskId).orderByDesc(TaskConfigResult::getTaskTime);
		IPage<TaskConfigResult> page1 = taskConfigResultService.page(page, queryWrapper);
		return page1.getRecords();

		// return this.getBaseMapper().getTaskOutputs(offsetValue,size,taskId);
	}

	private boolean sendToKafka(String message) {
		Properties props = new Properties();
		props.put("bootstrap.servers", "192.168.16.219:9092");
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 5 * 1024 * 1024);

		try (Producer<String, String> producer = new KafkaProducer<>(props)) {
			ProducerRecord<String, String> record = new ProducerRecord<>("task_config_source", message);
			Future<RecordMetadata> future = producer.send(record);
			future.get(); // 这里会阻塞直到消息被发送或者抛出异常
			return true;
		}
		catch (ExecutionException | InterruptedException e) {
			System.err.println("Failed to send message to Kafka: " + e.getMessage());
			e.printStackTrace(System.err);
			Thread.currentThread().interrupt(); // Restore interrupted status
		}
		catch (Exception e) {
			System.err.println("An unexpected error occurred while sending message to Kafka: " + e.getMessage());
			e.printStackTrace(System.err);
		}
		return false;
	}

	private String processTaskConfigs(List<TaskConfig> configs) {
		StringBuilder taskProcessBuilder = new StringBuilder();
		configs.sort(Comparator.comparing(TaskConfig::getSequence));
		for (int i = 0; i < configs.size(); i++) {
			String step;
			if (i < configs.size() - 1) {
				step = "步骤" + configs.get(i).getSequence().toString() + "：" + configs.get(i).getAlType() + "-"
						+ configs.get(i).getAlName() + "；";
			}
			else {
				step = "步骤" + configs.get(i).getSequence().toString() + "：" + configs.get(i).getAlType() + "-"
						+ configs.get(i).getAlName();
			}
			taskProcessBuilder.append(step);
		}

		return taskProcessBuilder.toString();
	}

}
