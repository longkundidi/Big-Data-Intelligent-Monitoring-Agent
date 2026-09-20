package sw.model3d.configModel.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import sw.model3d.configModel.entity.ConfigPerceivedTaskVariable;

import java.util.List;

/**
 * (ConfigPerceivedTaskVariable)表数据库访问层
 *
 * @author makejava
 * @since 2024-05-13 11:37:19
 */
@Mapper
public interface ConfigPerceivedTaskVariableMapper extends BaseMapper<ConfigPerceivedTaskVariable> {

	@Select("SELECT * FROM config_perceived_task_variable WHERE task_id = #{taskId}")
	List<ConfigPerceivedTaskVariable> getVarsByTaskId(@Param("taskId") String taskId);

}
