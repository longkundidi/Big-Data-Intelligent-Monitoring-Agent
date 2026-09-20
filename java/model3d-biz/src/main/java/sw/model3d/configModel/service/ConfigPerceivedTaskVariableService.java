package sw.model3d.configModel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.model3d.configModel.entity.ConfigPerceivedTaskVariable;

import sw.model3d.configModel.entity.ConfigPerceivedTaskVariable;

import java.util.List;

/**
 * (ConfigPerceivedTaskVariable)表服务接口
 *
 * @author makejava
 * @since 2024-05-13 11:37:19
 */
public interface ConfigPerceivedTaskVariableService extends IService<ConfigPerceivedTaskVariable> {

	int delTaskByTaskId(Long taskId);

	List<ConfigPerceivedTaskVariable> getVarsByTaskId(String taskId);

}
