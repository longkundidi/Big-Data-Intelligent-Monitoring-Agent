package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.mapper.al.AlDataCleanMapper;
import com.algorithm.web.service.al.AlDataCleanService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class AlDataCleanServiceImpl extends ServiceImpl<AlDataCleanMapper, AlDataClean> implements AlDataCleanService {

	@Autowired
	private AlDataCleanMapper alDataCleanMapper;

	@Override
	public AlDataClean getbyname(String alName) {
		return alDataCleanMapper.getbyname(alName);
	}

	@Override
	public IPage<AlDataClean> getPage(IPage page, AlDataClean alDataClean) {
		String alType = alDataClean.getAlType();
		if (Objects.equals(alType, "")) {
			return alDataCleanMapper.selectClassPagenull(page);
		}
		else {
			return alDataCleanMapper.selectClassPage(page, alType);
		}
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alDataCleanMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String alName) {
		// alName是否为空
		if (alName == null || alName.trim().isEmpty()) {
			throw new BizException("算法名称不能为空");
		}
		AlDataClean alDataClean = alDataCleanMapper.getbyname(alName);
		if (alDataClean == null) {
			throw new BizException("未找到名称为 " + alName + " 的算法");
		}
		String code = String.format("dc-%d", alDataClean.getId());
		alDataClean.setAlCode(code);
		return this.updateById(alDataClean);
	}

	@Override
	public int[] getTypeNum(String alType) {
		if (alType.equals("数据清洗算法")) {
			int type1 = alDataCleanMapper.gettype1();
			int type2 = alDataCleanMapper.gettype2();
			int type3 = alDataCleanMapper.gettype3();
			int type4 = alDataCleanMapper.gettype4();
			int[] result = new int[] { type1, type2, type3, type4 };
			return result;
		}
		if (alType.equals("数据挖掘算法")) {
			int type5 = alDataCleanMapper.gettype5();
			int type6 = alDataCleanMapper.gettype6();
			int type7 = alDataCleanMapper.gettype7();
			int type8 = alDataCleanMapper.gettype8();
			int[] result = new int[] { type5, type6, type7, type8 };
			return result;
		}
		if (alType.equals("文本数据提取算法")) {
			int type9 = alDataCleanMapper.gettype9();
			int type10 = alDataCleanMapper.gettype10();
			int[] result = new int[] { type9, type10 };
			return result;
		}
		else {
			return null;
		}
	};

}
