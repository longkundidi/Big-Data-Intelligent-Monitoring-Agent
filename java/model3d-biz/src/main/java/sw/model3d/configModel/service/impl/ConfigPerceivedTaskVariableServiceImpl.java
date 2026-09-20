package sw.model3d.configModel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.model3d.configModel.entity.ConfigPerceivedTask;
import sw.model3d.configModel.mapper.ConfigPerceivedTaskVariableMapper;
import sw.model3d.configModel.entity.ConfigPerceivedTaskVariable;
import sw.model3d.configModel.service.ConfigPerceivedTaskVariableService;

import java.util.List;

/**
 * (ConfigPerceivedTaskVariable)表服务实现类
 *
 * @author makejava
 * @since 2024-05-13 11:37:20
 */
@Service
public class ConfigPerceivedTaskVariableServiceImpl
		extends ServiceImpl<ConfigPerceivedTaskVariableMapper, ConfigPerceivedTaskVariable>
		implements ConfigPerceivedTaskVariableService {

	@Autowired
	private ConfigPerceivedTaskVariableMapper configPerceivedTaskVariableMapper;

	@Override
	public int delTaskByTaskId(Long taskId) {
		LambdaQueryWrapper<ConfigPerceivedTaskVariable> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTaskVariable::getTaskId, taskId);
		int delete = this.getBaseMapper().delete(queryWrapper);
		return delete;

	}

	@Override
	public List<ConfigPerceivedTaskVariable> getVarsByTaskId(String taskId) {
		return configPerceivedTaskVariableMapper.getVarsByTaskId(taskId);
	}

}
