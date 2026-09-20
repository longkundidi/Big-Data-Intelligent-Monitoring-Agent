package com.algorithm.web.faultdiagnosis;

import com.algorithm.web.model.dto.al.DiagnoseInfoDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 风机故障诊断样板用例。
 *
 * <p>
 * 数据来源于用户提供的时序库实际记录：
 * </p>
 * <ul>
 * <li>farmName = 普格海口风电场</li>
 * <li>turbineName = 风机#4</li>
 * <li>part = 发电机</li>
 * <li>location = 发电机非驱动端轴承</li>
 * <li>startTime = 2026-04-28T11:20:16.548Z</li>
 * <li>endTime = 2026-04-28T12:20:16.548Z</li>
 * </ul>
 *
 * <p>
 * 这个测试先固定住请求样例和期望结果结构，便于后续：
 * </p>
 * <ul>
 * <li>手动调用 /api/alFaultDiagnosisbase/operate 时复用</li>
 * <li>排查模型返回是否仍然是空对象 {}</li>
 * <li>后续补真正的集成测试</li>
 * </ul>
 */
public class WindpowerDiagnosisExampleTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Test
	public void shouldBuildWindpowerFourExampleRequest() {
		DiagnoseInfoDto diagnoseInfoDto = buildWindpowerFourExampleRequest();

		Assert.assertEquals("普格海口风电场", diagnoseInfoDto.getFarmName());
		Assert.assertEquals("风机#4", diagnoseInfoDto.getTurbineName());
		Assert.assertEquals("发电机", diagnoseInfoDto.getPart());
		Assert.assertEquals("发电机非驱动端轴承", diagnoseInfoDto.getLocation());
		Assert.assertEquals("2026-04-28T11:20:16.548Z", diagnoseInfoDto.getStartTime());
		Assert.assertEquals("2026-04-28T12:20:16.548Z", diagnoseInfoDto.getEndTime());
	}

	@Test
	public void shouldSerializeWindpowerFourExampleRequestToExpectedJson() throws Exception {
		Map<String, String> expected = new LinkedHashMap<>();
		expected.put("farmName", "普格海口风电场");
		expected.put("turbineName", "风机#4");
		expected.put("part", "发电机");
		expected.put("location", "发电机非驱动端轴承");
		expected.put("startTime", "2026-04-28T11:20:16.548Z");
		expected.put("endTime", "2026-04-28T12:20:16.548Z");

		Map<String, String> actual = OBJECT_MAPPER.readValue(
				OBJECT_MAPPER.writeValueAsString(buildWindpowerFourExampleRequest()),
				new TypeReference<Map<String, String>>() {
				});

		Assert.assertEquals(expected, actual);
	}

	@Test
	public void shouldParseExampleDiagnosisResultShape() throws Exception {
		String exampleResponse = "{\n" + "  \"case1\": {\n" + "    \"type\": \"发电机弹性支承故障\",\n"
				+ "    \"suggest\": \"更换\",\n" + "    \"possibility\": 1\n" + "  }\n" + "}";

		Map<String, Object> parsed = OBJECT_MAPPER.readValue(exampleResponse, new TypeReference<Map<String, Object>>() {
		});

		Assert.assertTrue(parsed.containsKey("case1"));

		@SuppressWarnings("unchecked")
		Map<String, Object> case1 = (Map<String, Object>) parsed.get("case1");
		Assert.assertEquals("发电机弹性支承故障", case1.get("type"));
		Assert.assertEquals("更换", case1.get("suggest"));
		Assert.assertEquals(1, ((Number) case1.get("possibility")).intValue());
	}

	public static DiagnoseInfoDto buildWindpowerFourExampleRequest() {
		DiagnoseInfoDto diagnoseInfoDto = new DiagnoseInfoDto();
		diagnoseInfoDto.setFarmName("普格海口风电场");
		diagnoseInfoDto.setTurbineName("风机#4");
		diagnoseInfoDto.setPart("发电机");
		diagnoseInfoDto.setLocation("发电机非驱动端轴承");
		diagnoseInfoDto.setStartTime("2026-04-28T11:20:16.548Z");
		diagnoseInfoDto.setEndTime("2026-04-28T12:20:16.548Z");
		return diagnoseInfoDto;
	}

}
