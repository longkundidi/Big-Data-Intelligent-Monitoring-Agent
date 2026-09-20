package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.service.al.AlAlgorithmMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algorithmMenu")
public class AlAlgorithmMenuController {

	private final AlAlgorithmMenuService alAlgorithmMenuService;

	@GetMapping("/tree")
	public RestResult getTree() {
		return RestResult.success(alAlgorithmMenuService.getTree());
	}

}
