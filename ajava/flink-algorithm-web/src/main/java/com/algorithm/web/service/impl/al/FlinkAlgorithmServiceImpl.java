package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.JobConfigMapper;
import com.algorithm.web.mapper.al.FlinkAlgorithmMapper;
import com.algorithm.web.mapper.al.FlinkServiceRegistryMapper;
import com.algorithm.web.model.entity.flink.FlinkAlgorithm;
import com.algorithm.web.model.entity.flink.FlinkInfo;
import com.algorithm.web.model.entity.flink.FlinkServiceRegistry;
import com.algorithm.web.model.vo.flink.FlinkDataStatsVo;
import com.algorithm.web.model.vo.flink.FlinkJobStatusVo;
import com.algorithm.web.model.vo.flink.FlinkOperationsOverviewVo;
import com.algorithm.web.model.vo.flink.FlinkServiceStatusVo;
import com.algorithm.web.service.al.FlinkAlgorithmService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FlinkAlgorithmServiceImpl implements FlinkAlgorithmService {

	private static final Gson GSON = new Gson();

	private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Shanghai");

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Autowired
	private FlinkAlgorithmMapper flinkAlgorithmMapper;

	@Autowired
	private JobConfigMapper jobConfigMapper;

	@Autowired
	private FlinkServiceRegistryMapper flinkServiceRegistryMapper;

	@Value("${flink.rest.api.url}")
	private String flinkRestApiUrl;

	@Value("${docker.host}")
	private String dockerHost;

	@Value("${elevator.monitor.task-id:231}")
	private long elevatorMonitorTaskId;

	private final RestTemplate restTemplate;

	public FlinkAlgorithmServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public FlinkInfo getFlinkInfo() {
		try {
			return buildFlinkInfo(loadFlinkOverview());
		}
		catch (Exception exception) {
			log.error("读取Flink集群总览失败", exception);
			return null;
		}
	}

	@Override
	public Page<FlinkAlgorithm> getPage(Page<FlinkAlgorithm> page, String jobId, String jobName) {
		return flinkAlgorithmMapper.getPage(page, jobId, jobName);
	}

	@Override
	public FlinkOperationsOverviewVo getOperationsOverview() {
		Map<String, Object> overview = loadFlinkOverview();
		List<Map<String, Object>> jobs = loadFlinkJobs();
		List<Map<String, Object>> containers = loadDockerContainers();
		List<FlinkServiceRegistry> registry = flinkServiceRegistryMapper
			.selectList(new LambdaQueryWrapper<FlinkServiceRegistry>().eq(FlinkServiceRegistry::getVisible, 1)
				.orderByAsc(FlinkServiceRegistry::getSortOrder));

		Set<String> claimedJobIds = new HashSet<>();
		List<FlinkServiceStatusVo> services = registry.stream()
			.map(item -> buildServiceStatus(item, jobs, containers, claimedJobIds))
			.collect(Collectors.toList());
		List<FlinkJobStatusVo> otherJobs = jobs.stream()
			.filter(job -> !claimedJobIds.contains(stringValue(job.get("jid"))))
			.map(this::buildJobStatus)
			.sorted(Comparator.comparing(FlinkJobStatusVo::getState).thenComparing(FlinkJobStatusVo::getJobName))
			.collect(Collectors.toList());

		LocalDateTime latestResultTime = flinkServiceRegistryMapper.selectLatestResultTime(elevatorMonitorTaskId);
		FlinkDataStatsVo dataStats = FlinkDataStatsVo.builder()
			.taskId(elevatorMonitorTaskId)
			.processedLastHour(defaultLong(flinkServiceRegistryMapper.countRecentResults(elevatorMonitorTaskId)))
			.alarmsLastSevenDays(defaultLong(flinkServiceRegistryMapper.countRecentAlarms(elevatorMonitorTaskId)))
			.latestResultTime(latestResultTime == null ? null : latestResultTime.format(DATE_TIME_FORMATTER))
			.build();

		return FlinkOperationsOverviewVo.builder()
			.clusterStatus("ONLINE")
			.flinkVersion(stringValue(overview.get("flink-version")))
			.refreshedAt(LocalDateTime.now(DISPLAY_ZONE).format(DATE_TIME_FORMATTER))
			.cluster(buildFlinkInfo(overview))
			.dataStats(dataStats)
			.services(services)
			.otherJobs(otherJobs)
			.build();
	}

	private Map<String, Object> loadFlinkOverview() {
		String response = restTemplate.getForObject(flinkRestApiUrl + "/overview", String.class);
		return GSON.fromJson(response, Map.class);
	}

	private List<Map<String, Object>> loadFlinkJobs() {
		String response = restTemplate.getForObject(flinkRestApiUrl + "/jobs/overview", String.class);
		Map<String, Object> payload = GSON.fromJson(response, Map.class);
		Object jobs = payload.get("jobs");
		return jobs instanceof List ? (List<Map<String, Object>>) jobs : Collections.emptyList();
	}

	private List<Map<String, Object>> loadDockerContainers() {
		try {
			String url = dockerHost.replaceAll("/$", "") + "/containers/json?all=1";
			ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<Map<String, Object>>>() {
					});
			return response.getBody() == null ? Collections.emptyList() : response.getBody();
		}
		catch (Exception exception) {
			log.warn("读取Docker容器状态失败: {}", exception.getMessage());
			return Collections.emptyList();
		}
	}

	private FlinkServiceStatusVo buildServiceStatus(FlinkServiceRegistry item, List<Map<String, Object>> jobs,
			List<Map<String, Object>> containers, Set<String> claimedJobIds) {
		if ("DOCKER_CONTAINER".equalsIgnoreCase(item.getSourceType())) {
			return buildContainerServiceStatus(item, containers);
		}

		List<Map<String, Object>> matchedJobs = jobs.stream()
			.filter(job -> matches(item, stringValue(job.get("name"))))
			.collect(Collectors.toList());
		matchedJobs.forEach(job -> claimedJobIds.add(stringValue(job.get("jid"))));
		long runningInstances = matchedJobs.stream().filter(job -> "RUNNING".equals(job.get("state"))).count();
		int expectedInstances = defaultExpectedInstances(item);
		String healthStatus;
		String state;
		String detail;
		if (runningInstances == 0) {
			healthStatus = "offline";
			state = matchedJobs.isEmpty() ? "NOT_FOUND" : stringValue(matchedJobs.get(0).get("state"));
			detail = matchedJobs.isEmpty() ? "16.219未发现匹配任务" : "当前没有运行实例";
		}
		else if (runningInstances != expectedInstances) {
			healthStatus = "warning";
			state = "RUNNING";
			detail = String.format("检测到%d个运行实例，预期%d个", runningInstances, expectedInstances);
		}
		else {
			healthStatus = "healthy";
			state = "RUNNING";
			detail = "运行正常";
		}

		List<FlinkJobStatusVo> jobStatuses = matchedJobs.stream()
			.map(this::buildJobStatus)
			.sorted(Comparator.comparing(job -> "RUNNING".equals(job.getState()) ? 0 : 1))
			.collect(Collectors.toList());
		return baseServiceStatus(item, healthStatus, state, (int) runningInstances, expectedInstances, detail,
				jobStatuses);
	}

	private FlinkServiceStatusVo buildContainerServiceStatus(FlinkServiceRegistry item,
			List<Map<String, Object>> containers) {
		List<Map<String, Object>> matchedContainers = containers.stream()
			.filter(container -> containerNames(container).stream().anyMatch(name -> matches(item, name)))
			.collect(Collectors.toList());
		long runningInstances = matchedContainers.stream()
			.filter(container -> "running".equalsIgnoreCase(stringValue(container.get("State"))))
			.count();
		int expectedInstances = defaultExpectedInstances(item);
		String statusText = matchedContainers.isEmpty() ? "16.219未发现匹配容器"
				: stringValue(matchedContainers.get(0).get("Status"));
		boolean unhealthy = statusText.toLowerCase().contains("unhealthy");
		String healthStatus = runningInstances == 0 ? "offline"
				: (runningInstances != expectedInstances || unhealthy ? "warning" : "healthy");
		String state = runningInstances > 0 ? "RUNNING" : (matchedContainers.isEmpty() ? "NOT_FOUND"
				: stringValue(matchedContainers.get(0).get("State")).toUpperCase());
		return baseServiceStatus(item, healthStatus, state, (int) runningInstances, expectedInstances, statusText,
				Collections.emptyList());
	}

	private FlinkServiceStatusVo baseServiceStatus(FlinkServiceRegistry item, String healthStatus, String state,
			int runningInstances, int expectedInstances, String detail, List<FlinkJobStatusVo> jobs) {
		return FlinkServiceStatusVo.builder()
			.serviceKey(item.getServiceKey())
			.displayName(item.getDisplayName())
			.componentType(item.getComponentType())
			.sourceType(item.getSourceType())
			.description(item.getDescription())
			.inputName(item.getInputName())
			.outputName(item.getOutputName())
			.healthStatus(healthStatus)
			.state(state)
			.runningInstances(runningInstances)
			.expectedInstances(expectedInstances)
			.detail(detail)
			.jobs(jobs)
			.build();
	}

	private FlinkJobStatusVo buildJobStatus(Map<String, Object> job) {
		long startTime = longValue(job.get("start-time"));
		return FlinkJobStatusVo.builder()
			.jobId(stringValue(job.get("jid")))
			.jobName(stringValue(job.get("name")))
			.state(stringValue(job.get("state")))
			.startTime(startTime <= 0 ? null
					: Instant.ofEpochMilli(startTime).atZone(DISPLAY_ZONE).format(DATE_TIME_FORMATTER))
			.durationMillis(longValue(job.get("duration")))
			.build();
	}

	private FlinkInfo buildFlinkInfo(Map<String, Object> overview) {
		FlinkInfo flinkInfo = new FlinkInfo();
		flinkInfo.setTaskManagers(longValue(overview.get("taskmanagers")));
		flinkInfo.setTotalSlots(longValue(overview.get("slots-total")));
		flinkInfo.setAvailableSlots(longValue(overview.get("slots-available")));
		flinkInfo.setRunning(longValue(overview.get("jobs-running")));
		flinkInfo.setFinished(longValue(overview.get("jobs-finished")));
		flinkInfo.setCanceled(longValue(overview.get("jobs-cancelled")));
		long failedTaskCount = defaultLong(jobConfigMapper.getFailedTaskCount(-1L));
		flinkInfo.setFailed(longValue(overview.get("jobs-failed")) + failedTaskCount);
		return flinkInfo;
	}

	private boolean matches(FlinkServiceRegistry item, String candidate) {
		if (candidate == null || item.getMatchValue() == null) {
			return false;
		}
		String matchType = stringValue(item.getMatchType()).toUpperCase();
		if ("PREFIX".equals(matchType)) {
			return candidate.startsWith(item.getMatchValue());
		}
		if ("CONTAINS".equals(matchType)) {
			return candidate.contains(item.getMatchValue());
		}
		return candidate.equals(item.getMatchValue());
	}

	private List<String> containerNames(Map<String, Object> container) {
		Object rawNames = container.get("Names");
		if (!(rawNames instanceof List)) {
			return Collections.emptyList();
		}
		List<String> names = new ArrayList<>();
		for (Object rawName : (List<?>) rawNames) {
			String name = stringValue(rawName);
			names.add(name.startsWith("/") ? name.substring(1) : name);
		}
		return names;
	}

	private int defaultExpectedInstances(FlinkServiceRegistry item) {
		return item.getExpectedInstances() == null || item.getExpectedInstances() <= 0 ? 1
				: item.getExpectedInstances();
	}

	private long defaultLong(Long value) {
		return value == null ? 0L : value;
	}

	private long longValue(Object value) {
		return value instanceof Number ? ((Number) value).longValue() : 0L;
	}

	private String stringValue(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

}
