package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlResourceSchedulingRegister1to2;
import com.algorithm.web.model.vo.AlResourceSchedulingRegister1to2Vo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlResourceSchedulingRegister1to2Mapper extends BaseMapper<AlResourceSchedulingRegister1to2> {

	void deletebyname(String modelName);

	IPage<AlResourceSchedulingRegister1to2Vo> selectClassPage(IPage page, @Param("modelName") String modelName);

}
