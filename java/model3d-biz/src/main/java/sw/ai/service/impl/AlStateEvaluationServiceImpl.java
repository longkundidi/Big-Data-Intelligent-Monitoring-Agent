package sw.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import sw.ai.domain.AlStateEvaluation;
import sw.ai.service.AlStateEvaluationService;
import sw.ai.mapper.AlStateEvaluationMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Tyrrell
 * @description 针对表【al_state_evaluation】的数据库操作Service实现
 * @createDate 2024-04-02 15:57:17
 */
@Service
public class AlStateEvaluationServiceImpl extends ServiceImpl<AlStateEvaluationMapper, AlStateEvaluation>
		implements AlStateEvaluationService {

	@Override
	public List<AlStateEvaluation> getAllByBD() {
		List<AlStateEvaluation> all = baseMapper.getAll();
		return all;
	}

}
