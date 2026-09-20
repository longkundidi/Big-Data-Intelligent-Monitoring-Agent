package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlKnowledgeExtractionMapper;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.al.AlKnowledgeExtraction;
import com.algorithm.web.service.al.AlKnowledgeExtractionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class AlKnowledgeExtractionServiceImpl extends ServiceImpl<AlKnowledgeExtractionMapper, AlKnowledgeExtraction>
		implements AlKnowledgeExtractionService {

	@Autowired
	private AlKnowledgeExtractionMapper alKnowledgeExtractionMapper;

	@Override
	public AlKnowledgeExtraction getbyname(String alName) {
		return alKnowledgeExtractionMapper.getbyname(alName);
	}

	@Override
	public IPage<AlKnowledgeExtraction> getPage(IPage page, AlKnowledgeExtraction alKnowledgeExtraction) {
		String alType = alKnowledgeExtraction.getAlType();
		if (Objects.equals(alType, "")) {
			return alKnowledgeExtractionMapper.selectClassPagenull(page);
		}
		else {
			return alKnowledgeExtractionMapper.selectClassPage(page, alType);
		}
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alKnowledgeExtractionMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String alName) {
		// alName是否为空
		if (alName == null || alName.trim().isEmpty()) {
			throw new BizException("算法名称不能为空");
		}
		AlKnowledgeExtraction alKnowledgeExtraction = alKnowledgeExtractionMapper.getbyname(alName);
		if (alKnowledgeExtraction == null) {
			throw new BizException("未找到名称为 " + alName + " 的算法");
		}
		String code = String.format("ke-%d", alKnowledgeExtraction.getId());
		alKnowledgeExtraction.setAlCode(code);
		return this.updateById(alKnowledgeExtraction);
	}

	@Override
	public int[] getTypeNum(String alType) {
		if (alType.equals("数据清洗算法")) {
			int type1 = alKnowledgeExtractionMapper.gettype1();
			int type2 = alKnowledgeExtractionMapper.gettype2();
			int type3 = alKnowledgeExtractionMapper.gettype3();
			int type4 = alKnowledgeExtractionMapper.gettype4();
			int[] result = new int[] { type1, type2, type3, type4 };
			return result;
		}
		if (alType.equals("数据挖掘算法")) {
			int type5 = alKnowledgeExtractionMapper.gettype5();
			int type6 = alKnowledgeExtractionMapper.gettype6();
			int type7 = alKnowledgeExtractionMapper.gettype7();
			int type8 = alKnowledgeExtractionMapper.gettype8();
			int[] result = new int[] { type5, type6, type7, type8 };
			return result;
		}
		if (alType.equals("文本数据提取算法")) {
			int type9 = alKnowledgeExtractionMapper.gettype9();
			int type10 = alKnowledgeExtractionMapper.gettype10();
			int[] result = new int[] { type9, type10 };
			return result;
		}
		else {
			return null;
		}
	};

}
