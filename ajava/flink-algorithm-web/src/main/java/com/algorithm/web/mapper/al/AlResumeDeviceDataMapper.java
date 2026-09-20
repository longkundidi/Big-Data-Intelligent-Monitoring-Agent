package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlResumeDeviceData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlResumeDeviceDataMapper extends BaseMapper<AlResumeDeviceData> {

	List<String> getFengji(@Param("project_id") Long projectId);

}
