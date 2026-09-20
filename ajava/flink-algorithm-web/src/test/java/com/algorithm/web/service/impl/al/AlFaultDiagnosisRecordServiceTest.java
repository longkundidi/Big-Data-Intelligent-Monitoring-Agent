package com.algorithm.web.service.impl.al;

import com.algorithm.web.mapper.al.AlDiagnosisRecordMapper;
import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.algorithm.web.model.entity.al.AlDiagnosisRecord;
import com.algorithm.web.model.entity.al.AlFaultDiagnosis;
import com.algorithm.web.model.entity.al.AlTask;
import com.algorithm.web.service.al.AlTaskService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlFaultDiagnosisRecordServiceTest {

	private AlDiagnosisRecordMapper recordMapper;

	private AlTaskService taskService;

	private AlFaultDiagnosisServiceImpl service;

	@BeforeEach
	public void setUp() {
		recordMapper = Mockito.mock(AlDiagnosisRecordMapper.class);
		taskService = Mockito.mock(AlTaskService.class);
		service = new AlFaultDiagnosisServiceImpl(taskService);
		ReflectionTestUtils.setField(service, "alDiagnosisRecordMapper", recordMapper);
	}

	@Test
	public void shouldReturnRecentDiagnosisRecordsForDevice() {
		AlDiagnosisRecord record = AlDiagnosisRecord.builder().id(1L).turbineCode("elevator-1").build();
		when(recordMapper.selectRecentByTurbineCode("elevator-1", "2026-07-17T00:00:00Z"))
			.thenReturn(Collections.singletonList(record));

		List<AlDiagnosisRecord> records = service.listDiagnosisRecords("elevator-1", "2026-07-17T00:00:00Z");

		Assertions.assertEquals(1, records.size());
		Assertions.assertEquals(1L, records.get(0).getId().longValue());
	}

	@Test
	public void shouldReturnPersistedResultAndOriginalTaskMessage() {
		AlDiagnosisRecord record = AlDiagnosisRecord.builder()
			.id(1L)
			.algorithmTaskId(100L)
			.resultJson("{\"fault_code\":2}")
			.build();
		AlTask task = AlTask.builder()
			.id(100L)
			.taskMsg("{\"values\":[1,2,3]}")
			.taskResult("{\"fault_code\":2}")
			.build();
		when(recordMapper.selectById(1L)).thenReturn(record);
		when(taskService.getById(100L)).thenReturn(task);

		Map<String, Object> result = service.getDiagnosisRecord(1L);

		Assertions.assertSame(record, result.get("record"));
		Assertions.assertEquals(task.getTaskMsg(), result.get("taskMsg"));
		Assertions.assertEquals(task.getTaskResult(), result.get("taskResult"));
	}

	@Test
	public void shouldReserveStableDiagnosisIdentity() {
		when(recordMapper.insert(any(AlDiagnosisRecord.class))).thenReturn(1);

		ReflectionTestUtils.invokeMethod(service, "reserveDiagnosis", request(), algorithm());

		ArgumentCaptor<AlDiagnosisRecord> captor = ArgumentCaptor.forClass(AlDiagnosisRecord.class);
		verify(recordMapper).insert(captor.capture());
		AlDiagnosisRecord record = captor.getValue();
		Assertions.assertEquals(64, record.getDiagnosisKey().length());
		Assertions.assertEquals("231", record.getAlarmTaskId());
		Assertions.assertEquals("2026-07-23T04:18:31Z", record.getAlarmTime());
		Assertions.assertEquals("367-奥克斯1-电梯#1", record.getTurbineCode());
		Assertions.assertEquals("2033379391593467906", record.getNodeId());
		Assertions.assertEquals(1, record.getDiagnosisStatus().intValue());
	}

	@Test
	public void shouldCompleteFaultRecordFromModelTask() {
		AlDiagnosisRecord record = AlDiagnosisRecord.builder().id(1L).diagnosisStatus(1).build();
		AlTask task = AlTask.builder()
			.id(100L)
			.taskState(2)
			.taskMsg("{\"monitorPointId\":\"point-1\",\"requestedTime\":\"2026-07-23T04:18:31Z\","
					+ "\"matchedTime\":\"2026-07-23T04:18:31.600Z\",\"timeOffsetSeconds\":0.6}")
			.taskResult("{\"fault_code\":2,\"fault_name\":\"闸瓦表面全磨损\",\"is_fault\":true," + "\"confidence\":0.400382}")
			.build();

		ReflectionTestUtils.invokeMethod(service, "completeDiagnosisRecord", record, task);

		ArgumentCaptor<AlDiagnosisRecord> captor = ArgumentCaptor.forClass(AlDiagnosisRecord.class);
		verify(recordMapper).updateById(captor.capture());
		AlDiagnosisRecord completed = captor.getValue();
		Assertions.assertEquals(2, completed.getDiagnosisStatus().intValue());
		Assertions.assertEquals(1, completed.getIsFault().intValue());
		Assertions.assertEquals(2, completed.getFaultCode().intValue());
		Assertions.assertEquals("闸瓦表面全磨损", completed.getFaultName());
		Assertions.assertEquals("7", completed.getDictionaryFaultCode());
		Assertions.assertEquals("全部磨损", completed.getDictionaryFaultName());
		Assertions.assertEquals("2026-07-23T04:18:31.600Z", completed.getMatchedTime());
		Assertions.assertEquals(0.6d, completed.getTimeOffsetSeconds(), 0.0001d);
	}

	@Test
	public void shouldMapAllElevatorFaultClassesToFailureDictionary() {
		String[] expectedCodes = { "2", "7", "1", "5", "0", "4", "6" };
		String[] expectedNames = { "部分磨损", "全部磨损", "表面油污", "表面存在异物", "制动力不足", "间隙过大", "未紧密贴合" };

		for (int modelCode = 1; modelCode <= expectedCodes.length; modelCode++) {
			AlDiagnosisRecord record = AlDiagnosisRecord.builder().isFault(1).faultCode(modelCode).build();
			ReflectionTestUtils.invokeMethod(service, "applyFailureDictionaryMapping", record);
			Assertions.assertEquals(expectedCodes[modelCode - 1], record.getDictionaryFaultCode());
			Assertions.assertEquals(expectedNames[modelCode - 1], record.getDictionaryFaultName());
		}
	}

	private DiagnoseInfoDto request() {
		DiagnoseInfoDto request = new DiagnoseInfoDto();
		request.setTaskId("231");
		request.setAlarmTime("2026-07-23T04:18:31Z");
		request.setFarmName("智慧家园小区");
		request.setTurbineName("电梯-A1");
		request.setTurbineCode("367-奥克斯1-电梯#1");
		request.setPart("测点1");
		request.setNodeId("2033379391593467906");
		request.setNodeCode("TM1-1-1");
		request.setAlgoId("256");
		return request;
	}

	private AlFaultDiagnosis algorithm() {
		AlFaultDiagnosis algorithm = new AlFaultDiagnosis();
		algorithm.setId(256L);
		algorithm.setModelShortName("FFCNet");
		return algorithm;
	}

}
