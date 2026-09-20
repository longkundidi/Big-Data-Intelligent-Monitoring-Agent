package sw.ai.service;

import sw.ai.domain.AlStateEvaluation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author Tyrrell
 * @description 针对表【al_state_evaluation】的数据库操作Service
 * @createDate 2024-04-02 15:57:17
 */
public interface AlStateEvaluationService extends IService<AlStateEvaluation> {

	List<AlStateEvaluation> getAllByBD();

}
