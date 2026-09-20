package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlResumeFaultDataMapper;
import com.algorithm.web.mapper.al.AlResumePerceivedDataMapper;
import com.algorithm.web.model.entity.al.AlResumeFaultData;
import com.algorithm.web.service.al.AlResumeFaultDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AlResumeFaultDataServiceImpl extends ServiceImpl<AlResumeFaultDataMapper, AlResumeFaultData>
		implements AlResumeFaultDataService {

	@Autowired
	AlResumeFaultDataMapper alResumeFaultDataMapper;

	@Override
	public List<Map<String, Object>> getFaultInfo(Long projectId, Long deviceId) {
		return alResumeFaultDataMapper.getFaultInfo(projectId, deviceId);
	}

}
