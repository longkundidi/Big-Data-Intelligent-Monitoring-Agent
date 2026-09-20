package sw.ai.mapper;

import org.apache.ibatis.annotations.Mapper;
import sw.ai.domain.AlStateEvaluation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author Tyrrell
 * @description 针对表【al_state_evaluation】的数据库操作Mapper
 * @createDate 2024-04-02 15:57:17
 * @Entity sw.ai.domain.AlStateEvaluation
 */
@Mapper
public interface AlStateEvaluationMapper extends BaseMapper<AlStateEvaluation> {

	List<AlStateEvaluation> getAll();

}
