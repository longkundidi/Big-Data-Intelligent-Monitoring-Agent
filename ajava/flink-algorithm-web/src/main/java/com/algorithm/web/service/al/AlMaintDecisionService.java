package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlMaintDecision;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlMaintDecisionService extends IService<AlMaintDecision> {

	String getnamebyid(long id);

	AlMaintDecision getbyname(String modelName);

	IPage<AlMaintDecision> getPage(IPage page, AlMaintDecision alMaintDecision);

	IPage<AlMaintDecision> getPageObj(IPage page, AlMaintDecision alMaintDecision);

	IPage<AlMaintDecision> getPageName(IPage page, AlMaintDecision alMaintDecision);

	AlMaintDecision getbyId(long d);

	Boolean exitName(String name);

	/**
	 * 注册时更新code
	 * @param modelName
	 * @return
	 */
	boolean updateCodeByName(String modelName);

}
