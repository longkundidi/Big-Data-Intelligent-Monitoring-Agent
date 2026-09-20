package com.algorithm.web.service.al;

import com.algorithm.web.model.entity.al.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.MalformedURLException;

public interface AlgorithmJobService {

	AlTask startJob(AlTask alTask, String backupModelUrl, String modelUrl, String modelShortName)
			throws JsonProcessingException, MalformedURLException;

	AlTask startConfiguredJob(AlTask alTask, String backupModelUrl, String modelUrl, String executorType,
			String executorConfig) throws JsonProcessingException, MalformedURLException;

	AlTask startAutoTest(AlTask alTask) throws MalformedURLException, JsonProcessingException;

	AlTask startAutoGetMetrics(AlTask alTask) throws MalformedURLException, JsonProcessingException;

	AlTask startOnlineTrain(AlTask alTask) throws MalformedURLException, JsonProcessingException;

}
