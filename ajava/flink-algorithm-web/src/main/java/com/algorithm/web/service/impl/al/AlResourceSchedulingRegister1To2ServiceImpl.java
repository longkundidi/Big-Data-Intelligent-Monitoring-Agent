package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlResourceSchedulingRegister1to2Mapper;
import com.algorithm.web.model.entity.al.AlResourceSchedulingRegister1to2;
import com.algorithm.web.model.vo.AlResourceSchedulingRegister1to2Vo;
import com.algorithm.web.service.al.AlResourceSchedulingRegister1to2Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlResourceSchedulingRegister1To2ServiceImpl
		extends ServiceImpl<AlResourceSchedulingRegister1to2Mapper, AlResourceSchedulingRegister1to2>
		implements AlResourceSchedulingRegister1to2Service {

	@Autowired
	private AlResourceSchedulingRegister1to2Mapper alResourceSchedulingRegister1To2Mapper;

	@Override
	public void deletebyname(String modelName) {
		alResourceSchedulingRegister1To2Mapper.deletebyname(modelName);
	}

	@Override
	public IPage<AlResourceSchedulingRegister1to2Vo> selectClassPage(Page page, String modelName) {
		return alResourceSchedulingRegister1To2Mapper.selectClassPage(page, modelName);
	}

}
