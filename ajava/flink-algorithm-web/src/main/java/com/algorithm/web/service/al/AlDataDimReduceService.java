package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.al.AlDataDimReduce;
import com.algorithm.web.model.entity.al.AlDataMining;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlDataDimReduceService extends IService<AlDataDimReduce> {

	AlDataDimReduce getbyname(String alName);

	IPage<AlDataDimReduce> getPage(IPage page, AlDataDimReduce alDataDimReduce);

	int[] getTypeNum(String alType);

	Boolean exitName(String name);

	/**
	 * 注册时更新code
	 * @param alName
	 * @return
	 */
	boolean updateCodeByName(String alName);

}
