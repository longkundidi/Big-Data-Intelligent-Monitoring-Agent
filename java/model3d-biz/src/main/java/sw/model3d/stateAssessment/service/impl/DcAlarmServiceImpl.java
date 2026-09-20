package sw.model3d.stateAssessment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import sw.model3d.stateAssessment.entity.DcAlarm;
import sw.model3d.stateAssessment.mapper.DcAlarmMapper;
import sw.model3d.stateAssessment.service.DcAlarmService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * (DcAlarm)表服务实现类
 *
 * @author makejava
 * @since 2026-04-22 18:10:00
 */
@Service
public class DcAlarmServiceImpl extends ServiceImpl<DcAlarmMapper, DcAlarm> implements DcAlarmService {

	@Override
	public List<Map<String, Object>> getFaultCountByTaskId(String taskId) {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(6);
		LocalDateTime end = endDate.plusDays(1).atStartOfDay();
		LocalDateTime start = startDate.atStartOfDay();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

		com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DcAlarm> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
		queryWrapper.select("DATE(dc_time) AS time", "COUNT(*) AS count")
			// 先固定位158测试，后续改成参数，taskId。
			.eq("task_id", taskId)
			.eq("anomaly_flag", "1")
			.ge("dc_time", start.format(formatter))
			.lt("dc_time", end.format(formatter))
			.groupBy("DATE(dc_time)")
			.orderByAsc("DATE(dc_time)");

		List<Map<String, Object>> dbResult = this.getBaseMapper().selectMaps(queryWrapper);
		Map<String, Long> dailyCountMap = new HashMap<>();
		for (Map<String, Object> row : dbResult) {
			Object timeObj = row.get("time");
			Object countObj = row.get("count");
			if (timeObj == null || countObj == null) {
				continue;
			}
			dailyCountMap.put(timeObj.toString(), Long.parseLong(countObj.toString()));
		}

		List<Map<String, Object>> result = new ArrayList<>();
		for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
			String dbKey = day.format(DateTimeFormatter.ISO_LOCAL_DATE);
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("time", day.format(dayFormatter));
			item.put("count", dailyCountMap.getOrDefault(dbKey, 0L));
			result.add(item);
		}

		return result;
	}

	@Override
	public Map<String, Object> getDiagnosisFaultStats(String turbineCode) {
		List<Map<String, Object>> faultTypes = this.getBaseMapper().selectDiagnosisFaultStats(turbineCode);
		long totalCount = faultTypes.stream()
			.map(item -> item.get("count"))
			.filter(java.util.Objects::nonNull)
			.mapToLong(value -> Long.parseLong(value.toString()))
			.sum();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("totalCount", totalCount);
		result.put("faultTypes", faultTypes);
		return result;
	}

}
