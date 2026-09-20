package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlFaultPropagationRegister3to4Mapper;
import com.algorithm.web.model.entity.al.AlFaultPropagationRegister3to4;
import com.algorithm.web.service.al.AlFaultPropagationRegister3to4Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlFaultPropagationRegister3To4ServiceImpl
		extends ServiceImpl<AlFaultPropagationRegister3to4Mapper, AlFaultPropagationRegister3to4>
		implements AlFaultPropagationRegister3to4Service {

	@Autowired
	private AlFaultPropagationRegister3to4Mapper alFaultPropagationRegister3To4Mapper;

	@Override
	public void deletebyname(String modelName) {
		alFaultPropagationRegister3To4Mapper.deletebyname(modelName);
	}

}
