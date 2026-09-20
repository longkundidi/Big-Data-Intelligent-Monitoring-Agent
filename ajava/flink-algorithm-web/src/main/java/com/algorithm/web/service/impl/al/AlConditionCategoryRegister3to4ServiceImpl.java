package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlConditionCategoryRegister3to4Mapper;
import com.algorithm.web.model.entity.al.AlConditionCategoryRegister3to4;
import com.algorithm.web.service.al.AlConditionCategoryRegister3to4Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlConditionCategoryRegister3to4ServiceImpl
		extends ServiceImpl<AlConditionCategoryRegister3to4Mapper, AlConditionCategoryRegister3to4>
		implements AlConditionCategoryRegister3to4Service {

	@Autowired
	private AlConditionCategoryRegister3to4Mapper alConditionCategoryRegister3to4Mapper;

	@Override
	public void deletebyname(String modelName) {
		alConditionCategoryRegister3to4Mapper.deletebyname(modelName);
	}

}
