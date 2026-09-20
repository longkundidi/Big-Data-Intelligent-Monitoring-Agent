package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlFaultDiagnosisRegister1to2Mapper;
import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister1to2;
import com.algorithm.web.model.vo.AlFaultDiagnosisRegister1to2Vo;
import com.algorithm.web.service.al.AlFaultDiagnosisRegister1to2Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlFaultDiagnosisRegister1To2ServiceImpl
		extends ServiceImpl<AlFaultDiagnosisRegister1to2Mapper, AlFaultDiagnosisRegister1to2>
		implements AlFaultDiagnosisRegister1to2Service {

	@Autowired
	private AlFaultDiagnosisRegister1to2Mapper alFaultDiagnosisRegister1To2Mapper;

	@Override
	public void deletebyname(String modelName) {
		alFaultDiagnosisRegister1To2Mapper.deletebyname(modelName);
	}

	@Override
	public IPage<AlFaultDiagnosisRegister1to2Vo> selectClassPage(Page page, String modelName) {
		return alFaultDiagnosisRegister1To2Mapper.selectClassPage(page, modelName);
	}

}
