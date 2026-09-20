package sw.model3d.configModel.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import sw.model3d.configModel.entity.ConfigPerceivedTaskVariable;
import sw.model3d.configModel.service.ConfigPerceivedTaskVariableService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

/**
 * (ConfigPerceivedTaskVariable)表控制层
 *
 * @author makejava
 * @since 2024-05-13 11:37:19
 */
@RestController
@RequestMapping("/configPerceivedTaskVariable")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@CrossOrigin
public class ConfigPerceivedTaskVariableController {

	/**
	 * 服务对象
	 */
	@Autowired
	private ConfigPerceivedTaskVariableService configPerceivedTaskVariableService;

	@Operation(summary = "根据指定taskid查找感知变量", description = "根据指定taskid查找感知变量")
	@SysLog("查询")
	@GetMapping("/getVariableByTaskId")
	public R getVariableByTaskId(@RequestParam Long taskId, @RequestParam Integer current,
			@RequestParam Integer pageSize) {
		LambdaQueryWrapper<ConfigPerceivedTaskVariable> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigPerceivedTaskVariable::getTaskId, taskId);
		Page<ConfigPerceivedTaskVariable> taskVariablePage = configPerceivedTaskVariableService.getBaseMapper()
			.selectPage(new Page<>(current, pageSize), queryWrapper);
		return R.ok(taskVariablePage);
	}

	@Operation(summary = "根据taskid查找结构树感知变量", description = "根据指定taskid查找结构树感知变量")
	@SysLog("查询")
	@GetMapping("/getVarsByTaskId")
	public R getVarsByTaskId(@RequestParam String taskId) {

		return R.ok(configPerceivedTaskVariableService.getVarsByTaskId(taskId), "查询成功");
	}

}
