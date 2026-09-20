package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlResourceSchedulingRegister3to4Mapper;
import com.algorithm.web.model.entity.al.AlResourceSchedulingRegister3to4;
import com.algorithm.web.service.al.AlResourceSchedulingRegister3to4Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlResourceSchedulingRegister3To4ServiceImpl
		extends ServiceImpl<AlResourceSchedulingRegister3to4Mapper, AlResourceSchedulingRegister3to4>
		implements AlResourceSchedulingRegister3to4Service {

	@Autowired
	private AlResourceSchedulingRegister3to4Mapper alResourceSchedulingRegister3To4Mapper;

	@Override
	public void deletebyname(String modelName) {
		alResourceSchedulingRegister3To4Mapper.deletebyname(modelName);
	}

}
