package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlMaintDecisionMapper;
import com.algorithm.web.model.entity.al.AlMaintDecision;
import com.algorithm.web.service.al.AlMaintDecisionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class AlMaintDecisionServiceImpl extends ServiceImpl<AlMaintDecisionMapper, AlMaintDecision>
		implements AlMaintDecisionService {

	@Autowired
	private AlMaintDecisionMapper alMaintDecisionMapper;

	@Override
	public String getnamebyid(long id) {
		return alMaintDecisionMapper.getnamebyid(id);
	}

	@Override
	public AlMaintDecision getbyname(String modelName) {
		return alMaintDecisionMapper.getbyname(modelName);
	}

	@Override
	public IPage<AlMaintDecision> getPage(IPage page, AlMaintDecision alMaintDecision) {
		String modelTypeFirst = alMaintDecision.getModelTypeFirst();
		String modelFunction = alMaintDecision.getModelFunction();
		if (Objects.equals(modelFunction, "")) {
			return alMaintDecisionMapper.selectClassPagenull(page, modelTypeFirst);
		}
		else {
			return alMaintDecisionMapper.selectClassPage(page, modelTypeFirst, modelFunction);
		}
	}

	@Override
	public IPage<AlMaintDecision> getPageObj(IPage page, AlMaintDecision alMaintDecision) {
		String modelTypeFirst = alMaintDecision.getModelTypeFirst();
		String modelFunction = alMaintDecision.getModelFunction();
		String modelObject = alMaintDecision.getModelObject();
		return alMaintDecisionMapper.selectObj(page, modelTypeFirst, modelFunction, modelObject);

	}

	@Override
	public IPage<AlMaintDecision> getPageName(IPage page, AlMaintDecision alMaintDecision) {
		String modelTypeFirst = alMaintDecision.getModelTypeFirst();
		String modelFunction = alMaintDecision.getModelFunction();
		String modelName = alMaintDecision.getModelName();
		return alMaintDecisionMapper.selectName(page, modelTypeFirst, modelFunction, modelName);
	}

	@Override
	public AlMaintDecision getbyId(long id) {
		return alMaintDecisionMapper.getbyId(id);
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alMaintDecisionMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String modelName) {
		// modelName是否为空
		if (modelName == null || modelName.trim().isEmpty()) {
			throw new BizException("模型名称不能为空");
		}
		AlMaintDecision alMaintDecision = alMaintDecisionMapper.getbyname(modelName);
		if (alMaintDecision == null) {
			throw new BizException("未找到名称为 " + modelName + " 的模型");
		}
		String code = String.format("md-%d", alMaintDecision.getId());
		alMaintDecision.setAlCode(code);
		return this.updateById(alMaintDecision);
	}

}
