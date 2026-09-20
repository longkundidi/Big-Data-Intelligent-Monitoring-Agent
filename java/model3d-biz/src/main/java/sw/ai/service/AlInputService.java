package sw.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.ai.domain.AlInput;
import sw.ai.domain.vo.AlInputResponse;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.List;
import java.util.Map;

public interface AlInputService extends IService<AlInput> {

	void saveAlInput(String alName, String alClass, List<Object> inputVariables);

	void removeByName(String name, String alClass);

	List<AlInputResponse> getCurrentVariables(String alName, String type);

	List<AlInputResponse> variablesValidation(List<String> varNames, String nodeId);

	List<Map<String, Object>> getVariablesByalNames(List<String> alModelNamelList);

}
