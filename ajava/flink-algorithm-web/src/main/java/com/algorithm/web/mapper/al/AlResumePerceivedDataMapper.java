package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlResumePerceivedData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AlResumePerceivedDataMapper extends BaseMapper<AlResumePerceivedData> {

	List<String> getPreVariables(@Param("project_id") Long projectId);

}
