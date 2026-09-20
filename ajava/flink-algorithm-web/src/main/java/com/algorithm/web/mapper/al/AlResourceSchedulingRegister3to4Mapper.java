package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlResourceSchedulingRegister3to4;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlResourceSchedulingRegister3to4Mapper extends BaseMapper<AlResourceSchedulingRegister3to4> {

	void deletebyname(String modelName);

}
