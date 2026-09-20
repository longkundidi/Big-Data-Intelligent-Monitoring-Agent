package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlResumePerceivedDataMapper;
import com.algorithm.web.model.entity.al.AlResumePerceivedData;
import com.algorithm.web.service.al.AlResumePerceivedDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlResumePerceivedDataServiceImpl extends ServiceImpl<AlResumePerceivedDataMapper, AlResumePerceivedData>
		implements AlResumePerceivedDataService {

	@Autowired
	AlResumePerceivedDataMapper alResumePerceivedDataMapper;

	@Override
	public List<String> getPreVariables(Long projectId) {
		return alResumePerceivedDataMapper.getPreVariables(projectId);
	}

}
