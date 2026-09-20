package com.algorithm.web.strategy;

import com.algorithm.web.mapper.al.AlDataCleanMapper;
import com.algorithm.web.model.entity.al.AlTaskVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlDataCleanQueryStrategy implements QueryStrategy {

	@Autowired
	private AlDataCleanMapper alDataCleanMapper;

	@Override
	public String getType(AlTaskVo alTask) {
		return alDataCleanMapper.getTypeById((alTask.getAlId()));
	}

	@Override
	public String getCreator(AlTaskVo alTask) {
		return alDataCleanMapper.getCreatorById(alTask.getAlId());
	}

	@Override
	public Long getNum(AlTaskVo alTask) {
		return alDataCleanMapper.getNumById(alTask.getAlId());
	}

}
