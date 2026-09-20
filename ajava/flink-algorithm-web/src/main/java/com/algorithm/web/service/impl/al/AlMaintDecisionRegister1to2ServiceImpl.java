package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlMaintDecisionRegister1to2Mapper;
import com.algorithm.web.model.entity.al.AlMaintDecisionRegister1to2;
import com.algorithm.web.model.vo.AlMaintDecisionRegister1to2Vo;
import com.algorithm.web.service.al.AlMaintDecisionRegister1to2Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlMaintDecisionRegister1to2ServiceImpl
		extends ServiceImpl<AlMaintDecisionRegister1to2Mapper, AlMaintDecisionRegister1to2>
		implements AlMaintDecisionRegister1to2Service {

	@Autowired
	private AlMaintDecisionRegister1to2Mapper alMaintDecisionRegister1to2Mapper;

	@Override
	public void deletebyname(String modelName) {
		alMaintDecisionRegister1to2Mapper.deletebyname(modelName);
	}

	@Override
	public IPage<AlMaintDecisionRegister1to2Vo> selectClassPage(Page page, String modelName) {
		return alMaintDecisionRegister1to2Mapper.selectClassPage(page, modelName);
	}

}
