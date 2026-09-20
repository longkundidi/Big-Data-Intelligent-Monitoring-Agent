package sw.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import sw.ai.domain.AlFaultDiagnosis;
import sw.ai.domain.AlStateEvaluation;
import sw.ai.service.AlFaultDiagnosisService;
import sw.ai.mapper.AlFaultDiagnosisMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tjhe
 * @description 针对表【al_fault_diagnosis】的数据库操作Service实现
 * @createDate 2024-04-17 16:28:14
 */
@Service
public class AlFaultDiagnosisServiceImpl extends ServiceImpl<AlFaultDiagnosisMapper, AlFaultDiagnosis>
		implements AlFaultDiagnosisService {

	@Override
	public List<AlFaultDiagnosis> getAllByBD() {
		List<AlFaultDiagnosis> all = baseMapper.getAll();
		return all;
	}

}
