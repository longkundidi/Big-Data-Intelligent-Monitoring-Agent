package sw.model3d.stateAssessment.service.impl;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import sw.model3d.stateAssessment.entity.DcAlgorithm;
import sw.model3d.stateAssessment.mapper.DcAlgorithmMapper;
import sw.model3d.stateAssessment.service.DcAlgorithmService;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.*;

/**
 * (DcAlgorithm)表服务实现类
 *
 * @author makejava
 * @since 2024-05-28 17:38:19
 */
@Service
public class DcAlgorithmServiceImpl extends ServiceImpl<DcAlgorithmMapper, DcAlgorithm> implements DcAlgorithmService {

	@Override
	public List<Object> getResultByTaskId(String taskId, String algoShortname) throws JsonProcessingException {
		LambdaQueryWrapper<DcAlgorithm> qw = new LambdaQueryWrapper<>();
		qw.eq(DcAlgorithm::getTaskId, taskId)
			.eq(DcAlgorithm::getAlgoShortname, algoShortname)
			.orderByDesc(DcAlgorithm::getDcTime)
			.last("LIMIT 60");

		// 30 秒一个窗口时，60 条正好覆盖最近约 30 分钟。数据库倒序限量后，
		// 在接口层恢复为时间正序，便于前端直接绘制连续趋势。
		List<DcAlgorithm> list = new ArrayList<>(this.getBaseMapper().selectList(qw));
		Collections.reverse(list);

		ObjectMapper mapper = new ObjectMapper();

		List<Double> dataList = new ArrayList<>(list.size());
		List<Double> thresholdList = new ArrayList<>(list.size());
		List<Double> anomalyFlagList = new ArrayList<>(list.size());
		List<String> dcTimeList = new ArrayList<>(list.size());

		for (DcAlgorithm row : list) {
			JsonNode json = mapper.readTree(row.getDcData());

			// dc_data：CAE 取 rco_hi[0]；其他取 rms_hi
			if ("CAE".equals(row.getAlgoShortname())) {
				JsonNode rcoHi = json.get("rco_hi");
				if (rcoHi != null && rcoHi.isArray() && rcoHi.size() > 0) {
					dataList.add(rcoHi.get(0).asDouble());
				}
				else {
					// 数据不完整时可选择跳过/填 null/填 0
					dataList.add(null);
				}
			}
			else {
				JsonNode rms = json.get("rms_hi");
				dataList.add(rms == null ? null : rms.asDouble());
			}

			JsonNode th = json.get("threshold");
			thresholdList.add(th == null ? null : th.asDouble());

			JsonNode af = json.get("anomaly_flag");
			anomalyFlagList.add(af == null ? null : af.asDouble());

			dcTimeList.add(row.getDcTime());
		}

		ObjectNode out = mapper.createObjectNode();
		out.set("dc_dataList", mapper.valueToTree(dataList));
		out.set("thresholdList", mapper.valueToTree(thresholdList));
		out.set("anomaly_flagList", mapper.valueToTree(anomalyFlagList));
		out.set("dcTimeList", mapper.valueToTree(dcTimeList));

		return Collections.singletonList(out);
	}

	@Override
	public List<Map<String, Object>> getResultCountByTaskId(String taskId) {
		LocalDate endDate = LocalDate.now();
		LocalDate startDate = endDate.minusDays(6);
		LocalDateTime end = endDate.plusDays(1).atStartOfDay();
		LocalDateTime start = startDate.atStartOfDay();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");

		com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DcAlgorithm> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
		queryWrapper.select("DATE(dc_time) AS time", "COUNT(*) AS count")
			// 先固定158测试，后续改成参数，taskId和algoShortname.
			.eq("task_id", taskId)
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

}
