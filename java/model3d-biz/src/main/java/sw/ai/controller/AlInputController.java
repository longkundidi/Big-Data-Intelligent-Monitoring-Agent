package sw.ai.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sw.ai.domain.vo.AlInputRequest;
import sw.ai.domain.vo.AlInputResponse;
import sw.ai.service.AlInputService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alInput")
public class AlInputController {

	private final AlInputService alInputService;

	@PostMapping("/saveAlInput")
	public R saveAlInput(@RequestBody AlInputRequest request) {

		alInputService.saveAlInput(request.getAlName(), request.getAlClass(), request.getInputVariables());
		return R.ok(null, "保存成功");
	}

	@DeleteMapping("/{name}/{alClass}")
	public R removeByName(@PathVariable String name, @PathVariable String alClass) {
		alInputService.removeByName(name, alClass);
		return R.ok(null, "删除成功");
	}

	@GetMapping("/getCurrentVariables")
	public R getCurrentVariables(@RequestParam("alName") String alName, @RequestParam("type") String type) {
		return R.ok(alInputService.getCurrentVariables(alName, type));
	}

	@GetMapping("/variablesValidation")
	public R variablesValidation(@RequestParam("varNames") List<String> varNames,
			@RequestParam("nodeId") String nodeId) {
		List<AlInputResponse> responses;
		responses = alInputService.variablesValidation(varNames, nodeId);
		if (responses != null && !responses.isEmpty()) {
			return R.ok(responses, "验证成功");
		}
		else {
			return R.ok(null, "验证失败");
		}

	}

	@GetMapping("/getUser")
	public R getUser() {
		return R.ok(SecurityUtils.getUser().getUsername());
	}

	@GetMapping("/getVariablesByalModelNames")
	public R getVariablesByalNames(@RequestParam("alModelNamelList") List<String> alModelNamelList) {
		List<Map<String, Object>> responses = alInputService.getVariablesByalNames(alModelNamelList);
		if (responses != null && !responses.isEmpty()) {
			return R.ok(responses, "查询成功");
		}
		else {
			return R.ok(null, "未查询到变量");
		}
	}

}
