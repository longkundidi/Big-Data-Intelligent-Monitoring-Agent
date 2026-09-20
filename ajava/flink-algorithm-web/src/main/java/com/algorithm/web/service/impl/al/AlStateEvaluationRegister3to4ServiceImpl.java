package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlStateEvaluationRegister3to4Mapper;
import com.algorithm.web.model.entity.al.AlStateEvaluationRegister3to4;
import com.algorithm.web.service.al.AlStateEvaluationRegister3to4Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlStateEvaluationRegister3to4ServiceImpl
		extends ServiceImpl<AlStateEvaluationRegister3to4Mapper, AlStateEvaluationRegister3to4>
		implements AlStateEvaluationRegister3to4Service {

	@Autowired
	private AlStateEvaluationRegister3to4Mapper alStateEvaluationRegister3to4Mapper;

	@Override
	public void deletebyname(String modelName) {
		alStateEvaluationRegister3to4Mapper.deletebyname(modelName);
	}

}
