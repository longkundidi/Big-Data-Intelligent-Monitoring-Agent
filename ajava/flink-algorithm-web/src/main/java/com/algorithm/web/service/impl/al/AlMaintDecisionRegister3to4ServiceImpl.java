package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlMaintDecisionRegister3to4Mapper;
import com.algorithm.web.model.entity.al.AlMaintDecisionRegister3to4;
import com.algorithm.web.service.al.AlMaintDecisionRegister3to4Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlMaintDecisionRegister3to4ServiceImpl
		extends ServiceImpl<AlMaintDecisionRegister3to4Mapper, AlMaintDecisionRegister3to4>
		implements AlMaintDecisionRegister3to4Service {

	@Autowired
	private AlMaintDecisionRegister3to4Mapper alMaintDecisionRegister3to4Mapper;

	@Override
	public void deletebyname(String modelName) {
		alMaintDecisionRegister3to4Mapper.deletebyname(modelName);
	}

}
