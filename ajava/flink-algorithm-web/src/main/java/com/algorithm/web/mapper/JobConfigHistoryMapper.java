package com.algorithm.web.mapper;

import com.algorithm.web.model.entity.JobConfigHistory;
import com.algorithm.web.model.param.JobConfigHisotryParam;
import com.github.pagehelper.Page;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobConfigHistoryMapper {

	int insert(JobConfigHistory record);

	List<JobConfigHistory> selectByJobConfigId(@Param("jobConfigId") Long jobConfigId);

	JobConfigHistory selectById(@Param("id") Long id);

	Page<JobConfigHistory> findJobConfigHistory(JobConfigHisotryParam jobConfigParam);

}
