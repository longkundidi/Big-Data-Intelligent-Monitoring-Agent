package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.AlResumeFaultData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface AlResumeFaultDataService extends IService<AlResumeFaultData> {

	List<Map<String, Object>> getFaultInfo(Long projectId, Long deviceId);

}
