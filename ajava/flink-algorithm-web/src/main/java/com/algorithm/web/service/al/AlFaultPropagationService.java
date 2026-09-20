package com.algorithm.web.service.al;

import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlFaultPropagation;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlFaultPropagationService extends IService<AlFaultPropagation> {

	String getnamebyid(long id);

	AlFaultPropagation getbyname(String modelName);

	IPage<AlFaultPropagation> getPage(IPage page, AlFaultPropagation alFaultPropagation);

	IPage<AlFaultPropagation> getPageObj(IPage page, AlFaultPropagation alFaultPropagation);

	IPage<AlFaultPropagation> getPageName(IPage page, AlFaultPropagation alFaultPropagation);

	AlFaultPropagation getbyId(long d);

	Boolean exitName(String name);

	Object operate(String useCase, DiagnoseInfoDto diagnoseInfoDto);

	/**
	 * 注册时更新code
	 * @param modelName
	 * @return
	 */
	boolean updateCodeByName(String modelName);

}
