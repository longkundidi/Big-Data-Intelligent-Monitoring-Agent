package sw.model3d.configModel.service.impl;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sw.ai.domain.AlStateEvaluation;
import sw.ai.domain.DomainModelConfiguration;
import sw.ai.mapper.DomainModelConfigurationMapper;
import sw.model3d.ConfigBomPerceivedVariable.entity.ConfigBomPerceivedVariable;
import sw.model3d.ConfigBomPerceivedVariable.service.ConfigBomPerceivedVariableService;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.model3d.configGbomTree.service.ConfigGbomTreeService;
import sw.model3d.configModel.entity.*;
import sw.model3d.configModel.entity.vo.ConfigModelVo;
import sw.model3d.configModel.entity.vo.ConfigPerceivedTaskSaveRequest;
import sw.model3d.configModel.mapper.ConfigBomTreeMapper;
import sw.model3d.configModel.mapper.ConfigPerceivedTaskMapper;
import sw.model3d.configModel.service.ConfigBomTreeService;
import sw.model3d.configModel.service.ConfigModelService;
import sw.model3d.configModel.service.ConfigPerceivedTaskService;
import sw.model3d.configModel.service.ConfigPerceivedTaskVariableService;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.model3d.configPerceivedVariable.service.ConfigPerceivedVariableService;
import sw.utils.kafkaTopic;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * (ConfigPerceivedTask)表服务实现类
 *
 * @author makejava
 * @since 2024-05-13 09:31:31
 */
@Service
public class ConfigPerceivedTaskServiceImpl extends ServiceImpl<ConfigPerceivedTaskMapper, ConfigPerceivedTask>
		implements ConfigPerceivedTaskService {

	@Autowired
	private ConfigPerceivedTaskVariableService configPerceivedTaskVariableService;

	@Autowired
	private ConfigBomTreeService configBomTreeService;

	@Autowired
	private ConfigGbomTreeService configGbomTreeService;

	@Autowired
	private ConfigModelService configModelService;

	@Autowired
	private ConfigPerceivedVariableService configPerceivedVariableService;

	@Autowired
	private ConfigBomPerceivedVariableService configBomPerceivedVariableService;

	@Autowired
	private ConfigGbomTreeMapper configGbomTreeMapper;

	@Autowired
	private ConfigBomTreeMapper configBomTreeMapper;

	@Autowired
	private DomainModelConfigurationMapper domainModelConfigurationMapper;

	@Override
	public boolean addConfigPerceivedTask(List<String> nodeIdList) {
		// 新增nodelist长度的任务信息和感知变量信息
		for (String bomNodeId : nodeIdList) {
			ConfigPerceivedTask configPerceivedTask = new ConfigPerceivedTask();

			// 根据节点名字和nodeType获取元结构树的nodeid
			String nodeName = configBomTreeService.getById(bomNodeId).getNodeName();
			String nodeType = configBomTreeService.getById(bomNodeId).getNodeType();
			String nodeCode = configBomTreeService.getById(bomNodeId).getNodeCode();
			String turbineCode = configBomTreeService.getById(bomNodeId).getTurbineCode();

			String parentName = configBomTreeMapper
				.getNodebyCode(nodeCode.substring(0, nodeCode.lastIndexOf("-")), turbineCode)
				.getNodeName();

			List<ConfigGbomTree> list = configGbomTreeService.list(new QueryWrapper<ConfigGbomTree>().lambda()
				.eq(ConfigGbomTree::getNodeName, nodeName)
				.eq(ConfigGbomTree::getNodeType, nodeType));

			String nodeId = "";
			for (ConfigGbomTree configGbomTree : list) {
				// String parent = configGbomTreeMapper
				// .getNodebyCode(configGbomTree.getNodeCode().substring(0,configGbomTree.getNodeCode().lastIndexOf("-")))
				// .getNodeName();

				String projectNum = turbineCode.split("-")[0]; // 截取前面的数字
				String sceneId = configGbomTreeMapper.getSceneIdByNodeId(projectNum);
				String parent = configGbomTreeMapper
					.getNodebyCodeAndSceneId(
							configGbomTree.getNodeCode().substring(0, configGbomTree.getNodeCode().lastIndexOf("-")),
							sceneId)
					.getNodeName();
				if (parent.equals(parentName)) {
					nodeId = configGbomTree.getNodeId();
					break;
				}
			}

			LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(ConfigPerceivedTask::getNodeId, bomNodeId);
			List<ConfigPerceivedTask> taskList = this.getBaseMapper().selectList(queryWrapper);
			if (taskList.size() == 0) { // 如果这个节点还未匹配，就生成状态感知任务

				// 根据元结构树的nodeId和modeltype查询获取默认模板信息
				List<ConfigModel> perceivedList = configModelService.getByNodeIdAndModelType(nodeId, "perceived");
				if (perceivedList.size() != 0) { // 存在
					ConfigModel defaultModel = perceivedList.get(0);

					BeanUtils.copyProperties(defaultModel, configPerceivedTask);
					List<AlStateEvaluation> alStateEvaluations = getAll();
					for (AlStateEvaluation alStateEvaluation : alStateEvaluations) {
						if (defaultModel.getAlgoId().equals(alStateEvaluation.getId())) {
							configPerceivedTask.setAlgoShortname(alStateEvaluation.getModelShortName());
						}
					}

					configPerceivedTask.setIsDefaultModel(1);
					configPerceivedTask.setNodeId(bomNodeId);
					configPerceivedTask.setStatus(0);
					ConfigBomTree node = configBomTreeService.getNode(bomNodeId);
					configPerceivedTask.setProId(node.getProId());
					// 插入到config_perceived_task表中
					this.save(configPerceivedTask);

					// 根据modelid获取对应的感知变量
					List<ConfigModelVariable> configModelVariableList = configPerceivedVariableService
						.getIDByModelId(defaultModel.getModelId());
					for (ConfigModelVariable configModelVariable : configModelVariableList) {
						ConfigPerceivedTaskVariable configPerceivedTaskVariable = new ConfigPerceivedTaskVariable();
						LambdaQueryWrapper<ConfigPerceivedVariable> queryWrapper1 = new LambdaQueryWrapper<>();
						queryWrapper1.eq(ConfigPerceivedVariable::getVarId, configModelVariable.getVarId());
						ConfigPerceivedVariable configPerceivedVariable = configPerceivedVariableService
							.getOne(queryWrapper1);
						configPerceivedTaskVariable.setTaskId(configPerceivedTask.getTaskId());
						BeanUtils.copyProperties(configPerceivedVariable, configPerceivedTaskVariable);
						configPerceivedTaskVariableService.save(configPerceivedTaskVariable);

					}

					createTopic(kafkaTopic.bootstrapServers,
							kafkaTopic.topicName + configPerceivedTask.getTaskId().toString(), kafkaTopic.partitions,
							kafkaTopic.replication);

					LambdaUpdateWrapper<ConfigPerceivedTask> updateWrapper = new LambdaUpdateWrapper<>();
					updateWrapper.eq(ConfigPerceivedTask::getTaskId, configPerceivedTask.getTaskId())
						.set(ConfigPerceivedTask::getVariableNum, configModelVariableList.size());
					this.update(updateWrapper);

				}

			}

		}

		return true;

	}

	@Override
	public boolean addCompositionTask(List<String> nodeIdList) {
		// 新增nodelist长度的任务信息和感知变量信息
		for (String bomNodeId : nodeIdList) {
			ConfigPerceivedTask configPerceivedTask = new ConfigPerceivedTask();

			// 根据节点名字和nodeType获取元结构树的nodeid
			String nodeName = configBomTreeService.getById(bomNodeId).getNodeName();
			String nodeType = configBomTreeService.getById(bomNodeId).getNodeType();
			String nodeCode = configBomTreeService.getById(bomNodeId).getNodeCode();
			String turbineCode = configBomTreeService.getById(bomNodeId).getTurbineCode();

			String parentName = configBomTreeMapper
				.getNodebyCode(nodeCode.substring(0, nodeCode.lastIndexOf("-")), turbineCode)
				.getNodeName();

			List<ConfigGbomTree> list = configGbomTreeService.list(new QueryWrapper<ConfigGbomTree>().lambda()
				.eq(ConfigGbomTree::getNodeName, nodeName)
				.eq(ConfigGbomTree::getNodeType, nodeType));

			String nodeId = "";
			for (ConfigGbomTree configGbomTree : list) {
				// String parent = configGbomTreeMapper
				// .getNodebyCode(configGbomTree.getNodeCode().substring(0,configGbomTree.getNodeCode().lastIndexOf("-")))
				// .getNodeName();
				// 新增sceneId查询逻辑
				String projectNum = turbineCode.split("-")[0]; // 截取前面的数字
				String sceneId = configGbomTreeMapper.getSceneIdByNodeId(projectNum);
				String parent = configGbomTreeMapper
					.getNodebyCodeAndSceneId(
							configGbomTree.getNodeCode().substring(0, configGbomTree.getNodeCode().lastIndexOf("-")),
							sceneId)
					.getNodeName();
				if (parent.equals(parentName)) {
					nodeId = configGbomTree.getNodeId();
					break;
				}
			}

			LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(ConfigPerceivedTask::getNodeId, bomNodeId);
			List<ConfigPerceivedTask> taskList = this.getBaseMapper().selectList(queryWrapper);
			if (taskList.size() == 0) { // 如果这个节点还未匹配，就生成状态感知任务

				// 根据元结构树的nodeId和modeltype查询获取默认模板信息
				List<ConfigModel> perceivedList = configModelService.getByNodeIdAndModelType(nodeId, "composition");
				if (perceivedList.size() != 0) { // 存在
					ConfigModel defaultModel = perceivedList.get(0);

					BeanUtils.copyProperties(defaultModel, configPerceivedTask);
					configPerceivedTask.setAlgoShortname(defaultModel.getAlgoId());
					configPerceivedTask.setIsDefaultModel(1);
					configPerceivedTask.setNodeId(bomNodeId);
					configPerceivedTask.setStatus(0);
					ConfigBomTree node = configBomTreeService.getNode(bomNodeId);
					configPerceivedTask.setProId(node.getProId());
					// 插入到config_perceived_task表中
					this.save(configPerceivedTask);

					// 根据modelid获取对应的感知变量
					List<ConfigModelVariable> configModelVariableList = configPerceivedVariableService
						.getIDByModelId(defaultModel.getModelId());
					for (ConfigModelVariable configModelVariable : configModelVariableList) {
						ConfigPerceivedTaskVariable configPerceivedTaskVariable = new ConfigPerceivedTaskVariable();
						LambdaQueryWrapper<ConfigPerceivedVariable> queryWrapper1 = new LambdaQueryWrapper<>();
						queryWrapper1.eq(ConfigPerceivedVariable::getVarId, configModelVariable.getVarId());
						ConfigPerceivedVariable configPerceivedVariable = configPerceivedVariableService
							.getOne(queryWrapper1);
						configPerceivedTaskVariable.setTaskId(configPerceivedTask.getTaskId());
						BeanUtils.copyProperties(configPerceivedVariable, configPerceivedTaskVariable);
						configPerceivedTaskVariableService.save(configPerceivedTaskVariable);

					}

					createTopic(kafkaTopic.bootstrapServers,
							kafkaTopic.topicName + configPerceivedTask.getTaskId().toString(), kafkaTopic.partitions,
							kafkaTopic.replication);

					LambdaUpdateWrapper<ConfigPerceivedTask> updateWrapper = new LambdaUpdateWrapper<>();
					updateWrapper.eq(ConfigPerceivedTask::getTaskId, configPerceivedTask.getTaskId())
						.set(ConfigPerceivedTask::getVariableNum, configModelVariableList.size());
					this.update(updateWrapper);

				}

			}

		}

		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ConfigPerceivedTask createTemplate(String modelId, String bomNodeId) {
		LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTask::getNodeId, bomNodeId)
			.eq(ConfigPerceivedTask::getModelId, modelId)
			.orderByDesc(ConfigPerceivedTask::getTaskId);
		List<ConfigPerceivedTask> existingTasks = this.list(queryWrapper);
		ConfigPerceivedTask configPerceivedTask1 = existingTasks.isEmpty() ? null : existingTasks.get(0);
		if (configPerceivedTask1 != null) {
			return configPerceivedTask1;
		}
		else {
			// 情况二：未生成过任务实例
			// 根据元结构树的modeltype查询获取模板信息
			ConfigModel configModel = configModelService.getById(modelId);
			ConfigPerceivedTask configPerceivedTask = new ConfigPerceivedTask();

			BeanUtils.copyProperties(configModel, configPerceivedTask);
			List<AlStateEvaluation> alStateEvaluations = getAll();
			for (AlStateEvaluation alStateEvaluation : alStateEvaluations) {
				if (configModel.getAlgoId().equals(alStateEvaluation.getId())) {
					configPerceivedTask.setAlgoShortname(alStateEvaluation.getModelShortName());
				}
			}

			configPerceivedTask.setIsDefaultModel(0);
			configPerceivedTask.setNodeId(bomNodeId);
			ConfigBomTree node = configBomTreeService.getNode(bomNodeId);
			configPerceivedTask.setProId(node.getProId());
			configPerceivedTask.setStatus(0);
			// 插入到config_perceived_task表中
			this.save(configPerceivedTask);

			// 根据modelid获取对应的感知变量
			List<ConfigModelVariable> configModelVariableList = configPerceivedVariableService
				.getIDByModelId(configModel.getModelId());
			for (ConfigModelVariable configModelVariable : configModelVariableList) {
				ConfigPerceivedTaskVariable configPerceivedTaskVariable = new ConfigPerceivedTaskVariable();
				LambdaQueryWrapper<ConfigPerceivedVariable> queryWrapper1 = new LambdaQueryWrapper<>();
				queryWrapper1.eq(ConfigPerceivedVariable::getVarId, configModelVariable.getVarId());
				ConfigPerceivedVariable configPerceivedVariable = configPerceivedVariableService.getOne(queryWrapper1);
				configPerceivedTaskVariable.setTaskId(configPerceivedTask.getTaskId());
				BeanUtils.copyProperties(configPerceivedVariable, configPerceivedTaskVariable);
				configPerceivedTaskVariableService.save(configPerceivedTaskVariable);
			}

			createTopic(kafkaTopic.bootstrapServers, kafkaTopic.topicName + configPerceivedTask.getTaskId().toString(),
					kafkaTopic.partitions, kafkaTopic.replication);

			LambdaUpdateWrapper<ConfigPerceivedTask> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(ConfigPerceivedTask::getTaskId, configPerceivedTask.getTaskId())
				.set(ConfigPerceivedTask::getVariableNum, configModelVariableList.size());
			this.update(updateWrapper);
			configPerceivedTask.setVariableNum((long) configModelVariableList.size());
			return configPerceivedTask;
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ConfigPerceivedTask saveConfiguration(ConfigPerceivedTaskSaveRequest request) {
		if (request == null || !StringUtils.hasText(request.getNodeId())
				|| !StringUtils.hasText(request.getModelId())) {
			throw new IllegalArgumentException("实例节点和模型不能为空");
		}
		if (!StringUtils.hasText(request.getAlgoId())) {
			throw new IllegalArgumentException("请选择服务算法");
		}
		if (request.getDataDimension() == null || request.getDataDimension() <= 0) {
			throw new IllegalArgumentException("数据维度必须是大于 0 的整数");
		}
		if (request.getVariables() == null || request.getVariables().isEmpty()) {
			throw new IllegalArgumentException("请至少选择一个感知变量");
		}

		ConfigModel model = configModelService.getById(request.getModelId());
		ConfigBomTree node = configBomTreeService.getById(request.getNodeId());
		if (model == null) {
			throw new IllegalArgumentException("状态感知模型不存在");
		}
		if (node == null) {
			throw new IllegalArgumentException("实例节点不存在");
		}

		String algoShortname = getAll().stream()
			.filter(item -> Objects.equals(String.valueOf(item.getId()), request.getAlgoId()))
			.map(AlStateEvaluation::getModelShortName)
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("选择的服务算法不存在"));

		List<ConfigBomPerceivedVariable> candidateVariables = configBomPerceivedVariableService
			.prepareTaskVariables(request.getNodeId(), request.getModelId());
		Map<String, ConfigBomPerceivedVariable> candidateById = candidateVariables.stream()
			.filter(item -> StringUtils.hasText(item.getVarId()))
			.collect(Collectors.toMap(ConfigBomPerceivedVariable::getVarId, item -> item, (left, right) -> left));
		LinkedHashSet<String> selectedIds = request.getVariables()
			.stream()
			.map(ConfigBomPerceivedVariable::getVarId)
			.filter(StringUtils::hasText)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		if (selectedIds.isEmpty() || !candidateById.keySet().containsAll(selectedIds)) {
			throw new IllegalArgumentException("所选感知变量不属于当前设备实例");
		}

		ConfigPerceivedTask task = null;
		if (request.getTaskId() != null) {
			task = this.getById(request.getTaskId());
			if (task == null || !Objects.equals(task.getNodeId(), request.getNodeId())
					|| !Objects.equals(task.getModelId(), request.getModelId())) {
				throw new IllegalArgumentException("任务与当前实例或模型不匹配");
			}
		}
		if (task == null) {
			List<ConfigPerceivedTask> matchingTasks = this.list(new LambdaQueryWrapper<ConfigPerceivedTask>()
				.eq(ConfigPerceivedTask::getNodeId, request.getNodeId())
				.eq(ConfigPerceivedTask::getModelId, request.getModelId())
				.orderByDesc(ConfigPerceivedTask::getTaskId));
			task = matchingTasks.isEmpty() ? null : matchingTasks.get(0);
		}

		boolean newTask = task == null;
		if (newTask) {
			task = new ConfigPerceivedTask();
			BeanUtils.copyProperties(model, task);
			task.setTaskId(null);
			task.setNodeId(request.getNodeId());
			task.setProId(node.getProId());
			task.setIsDefaultModel(0);
			task.setStatus(0);
		}
		task.setModelName(StringUtils.hasText(request.getModelName()) ? request.getModelName() : model.getModelName());
		task.setAlgoId(request.getAlgoId());
		task.setAlgoShortname(algoShortname);
		task.setDataDimension(request.getDataDimension());
		task.setVariableNum((long) selectedIds.size());

		if (newTask) {
			this.save(task);
		}
		else {
			this.updateById(task);
		}

		configPerceivedTaskVariableService.delTaskByTaskId(task.getTaskId());
		for (String variableId : selectedIds) {
			ConfigPerceivedTaskVariable taskVariable = new ConfigPerceivedTaskVariable();
			BeanUtils.copyProperties(candidateById.get(variableId), taskVariable);
			taskVariable.setId(null);
			taskVariable.setTaskId(task.getTaskId());
			configPerceivedTaskVariableService.save(taskVariable);
		}

		if (newTask) {
			createTopic(kafkaTopic.bootstrapServers, kafkaTopic.topicName + task.getTaskId(), kafkaTopic.partitions,
					kafkaTopic.replication);
		}
		return task;
	}

	public void createCompositionTemplate(String modelId, String bomNodeId) {
		LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTask::getNodeId, modelId);
		ConfigPerceivedTask configPerceivedTask1 = this.getOne(queryWrapper);
		if (configPerceivedTask1 != null) {
			// 情况一：对于已经生成过任务实例

			LambdaUpdateWrapper<ConfigPerceivedTask> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(ConfigPerceivedTask::getTaskId, configPerceivedTask1.getTaskId())
				.set(ConfigPerceivedTask::getStatus, 1);
			this.update(updateWrapper);

		}
		else {
			// 情况二：未生成过任务实例
			// 根据元结构树的modeltype查询获取模板信息
			ConfigModel configModel = configModelService.getById(modelId);
			ConfigPerceivedTask configPerceivedTask = new ConfigPerceivedTask();

			BeanUtils.copyProperties(configModel, configPerceivedTask);
			configPerceivedTask.setAlgoShortname(configModel.getAlgoId());

			configPerceivedTask.setIsDefaultModel(0);
			configPerceivedTask.setNodeId(bomNodeId);
			ConfigBomTree node = configBomTreeService.getNode(bomNodeId);
			configPerceivedTask.setProId(node.getProId());
			configPerceivedTask.setStatus(0);
			// 插入到config_perceived_task表中
			this.save(configPerceivedTask);

			// 根据modelid获取对应的感知变量
			List<ConfigModelVariable> configModelVariableList = configPerceivedVariableService
				.getIDByModelId(configModel.getModelId());
			for (ConfigModelVariable configModelVariable : configModelVariableList) {
				ConfigPerceivedTaskVariable configPerceivedTaskVariable = new ConfigPerceivedTaskVariable();
				LambdaQueryWrapper<ConfigPerceivedVariable> queryWrapper1 = new LambdaQueryWrapper<>();
				queryWrapper1.eq(ConfigPerceivedVariable::getVarId, configModelVariable.getVarId());
				ConfigPerceivedVariable configPerceivedVariable = configPerceivedVariableService.getOne(queryWrapper1);
				configPerceivedTaskVariable.setTaskId(configPerceivedTask.getTaskId());
				BeanUtils.copyProperties(configPerceivedVariable, configPerceivedTaskVariable);
				configPerceivedTaskVariableService.save(configPerceivedTaskVariable);
			}

			createTopic(kafkaTopic.bootstrapServers, kafkaTopic.topicName + configPerceivedTask.getTaskId().toString(),
					kafkaTopic.partitions, kafkaTopic.replication);

			LambdaUpdateWrapper<ConfigPerceivedTask> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(ConfigPerceivedTask::getTaskId, configPerceivedTask.getTaskId())
				.set(ConfigPerceivedTask::getVariableNum, configModelVariableList.size());
			this.update(updateWrapper);
		}
	}

	@Override
	public List<ConfigModelVo> getModelListByNodeId(String nodeId, String modelType) {
		// 查询默认模板
		LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTask::getNodeId, nodeId).eq(ConfigPerceivedTask::getModelType, modelType);
		List<ConfigPerceivedTask> taskList = this.getBaseMapper().selectList(queryWrapper);

		// 根据节点名字和nodeType获取元结构树的nodeId
		ConfigBomTree bomTree = configBomTreeService.getById(nodeId);

		if (bomTree.getNodeType().equals("Root")) {
			return null;
		}

		String parentName = configBomTreeMapper
			.getNodebyCode(bomTree.getNodeCode().substring(0, bomTree.getNodeCode().lastIndexOf("-")),
					bomTree.getTurbineCode())
			.getNodeName();

		List<ConfigGbomTree> list = configGbomTreeService.list(new QueryWrapper<ConfigGbomTree>().lambda()
			.eq(ConfigGbomTree::getNodeName, bomTree.getNodeName())
			.eq(ConfigGbomTree::getNodeType, bomTree.getNodeType()));

		String gNodeId = "";
		for (ConfigGbomTree configGbomTree : list) {
			// String parent = configGbomTreeMapper
			// .getNodebyCode(configGbomTree.getNodeCode().substring(0,configGbomTree.getNodeCode().lastIndexOf("-")))
			// .getNodeName();
			// 新增sceneId查询逻辑
			String turbineCode = bomTree.getTurbineCode();
			String projectNum = turbineCode.split("-")[0]; // 截取前面的数字
			String sceneId = configGbomTreeMapper.getSceneIdByNodeId(projectNum);
			String nodeCode = configGbomTree.getNodeCode().substring(0, configGbomTree.getNodeCode().lastIndexOf("-"));
			String parent = configGbomTreeMapper.getNodebyCodeAndSceneId(nodeCode, sceneId).getNodeName();
			if (parent.equals(parentName)) {
				gNodeId = configGbomTree.getNodeId();
				break;
			}
		}

		List<ConfigModel> modelList = configModelService.getByNodeIdAndModelType(gNodeId, modelType);
		for (ConfigPerceivedTask configPerceivedTask : taskList) {
			ConfigModel defaultModel = new ConfigModel();
			BeanUtils.copyProperties(configPerceivedTask, defaultModel);

			String modelId = configPerceivedTask.getModelId();

			// 查询除默认模板外得其他模板
			// string的相等不能用==，只能用equal
			modelList.removeIf(item -> item != null && Objects.equals(item.getModelId(), modelId));
			modelList.add(defaultModel);
		}

		List<ConfigModelVo> modelVoList = modelList.stream().map(new Function<ConfigModel, ConfigModelVo>() {
			@Override
			public ConfigModelVo apply(ConfigModel configModel) {
				ConfigModelVo configModelVo = new ConfigModelVo();
				BeanUtils.copyProperties(configModel, configModelVo);

				// 设置taskid
				LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
				queryWrapper.eq(ConfigPerceivedTask::getNodeId, nodeId);
				List<ConfigPerceivedTask> taskList = baseMapper.selectList(queryWrapper);
				for (ConfigPerceivedTask configPerceivedTask : taskList) {
					if (Objects.equals(configModel.getModelId(), configPerceivedTask.getModelId())) {
						configModelVo.setTaskId(configPerceivedTask.getTaskId());
						configModelVo.setStatus(configPerceivedTask.getStatus());
						configModelVo.setIsDefaultModel(configPerceivedTask.getIsDefaultModel());
						configModelVo.setAlgoShortname(configPerceivedTask.getAlgoShortname());
						configModelVo.setVariableNum(configPerceivedTask.getVariableNum());
						configModelVo.setDataDimension(configPerceivedTask.getDataDimension());
					}

				}

				return configModelVo;
			}
		}).collect(Collectors.toList());

		// 装配模板list返回
		return modelVoList;
	}

	@Override
	public int delTaskByTaskId(Long taskId) {
		System.out.println(taskId);
		LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTask::getTaskId, taskId);
		deleteTopic(kafkaTopic.bootstrapServers, kafkaTopic.topicName + taskId.toString());
		int delete = this.getBaseMapper().delete(queryWrapper);
		return delete;
	}

	@Override
	public ConfigPerceivedTask getModelInfoByTaskId(Long taskId) {
		ConfigPerceivedTask configPerceivedTask = this.getById(taskId);

		return configPerceivedTask;
	}

	@Override
	public List<AlStateEvaluation> getAll() {
		return this.getBaseMapper().getAll();
	}

	@Override
	public String operateTask(String modelId, String bomNodeId) throws Exception {

		LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTask::getNodeId, bomNodeId).eq(ConfigPerceivedTask::getModelId, modelId);
		ConfigPerceivedTask task = this.getBaseMapper().selectOne(queryWrapper);
		// /*请求东电推送数据的接口*/
		// //获取url
		String url = "http://127.0.0.1:6000/api/v1/cmsdatapost/scada/taskset";

		// 获取风场名称
		LambdaQueryWrapper<ConfigBomTree> queryWrapper1 = new LambdaQueryWrapper<>();
		queryWrapper1.eq(ConfigBomTree::getNodeId, task.getNodeId());
		String proId = configBomTreeService.getOne(queryWrapper1).getProId();
		String projectName = getProjectName(proId);

		// 获取风场机型
		String productModel = getProductModel(proId);

		// 获取风机名称
		LambdaQueryWrapper<ConfigBomTree> queryWrapper2 = new LambdaQueryWrapper<>();
		queryWrapper2.eq(ConfigBomTree::getTurbineCode, configBomTreeService.getOne(queryWrapper1).getTurbineCode())
			.eq(ConfigBomTree::getNodeType, "Root");
		String nodeName = configBomTreeService.getOne(queryWrapper2).getNodeName();

		ArrayList<String> vidList = new ArrayList<>();
		// 根据taskid查询模型所用的元感知变量
		List<ConfigPerceivedTaskVariable> vars = configPerceivedTaskVariableService
			.getVarsByTaskId(task.getTaskId().toString());

		// 获取选取节点id的turbinecode对应的感知变量
		ConfigBomTree configBomTree = configBomTreeService.getById(bomNodeId);
		LambdaQueryWrapper<ConfigBomPerceivedVariable> queryWrapper3 = new LambdaQueryWrapper<>();
		queryWrapper3.eq(ConfigBomPerceivedVariable::getTurbineCode, configBomTree.getTurbineCode());
		List<ConfigBomPerceivedVariable> p_vars = configBomPerceivedVariableService.getBaseMapper()
			.selectList(queryWrapper3);

		// 从风机感知变量中选出模型所用的元感知变量
		p_vars.retainAll(vars);// 只保留元感知变量的元素
		for (ConfigBomPerceivedVariable var : p_vars) {
			vidList.add(var.getVarId());
		}

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("farmName", projectName);
		jsonObject.put("productModel", productModel);
		jsonObject.put("turbineName", nodeName);
		jsonObject.put("taskID", task.getTaskId());
		jsonObject.put("frequency", 1);
		jsonObject.put("vid", vidList);
		// 接入东电接口使用
		// String body = JsonUtil.toJson(jsonObject);
		// String res = RestTemplateUtil.post(url, body);
		// JSONObject resObject = JsonUtil.fromJson(res, JSONObject.class);

		// 更新运行状态
		LambdaUpdateWrapper<ConfigPerceivedTask> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(ConfigPerceivedTask::getTaskId, task.getTaskId()).set(ConfigPerceivedTask::getStatus, 1);
		this.update(updateWrapper);

		return null; // 本地测试使用
		// return resObject;//接入东电接口使用
	}

	@Override
	public String stopTask(Long taskId) {
		// String url = "http://localhost:6000/api/v1/cmsdatapost/scada/taskcontrol";
		// JSONObject jsonObject = new JSONObject();
		// jsonObject.put("taskID", taskId);
		// jsonObject.put("option", 1);
		// String body = JsonUtil.toJson(jsonObject);
		// String res = RestTemplateUtil.post(url, body); //接入东电接口使用

		// 更新运行状态
		LambdaUpdateWrapper<ConfigPerceivedTask> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(ConfigPerceivedTask::getTaskId, taskId).set(ConfigPerceivedTask::getStatus, 0);
		this.update(updateWrapper);
		return "res";
		// return res;//接入东电接口使用
	}

	@Override
	public String getProjectName(String proId) {

		return this.getBaseMapper().getProjectName(proId);
	}

	@Override
	public String getProductModel(String proId) {
		return this.getBaseMapper().getProductModel(proId);
	}

	@Override
	public List<CompositionTaskVo> getCompositionTask(String nodeId) {
		// 查询默认模板
		LambdaQueryWrapper<ConfigPerceivedTask> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTask::getNodeId, nodeId)
			.eq(ConfigPerceivedTask::getModelType, "composition")
			.eq(ConfigPerceivedTask::getStatus, 1);
		List<ConfigPerceivedTask> taskList = this.getBaseMapper().selectList(queryWrapper);

		List<CompositionTaskVo> result = new ArrayList<>();

		// 遍历 taskList，为每个任务查找相关的组态数据
		for (ConfigPerceivedTask task : taskList) {
			String algoID = task.getAlgoId();

			// 根据 algoID 查找对应的 domain 表记录
			LambdaQueryWrapper<DomainModelConfiguration> domainQueryWrapper = new LambdaQueryWrapper<>();
			domainQueryWrapper.eq(DomainModelConfiguration::getCode, algoID);
			List<DomainModelConfiguration> domainList = domainModelConfigurationMapper.selectList(domainQueryWrapper);

			// 创建 ConfigModelVo 对象，将任务信息和 domain 记录一起封装
			CompositionTaskVo compositionTaskVo = new CompositionTaskVo();
			BeanUtils.copyProperties(task, compositionTaskVo);
			compositionTaskVo.setProcess(domainList);

			// 将构建的对象加入结果列表
			result.add(compositionTaskVo);
		}

		return result;
	}

	/**
	 * 创建topic
	 * @param bootstrapServers kafka集群地址
	 * 12.12.12.12:9092;12.12.12.10:9092;12.12.12.11:9092
	 * @param topicName topic的名称
	 * @param partitions 分区数
	 * @param replication 副本数
	 */
	public static boolean createTopic(String bootstrapServers, String topicName, int partitions, short replication) {
		Properties properties = new Properties();
		properties.put("bootstrap.servers", bootstrapServers);
		properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		AdminClient adminClient = null;
		try {
			adminClient = KafkaAdminClient.create(properties);
			NewTopic newTopic = new NewTopic(topicName, partitions, replication);
			CreateTopicsResult createTopicsResult = adminClient.createTopics(Lists.newArrayList(newTopic));
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		finally {
			if (adminClient != null) {
				adminClient.close();
			}
		}
		return true;
	}

	/**
	 * 删除topic的名称
	 * @param bootstrapServers kafka集群地址
	 * 12.12.12.12:9092;12.12.12.10:9092;12.12.12.11:9092
	 * @param topicName 删除的topic的名称
	 */
	public boolean deleteTopic(String bootstrapServers, String topicName) {
		Properties properties = new Properties();
		properties.put("bootstrap.servers", bootstrapServers);
		properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		AdminClient adminClient = null;
		try {
			adminClient = KafkaAdminClient.create(properties);
			adminClient.deleteTopics(Arrays.asList(topicName));
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (adminClient != null) {
				adminClient.close();
			}
		}
		return false;
	}

}
