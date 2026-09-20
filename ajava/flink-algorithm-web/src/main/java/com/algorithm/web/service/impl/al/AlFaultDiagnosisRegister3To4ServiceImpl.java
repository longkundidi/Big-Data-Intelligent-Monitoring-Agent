package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlFaultDiagnosisRegister3to4Mapper;
import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister3to4;
import com.algorithm.web.service.al.AlFaultDiagnosisRegister3to4Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlFaultDiagnosisRegister3To4ServiceImpl
		extends ServiceImpl<AlFaultDiagnosisRegister3to4Mapper, AlFaultDiagnosisRegister3to4>
		implements AlFaultDiagnosisRegister3to4Service {

	@Autowired
	private AlFaultDiagnosisRegister3to4Mapper alFaultDiagnosisRegister3To4Mapper;

	@Override
	public void deletebyname(String modelName) {
		alFaultDiagnosisRegister3To4Mapper.deletebyname(modelName);
	}

}
