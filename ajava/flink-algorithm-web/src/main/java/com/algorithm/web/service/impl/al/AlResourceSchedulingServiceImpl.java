package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import cn.hutool.json.JSONObject;
import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.enums.SysConfigEnum;
import com.algorithm.web.mapper.al.AlResourceSchedulingMapper;
import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlResourceScheduling;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.service.al.AlResourceSchedulingService;
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
public class AlResourceSchedulingServiceImpl extends ServiceImpl<AlResourceSchedulingMapper, AlResourceScheduling>
		implements AlResourceSchedulingService {

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	private AlResourceSchedulingMapper alResourceSchedulingMapper;

	private final AlTaskService alTaskService;

	@Autowired
	public AlResourceSchedulingServiceImpl(@Lazy AlTaskService alTaskService) {
		this.alTaskService = alTaskService;
	}

	@Autowired
	private Map<String, BuildTaskMsgService> algorithmServiceMap;

	public String getnamebyid(long id) {
		return alResourceSchedulingMapper.getnamebyid(id);
	}

	@Override
	public AlResourceScheduling getbyname(String modelName) {
		return alResourceSchedulingMapper.getbyname(modelName);
	}

	public AlResourceScheduling getbyId(long id) {
		return alResourceSchedulingMapper.getbyId(id);
	}

	@Override
	public IPage<AlResourceScheduling> getPage(IPage page, AlResourceScheduling alResourceScheduling) {
		String modelTypeFirst = alResourceScheduling.getModelTypeFirst();
		String modelFunction = alResourceScheduling.getModelFunction();
		if (Objects.equals(modelFunction, "")) {
			return alResourceSchedulingMapper.selectClassPagenull(page, modelTypeFirst);
		}
		else {
			return alResourceSchedulingMapper.selectClassPage(page, modelTypeFirst, modelFunction);
		}
	}

	public IPage<AlResourceScheduling> getPageObj(IPage page, AlResourceScheduling alResourceScheduling) {
		String modelTypeFirst = alResourceScheduling.getModelTypeFirst();
		String modelFunction = alResourceScheduling.getModelFunction();
		String modelObject = alResourceScheduling.getModelObject();
		return alResourceSchedulingMapper.selectObj(page, modelTypeFirst, modelFunction, modelObject);
	}

	public IPage<AlResourceScheduling> getPageName(IPage page, AlResourceScheduling alResourceScheduling) {
		String modelTypeFirst = alResourceScheduling.getModelTypeFirst();
		String modelFunction = alResourceScheduling.getModelFunction();
		String modelName = alResourceScheduling.getModelName();
		return alResourceSchedulingMapper.selectName(page, modelTypeFirst, modelFunction, modelName);
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alResourceSchedulingMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String modelName) {
		// modelName是否为空
		if (modelName == null || modelName.trim().isEmpty()) {
			throw new BizException("模型名称不能为空");
		}
		AlResourceScheduling alResourceScheduling = alResourceSchedulingMapper.getbyname(modelName);
		if (alResourceScheduling == null) {
			throw new BizException("未找到名称为 " + modelName + " 的模型");
		}
		String code = String.format("ss-%d", alResourceScheduling.getId());
		alResourceScheduling.setAlCode(code);
		return this.updateById(alResourceScheduling);
	}

	@Override
	public Object operate(String useCase, DiagnoseInfoDto diagnoseInfoDto) {
		AlResourceScheduling alResourceScheduling = this.getbyId(Long.parseLong(diagnoseInfoDto.getAlgoId()));
		String url = alResourceScheduling.getModelUrl();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("farmName", diagnoseInfoDto.getFarmName());
		jsonObject.put("turbineName", diagnoseInfoDto.getTurbineName());
		jsonObject.put("location", diagnoseInfoDto.getLocation());
		jsonObject.put("startTime", diagnoseInfoDto.getStartTime());
		jsonObject.put("endTime", diagnoseInfoDto.getEndTime());

		Optional<BuildTaskMsgService> optional = Optional.ofNullable(
				algorithmServiceMap.get(TaskMsgServices.getTaskMsgService(alResourceScheduling.getModelShortName()))); // 获取服务名（算法+service）
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
