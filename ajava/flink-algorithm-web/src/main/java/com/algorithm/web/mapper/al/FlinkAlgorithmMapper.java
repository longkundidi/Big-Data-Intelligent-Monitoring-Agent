package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.flink.FlinkAlgorithm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FlinkAlgorithmMapper extends BaseMapper<FlinkAlgorithm> {

	Page<FlinkAlgorithm> getPage(Page<FlinkAlgorithm> page, @Param("jobId") String jobId,
			@Param("jobName") String jobName);

}
