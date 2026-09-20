package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.dto.al.ModelTestRequest;
import com.algorithm.web.service.al.ModelTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/model-test")
public class ModelTestController {

	private final ModelTestService modelTestService;

	@PostMapping
	public RestResult execute(@RequestBody ModelTestRequest request) {
		return RestResult.success(modelTestService.execute(request));
	}

}
