package sw.model3d.stateAssessment.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.Operation;

import sw.model3d.stateAssessment.entity.DcAlgorithm;
import sw.model3d.stateAssessment.service.DcAlgorithmService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

/**
 * (DcAlgorithm)表控制层
 *
 * @author makejava
 * @since 2024-05-28 17:38:08
 */
@RestController
@RequestMapping("/dcAlgorithm")
@CrossOrigin
public class DcAlgorithmController {

	/**
	 * 服务对象
	 */
	@Autowired
	private DcAlgorithmService dcAlgorithmService;

	@Operation(summary = "根据taskId和算法名获取算法结果组成的数组")
	@SysLog("查询")
	@GetMapping("/getResultByTaskId")
	public R getResultByTaskId(@RequestParam String taskId, @RequestParam String algoShortname)
			throws JsonProcessingException {
		try {
			return R.ok(dcAlgorithmService.getResultByTaskId(taskId, algoShortname), "查询算法输出结果成功");
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Operation(summary = "根据taskId统计当前时间（含今天）往前7天的数据量")
	@SysLog("查询")
	@GetMapping("/getResultCountByTaskId")
	public R getResultCountByTaskId(@RequestParam String taskId) {
		return R.ok(dcAlgorithmService.getResultCountByTaskId(taskId), "查询近7天算法结果数量成功");
	}

}
