package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.JobConfigMapper;
import com.algorithm.web.mapper.al.FlinkServiceRegistryMapper;
import com.algorithm.web.model.entity.flink.FlinkServiceRegistry;
import com.algorithm.web.model.vo.flink.FlinkOperationsOverviewVo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class FlinkAlgorithmServiceImplTest {

	@Test
	public void shouldBuildElevatorOperationsFromLiveRuntime() {
		RestTemplate restTemplate = new RestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
		server.expect(once(), requestTo("http://flink:8000/overview"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("{\"taskmanagers\":3,\"slots-total\":60,\"slots-available\":43,\"jobs-running\":5,"
					+ "\"jobs-finished\":0,\"jobs-cancelled\":1,\"jobs-failed\":0,\"flink-version\":\"1.17.0\"}",
					MediaType.APPLICATION_JSON));
		server.expect(once(), requestTo("http://flink:8000/jobs/overview"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess("{\"jobs\":[" + job("guard-1", "dc-guard") + "," + job("guard-2", "dc-guard") + ","
					+ job("regtcn-1", "algorithm_REGTCN") + ","
					+ job("sink-1", "insert-into_default_catalog.default_database.dc_algorithm_mysql,dc_alarm_mysql")
					+ "," + job("other-1", "algorithm_Other") + "]}", MediaType.APPLICATION_JSON));
		server.expect(once(), requestTo("http://docker:2375/containers/json?all=1"))
			.andExpect(method(HttpMethod.GET))
			.andRespond(withSuccess(
					"[{\"Names\":[\"/elevator-anomaly-monitoring\"],\"State\":\"running\","
							+ "\"Status\":\"Up 1 hour (healthy)\"},{\"Names\":[\"/elevator-fault-diagnosis\"],"
							+ "\"State\":\"running\",\"Status\":\"Up 1 hour (healthy)\"}]",
					MediaType.APPLICATION_JSON));

		FlinkServiceRegistryMapper registryMapper = Mockito.mock(FlinkServiceRegistryMapper.class);
		Mockito.when(registryMapper.selectList(Mockito.any()))
			.thenReturn(Arrays.asList(registry("router", "dc-guard", "FLINK_JOB", "EXACT", 1, 10),
					registry("monitor", "algorithm_REGTCN", "FLINK_JOB", "EXACT", 1, 20),
					registry("sink", "insert-into_default_catalog.default_database.dc_algorithm_mysql", "FLINK_JOB",
							"PREFIX", 1, 30),
					registry("monitor-model", "elevator-anomaly-monitoring", "DOCKER_CONTAINER", "EXACT", 1, 40),
					registry("diagnosis-model", "elevator-fault-diagnosis", "DOCKER_CONTAINER", "EXACT", 1, 50)));
		Mockito.when(registryMapper.countRecentResults(231L)).thenReturn(120L);
		Mockito.when(registryMapper.countRecentAlarms(231L)).thenReturn(4L);
		Mockito.when(registryMapper.selectLatestResultTime(231L)).thenReturn(LocalDateTime.of(2026, 7, 23, 18, 30, 34));

		JobConfigMapper jobConfigMapper = Mockito.mock(JobConfigMapper.class);
		Mockito.when(jobConfigMapper.getFailedTaskCount(-1L)).thenReturn(0L);

		FlinkAlgorithmServiceImpl service = new FlinkAlgorithmServiceImpl(restTemplate);
		ReflectionTestUtils.setField(service, "flinkServiceRegistryMapper", registryMapper);
		ReflectionTestUtils.setField(service, "jobConfigMapper", jobConfigMapper);
		ReflectionTestUtils.setField(service, "flinkRestApiUrl", "http://flink:8000");
		ReflectionTestUtils.setField(service, "dockerHost", "http://docker:2375");
		ReflectionTestUtils.setField(service, "elevatorMonitorTaskId", 231L);

		FlinkOperationsOverviewVo overview = service.getOperationsOverview();

		Assertions.assertEquals("ONLINE", overview.getClusterStatus());
		Assertions.assertEquals(5, overview.getServices().size());
		Assertions.assertEquals("warning", overview.getServices().get(0).getHealthStatus());
		Assertions.assertEquals(2, overview.getServices().get(0).getRunningInstances().intValue());
		Assertions.assertEquals("healthy", overview.getServices().get(1).getHealthStatus());
		Assertions.assertEquals(1, overview.getOtherJobs().size());
		Assertions.assertEquals("algorithm_Other", overview.getOtherJobs().get(0).getJobName());
		Assertions.assertEquals(120L, overview.getDataStats().getProcessedLastHour().longValue());
		Assertions.assertEquals(4L, overview.getDataStats().getAlarmsLastSevenDays().longValue());
		server.verify();
	}

	private static String job(String id, String name) {
		return String.format(
				"{\"jid\":\"%s\",\"name\":\"%s\",\"state\":\"RUNNING\",\"start-time\":1784800000000,\"duration\":60000}",
				id, name);
	}

	private static FlinkServiceRegistry registry(String key, String matchValue, String sourceType, String matchType,
			int expectedInstances, int sortOrder) {
		FlinkServiceRegistry registry = new FlinkServiceRegistry();
		registry.setServiceKey(key);
		registry.setDisplayName(key);
		registry.setComponentType("TEST");
		registry.setSourceType(sourceType);
		registry.setMatchType(matchType);
		registry.setMatchValue(matchValue);
		registry.setExpectedInstances(expectedInstances);
		registry.setVisible(1);
		registry.setSortOrder(sortOrder);
		return registry;
	}

}
