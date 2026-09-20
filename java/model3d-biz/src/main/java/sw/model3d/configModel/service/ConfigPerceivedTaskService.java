package sw.model3d.configModel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.ai.domain.AlStateEvaluation;
import sw.model3d.configModel.entity.CompositionTaskVo;
import sw.model3d.configModel.entity.ConfigPerceivedTask;
import sw.model3d.configModel.entity.vo.ConfigModelVo;
import sw.model3d.configModel.entity.vo.ConfigPerceivedTaskSaveRequest;

import java.util.List;

/**
 * (ConfigPerceivedTask)表服务接口
 *
 * @author makejava
 * @since 2024-05-13 09:31:29
 */
public interface ConfigPerceivedTaskService extends IService<ConfigPerceivedTask> {

	boolean addConfigPerceivedTask(List<String> nodeIdList);

	boolean addCompositionTask(List<String> nodeIdList);

	ConfigPerceivedTask createTemplate(String modelId, String bomNodeId);

	ConfigPerceivedTask saveConfiguration(ConfigPerceivedTaskSaveRequest request);

	List<ConfigModelVo> getModelListByNodeId(String nodeId, String modelType);

	int delTaskByTaskId(Long taskId);

	ConfigPerceivedTask getModelInfoByTaskId(Long taskId);

	void createCompositionTemplate(String modelId, String bomNodeId);

	List<AlStateEvaluation> getAll();

	String operateTask(String modelId, String bomNodeId) throws Exception;

	String stopTask(Long taskId);

	String getProjectName(String proId);

	String getProductModel(String proId);

	List<CompositionTaskVo> getCompositionTask(String nodeId);

}
