package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.al.AlDataMining;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlDataMiningService extends IService<AlDataMining> {

	AlDataMining getbyname(String alName);

	IPage<AlDataMining> getPage(IPage page, AlDataMining alDataMining);

	int[] getTypeNum(String alType);

	Boolean exitName(String name);

	/**
	 * 注册时更新code
	 * @param alName
	 * @return
	 */
	boolean updateCodeByName(String alName);

}
