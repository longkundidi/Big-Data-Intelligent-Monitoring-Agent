package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import cn.hutool.json.JSONObject;
import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.enums.SysConfigEnum;
import com.algorithm.web.mapper.al.AlFaultPropagationMapper;
import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlFaultPropagation;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.service.al.AlFaultPropagationService;
import com.algorithm.web.service.al.AlTaskService;
import com.algorithm.web.service.al.executealgorithm.BuildTaskMsgService;
import com.algorithm.web.utils.RestTemplateUtil;
import com.algorithm.web.utils.TaskMsgServices;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class AlFaultPropagationServiceImpl extends ServiceImpl<AlFaultPropagationMapper, AlFaultPropagation>
		implements AlFaultPropagationService {

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	private AlFaultPropagationMapper alFaultPropagationMapper;

	private final AlTaskService alTaskService;

	@Autowired
	public AlFaultPropagationServiceImpl(@Lazy AlTaskService alTaskService) {
		this.alTaskService = alTaskService;
	}

	@Autowired
	private Map<String, BuildTaskMsgService> algorithmServiceMap;

	public String getnamebyid(long id) {
		return alFaultPropagationMapper.getnamebyid(id);
	}

	@Override
	public AlFaultPropagation getbyname(String modelName) {
		return alFaultPropagationMapper.getbyname(modelName);
	}

	public AlFaultPropagation getbyId(long id) {
		return alFaultPropagationMapper.getbyId(id);
	}

	@Override
	public IPage<AlFaultPropagation> getPage(IPage page, AlFaultPropagation alFaultPropagation) {
		String modelTypeFirst = alFaultPropagation.getModelTypeFirst();
		String modelFunction = alFaultPropagation.getModelFunction();
		if (Objects.equals(modelFunction, "")) {
			return alFaultPropagationMapper.selectClassPagenull(page, modelTypeFirst);
		}
		else {
			return alFaultPropagationMapper.selectClassPage(page, modelTypeFirst, modelFunction);
		}
	}

	public IPage<AlFaultPropagation> getPageObj(IPage page, AlFaultPropagation alFaultPropagation) {
		String modelTypeFirst = alFaultPropagation.getModelTypeFirst();
		String modelFunction = alFaultPropagation.getModelFunction();
		String modelObject = alFaultPropagation.getModelObject();
		return alFaultPropagationMapper.selectObj(page, modelTypeFirst, modelFunction, modelObject);
	}

	public IPage<AlFaultPropagation> getPageName(IPage page, AlFaultPropagation alFaultPropagation) {
		String modelTypeFirst = alFaultPropagation.getModelTypeFirst();
		String modelFunction = alFaultPropagation.getModelFunction();
		String modelName = alFaultPropagation.getModelName();
		return alFaultPropagationMapper.selectName(page, modelTypeFirst, modelFunction, modelName);
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alFaultPropagationMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String modelName) {
		// modelName是否为空
		if (modelName == null || modelName.trim().isEmpty()) {
			throw new BizException("模型名称不能为空");
		}
		AlFaultPropagation alFaultPropagation = alFaultPropagationMapper.getbyname(modelName);
		if (alFaultPropagation == null) {
			throw new BizException("未找到名称为 " + modelName + " 的模型");
		}
		String code = String.format("fp-%d", alFaultPropagation.getId());
		alFaultPropagation.setAlCode(code);
		return this.updateById(alFaultPropagation);
	}

	@Override
	public Object operate(String useCase, DiagnoseInfoDto diagnoseInfoDto) {
		AlFaultPropagation alFaultPropagation = this.getbyId(Long.parseLong(diagnoseInfoDto.getAlgoId()));
		String url = alFaultPropagation.getModelUrl();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("farmName", diagnoseInfoDto.getFarmName());
		jsonObject.put("turbineName", diagnoseInfoDto.getTurbineName());
		jsonObject.put("location", diagnoseInfoDto.getLocation());
		jsonObject.put("startTime", diagnoseInfoDto.getStartTime());
		jsonObject.put("endTime", diagnoseInfoDto.getEndTime());

		Optional<BuildTaskMsgService> optional = Optional.ofNullable(
				algorithmServiceMap.get(TaskMsgServices.getTaskMsgService(alFaultPropagation.getModelShortName()))); // 获取服务名（算法+service）
		BuildTaskMsgService buildTaskMsgService = optional.get();

		AlTask alTask = new AlTask();
		alTask.setTaskReUrl(systemConfigService.getSystemConfigByKey(SysConfigEnum.ALGORITHM_CALLBACK_URL.getKey())
				+ "algorithm/job/algorithmJobCallback");
		String taskMsg = JsonUtil.toJson(jsonObject);

		alTask.setAlId(Long.parseLong(diagnoseInfoDto.getAlgoId()));
		alTask.setTaskMsg(buildTaskMsgService.buildTaskMsg(taskMsg));
		alTask.setTaskUrl(url);
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTask.setTaskResult("\"\"");
		alTask.setUseCase(useCase);

		alTaskService.save(alTask);
		alTask.setTaskId(alTask.getId());
		// 获取返回结果
		String body = JsonUtil.toJson(alTask);
		String res = RestTemplateUtil.post(url, body);
		AlTask resObject = JsonUtil.fromJson(res, AlTask.class);
		return resObject;
	}

}
