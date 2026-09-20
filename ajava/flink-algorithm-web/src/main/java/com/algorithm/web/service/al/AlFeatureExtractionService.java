package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlFeatureExtraction;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlFeatureExtractionService extends IService<AlFeatureExtraction> {

	AlFeatureExtraction getbyname(String alName);

	IPage<AlFeatureExtraction> getPage(IPage page, AlFeatureExtraction alFeatureExtraction);

	int[] getTypeNum(String alType);

	Boolean exitName(String name);

	/**
	 * 注册时更新code
	 * @param alName
	 * @return
	 */
	boolean updateCodeByName(String alName);

}
