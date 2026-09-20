package com.algorithm.web.strategy;

import com.algorithm.web.mapper.al.AlFaultDiagnosisMapper;
import com.algorithm.web.model.entity.al.AlTaskVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlFaultDiagnosisQueryStrategy implements QueryStrategy {

	@Autowired
	private AlFaultDiagnosisMapper alFaultDiagnosisMapper;

	@Override
	public String getType(AlTaskVo alTask) {
		return alFaultDiagnosisMapper.getTypeById(alTask.getAlId());
	}

	@Override
	public String getCreator(AlTaskVo alTask) {
		return alFaultDiagnosisMapper.getCreatorById(alTask.getAlId());
	}

	@Override
	public Long getNum(AlTaskVo alTask) {
		return alFaultDiagnosisMapper.getNumById(alTask.getAlId());
	}

}
