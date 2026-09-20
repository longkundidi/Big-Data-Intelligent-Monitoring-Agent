package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.service.impl.al.AlgorithmManagementService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algorithms")
public class AlgorithmManagementController {

	private final AlgorithmManagementService algorithmManagementService;

	@GetMapping("/{type}/{id}")
	public RestResult get(@PathVariable String type, @PathVariable Long id) {
		return RestResult.success(algorithmManagementService.get(type, id));
	}

	@PostMapping("/{type}")
	public RestResult create(@PathVariable String type, @RequestBody JsonNode payload) {
		return RestResult.success(algorithmManagementService.create(type, payload));
	}

	@PutMapping("/{type}/{id}")
	public RestResult update(@PathVariable String type, @PathVariable Long id, @RequestBody JsonNode payload) {
		return RestResult.success(algorithmManagementService.update(type, id, payload));
	}

	@DeleteMapping("/{type}/{id}")
	public RestResult delete(@PathVariable String type, @PathVariable Long id) {
		return RestResult.success(algorithmManagementService.delete(type, id));
	}

}
