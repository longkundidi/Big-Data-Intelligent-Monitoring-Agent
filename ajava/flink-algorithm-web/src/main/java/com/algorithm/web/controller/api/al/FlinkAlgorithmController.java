package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.model.entity.al.AlDataClean;
import com.algorithm.web.model.entity.flink.FlinkAlgorithm;
import com.algorithm.web.service.al.FlinkAlgorithmService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/flinkStatus")
public class FlinkAlgorithmController {

	@Autowired
	private FlinkAlgorithmService flinkAlgorithmService;

	@GetMapping("/getJobsCount")
	public RestResult getJobsCount() {
		return RestResult.success(flinkAlgorithmService.getFlinkInfo());
	}

	@GetMapping("/page")
	public RestResult getAlAlgorithmPage(@RequestParam Integer currentPage, @RequestParam Integer pageSize,
			@RequestParam(defaultValue = "") String jobId, @RequestParam(defaultValue = "") String jobName) {
		return RestResult.success(flinkAlgorithmService.getPage(new Page<>(currentPage, pageSize), jobId, jobName));
	}

	@GetMapping("/operations")
	public RestResult getOperationsOverview() {
		return RestResult.success(flinkAlgorithmService.getOperationsOverview());
	}

}
