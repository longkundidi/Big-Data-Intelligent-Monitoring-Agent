package com.algorithm.web.service.impl;

import com.algorithm.web.enums.YN;
import com.algorithm.web.mapper.AlarmLogMapper;
import com.algorithm.web.model.entity.AlartLog;
import com.algorithm.web.service.AlartLogService;
import com.algorithm.web.model.dto.AlartLogDTO;
import com.algorithm.web.model.dto.PageModel;
import com.algorithm.web.model.param.AlartLogParam;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhuhuipei
 * @Description:
 * @date 2020-09-25
 * @time 21:43
 */
@Slf4j
@Service
public class AlartLogServiceImpl implements AlartLogService {

	@Autowired
	private AlarmLogMapper alarmLogMapper;

	@Override
	public void addAlartLog(AlartLogDTO alartLogDTO) {
		if (alartLogDTO == null) {
			return;
		}
		alarmLogMapper.insert(AlartLogDTO.toEntity(alartLogDTO));
	}

	@Override
	public AlartLogDTO findLogById(Long id) {
		return AlartLogDTO.toDTO(alarmLogMapper.selectByPrimaryKey(id));
	}

	@Override
	public PageModel<AlartLogDTO> queryAlartLog(AlartLogParam alartLogParam) {
		if (alartLogParam == null) {
			alartLogParam = new AlartLogParam();
		}
		PageHelper.startPage(alartLogParam.getPageNum(), alartLogParam.getPageSize(), YN.Y.getCode());

		// 只能查最近30天的
		Page<AlartLog> page = alarmLogMapper.selectByParam(alartLogParam);
		if (page == null) {
			return null;
		}
		PageModel<AlartLogDTO> pageModel = new PageModel<>();
		pageModel.setPageNum(page.getPageNum());
		pageModel.setPages(page.getPages());
		pageModel.setPageSize(page.getPageSize());
		pageModel.setTotal(page.getTotal());
		pageModel.addAll(AlartLogDTO.toListDTO(page.getResult()));
		return pageModel;

	}

}
