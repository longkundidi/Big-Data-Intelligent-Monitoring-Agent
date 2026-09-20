package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.*;
import com.algorithm.web.model.entity.al.*;
import com.algorithm.web.model.entity.taskConfig.ServerList;
import com.algorithm.web.model.entity.taskConfig.vo.TaskVo;
import com.algorithm.web.service.al.*;
import com.algorithm.web.service.taskConfig.ServerListService;
import com.algorithm.web.strategy.QueryStrategy;
import com.algorithm.web.strategy.QueryStrategyFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlTaskServiceImpl extends ServiceImpl<AlTaskMapper, AlTask> implements AlTaskService {

	@Autowired
	AlTaskMapper alTaskMapper;

	@Autowired
	AlDataCleanMapper alDataCleanMapper;

	@Autowired
	AlKnowledgeExtractionMapper alKnowledgeExtractionMapper;

	@Autowired
	AlFaultDiagnosisMapper alFaultDiagnosisMapper;

	@Autowired
	AlStateEvaluationMapper alStateEvaluationMapper;

	@Autowired
	private QueryStrategyFactory queryStrategyFactory;

	@Autowired
	private AlDataCleanService alDataCleanService;

	@Autowired
	private AlKnowledgeExtractionService alKnowledgeExtractionService;

	private final AlFaultDiagnosisService alFaultDiagnosisService;

	@Autowired
	public AlTaskServiceImpl(AlFaultDiagnosisService alFaultDiagnosisService) {
		this.alFaultDiagnosisService = alFaultDiagnosisService;
	}

	@Autowired
	private AlStateEvaluationService alStateEvaluationService;

	@Autowired
	private DomainModelService domainModelService;

	@Override
	public Page<AlTaskVo> getPage(Page page, String serverName) {

		// 查询所有记录
		List<AlTask> tasks = alTaskMapper.selectAll(alTaskMapper.getUrlByName(serverName));

		// 使用 Map 存储每个 alName 及其对应的最新记录
		Map<String, AlTask> latestTasks = new HashMap<>();
		for (AlTask task : tasks) {
			String key = task.getAlName();
			if (!latestTasks.containsKey(key) || task.getCreateTime().isAfter(latestTasks.get(key).getCreateTime())) {
				latestTasks.put(key, task);
			}
		}

		// 处理 taskId 为 null 的情况
		latestTasks.values().forEach(task -> {
			if (task.getTaskId() == null) {
				task.setTaskId(task.getId());
			}
		});

		// 将 Map 中的记录转换为 List
		List<AlTask> filteredTasks = latestTasks.values()
			.stream()
			.sorted(Comparator.comparing(AlTask::getTaskId).reversed())
			.collect(Collectors.toList());

		ArrayList<AlTaskVo> alTaskVos = new ArrayList<>();
		// 加入is_service
		for (AlTask task : filteredTasks) {
			AlTaskVo alTaskVo = new AlTaskVo();
			BeanUtils.copyProperties(task, alTaskVo);
			// 根据alid和alclass选出算法，并设置任务的is_service为该算法的is_service
			String alClass = task.getAlClass();
			Long alId = task.getAlId();
			Integer isService = 0;

			if (alClass.equals("alDataCleaning")) {
				isService = alDataCleanService.getById(alId).getIsService();
			}
			if (alClass.equals("alFaultDiagnosis")) {
				isService = alFaultDiagnosisService.getById(alId).getIsService();
			}
			if (alClass.equals("alKnowledgeExtraction")) {
				isService = alKnowledgeExtractionService.getById(alId).getIsService();
			}
			if (alClass.equals("alStateEvaluation")) {
				isService = alStateEvaluationService.getById(alId).getIsService();
			}
			alTaskVo.setIsService(isService);
			alTaskVo.setAlId(alId);
			alTaskVos.add(alTaskVo);
			alTaskVo.setAlClass(alClass);
		}
		// 设置分页信息
		page.setTotal(alTaskVos.size());
		long fromIndex = (page.getCurrent() - 1) * page.getSize();
		long toIndex = Math.min(fromIndex + page.getSize(), alTaskVos.size());
		List<AlTaskVo> pageTasks = alTaskVos.subList((int) fromIndex, (int) toIndex);

		// 为每条记录设置序号
		long startIndex = (page.getCurrent() - 1) * page.getSize() + 1;
		for (int i = 0; i < pageTasks.size(); i++) {
			AlTaskVo task = pageTasks.get(i);
			task.setId(startIndex + i); // 设置序号

			// 根据 alClass 获取相应的策略并查询 type
			QueryStrategy strategy = queryStrategyFactory.getStrategy(task.getAlClass());
			task.setAlType(strategy.getType(task));
			task.setAlNum(strategy.getNum(task));
			task.setCreator(strategy.getCreator(task));
		}

		page.setRecords(pageTasks);

		return page;
	}

	@Override
	public Page<AlTask> getByName(Page page, String alModelName) {
		List<AlTask> alTasks = new ArrayList<>();

		String pattern = "测试";
		if (alModelName.matches(".*?(" + pattern + ").*")) {
			alTasks.add(alTaskMapper.getAutoTest());
		}

		List<AlDataClean> alDataCleans = alDataCleanMapper.getByNameLike(alModelName);
		if (alDataCleans != null && !alDataCleans.isEmpty()) {
			for (AlDataClean al : alDataCleans) {
				alTasks.addAll(alTaskMapper.searchTasks(al.getId(), "alDataCleaning"));
			}
		}

		List<AlKnowledgeExtraction> alKnowledgeExtractions = alKnowledgeExtractionMapper.getByNameLike(alModelName);
		if (alKnowledgeExtractions != null && !alKnowledgeExtractions.isEmpty()) {
			for (AlKnowledgeExtraction al : alKnowledgeExtractions) {
				alTasks.addAll(alTaskMapper.searchTasks(al.getId(), "alKnowledgeExtraction"));
			}
		}

		List<AlFaultDiagnosis> alFaultDiagnoses = alFaultDiagnosisMapper.getByNameLike(alModelName);
		if (alFaultDiagnoses != null && !alFaultDiagnoses.isEmpty()) {
			for (AlFaultDiagnosis model : alFaultDiagnoses) {
				alTasks.addAll(alTaskMapper.searchTasks(model.getId().intValue(), "alFaultDiagnosis"));
			}
		}

		List<AlStateEvaluation> alStateEvaluations = alStateEvaluationMapper.getByNameLike(alModelName);
		if (alStateEvaluations != null && !alStateEvaluations.isEmpty()) {
			for (AlStateEvaluation model : alStateEvaluations) {
				alTasks.addAll(alTaskMapper.searchTasks(model.getId().intValue(), "alStateEvaluation"));
			}
		}
		int totalRecords = alTasks.size();
		long fromIndex = (page.getCurrent() - 1) * page.getSize();
		long toIndex = Math.min(fromIndex + page.getSize(), totalRecords);

		if (fromIndex > totalRecords) {
			fromIndex = totalRecords;
		}

		List<AlTask> paginatedTasks = alTasks.subList((int) fromIndex, (int) toIndex);
		page.setRecords(paginatedTasks);
		page.setTotal(paginatedTasks.size());
		return page;
	}

	@Override
	public boolean updateIsService(AlTaskVo alTaskVo) {
		Long alId = alTaskVo.getAlId();
		String alClass = alTaskVo.getAlClass();
		Integer isService = (alTaskVo.getIsService() == 0) ? 1 : 0;
		;

		if (alClass.equals("alDataCleaning")) {
			LambdaUpdateWrapper<AlDataClean> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(AlDataClean::getId, alId).set(AlDataClean::getIsService, isService);
			alDataCleanService.update(updateWrapper);
		}
		if (alClass.equals("alFaultDiagnosis")) {
			LambdaUpdateWrapper<AlFaultDiagnosis> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(AlFaultDiagnosis::getId, alId).set(AlFaultDiagnosis::getIsService, isService);
			alFaultDiagnosisService.update(updateWrapper);
		}
		if (alClass.equals("alKnowledgeExtraction")) {
			LambdaUpdateWrapper<AlKnowledgeExtraction> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(AlKnowledgeExtraction::getId, alId).set(AlKnowledgeExtraction::getIsService, isService);
			alKnowledgeExtractionService.update(updateWrapper);
		}
		if (alClass.equals("alStateEvaluation")) {
			LambdaUpdateWrapper<AlStateEvaluation> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(AlStateEvaluation::getId, alId).set(AlStateEvaluation::getIsService, isService);
			alStateEvaluationService.update(updateWrapper);
		}
		if (alClass.equals("domainModel")) {
			LambdaUpdateWrapper<DomainModel> updateWrapper = new LambdaUpdateWrapper<>();
			updateWrapper.eq(DomainModel::getId, alId).set(DomainModel::getIsService, isService);
			domainModelService.update(updateWrapper);
		}

		return true;
	}

}
