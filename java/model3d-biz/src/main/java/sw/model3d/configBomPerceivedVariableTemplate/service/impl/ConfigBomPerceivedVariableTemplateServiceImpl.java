package sw.model3d.configBomPerceivedVariableTemplate.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sw.model3d.configBomPerceivedVariableTemplate.entity.dto.varTempDto;
import sw.model3d.configBomPerceivedVariableTemplate.mapper.ConfigBomPerceivedVariableTemplateMapper;
import sw.model3d.configBomPerceivedVariableTemplate.entity.ConfigBomPerceivedVariableTemplate;
import sw.model3d.configBomPerceivedVariableTemplate.service.ConfigBomPerceivedVariableTemplateService;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.List;

/**
 * 项目结构树节点对应的感知变量信息(ConfigBomPerceivedVariableTemplate)表服务实现类
 *
 * @author makejava
 * @since 2024-06-27 18:37:01
 */
@Service
public class ConfigBomPerceivedVariableTemplateServiceImpl
		extends ServiceImpl<ConfigBomPerceivedVariableTemplateMapper, ConfigBomPerceivedVariableTemplate>
		implements ConfigBomPerceivedVariableTemplateService {

	@Override
	public boolean createProVarTemplate(String project, String productModel, List<varTempDto> varList) {
		Long proId = baseMapper.getProId(project, productModel);
		return createProVarTempalteById(proId, varList);
	}

	@Override
	public boolean createProVarTempalteById(Long id, List<varTempDto> varList) {
		for (varTempDto varTempDto : varList) {
			ConfigBomPerceivedVariableTemplate var_template = new ConfigBomPerceivedVariableTemplate();
			var_template.setVarId(varTempDto.getVId());
			var_template.setVarName(varTempDto.getName_ch());
			var_template.setUsable(varTempDto.getUsable().toString());
			var_template.setProId(id);
			this.save(var_template);
		}
		return true;
	}

	@Override
	public void deleteByVarId(String varId) {
		baseMapper.deleteByVarId(varId);
	}

	@Override
	public void updateByVarId(ConfigPerceivedVariable configPerceivedVariable) {
		List<ConfigBomPerceivedVariableTemplate> updateVariables = baseMapper
			.getAllByVarId(configPerceivedVariable.getVarId());
		for (ConfigBomPerceivedVariableTemplate item : updateVariables) {
			String nodeId = item.getNodeId();
			BeanUtils.copyProperties(configPerceivedVariable, item);
			item.setNodeId(nodeId);
			baseMapper.updateById(item);
		}
	}

}
