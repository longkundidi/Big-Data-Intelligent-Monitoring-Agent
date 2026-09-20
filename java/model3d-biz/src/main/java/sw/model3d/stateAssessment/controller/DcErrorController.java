package sw.model3d.stateAssessment.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import sw.model3d.stateAssessment.service.DcErrorService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * (DcError)表控制层
 *
 * @author makejava
 * @since 2024-05-29 09:49:12
 */
@RestController
@RequestMapping("/dcError")
@CrossOrigin
public class DcErrorController {

	/**
	 * 服务对象
	 */
	@Autowired
	private DcErrorService dcErrorService;

	@Operation(summary = "根据taskId和算法名获取算法错误信息组成的数组")
	@SysLog("查询")
	@GetMapping("/getVarerrorByTaskId")
	public R getVarerrorByTaskId(@RequestParam String taskId, @RequestParam String algoShortname) {
		return R.ok(dcErrorService.getVarerrorByTaskId(taskId, algoShortname), "查询算法错误结果成功");

	}

}
