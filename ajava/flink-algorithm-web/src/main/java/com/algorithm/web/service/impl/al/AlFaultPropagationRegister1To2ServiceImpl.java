package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlFaultPropagationRegister1to2Mapper;
import com.algorithm.web.model.entity.al.AlFaultPropagationRegister1to2;
import com.algorithm.web.model.vo.AlFaultPropagationRegister1to2Vo;
import com.algorithm.web.service.al.AlFaultPropagationRegister1to2Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlFaultPropagationRegister1To2ServiceImpl
		extends ServiceImpl<AlFaultPropagationRegister1to2Mapper, AlFaultPropagationRegister1to2>
		implements AlFaultPropagationRegister1to2Service {

	@Autowired
	private AlFaultPropagationRegister1to2Mapper alFaultPropagationRegister1To2Mapper;

	@Override
	public void deletebyname(String modelName) {
		alFaultPropagationRegister1To2Mapper.deletebyname(modelName);
	}

	@Override
	public IPage<AlFaultPropagationRegister1to2Vo> selectClassPage(Page page, String modelName) {
		return alFaultPropagationRegister1To2Mapper.selectClassPage(page, modelName);
	}

}
