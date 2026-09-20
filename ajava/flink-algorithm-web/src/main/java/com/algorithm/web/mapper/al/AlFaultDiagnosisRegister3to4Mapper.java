package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlFaultDiagnosisRegister3to4;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlFaultDiagnosisRegister3to4Mapper extends BaseMapper<AlFaultDiagnosisRegister3to4> {

	void deletebyname(String modelName);

}
