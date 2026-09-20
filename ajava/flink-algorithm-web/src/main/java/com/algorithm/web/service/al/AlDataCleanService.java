package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlDataClean;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlDataCleanService extends IService<AlDataClean> {

	AlDataClean getbyname(String alName);

	IPage<AlDataClean> getPage(IPage page, AlDataClean alDataClean);

	int[] getTypeNum(String alType);

	Boolean exitName(String name);

	/**
	 * 注册时更新code
	 * @param alName
	 * @return
	 */
	boolean updateCodeByName(String alName);

}
