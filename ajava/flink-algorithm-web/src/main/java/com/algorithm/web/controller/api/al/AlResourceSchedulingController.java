package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlResourceScheduling;
import com.algorithm.web.service.al.AlResourceSchedulingRegister1to2Service;
import com.algorithm.web.service.al.AlResourceSchedulingRegister3to4Service;
import com.algorithm.web.service.al.AlResourceSchedulingService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/resourceScheduling")
public class AlResourceSchedulingController extends BaseController {

	private final AlResourceSchedulingService alResourceSchedulingService;

	private final AlResourceSchedulingRegister1to2Service alResourceSchedulingRegister1To2Service;

	private final AlResourceSchedulingRegister3to4Service alResourceSchedulingRegister3To4Service;

	@GetMapping("/page")
	public RestResult getAlResourceSchedulingBasePage(Page page, AlResourceScheduling alResourceScheduling) {
		return RestResult.success(alResourceSchedulingService.getPage(page, alResourceScheduling));
	}

	/* 模糊查询对象 */
	@GetMapping("/page1")
	public RestResult getAlResourceSchedulingBasePage1(Page page, AlResourceScheduling alResourceScheduling) {
		return RestResult.success(alResourceSchedulingService.getPageObj(page, alResourceScheduling));
	}

	/* 模糊查询模型 */
	@GetMapping("/page2")
	public RestResult getAlResourceSchedulingBasePage2(Page page, AlResourceScheduling alResourceScheduling) {
		return RestResult.success(alResourceSchedulingService.getPageName(page, alResourceScheduling));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(alResourceSchedulingService.getById(id));
	}

	@PostMapping
	public RestResult save(@RequestBody AlResourceScheduling alResourceScheduling) {
		String programUrl = alResourceScheduling.getProgramUrl();
		alResourceScheduling.setIsService(0);
		alResourceScheduling
			.setModelShortName(programUrl.substring(programUrl.lastIndexOf('/') + 1, programUrl.indexOf('.')));
		alResourceSchedulingService.save(alResourceScheduling);
		return RestResult.success(alResourceSchedulingService.updateCodeByName(alResourceScheduling.getModelName()));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlResourceScheduling alResourceScheduling) {
		return RestResult.success(alResourceSchedulingService.updateById(alResourceScheduling));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById1(@PathVariable Long id) {
		String modelName = alResourceSchedulingService.getnamebyid(id);
		alResourceSchedulingRegister1To2Service.deletebyname(modelName);
		alResourceSchedulingRegister3To4Service.deletebyname(modelName);
		return RestResult.success(alResourceSchedulingService.removeById(id));
	}

	@GetMapping("/list")
	public RestResult getlist() {

		return RestResult.success(alResourceSchedulingService.list());
	}

	@GetMapping("/exitName/{name}")
	public RestResult exitName(@PathVariable String name) {
		Boolean exitName = alResourceSchedulingService.exitName(name);
		if (!exitName) {
			return RestResult.success();
		}
		else {
			return RestResult.error("该名称已存在");
		}
	}

	// 执行风机故障诊断
	@PostMapping("/operate/{useCase}")
	public RestResult operate(@PathVariable String useCase, @RequestBody DiagnoseInfoDto diagnoseInfoDto) {
		return RestResult.success(alResourceSchedulingService.operate(useCase, diagnoseInfoDto));
	}

}
