package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.ResourceUsageMapper;
import com.algorithm.web.mapper.taskConfig.ServerListMapper;
import com.algorithm.web.model.entity.al.ResourceUsage;
import com.algorithm.web.model.vo.docker.ContainerStatsResponse;
import com.algorithm.web.service.al.ResourceUsageService;
import com.algorithm.web.service.taskConfig.ServerListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Slf4j
@Service
public class ResourceUsageServiceImpl extends ServiceImpl<ResourceUsageMapper, ResourceUsage>
		implements ResourceUsageService {

	private final RestTemplate restTemplate;

	public ResourceUsageServiceImpl(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Autowired
	ResourceUsageMapper resourceUsageMapper;

	@Autowired
	ServerListMapper serverListMapper;

	@Override
	public ContainerStatsResponse containerStats(String serverName, String containerName) {
		if (serverName.equals("219服务器") || serverName.equals("220服务器")) {
			return defaultStats(serverName, containerName);
		}

		String dockerHost = serverListMapper.getUrlByName(serverName);

		// 获取容器ID
		String containersUrl = UriComponentsBuilder.fromHttpUrl(dockerHost + ":2375/containers/json?all=1")
			.toUriString();
		ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(containersUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<List<Map<String, Object>>>() {
				});

		List<Map<String, Object>> containers = response.getBody();

		// 确保 containers 不为 null 并且不为空
		if (containers == null || containers.isEmpty()) {
			throw new RuntimeException("No containers found");
		}

		// 查找容器 ID
		Optional<Map<String, Object>> containerOptional = containers.stream().filter(container -> {
			List<?> names = (List<?>) container.get("Names");
			if (names != null && !names.isEmpty() && names.get(0) instanceof String) {
				String name = (String) names.get(0);
				return name.replace("/", "").equals(containerName);
			}
			return false;
		}).findFirst();

		if (!containerOptional.isPresent()) {
			throw new RuntimeException("Container not found: " + containerName);
		}

		String containerId = (String) containerOptional.get().get("Id");

		// 获取容器统计数据
		String statsUrl = dockerHost + ":2375/containers/" + containerId + "/stats?stream=false";
		ResponseEntity<Map<String, Object>> statsResponse = restTemplate.exchange(statsUrl, HttpMethod.GET, null,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		Map<String, Object> currentStats = statsResponse.getBody();
		// 提取CPU和内存使用情况
		Map<String, Object> cpuStats = (Map<String, Object>) currentStats.get("cpu_stats");
		Map<String, Object> cpuUsage = (Map<String, Object>) cpuStats.get("cpu_usage");
		Number currentCpuUsage = (Number) cpuUsage.get("total_usage");
		Number currentSystemCpuUsage = (Number) cpuStats.get("system_cpu_usage");

		Map<String, Object> memoryStats = (Map<String, Object>) currentStats.get("memory_stats");
		Number memoryUsage = (Number) memoryStats.get("usage");
		Number memoryLimit = (Number) memoryStats.get("limit");

		// 从数据库中获取上次的统计数据
		ResourceUsage previousStats = resourceUsageMapper.findStatsByContainerId(containerId);
		if (previousStats == null) {
			// 如果是第一次运行，没有上次的数据，初始化 previousStats
			previousStats = new ResourceUsage();
			previousStats.setCpuUsageTotal(0L);
			previousStats.setSystemCpuUsage(0L);
		}

		// 保存当前统计数据到数据库
		ResourceUsage resourceUsage = new ResourceUsage();
		resourceUsage.setContainerId(containerId);
		resourceUsage.setContainerName(containerName);
		resourceUsage.setCpuUsageTotal(currentCpuUsage.longValue());
		resourceUsage.setSystemCpuUsage(currentSystemCpuUsage.longValue());
		resourceUsage.setMemoryUsage(memoryUsage.longValue());
		resourceUsage.setMemoryLimit(memoryLimit.longValue());
		resourceUsage.setCreatedTime(LocalDateTime.now());
		this.save(resourceUsage);

		// 计算增量
		BigDecimal deltaContainerCpuUsage = new BigDecimal(
				currentCpuUsage.longValue() - previousStats.getCpuUsageTotal());
		BigDecimal deltaSystemCpuUsage = new BigDecimal(
				currentSystemCpuUsage.longValue() - previousStats.getSystemCpuUsage());

		// 确保分母不为零
		if (deltaSystemCpuUsage.compareTo(BigDecimal.ZERO) == 0) {
			throw new IllegalArgumentException("System CPU usage increment cannot be zero.");
		}

		// 计算 CPU 使用率
		BigDecimal cpuPercentBD = deltaContainerCpuUsage.divide(deltaSystemCpuUsage, 10, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf((int) cpuStats.get("online_cpus")))
			.multiply(BigDecimal.valueOf(100));
		double cpuPercent = cpuPercentBD.doubleValue();

		// 计算内存使用率
		BigDecimal memoryUsageBD = new BigDecimal(memoryUsage.longValue());
		BigDecimal memoryLimitBD = new BigDecimal(memoryLimit.longValue());
		BigDecimal memoryPercentBD = memoryUsageBD.divide(memoryLimitBD, 10, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100));
		double memoryPercent = memoryPercentBD.doubleValue();
		// 转换内存限制为 GB
		double limitInGB = (double) memoryLimit.longValue() / (1024 * 1024 * 1024);

		// 构建响应对象
		ContainerStatsResponse containerStatsResponse = new ContainerStatsResponse();
		containerStatsResponse.setCpuPercent(cpuPercent);
		containerStatsResponse.setCpuTotal((int) cpuStats.get("online_cpus"));
		containerStatsResponse.setMemoryPercent(memoryPercent);
		containerStatsResponse.setMemoryTotal(String.format("%.2f", limitInGB) + " GB");

		return containerStatsResponse;
	}

	private ContainerStatsResponse defaultStats(String serverName, String containerName) {
		ContainerStatsResponse response = new ContainerStatsResponse();
		if (serverName.equals("219服务器")) {
			response.setCpuPercent(7.8);
			response.setCpuTotal(15);
			response.setMemoryPercent(5.7);
			response.setMemoryTotal("19.17 GB");
		}
		else if (serverName.equals("220服务器")) {
			response.setCpuPercent(3.3);
			response.setCpuTotal(8);
			response.setMemoryPercent(5.07);
			response.setMemoryTotal("2.71 GB");
		}

		// 可选：记录异常信息到日志或返回给页面
		log.warn("Returning default stats: {默认值，因为服务器暂不可用}");
		return response;
	}

	// 定期清理旧数据
	@Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 每小时执行一次
	public void cleanOldData() {
		try {
			LocalDateTime cutoffTime = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
			int deletedCount = resourceUsageMapper.deleteByCreatedTimeBefore(cutoffTime);
			System.out.println("删除数据表resource_usage 20分钟前的数据，共 { " + deletedCount + " } 条记录。");
		}
		catch (Exception e) {
			System.out.println("删除数据表resource_usage 20分钟前的数据时发生错误：" + e);
		}
	}

}
