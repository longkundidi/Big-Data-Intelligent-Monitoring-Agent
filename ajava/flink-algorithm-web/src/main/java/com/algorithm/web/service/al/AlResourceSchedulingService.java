package com.algorithm.web.service.al;

import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlResourceScheduling;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlResourceSchedulingService extends IService<AlResourceScheduling> {

	String getnamebyid(long id);

	AlResourceScheduling getbyname(String modelName);

	IPage<AlResourceScheduling> getPage(IPage page, AlResourceScheduling alResourceScheduling);

	IPage<AlResourceScheduling> getPageObj(IPage page, AlResourceScheduling alResourceScheduling);

	IPage<AlResourceScheduling> getPageName(IPage page, AlResourceScheduling alResourceScheduling);

	AlResourceScheduling getbyId(long d);

	Boolean exitName(String name);

	Object operate(String useCase, DiagnoseInfoDto diagnoseInfoDto);

	/**
	 * 注册时更新code
	 * @param modelName
	 * @return
	 */
	boolean updateCodeByName(String modelName);

}
