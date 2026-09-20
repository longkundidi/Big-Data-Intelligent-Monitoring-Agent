package sw.model3d.stateAssessment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.stateAssessment.entity.DcAlarm;

import java.util.List;
import java.util.Map;

/**
 * (DcAlarm)表数据库访问层
 *
 * @author makejava
 * @since 2026-04-22 18:10:00
 */
@Mapper
public interface DcAlarmMapper extends BaseMapper<DcAlarm> {

	@Select("SELECT failure_mode.failure_code AS faultCode, failure_mode.failure_name AS faultName, "
			+ "COUNT(DISTINCT completed.id) AS count "
			+ "FROM (SELECT DISTINCT turbine_code, node_code FROM al_diagnosis_record "
			+ "WHERE turbine_code = #{turbineCode}) diagnosed_node " + "JOIN config_bom_tree instance_node "
			+ "ON BINARY instance_node.turbine_code = BINARY diagnosed_node.turbine_code "
			+ "AND BINARY instance_node.node_code = BINARY diagnosed_node.node_code "
			+ "JOIN al_resume_data resume ON CAST(resume.id AS CHAR) = instance_node.pro_id "
			+ "JOIN config_gbom_tree template_node ON template_node.scene_id = resume.meta_model_id "
			+ "AND template_node.node_code = instance_node.node_code "
			+ "JOIN config_failure_mode failure_mode ON failure_mode.node_id = template_node.node_id "
			+ "LEFT JOIN al_diagnosis_record completed " + "ON completed.turbine_code = diagnosed_node.turbine_code "
			+ "AND completed.node_code = diagnosed_node.node_code "
			+ "AND completed.diagnosis_status = 2 AND completed.is_fault = 1 "
			+ "AND BINARY completed.dictionary_fault_code = BINARY failure_mode.failure_code "
			+ "GROUP BY failure_mode.failure_code, failure_mode.failure_name "
			+ "ORDER BY CAST(failure_mode.failure_code AS UNSIGNED), failure_mode.failure_code")
	List<Map<String, Object>> selectDiagnosisFaultStats(@Param("turbineCode") String turbineCode);

}
