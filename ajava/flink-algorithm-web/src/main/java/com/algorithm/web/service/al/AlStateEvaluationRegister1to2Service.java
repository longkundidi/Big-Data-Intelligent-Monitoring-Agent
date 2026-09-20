package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlStateEvaluationRegister1to2;
import com.algorithm.web.model.vo.AlStateEvaluationRegister1to2Vo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.baomidou.mybatisplus.core.metadata.IPage;

public interface AlStateEvaluationRegister1to2Service extends IService<AlStateEvaluationRegister1to2> {

	void deletebyname(String modelName);

	IPage<AlStateEvaluationRegister1to2Vo> selectClassPage(Page page, String modelName);

}
