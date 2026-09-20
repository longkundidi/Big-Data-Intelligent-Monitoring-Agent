package sw.model3d.stateAssessment.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sw.model3d.stateAssessment.service.DcAlarmService;

/**
 * (DcAlarm)表控制层
 *
 * @author makejava
 * @since 2026-04-22 18:10:00
 */
@RestController
@RequestMapping("/dcAlarm")
@CrossOrigin
public class DcAlarmController {

	@Autowired
	private DcAlarmService dcAlarmService;

	@Operation(summary = "根据taskId按天统计近7天故障数量")
	@SysLog("查询")
	@GetMapping("/getFaultCountByTaskId")
	public R getFaultCountByTaskId(@RequestParam String taskId) {
		return R.ok(dcAlarmService.getFaultCountByTaskId(taskId), "查询近7天故障数量成功");
	}

	@Operation(summary = "按设备实例统计已完成故障诊断结果")
	@GetMapping("/getDiagnosisFaultStats")
	public R getDiagnosisFaultStats(@RequestParam String turbineCode) {
		return R.ok(dcAlarmService.getDiagnosisFaultStats(turbineCode), "查询故障诊断统计成功");
	}

}
