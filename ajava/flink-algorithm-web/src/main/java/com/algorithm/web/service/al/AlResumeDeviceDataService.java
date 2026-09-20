package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlResumeDeviceData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AlResumeDeviceDataService extends IService<AlResumeDeviceData> {

	List<String> getFengji(Long projectId);

}
