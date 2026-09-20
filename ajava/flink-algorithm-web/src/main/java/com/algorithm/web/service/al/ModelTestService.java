package com.algorithm.web.service.al;

import com.algorithm.web.model.dto.al.ModelTestRequest;
import com.algorithm.web.model.entity.al.AlTask;

public interface ModelTestService {

	AlTask execute(ModelTestRequest request);

}
