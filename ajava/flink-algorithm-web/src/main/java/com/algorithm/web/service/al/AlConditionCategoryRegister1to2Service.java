package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlConditionCategoryRegister1to2;
import com.algorithm.web.model.vo.AlConditionCategoryRegister1to2Vo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlConditionCategoryRegister1to2Service extends IService<AlConditionCategoryRegister1to2> {

	void deletebyname(String modelName);

	IPage<AlConditionCategoryRegister1to2Vo> selectClassPage(Page page, String modelName);

}
