package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlConditionCategoryRegister1to2Mapper;
import com.algorithm.web.model.entity.al.AlConditionCategoryRegister1to2;
import com.algorithm.web.model.vo.AlConditionCategoryRegister1to2Vo;
import com.algorithm.web.service.al.AlConditionCategoryRegister1to2Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlConditionCategoryRegister1to2ServiceImpl
		extends ServiceImpl<AlConditionCategoryRegister1to2Mapper, AlConditionCategoryRegister1to2>
		implements AlConditionCategoryRegister1to2Service {

	@Autowired
	private AlConditionCategoryRegister1to2Mapper alConditionCategoryRegister1to2Mapper;

	@Override
	public void deletebyname(String modelName) {
		alConditionCategoryRegister1to2Mapper.deletebyname(modelName);
	}

	@Override
	public IPage<AlConditionCategoryRegister1to2Vo> selectClassPage(Page page, String modelName) {
		return alConditionCategoryRegister1to2Mapper.selectClassPage(page, modelName);
	}

}
