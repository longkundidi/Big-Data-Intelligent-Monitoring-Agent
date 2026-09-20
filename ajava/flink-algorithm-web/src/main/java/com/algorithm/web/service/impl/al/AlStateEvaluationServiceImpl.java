package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlStateEvaluationMapper;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.al.AlStateEvaluation;
import com.algorithm.web.service.al.AlStateEvaluationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AlStateEvaluationServiceImpl extends ServiceImpl<AlStateEvaluationMapper, AlStateEvaluation>
		implements AlStateEvaluationService {

	@Autowired
	private AlStateEvaluationMapper alStateEvaluationMapper;

	@Override
	public String getnamebyid(long id) {
		return alStateEvaluationMapper.getnamebyid(id);
	}

	@Override
	public AlStateEvaluation getbyname(String modelName) {
		return alStateEvaluationMapper.getbyname(modelName);
	}

	@Override
	public AlStateEvaluation getbyId(long id) {
		return alStateEvaluationMapper.getbyId(id);
	}

	@Override
	public IPage<AlStateEvaluation> getPage(IPage page, AlStateEvaluation alStateEvaluation) {
		String modelTypeFirst = alStateEvaluation.getModelTypeFirst();
		String modelFunction = alStateEvaluation.getModelFunction();
		if (Objects.equals(modelFunction, "")) {
			return alStateEvaluationMapper.selectClassPagenull(page, modelTypeFirst);
		}
		else {
			return alStateEvaluationMapper.selectClassPage(page, modelTypeFirst, modelFunction);
		}
	}

	@Override
	public IPage<AlStateEvaluation> getPageObj(IPage page, AlStateEvaluation alStateEvaluation) {
		String modelTypeFirst = alStateEvaluation.getModelTypeFirst();
		String modelFunction = alStateEvaluation.getModelFunction();
		String modelObject = alStateEvaluation.getModelObject();
		return alStateEvaluationMapper.selectObj(page, modelTypeFirst, modelFunction, modelObject);
	}

	@Override
	public IPage<AlStateEvaluation> getPageName(IPage page, AlStateEvaluation alStateEvaluation) {
		String modelTypeFirst = alStateEvaluation.getModelTypeFirst();
		String modelFunction = alStateEvaluation.getModelFunction();
		String modelName = alStateEvaluation.getModelName();
		return alStateEvaluationMapper.selectName(page, modelTypeFirst, modelFunction, modelName);
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alStateEvaluationMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String modelName) {
		// modelName是否为空
		if (modelName == null || modelName.trim().isEmpty()) {
			throw new BizException("模型名称不能为空");
		}
		AlStateEvaluation alStateEvaluation = alStateEvaluationMapper.getbyname(modelName);
		if (alStateEvaluation == null) {
			throw new BizException("未找到名称为 " + modelName + " 的模型");
		}
		String code = String.format("se-%d", alStateEvaluation.getId());
		alStateEvaluation.setAlCode(code);
		return this.updateById(alStateEvaluation);
	}

}
