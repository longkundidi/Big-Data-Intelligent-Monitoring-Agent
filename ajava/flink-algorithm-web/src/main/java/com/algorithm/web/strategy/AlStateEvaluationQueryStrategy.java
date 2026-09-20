package com.algorithm.web.strategy;

import com.algorithm.web.mapper.al.AlStateEvaluationMapper;
import com.algorithm.web.model.entity.al.AlTaskVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlStateEvaluationQueryStrategy implements QueryStrategy {

	@Autowired
	private AlStateEvaluationMapper alStateEvaluationMapper;

	@Override
	public String getType(AlTaskVo alTask) {
		return alStateEvaluationMapper.getTypeById(alTask.getAlId());
	}

	@Override
	public String getCreator(AlTaskVo alTask) {
		return alStateEvaluationMapper.getCreatorById(alTask.getAlId());
	}

	@Override
	public Long getNum(AlTaskVo alTask) {
		return alStateEvaluationMapper.getNumById(alTask.getAlId());
	}

}
