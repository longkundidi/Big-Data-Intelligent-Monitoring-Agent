package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlStateEvaluationRegister1to2;
import com.algorithm.web.model.vo.AlStateEvaluationRegister1to2Vo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlStateEvaluationRegister1to2Mapper extends BaseMapper<AlStateEvaluationRegister1to2> {

	void deletebyname(String modelName);

	IPage<AlStateEvaluationRegister1to2Vo> selectClassPage(IPage page, @Param("modelName") String modelName);

}
