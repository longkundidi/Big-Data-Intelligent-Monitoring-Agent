package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlConditionCategoryMapper;
import com.algorithm.web.model.entity.al.AlConditionCategory;

import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.service.al.AlConditionCategoryService;
import com.algorithm.web.service.al.AlTaskService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AlConditionCategoryServiceImpl extends ServiceImpl<AlConditionCategoryMapper, AlConditionCategory>
		implements AlConditionCategoryService {

	@Autowired
	private AlConditionCategoryMapper alConditionCategoryMapper;

	@Override
	public String getnamebyid(long id) {
		return alConditionCategoryMapper.getnamebyid(id);
	}

	@Override
	public AlConditionCategory getbyname(String modelName) {
		return alConditionCategoryMapper.getbyname(modelName);
	}

	@Override
	public IPage<AlConditionCategory> getPage(IPage page, AlConditionCategory alConditionCategory) {
		String modelTypeFirst = alConditionCategory.getModelTypeFirst();
		String modelFunction = alConditionCategory.getModelFunction();
		if (Objects.equals(modelFunction, "")) {
			return alConditionCategoryMapper.selectClassPagenull(page, modelTypeFirst);
		}
		else {
			return alConditionCategoryMapper.selectClassPage(page, modelTypeFirst, modelFunction);
		}

	}

	@Override
	public IPage<AlConditionCategory> getPageObj(IPage page, AlConditionCategory alConditionCategory) {
		String modelTypeFirst = alConditionCategory.getModelTypeFirst();
		String modelFunction = alConditionCategory.getModelFunction();
		String modelObject = alConditionCategory.getModelObject();
		return alConditionCategoryMapper.selectObj(page, modelTypeFirst, modelFunction, modelObject);
	}

	@Override
	public IPage<AlConditionCategory> getPageName(IPage page, AlConditionCategory alConditionCategory) {
		String modelTypeFirst = alConditionCategory.getModelTypeFirst();
		String modelFunction = alConditionCategory.getModelFunction();
		String modelName = alConditionCategory.getModelName();
		return alConditionCategoryMapper.selectName(page, modelTypeFirst, modelFunction, modelName);

	}

	@Override
	public AlConditionCategory getbyId(long id) {
		return alConditionCategoryMapper.getbyId(id);
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alConditionCategoryMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String modelName) {
		// alName是否为空
		if (modelName == null || modelName.trim().isEmpty()) {
			throw new BizException("模型名称不能为空");
		}
		AlConditionCategory alConditionCategory = alConditionCategoryMapper.getbyname(modelName);
		if (alConditionCategory == null) {
			throw new BizException("未找到名称为 " + modelName + " 的模型");
		}
		String code = String.format("cc-%d", alConditionCategory.getId());
		alConditionCategory.setAlCode(code);
		return this.updateById(alConditionCategory);
	}

}
