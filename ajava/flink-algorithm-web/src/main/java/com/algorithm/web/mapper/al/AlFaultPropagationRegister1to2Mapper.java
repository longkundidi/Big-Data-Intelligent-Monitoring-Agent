package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlFaultPropagationRegister1to2;
import com.algorithm.web.model.vo.AlFaultPropagationRegister1to2Vo;
import com.algorithm.web.model.vo.AlStateEvaluationRegister1to2Vo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlFaultPropagationRegister1to2Mapper extends BaseMapper<AlFaultPropagationRegister1to2> {

	void deletebyname(String modelName);

	IPage<AlFaultPropagationRegister1to2Vo> selectClassPage(IPage page, @Param("modelName") String modelName);

}
