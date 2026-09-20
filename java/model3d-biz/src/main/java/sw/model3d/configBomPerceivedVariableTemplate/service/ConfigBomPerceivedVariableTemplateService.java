package sw.model3d.configBomPerceivedVariableTemplate.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;

import sw.model3d.configBomPerceivedVariableTemplate.entity.ConfigBomPerceivedVariableTemplate;
import sw.model3d.configBomPerceivedVariableTemplate.entity.dto.varTempDto;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.List;

/**
 * 项目结构树节点对应的感知变量信息(ConfigBomPerceivedVariableTemplate)表服务接口
 *
 * @author makejava
 * @since 2024-06-27 18:37:01
 */
public interface ConfigBomPerceivedVariableTemplateService extends IService<ConfigBomPerceivedVariableTemplate> {

	boolean createProVarTemplate(String project, String productModel, List<varTempDto> varList);

	boolean createProVarTempalteById(Long sceneId, List<varTempDto> varList);

	void deleteByVarId(String varId);

	void updateByVarId(ConfigPerceivedVariable configPerceivedVariable);

}
