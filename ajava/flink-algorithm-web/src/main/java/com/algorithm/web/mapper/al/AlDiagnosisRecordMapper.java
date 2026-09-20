package com.algorithm.web.mapper.al;

import com.algorithm.web.model.entity.al.AlDiagnosisRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlDiagnosisRecordMapper extends BaseMapper<AlDiagnosisRecord> {

	@Select({ "SELECT id, alarm_task_id AS alarmTaskId, alarm_time AS alarmTime,",
			"turbine_code AS turbineCode, node_id AS nodeId, algorithm_id AS algorithmId,",
			"algorithm_short_name AS algorithmShortName, diagnosis_status AS diagnosisStatus",
			"FROM al_diagnosis_record", "WHERE turbine_code = #{turbineCode}", "AND alarm_time >= #{startTime}",
			"ORDER BY alarm_time DESC, id DESC" })
	List<AlDiagnosisRecord> selectRecentByTurbineCode(@Param("turbineCode") String turbineCode,
			@Param("startTime") String startTime);

}
