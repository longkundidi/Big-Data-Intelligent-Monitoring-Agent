package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlResumeDeviceDataMapper;
import com.algorithm.web.model.entity.al.AlResumeDeviceData;
import com.algorithm.web.service.al.AlResumeDeviceDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlResumeDeviceDataServiceImpl extends ServiceImpl<AlResumeDeviceDataMapper, AlResumeDeviceData>
		implements AlResumeDeviceDataService {

	@Autowired
	AlResumeDeviceDataMapper alResumeDeviceDataMapper;

	@Override
	public List<String> getFengji(Long projectId) {

		return alResumeDeviceDataMapper.getFengji(projectId);
	}

}
