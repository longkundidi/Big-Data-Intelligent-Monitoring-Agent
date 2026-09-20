package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.*;
import com.algorithm.web.model.entity.al.DomainModel;
import com.algorithm.web.service.al.DomainModelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 领域模型基本信息表(DomainModel)表服务实现类
 *
 * @author makejava
 * @since 2024-11-12 17:46:21
 */
@Service
public class DomainModelServiceImpl extends ServiceImpl<DomainModelMapper, DomainModel> implements DomainModelService {

	@Autowired
	AlStateEvaluationMapper alStateEvaluationMapper;

	@Autowired
	AlFaultDiagnosisMapper alFaultDiagnosisMapper;

	@Autowired
	AlConditionCategoryMapper alConditionCategoryMapper;

	@Autowired
	AlFaultPropagationMapper alFaultPropagationMapper;

	@Autowired
	AlResourceSchedulingMapper alResourceSchedulingMapper;

	@Autowired
	AlMaintDecisionMapper alMaintDecisionMapper;

	private final Map<String, Function<Long, Map<String, Object>>> typeHandlers = new HashMap<>();

	@Override
	public Boolean exitName(String name) {
		String modelName = this.getBaseMapper().exitName(name);
		return modelName != null;
	}

	@Override
	public List<DomainModel> getlistByBasicAlgorithm(String basicAlgorithm, String modelType) {
		LambdaQueryWrapper<DomainModel> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(DomainModel::getBasicAlgorithm, basicAlgorithm).eq(DomainModel::getModelType, modelType);
		return this.list(queryWrapper);
	}

	@PostConstruct
	public void initializeTypeHandlers() {
		typeHandlers.put("classify", alId -> alConditionCategoryMapper.getCurrentObject(alId));
		typeHandlers.put("evaluation", alId -> alStateEvaluationMapper.getCurrentObject(alId));
		typeHandlers.put("diagnosis", alId -> alFaultDiagnosisMapper.getCurrentObject(alId));
		typeHandlers.put("decision", alId -> alMaintDecisionMapper.getCurrentObject(alId));
		typeHandlers.put("scheduling", alId -> alResourceSchedulingMapper.getCurrentObject(alId));
		typeHandlers.put("transmit", alId -> alFaultPropagationMapper.getCurrentObject(alId));
	}

	@Override
	public Map<String, Object> getCurrentObject(Long alId, String type) {
		Function<Long, Map<String, Object>> handler = typeHandlers.get(type);
		if (handler == null) {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
		return handler.apply(alId);
	}

}
