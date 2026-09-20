package com.algorithm.web.strategy;

import com.algorithm.web.model.entity.al.AlTaskVo;

public interface QueryStrategy {

	String getType(AlTaskVo alTask);

	String getCreator(AlTaskVo alTask);

	Long getNum(AlTaskVo alTask);

}
