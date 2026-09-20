package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlStateEvaluationRegister1to2Mapper;
import com.algorithm.web.model.entity.al.AlStateEvaluationRegister1to2;
import com.algorithm.web.model.vo.AlStateEvaluationRegister1to2Vo;
import com.algorithm.web.service.al.AlStateEvaluationRegister1to2Service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlStateEvaluationRegister1to2ServiceImpl
		extends ServiceImpl<AlStateEvaluationRegister1to2Mapper, AlStateEvaluationRegister1to2>
		implements AlStateEvaluationRegister1to2Service {

	@Autowired
	private AlStateEvaluationRegister1to2Mapper alStateEvaluationRegister1to2Mapper;

	@Override
	public void deletebyname(String modelName) {
		alStateEvaluationRegister1to2Mapper.deletebyname(modelName);
	}

	@Override
	public IPage<AlStateEvaluationRegister1to2Vo> selectClassPage(Page page, String modelName) {
		return alStateEvaluationRegister1to2Mapper.selectClassPage(page, modelName);
	}

}
