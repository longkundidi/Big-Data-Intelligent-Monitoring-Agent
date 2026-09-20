package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.al.AlFeatureExtraction;
import com.algorithm.web.mapper.al.AlFeatureExtractionMapper;
import com.algorithm.web.service.al.AlFeatureExtractionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class AlFeatureExtractionServiceImpl extends ServiceImpl<AlFeatureExtractionMapper, AlFeatureExtraction>
		implements AlFeatureExtractionService {

	@Autowired
	private AlFeatureExtractionMapper alFeatureExtractionMapper;

	@Override
	public AlFeatureExtraction getbyname(String alName) {
		return alFeatureExtractionMapper.getbyname(alName);
	}

	@Override
	public IPage<AlFeatureExtraction> getPage(IPage page, AlFeatureExtraction alFeatureExtraction) {
		String alType = alFeatureExtraction.getAlType();
		if (Objects.equals(alType, "")) {
			return alFeatureExtractionMapper.selectClassPagenull(page);
		}
		else {
			return alFeatureExtractionMapper.selectClassPage(page, alType);
		}
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alFeatureExtractionMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String alName) {
		// alName是否为空
		if (alName == null || alName.trim().isEmpty()) {
			throw new BizException("算法名称不能为空");
		}
		AlFeatureExtraction alFeatureExtraction = alFeatureExtractionMapper.getbyname(alName);
		if (alFeatureExtraction == null) {
			throw new BizException("未找到名称为 " + alName + " 的算法");
		}
		String code = String.format("fe-%d", alFeatureExtraction.getId());
		alFeatureExtraction.setAlCode(code);
		return this.updateById(alFeatureExtraction);
	}

	@Override
	public int[] getTypeNum(String alType) {
		if (alType.equals("数据清洗算法")) {
			int type1 = alFeatureExtractionMapper.gettype1();
			int type2 = alFeatureExtractionMapper.gettype2();
			int type3 = alFeatureExtractionMapper.gettype3();
			int type4 = alFeatureExtractionMapper.gettype4();
			int[] result = new int[] { type1, type2, type3, type4 };
			return result;
		}
		if (alType.equals("数据挖掘算法")) {
			int type5 = alFeatureExtractionMapper.gettype5();
			int type6 = alFeatureExtractionMapper.gettype6();
			int type7 = alFeatureExtractionMapper.gettype7();
			int type8 = alFeatureExtractionMapper.gettype8();
			int[] result = new int[] { type5, type6, type7, type8 };
			return result;
		}
		if (alType.equals("文本数据提取算法")) {
			int type9 = alFeatureExtractionMapper.gettype9();
			int type10 = alFeatureExtractionMapper.gettype10();
			int[] result = new int[] { type9, type10 };
			return result;
		}
		if (alType.equals("特征提取算法")) {
			int type11 = alFeatureExtractionMapper.gettype11();
			int type12 = alFeatureExtractionMapper.gettype12();
			int[] result = new int[] { type11, type12 };
			return result;
		}
		else {
			return null;
		}
	};

}
