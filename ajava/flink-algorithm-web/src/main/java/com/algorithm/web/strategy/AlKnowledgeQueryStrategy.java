package com.algorithm.web.strategy;

import com.algorithm.web.mapper.al.AlKnowledgeExtractionMapper;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.model.entity.al.AlTaskVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlKnowledgeQueryStrategy implements QueryStrategy {

	@Autowired
	private AlKnowledgeExtractionMapper alKnowledgeExtractionMapper;

	@Override
	public String getType(AlTaskVo alTask) {
		return alKnowledgeExtractionMapper.getTypeById(alTask.getAlId());
	}

	@Override
	public String getCreator(AlTaskVo alTask) {
		return alKnowledgeExtractionMapper.getCreatorById(alTask.getAlId());
	}

	@Override
	public Long getNum(AlTaskVo alTask) {
		return alKnowledgeExtractionMapper.getNumById(alTask.getAlId());
	}

}
