package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlMaintDecisionRegister1to2;
import com.algorithm.web.model.vo.AlMaintDecisionRegister1to2Vo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;

public interface AlMaintDecisionRegister1to2Mapper extends BaseMapper<AlMaintDecisionRegister1to2> {

	void deletebyname(String modelName);

	IPage<AlMaintDecisionRegister1to2Vo> selectClassPage(IPage page, @Param("modelName") String modelName);

}
