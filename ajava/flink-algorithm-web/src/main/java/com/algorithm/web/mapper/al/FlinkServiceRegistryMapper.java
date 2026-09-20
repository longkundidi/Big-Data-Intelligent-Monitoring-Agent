package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.flink.FlinkServiceRegistry;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface FlinkServiceRegistryMapper extends BaseMapper<FlinkServiceRegistry> {

	@Select("SELECT COUNT(*) FROM dc_algorithm WHERE task_id = #{taskId} AND dc_time >= NOW() - INTERVAL 1 HOUR")
	Long countRecentResults(@Param("taskId") Long taskId);

	@Select("SELECT COUNT(*) FROM dc_alarm WHERE task_id = #{taskId} AND dc_time >= NOW() - INTERVAL 7 DAY")
	Long countRecentAlarms(@Param("taskId") Long taskId);

	@Select("SELECT MAX(dc_time) FROM dc_algorithm WHERE task_id = #{taskId}")
	LocalDateTime selectLatestResultTime(@Param("taskId") Long taskId);

}
