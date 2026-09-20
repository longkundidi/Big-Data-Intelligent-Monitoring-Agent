package com.algorithm.web.service.impl.al;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlDataDimReduceMapper;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.al.AlDataDimReduce;
import com.algorithm.web.model.entity.al.AlDataMining;
import com.algorithm.web.service.al.AlDataDimReduceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AlDataDimReduceServiceImpl extends ServiceImpl<AlDataDimReduceMapper, AlDataDimReduce>
		implements AlDataDimReduceService {

	@Autowired
	private AlDataDimReduceMapper alDataDimReduceMapper;

	@Override
	public AlDataDimReduce getbyname(String alName) {
		return alDataDimReduceMapper.getbyname(alName);
	}

	@Override
	public IPage<AlDataDimReduce> getPage(IPage page, AlDataDimReduce alDataDimReduce) {
		String alType = alDataDimReduce.getAlType();
		if (Objects.equals(alType, "")) {
			return alDataDimReduceMapper.selectClassPagenull(page);
		}
		else {
			return alDataDimReduceMapper.selectClassPage(page, alType);
		}
	}

	@Override
	public Boolean exitName(String name) {
		String alName = alDataDimReduceMapper.exitName(name);
		return alName != null;
	}

	@Override
	public boolean updateCodeByName(String alName) {
		// alName是否为空
		if (alName == null || alName.trim().isEmpty()) {
			throw new BizException("算法名称不能为空");
		}
		AlDataDimReduce alDataDimReduce = alDataDimReduceMapper.getbyname(alName);
		if (alDataDimReduce == null) {
			throw new BizException("未找到名称为 " + alName + " 的算法");
		}
		String code = String.format("ddr-%d", alDataDimReduce.getId());
		alDataDimReduce.setAlCode(code);
		return this.updateById(alDataDimReduce);
	}

	@Override
	public int[] getTypeNum(String alType) {
		if (alType.equals("数据清洗算法")) {
			int type1 = alDataDimReduceMapper.gettype1();
			int type2 = alDataDimReduceMapper.gettype2();
			int type3 = alDataDimReduceMapper.gettype3();
			int type4 = alDataDimReduceMapper.gettype4();
			int[] result = new int[] { type1, type2, type3, type4 };
			return result;
		}
		if (alType.equals("数据挖掘算法")) {
			int type5 = alDataDimReduceMapper.gettype5();
			int type6 = alDataDimReduceMapper.gettype6();
			int type7 = alDataDimReduceMapper.gettype7();
			int type8 = alDataDimReduceMapper.gettype8();
			int[] result = new int[] { type5, type6, type7, type8 };
			return result;
		}
		if (alType.equals("文本数据提取算法")) {
			int type9 = alDataDimReduceMapper.gettype9();
			int type10 = alDataDimReduceMapper.gettype10();
			int[] result = new int[] { type9, type10 };
			return result;
		}
		else {
			return null;
		}
	}

}
