package sw.model3d.stateAssessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import sw.model3d.stateAssessment.entity.DcAlgorithm;

import java.util.List;
import java.util.Map;

/**
 * (DcAlgorithm)表服务接口
 *
 * @author makejava
 * @since 2024-05-28 17:38:16
 */
public interface DcAlgorithmService extends IService<DcAlgorithm> {

	List<Object> getResultByTaskId(String taskId, String algoShortname) throws JsonProcessingException;

	List<Map<String, Object>> getResultCountByTaskId(String taskId);

}
