package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlKnowledgeExtraction;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlKnowledgeExtractionService extends IService<AlKnowledgeExtraction> {

	AlKnowledgeExtraction getbyname(String alName);

	IPage<AlKnowledgeExtraction> getPage(IPage page, AlKnowledgeExtraction alKnowledgeExtraction);

	int[] getTypeNum(String alType);

	Boolean exitName(String name);

	/**
	 * 注册时更新code
	 * @param alName
	 * @return
	 */
	boolean updateCodeByName(String alName);

}
