package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlStateEvaluationRegister3to4;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlStateEvaluationRegister3to4Mapper extends BaseMapper<AlStateEvaluationRegister3to4> {

	void deletebyname(String modelName);

}
