package sw.model3d.configModel.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import org.springframework.web.bind.annotation.*;

import sw.ai.domain.AlStateEvaluation;
import sw.ai.service.AlStateEvaluationService;
import sw.model3d.configModel.entity.CompositionTaskVo;
import sw.model3d.configModel.entity.ConfigModel;
import sw.model3d.configModel.entity.ConfigPerceivedTask;
import sw.model3d.configModel.entity.ConfigPerceivedTaskVariable;
import sw.model3d.configModel.entity.vo.ConfigModelVo;
import sw.model3d.configModel.entity.vo.ConfigPerceivedTaskSaveRequest;
import sw.model3d.configModel.entity.vo.ConfigPerceivedVariableVo;
import sw.model3d.configModel.mapper.ConfigPerceivedTaskMapper;
import sw.model3d.configModel.service.ConfigPerceivedTaskService;
import sw.model3d.configModel.service.ConfigPerceivedTaskVariableService;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * (ConfigPerceivedTask)表控制层
 *
 * @author makejava
 * @since 2024-05-13 09:31:15
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configPerceivedTask")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ConfigPerceivedTaskController {

	private final ConfigPerceivedTaskService configPerceivedTaskService;

	private final ConfigPerceivedTaskVariableService configPerceivedTaskVariableService;

	private final AlStateEvaluationService alStateEvaluationService;

	@Autowired
	private ConfigPerceivedTaskMapper configPerceivedTaskMapper;

	@Operation(summary = "生成默认状态感知任务模板", description = "生成默认状态感知任务")
	@SysLog("新增")
	@PostMapping
	public R save(@RequestBody List<String> nodeIdList) {

		return R.ok(configPerceivedTaskService.addConfigPerceivedTask(nodeIdList), "新增成功");

	}

	@Operation(summary = "生成默认组态感知任务模板", description = "生成默认组态感知任务模板")
	@SysLog("新增")
	@PostMapping("/saveComposition")
	public R saveComposition(@RequestBody List<String> nodeIdList) {

		return R.ok(configPerceivedTaskService.addCompositionTask(nodeIdList), "新增成功");

	}

	@Operation(summary = "生成其他状态感知任务模板", description = "生成其他状态感知任务模板")
	@SysLog("新增")
	@PostMapping("/createTemplate")
	public R createTemplate(@RequestParam String modelId, @RequestParam String bomNodeId) {
		return R.ok(configPerceivedTaskService.createTemplate(modelId, bomNodeId), "生成成功");

	}

	@Operation(summary = "保存状态感知任务配置", description = "创建或更新实例任务及其感知变量")
	@SysLog("保存状态感知任务配置")
	@PutMapping("/saveConfiguration")
	public R saveConfiguration(@RequestBody ConfigPerceivedTaskSaveRequest request) {
		try {
			return R.ok(configPerceivedTaskService.saveConfiguration(request), "保存成功");
		}
		catch (IllegalArgumentException exception) {
			return R.failed(exception.getMessage());
		}
	}

	@Operation(summary = "生成其他组态感知任务模板", description = "生成其他组态感知任务模板")
	@SysLog("新增")
	@PostMapping("/createCompositionTemplate")
	public R createCompositionTemplate(@RequestParam String modelId, @RequestParam String bomNodeId) {
		configPerceivedTaskService.createCompositionTemplate(modelId, bomNodeId);
		return R.ok(null, "生成成功");

	}

	@Operation(summary = "执行状态感知任务", description = "执行状态感知任务")
	@SysLog("新增")
	@PostMapping("/operateTask")
	public R operateTask(@RequestParam String modelId, @RequestParam String bomNodeId) throws Exception {

		return R.ok(configPerceivedTaskService.operateTask(modelId, bomNodeId), "执行状态感知任务成功");

	}

	@Operation(summary = "停止状态感知任务", description = "停止状态感知任务")
	@SysLog("更新")
	@PutMapping("/stopTask")
	public R stopTask(@RequestParam Long taskId) {
		return R.ok(configPerceivedTaskService.stopTask(taskId), "停止状态感知任务成功");

	}

	@Operation(summary = "根据指定节点id查找模板列表", description = "根据指定节点id查找模板列表")
	@SysLog("查询")
	@GetMapping("/getModelByNodeId")
	public R getModelByNodeId(@RequestParam String nodeId, @RequestParam String modelType) {
		List<ConfigModelVo> modelList = configPerceivedTaskService.getModelListByNodeId(nodeId, modelType);
		return R.ok(modelList);
	}

	@Operation(summary = "根据指定节点id查找组态模板列表", description = "根据指定节点id查找组态模板列表")
	@SysLog("查询")
	@GetMapping("/getCompositionTask")
	public R getCompositionTask(@RequestParam String nodeId) {
		List<CompositionTaskVo> modelList = configPerceivedTaskService.getCompositionTask(nodeId);
		return R.ok(modelList);
	}

	@Operation(summary = "根据指定taskid查找模板信息", description = "根据指定taskid查找模板信息")
	@SysLog("查询")
	@GetMapping("/getModelInfoByTaskId")
	public R getModelInfoByTaskId(@RequestParam Long taskId) {
		ConfigPerceivedTask configPerceivedTask = configPerceivedTaskService.getModelInfoByTaskId(taskId);
		return R.ok(configPerceivedTask, "获取模板信息成功");
	}

	@Operation(summary = "根据指定taskId删除任务", description = "根据指定taskId删除任务")
	@SysLog("删除")
	@DeleteMapping("/{taskId}")
	public R delTaskByTaskId(@PathVariable Long taskId) {
		configPerceivedTaskService.delTaskByTaskId(taskId);
		configPerceivedTaskVariableService.delTaskByTaskId(taskId);
		return R.ok(null, "删除成功");

	}

	/**
	 * @param objList:[0:{taskId:'',modelId:'',nodeId:''等等任务信息字段},1:['1772893173897936898','1772893350184534018'等新选择的感知变量var_id]]
	 * @return
	 */
	@Operation(summary = "根据指定taskId编辑任务", description = "根据指定taskId编辑任务")
	@SysLog("更新")
	@PutMapping("/editTaskByTaskId")
	public R editTaskByTaskId(@RequestBody List<Object> objList) {
		ConfigPerceivedTask newConfigPerceivedTask = JSON.parseObject(JSON.toJSONString(objList.get(0)),
				ConfigPerceivedTask.class);
		newConfigPerceivedTask
			.setAlgoShortname((alStateEvaluationService.getOne(new QueryWrapper<AlStateEvaluation>().lambda()
				.eq(AlStateEvaluation::getId, newConfigPerceivedTask.getAlgoId()))).getModelShortName());

		List<LinkedHashMap> varList = (List<LinkedHashMap>) objList.get(1);
		newConfigPerceivedTask.setVariableNum(new Long(varList.size()));
		configPerceivedTaskService.updateById(newConfigPerceivedTask);

		Long taskId = newConfigPerceivedTask.getTaskId();
		configPerceivedTaskVariableService.delTaskByTaskId(taskId);

		for (LinkedHashMap map : varList) {
			ConfigPerceivedTaskVariable taskVariable = new ConfigPerceivedTaskVariable();
			taskVariable.setTaskId(taskId);
			ConfigPerceivedVariableVo vo = JSON.parseObject(JSON.toJSONString(map), ConfigPerceivedVariableVo.class);
			BeanUtils.copyProperties(vo, taskVariable);

			configPerceivedTaskVariableService.save(taskVariable);
		}

		return R.ok(null, "编辑成功");
	}

	@GetMapping("/getComponentCount")
	public R getComponentCount() {
		return R.ok(configPerceivedTaskMapper.getComponentCount());
	}

}
