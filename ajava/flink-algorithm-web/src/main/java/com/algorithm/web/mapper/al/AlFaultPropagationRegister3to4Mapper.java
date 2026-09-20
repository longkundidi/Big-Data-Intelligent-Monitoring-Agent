package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlFaultPropagationRegister3to4;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlFaultPropagationRegister3to4Mapper extends BaseMapper<AlFaultPropagationRegister3to4> {

	void deletebyname(String modelName);

}
