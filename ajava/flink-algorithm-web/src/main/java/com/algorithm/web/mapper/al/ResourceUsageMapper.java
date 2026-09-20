package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.ResourceUsage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ResourceUsageMapper extends BaseMapper<ResourceUsage> {

	@Select("SELECT * FROM resource_usage WHERE container_id = #{containerId} ORDER BY created_time DESC LIMIT 1")
	ResourceUsage findStatsByContainerId(@Param("containerId") String containerId);

	@Delete("DELETE FROM resource_usage WHERE created_time < #{cutoffTime}")
	int deleteByCreatedTimeBefore(@Param("cutoffTime") LocalDateTime cutoffTime);

}
