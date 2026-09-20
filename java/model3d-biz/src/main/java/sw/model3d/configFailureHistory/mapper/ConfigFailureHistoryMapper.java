package sw.model3d.configFailureHistory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Mapper;
import sw.model3d.configFailureHistory.entity.ConfigFailureHistory;
import sw.model3d.configFailureHistory.entity.ConfigFailureHistoryVo;

import java.util.List;

@Mapper
public interface ConfigFailureHistoryMapper extends BaseMapper<ConfigFailureHistory> {

	@Select("SELECT failure_id, failure_name, failure_code, memo, node_id, scene_id, device_name FROM config_failure_history where node_id=#{nodeId}")
	List<ConfigFailureHistoryVo> getByNodeId(@Param("nodeId") String nodeId);

}