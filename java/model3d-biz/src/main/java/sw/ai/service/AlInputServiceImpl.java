package sw.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sw.ai.domain.AlInput;
import sw.ai.domain.vo.AlInputResponse;
import sw.ai.mapper.AlInputMapper;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.model3d.configPerceivedVariable.mapper.ConfigPerceivedVariableMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlInputServiceImpl extends ServiceImpl<AlInputMapper, AlInput> implements AlInputService {

	@Autowired
	AlInputMapper alInputMapper;

	@Autowired
	ConfigPerceivedVariableMapper configPerceivedVariableMapper;

	@Override
	public void saveAlInput(String alName, String alClass, List<Object> inputVariables) {
		for (Object variable : inputVariables) {
			AlInput alInput = new AlInput();
			alInput.setAlName(alName);
			alInput.setAlClass(alClass);

			if (variable instanceof String) {
				alInput.setVarId(configPerceivedVariableMapper.getVarId((String) variable));
			}
			else if (variable instanceof ConfigPerceivedVariable) {
				alInput.setVarId(((ConfigPerceivedVariable) variable).getVarId());
			}
			else {
				throw new IllegalArgumentException("Unsupported inputVariables element type: " + variable.getClass());
			}

			this.save(alInput);
		}
	}

	@Override
	public void removeByName(String name, String alClass) {
		alInputMapper.removeByName(name, alClass);
	}

	@Override
	public List<AlInputResponse> getCurrentVariables(String alName, String type) {
		String alClass = type.equals("evaluation") ? "alStateEvaluation" : "alFaultDiagnosis";
		LambdaQueryWrapper<AlInput> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AlInput::getAlName, alName).eq(AlInput::getAlClass, alClass);
		List<AlInput> inputs = this.list(queryWrapper);
		List<AlInputResponse> responses = new ArrayList<>();
		for (AlInput input : inputs) {
			AlInputResponse response = new AlInputResponse();
			response.setVarId(input.getVarId());
			response.setVarName(configPerceivedVariableMapper.findVariableNameById(input.getVarId()));
			responses.add(response);
		}
		return responses;
	}

	@Override
	public List<AlInputResponse> variablesValidation(List<String> varNames, String nodeId) {
		// 获取当前 nodeId 对应的所有 var
		List<Map<String, Object>> validVariablesList = configPerceivedVariableMapper.findVariableNamesByNodeId(nodeId);

		Map<String, Object> validVariables = validVariablesList.stream()
			.collect(Collectors.toMap(map -> (String) map.get("var_name"), map -> map.get("var_id")));

		List<AlInputResponse> responses = new ArrayList<>();
		if (new HashSet<>(validVariables.keySet()).containsAll(varNames)) {
			for (String varName : varNames) {
				AlInputResponse response = new AlInputResponse();
				response.setVarName(varName);
				response.setVarId((String) validVariables.get(varName));
				responses.add(response);
			}
			return responses;
		}
		else {
			return null;
		}
	}

	@Override
	public List<Map<String, Object>> getVariablesByalNames(List<String> alModelNamelList) {

		List<Map<String, Object>> allResponses = new ArrayList<>();
		int idCounter = 1; // 用于生成自增ID
		Set<String> uniqueVarNames = new HashSet<>(); // 全局去重
		if (alModelNamelList.size() != 0) {
			// 遍历alModelNamelList中的每个模型名称
			for (String alModelName : alModelNamelList) {
				LambdaQueryWrapper<AlInput> queryWrapper = new LambdaQueryWrapper<>();
				queryWrapper.eq(AlInput::getAlName, alModelName);

				// 查询对应模型的所有输入
				List<AlInput> inputs = this.list(queryWrapper);

				for (AlInput input : inputs) {
					String varName = configPerceivedVariableMapper.findVariableNameById(input.getVarId());
					if (varName != null && !uniqueVarNames.contains(varName)) {
						Map<String, Object> response = new HashMap<>();
						response.put("varId", idCounter++);
						response.put("varName", varName);
						allResponses.add(response);
						uniqueVarNames.add(varName); // 记录已添加的变量名
					}
				}
			}
		}

		return allResponses;
	}

}
