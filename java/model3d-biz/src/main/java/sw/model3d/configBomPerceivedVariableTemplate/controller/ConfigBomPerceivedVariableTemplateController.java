package sw.model3d.configBomPerceivedVariableTemplate.controller;

import com.alibaba.fastjson.JSONObject;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import sw.model3d.configBomPerceivedVariableTemplate.entity.dto.varTempDto;
import sw.model3d.configBomPerceivedVariableTemplate.service.ConfigBomPerceivedVariableTemplateService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

/**
 * 项目结构树节点对应的感知变量信息(ConfigBomPerceivedVariableTemplate)表控制层
 *
 * @author makejava
 * @since 2024-06-27 18:36:59
 */
@RestController
@RequestMapping("/configBomPerceivedVariableTemplate")
@CrossOrigin
public class ConfigBomPerceivedVariableTemplateController {

	/**
	 * 服务对象
	 */
	@Autowired
	private ConfigBomPerceivedVariableTemplateService configBomPerceivedVariableTemplateService;

	@Operation(summary = "根据项目名project、机型productmodel和感知变量varList导入感知变量模板信息",
			description = "根据项目名project、机型productmodel和感知变量varList导入感知变量模板信息")
	@PostMapping("/createProVarTempalte/{project}/{productModel}")
	public R createProVarTempalte(@PathVariable String project, @PathVariable String productModel,
			@RequestBody List<varTempDto> varList) {
		return R.ok(configBomPerceivedVariableTemplateService.createProVarTemplate(project, productModel, varList),
				"导入风机感知变量模板成功");
	}

	@Operation(summary = "根据场景id和感知变量varList导入感知变量模板信息", description = "根据场景id和感知变量varList导入感知变量模板信息")
	@PostMapping("/createProVarTempalte/{sceneId}")
	public R createProVarTempalteById(@PathVariable String sceneId, @RequestBody List<varTempDto> varList) {
		return R.ok(configBomPerceivedVariableTemplateService.createProVarTempalteById(Long.valueOf(sceneId), varList),
				"导入风机感知变量模板成功");
	}

	/**
	 * 通过id删除
	 * @param id id
	 * @return R
	 */
	@Operation(summary = "通过id删除", description = "通过id删除")
	@SysLog("通过id删除")
	@DeleteMapping("/{id}")
	// @PreAuthorize("@pms.hasPermission('configPerceivedVariable_configperceivedvariable_del')"
	// )
	public R removeById(@PathVariable String id) {
		boolean isDeleted = configBomPerceivedVariableTemplateService.removeById(id);
		if (isDeleted) {
			return R.ok(null, "删除成功");
		}
		return R.failed("删除失败");
	}

}
