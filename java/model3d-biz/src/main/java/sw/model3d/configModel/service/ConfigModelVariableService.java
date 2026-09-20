package sw.model3d.configModel.service;

import sw.model3d.configModel.entity.ConfigModelVariable;
import com.baomidou.mybatisplus.extension.service.IService;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Tyrrell
 * @description 针对表【config_model_variable】的数据库操作Service
 * @createDate 2024-04-01 15:36:20
 */
public interface ConfigModelVariableService extends IService<ConfigModelVariable> {

	int batchAdd(String configModelId, List<LinkedHashMap> lists);

	boolean batchEdit(String configModelId, List<LinkedHashMap> lists);

}
