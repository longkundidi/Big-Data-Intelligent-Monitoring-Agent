package com.algorithm.web.service.impl.executealgorithm;

import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.dto.al.ElevatorCmsWaveformRequest;
import com.algorithm.web.model.dto.influxdb.SensorData;
import com.algorithm.web.model.vo.ElevatorCmsWaveformVo;
import com.algorithm.web.service.al.influxdb.InfluxDBService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElevatorFaultDiagnosisServiceTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private InfluxDBService influxDBService;

	private ElevatorFaultDiagnosisService service;

	@BeforeEach
	public void setUp() {
		influxDBService = Mockito.mock(InfluxDBService.class);
		service = new ElevatorFaultDiagnosisService(influxDBService);
		ReflectionTestUtils.setField(service, "maxTimeDifferenceMinutes", 3L);
		ReflectionTestUtils.setField(service, "latestMaxAgeSeconds", 45L);
	}

	@Test
	public void shouldUseNearestElevatorWindowAroundAlarmTime() throws Exception {
		Instant alarmTime = Instant.parse("2026-07-23T04:18:31Z");
		SensorData earlier = sensorData("point-1", alarmTime.minusSeconds(40));
		SensorData nearest = sensorData("point-1", alarmTime.plusSeconds(10));
		when(influxDBService.getElevatorSensorData(eq("智慧家园小区"), eq("电梯-A1"), eq("测点1"), anyString(),
				any(Instant.class), any(Instant.class)))
			.thenReturn(Arrays.asList(earlier, nearest));

		String result = service.buildTaskMsg(buildRequest(alarmTime));
		Map<String, Object> payload = OBJECT_MAPPER.readValue(result, new TypeReference<Map<String, Object>>() {
		});

		Assertions.assertEquals(1024, ((Number) payload.get("sampleCount")).intValue());
		Assertions.assertEquals("point-1", payload.get("monitorPointId"));
		Assertions.assertEquals(alarmTime.toString(), payload.get("requestedTime"));
		Assertions.assertEquals(nearest.getMpTime().toString(), payload.get("matchedTime"));
		Assertions.assertEquals(10.0d, ((Number) payload.get("timeOffsetSeconds")).doubleValue(), 0.001d);
		Assertions.assertEquals(nearest.getMpTime().toString(), payload.get("endTime"));

		ArgumentCaptor<Instant> startCaptor = ArgumentCaptor.forClass(Instant.class);
		ArgumentCaptor<Instant> endCaptor = ArgumentCaptor.forClass(Instant.class);
		verify(influxDBService).getElevatorSensorData(eq("智慧家园小区"), eq("电梯-A1"), eq("测点1"), anyString(),
				startCaptor.capture(), endCaptor.capture());
		Assertions.assertEquals(alarmTime.minusSeconds(180), startCaptor.getValue());
		Assertions.assertEquals(alarmTime.plusSeconds(180).plusMillis(1), endCaptor.getValue());
	}

	@Test
	public void shouldRejectDataOutsideMaximumTimeDifference() {
		Instant alarmTime = Instant.parse("2026-07-23T04:18:31Z");
		when(influxDBService.getElevatorSensorData(eq("智慧家园小区"), eq("电梯-A1"), eq("测点1"), anyString(),
				any(Instant.class), any(Instant.class)))
			.thenReturn(Arrays.asList(sensorData("point-1", alarmTime.plusSeconds(181))));

		Assertions.assertThrows(BizException.class, () -> service.buildTaskMsg(buildRequest(alarmTime)));
	}

	@Test
	public void shouldReturnNearestCmsWaveformForAlarmHistory() {
		Instant alarmTime = Instant.parse("2026-07-23T04:18:31Z");
		SensorData nearest = sensorData("point-1", alarmTime.minusSeconds(12));
		when(influxDBService.getElevatorSensorData(eq("智慧家园小区"), eq("电梯-A1"), eq("测点1"), anyString(),
				any(Instant.class), any(Instant.class)))
			.thenReturn(Arrays.asList(nearest));

		ElevatorCmsWaveformVo waveform = service.queryNearestWaveform(waveformRequest(alarmTime));

		Assertions.assertTrue(waveform.getAvailable());
		Assertions.assertEquals(1024, waveform.getSampleCount().intValue());
		Assertions.assertEquals(45, waveform.getSampleRateHz().intValue());
		Assertions.assertEquals(nearest.getMpTime().toString(), waveform.getMatchedTime());
		Assertions.assertEquals(12.0d, waveform.getTimeOffsetSeconds(), 0.001d);
	}

	@Test
	public void shouldReturnLatestCmsWaveformWithinFreshnessWindow() {
		SensorData latest = sensorData("point-1", Instant.now().minusSeconds(5));
		when(influxDBService.getElevatorSensorData(eq("智慧家园小区"), eq("电梯-A1"), eq("测点1"), anyString(),
				any(Instant.class), any(Instant.class)))
			.thenReturn(Arrays.asList(latest));

		ElevatorCmsWaveformVo waveform = service.queryLatestWaveform(waveformRequest(Instant.now()));

		Assertions.assertTrue(waveform.getAvailable());
		Assertions.assertEquals(1024, waveform.getValues().size());
		Assertions.assertEquals("point-1", waveform.getMonitorPointId());
		Assertions.assertTrue(waveform.getTimeOffsetSeconds() < 45.0d);
	}

	private static ElevatorCmsWaveformRequest waveformRequest(Instant targetTime) {
		ElevatorCmsWaveformRequest request = new ElevatorCmsWaveformRequest();
		request.setFarmName("智慧家园小区");
		request.setTurbineName("电梯-A1");
		request.setNodeName("测点1");
		request.setMonitorPointId("");
		request.setTargetTime(targetTime.toString());
		return request;
	}

	private static String buildRequest(Instant alarmTime) {
		return "{\"farmName\":\"智慧家园小区\",\"turbineName\":\"电梯-A1\","
				+ "\"part\":\"测点1\",\"location\":\"电梯温度\",\"monitorPointId\":\"\"," + "\"alarmTime\":\"" + alarmTime
				+ "\",\"startTime\":\"" + alarmTime.minusSeconds(120) + "\",\"endTime\":\"" + alarmTime + "\"}";
	}

	private static SensorData sensorData(String monitorPointId, Instant time) {
		List<String> values = new ArrayList<>(1024);
		for (int index = 0; index < 1024; index++) {
			values.add(String.valueOf(index / 1000.0d));
		}
		return SensorData.builder()
			.monitorPointId(monitorPointId)
			.mpTime(time)
			.mpData("[" + String.join(",", values) + "]")
			.build();
	}

}
