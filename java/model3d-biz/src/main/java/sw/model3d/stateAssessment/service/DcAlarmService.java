package sw.model3d.stateAssessment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.model3d.stateAssessment.entity.DcAlarm;

import java.util.List;
import java.util.Map;

/**
 * (DcAlarm)表服务接口
 *
 * @author makejava
 * @since 2026-04-22 18:10:00
 */
public interface DcAlarmService extends IService<DcAlarm> {

	List<Map<String, Object>> getFaultCountByTaskId(String taskId);

	Map<String, Object> getDiagnosisFaultStats(String turbineCode);

}
