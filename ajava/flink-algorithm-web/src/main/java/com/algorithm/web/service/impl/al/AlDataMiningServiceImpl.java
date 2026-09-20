package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlDataMiningMapper;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.al.AlDataMining;
import com.algorithm.web.service.al.AlDataMiningService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AlDataMiningServiceImpl extends ServiceImpl<AlDataMiningMapper, AlDataMining>
		implements AlDataMiningService {

	@Autowired
	private AlDataMiningMapper alDataMiningMapper;

	@Override
	public AlDataMining getbyname(String alName) {
		return alDataMiningMapper.getbyname(alName);
	}

	@Override
	public IPage<AlDataMining> getPage(IPage page, AlDataMining alDataMining) {
		String alType = alDataMining.getAlType();
		if (Objects.equals(alType, "")) {
			return alDataMiningMapper.selectClassPagenull(page);
		}
		else {
			return alDataMiningMapper.selectClassPage(page, alType);
		}
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alDataMiningMapper.exitName(name);
		return alName != null;

	}

	@Override
	public boolean updateCodeByName(String alName) {
		// alName是否为空
		if (alName == null || alName.trim().isEmpty()) {
			throw new BizException("算法名称不能为空");
		}
		AlDataMining alDataMining = alDataMiningMapper.getbyname(alName);
		if (alDataMining == null) {
			throw new BizException("未找到名称为 " + alName + " 的算法");
		}
		String code = String.format("dm-%d", alDataMining.getId());
		alDataMining.setAlCode(code);
		return this.updateById(alDataMining);
	}

	@Override
	public int[] getTypeNum(String alType) {
		if (alType.equals("数据清洗算法")) {
			int type1 = alDataMiningMapper.gettype1();
			int type2 = alDataMiningMapper.gettype2();
			int type3 = alDataMiningMapper.gettype3();
			int type4 = alDataMiningMapper.gettype4();
			int[] result = new int[] { type1, type2, type3, type4 };
			return result;
		}
		if (alType.equals("数据挖掘算法")) {
			int type5 = alDataMiningMapper.gettype5();
			int type6 = alDataMiningMapper.gettype6();
			int type7 = alDataMiningMapper.gettype7();
			int type8 = alDataMiningMapper.gettype8();
			int[] result = new int[] { type5, type6, type7, type8 };
			return result;
		}
		if (alType.equals("文本数据提取算法")) {
			int type9 = alDataMiningMapper.gettype9();
			int type10 = alDataMiningMapper.gettype10();
			int[] result = new int[] { type9, type10 };
			return result;
		}
		else {
			return null;
		}

	}

}
