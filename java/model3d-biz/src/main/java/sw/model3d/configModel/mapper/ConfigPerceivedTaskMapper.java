package sw.model3d.configModel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.ai.domain.AlStateEvaluation;
import sw.model3d.configModel.entity.ConfigPerceivedTask;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * (ConfigPerceivedTask)表数据库访问层
 *
 * @author makejava
 * @since 2024-05-13 09:32:12
 */
@Mapper
public interface ConfigPerceivedTaskMapper extends BaseMapper<ConfigPerceivedTask> {

	@Select("SELECT * FROM al_state_evaluation")
	List<AlStateEvaluation> getAll();

	@Select("SELECT project FROM al_resume_data where id=#{proId}")
	String getProjectName(@Param("proId") String proId);

	@Select("SELECT product_model  FROM al_resume_data where id=#{proId}")
	String getProductModel(@Param("proId") String proId);

	@Select("SELECT task_id FROM config_perceived_task where node_id=#{nodeId}")
	List<String> getTaskId(@Param("nodeId") String nodeId);

	@Select("SELECT model_id FROM config_perceived_task where task_id=#{taskId}")
	String getModelId(@Param("taskId") Long taskId);

	@Select("SELECT COUNT(distinct node_id) FROM config_perceived_task WHERE status=0  ")
	Long getComponentCount();

	List<String> selectDistinctNodeIds();

	List<String> selectRunningNodeIdsByProIds(@Param("proIds") List<Long> proIds);

	List<String> selectNodeIdsByTaskIds(@Param("taskIds") List<String> taskIds);

}
