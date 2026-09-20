package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlResumePerceivedData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlResumePerceivedDataService extends IService<AlResumePerceivedData> {

	List<String> getPreVariables(Long projectId);

}
