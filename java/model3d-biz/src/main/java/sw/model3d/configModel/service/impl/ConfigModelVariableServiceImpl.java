package sw.model3d.configModel.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import sw.model3d.configModel.entity.ConfigModelVariable;
import sw.model3d.configModel.service.ConfigModelService;
import sw.model3d.configModel.service.ConfigModelVariableService;
import sw.model3d.configModel.mapper.ConfigModelVariableMapper;
import org.springframework.stereotype.Service;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.model3d.configPerceivedVariable.service.ConfigPerceivedVariableService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyrrell
 * @description 针对表【config_model_variable】的数据库操作Service实现
 * @createDate 2024-04-01 15:36:19
 */
@Service
public class ConfigModelVariableServiceImpl extends ServiceImpl<ConfigModelVariableMapper, ConfigModelVariable>
		implements ConfigModelVariableService {

	@Override
	public int batchAdd(String configModelId, List<LinkedHashMap> lists) {
		List<ConfigModelVariable> addSet = new ArrayList<>();
		for (LinkedHashMap list : lists) {
			ConfigPerceivedVariable configPerceivedVariable = JSON.parseObject(JSON.toJSONString(list),
					ConfigPerceivedVariable.class);// 将LinkedHashMap转为对象
			ConfigModelVariable obj = new ConfigModelVariable();
			obj.setVarId(configPerceivedVariable.getVarId());
			obj.setModelId(configModelId.toString());
			addSet.add(obj);
		}
		if (addSet.size() > 0)
			this.saveBatch(addSet);
		return addSet.size();
	}

	@Override
	public boolean batchEdit(String configModelId, List<LinkedHashMap> lists) {
		// 1. 拿到之前的相关感知变量列表
		List<ConfigModelVariable> before = this
			.list(new QueryWrapper<ConfigModelVariable>().lambda().eq(ConfigModelVariable::getModelId, configModelId));
		// 2. 将lists转化为感知变量列表after
		List<ConfigModelVariable> after = new ArrayList<>();
		for (LinkedHashMap list : lists) {
			ConfigPerceivedVariable configPerceivedVariable = JSON.parseObject(JSON.toJSONString(list),
					ConfigPerceivedVariable.class);// 将LinkedHashMap转为对象
			ConfigModelVariable obj = new ConfigModelVariable();
			obj.setVarId(configPerceivedVariable.getVarId());
			obj.setModelId(configModelId.toString());
			after.add(obj);
		}
		// 3. 对比before与after，找出新增的new
		List<ConfigModelVariable> newList = after.stream()
			.filter(variable -> !containsVariable(before, variable))
			.collect(Collectors.toList());
		// 4. 对比before与after，找出需要删除的old
		List<ConfigModelVariable> oldList = before.stream()
			.filter(variable -> !containsVariable(after, variable))
			.collect(Collectors.toList());
		// 5. new写入数据库，从数据库中删除old
		this.saveBatch(newList);
		this.removeBatchByIds(oldList);
		return true;
	}

	public static boolean containsVariable(List<ConfigModelVariable> list, ConfigModelVariable variable) {
		for (ConfigModelVariable item : list) {
			if (item.getVarId().equals(variable.getVarId()) && item.getModelId().equals(variable.getModelId())) {
				return true;
			}
		}
		return false;
	}

}
