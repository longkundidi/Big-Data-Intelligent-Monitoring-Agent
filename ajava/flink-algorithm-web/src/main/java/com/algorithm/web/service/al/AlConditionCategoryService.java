package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlConditionCategory;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlConditionCategoryService extends IService<AlConditionCategory> {

	String getnamebyid(long id);

	AlConditionCategory getbyname(String modelName);

	IPage<AlConditionCategory> getPage(IPage page, AlConditionCategory alConditionCategory);

	IPage<AlConditionCategory> getPageObj(IPage page, AlConditionCategory alConditionCategory);

	IPage<AlConditionCategory> getPageName(IPage page, AlConditionCategory alConditionCategory);

	AlConditionCategory getbyId(long d);

	Boolean exitName(String name);

	/**
	 * 注册时更新code
	 * @param modelName
	 * @return
	 */
	boolean updateCodeByName(String modelName);

}
