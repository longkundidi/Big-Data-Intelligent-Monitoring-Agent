package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlStateEvaluation;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlStateEvaluationService extends IService<AlStateEvaluation> {

	String getnamebyid(long id);

	AlStateEvaluation getbyname(String modelName);

	IPage<AlStateEvaluation> getPage(IPage page, AlStateEvaluation alStateEvaluation);

	IPage<AlStateEvaluation> getPageObj(IPage page, AlStateEvaluation alStateEvaluation);

	IPage<AlStateEvaluation> getPageName(IPage page, AlStateEvaluation alStateEvaluation);

	AlStateEvaluation getbyId(long id);

	Boolean exitName(String name);

	/**
	 * 注册时更新code
	 * @param modelName
	 * @return
	 */
	boolean updateCodeByName(String modelName);

}
