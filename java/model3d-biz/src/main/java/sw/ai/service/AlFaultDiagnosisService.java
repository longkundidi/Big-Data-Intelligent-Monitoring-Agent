package sw.ai.service;

import sw.ai.domain.AlFaultDiagnosis;
import com.baomidou.mybatisplus.extension.service.IService;
import sw.ai.domain.AlStateEvaluation;

import java.util.List;

/**
 * @author tjhe
 * @description 针对表【al_fault_diagnosis】的数据库操作Service
 * @createDate 2024-04-17 16:28:14
 */
public interface AlFaultDiagnosisService extends IService<AlFaultDiagnosis> {

	List<AlFaultDiagnosis> getAllByBD();

}
