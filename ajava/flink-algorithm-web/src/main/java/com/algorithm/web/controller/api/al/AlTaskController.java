package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.common.util.JsonUtil;
import com.algorithm.web.controller.web.BaseController;
import com.algorithm.web.enums.SysConfigEnum;
import com.algorithm.web.model.entity.al.*;
import com.algorithm.web.model.vo.docker.ContainerStatsResponse;
import com.algorithm.web.service.SystemConfigService;
import com.algorithm.web.service.al.*;
import com.algorithm.web.utils.PathUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.com.google.gson.JsonObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.server.ExportException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/altask")
public class AlTaskController extends BaseController {

	private final AlTaskService alTaskService;

	private final AlDataCleanService alDataCleanService;

	private final AlDataMiningService alDataMiningService;

	private final AlDataDimReduceService alDataDimReduceService;

	private final AlFeatureExtractionService alFeatureExtractionService;

	private final AlKnowledgeExtractionService alKnowledgeExtractionService;

	private final AlFaultDiagnosisService alFaultDiagnosisService;

	private final AlConditionCategoryService alConditionCategoryService;

	private final AlResourceSchedulingService alResourceSchedulingService;

	private final AlMaintDecisionService alMaintDecisionService;

	private final AlFaultPropagationService alFaultPropagationService;

	private final DomainModelService domainModelService;

	private final AlStateEvaluationService alStateEvaluationService;

	@Autowired
	private MinioService minioService;

	@Autowired
	private MinioClient minioClient;

	@Autowired
	private SystemConfigService systemConfigService;

	@Autowired
	private AlgorithmJobService algorithmJobService;

	private final RestTemplate restTemplate;

	private String filePath;

	private String path;

	@GetMapping("/page")
	public RestResult getAlTaskPage(Page page, String serverName) {
		return RestResult.success(alTaskService.getPage(page, serverName));
	}

	@PostMapping("/updateIsService")
	public RestResult updateIsService(@RequestBody AlTaskVo alTaskVo) {
		return RestResult.success(alTaskService.updateIsService(alTaskVo));
	}

	@GetMapping("/{id}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(alTaskService.getById(id));
	}

	@GetMapping("/getByName")
	public RestResult getByName(Page page, @RequestParam String alModelName) {
		return RestResult.success(alTaskService.getByName(page, alModelName));
	}

	@GetMapping("/startJob")
	public RestResult startJob(@RequestParam Long id, @RequestParam String alModelType, @RequestParam String useCase)
			throws JsonProcessingException, MalformedURLException {
		AlTask alTask = alTaskService.getById(id);
		alTask.setTaskId(alTask.getId());

		switch (alModelType) {
			case "alDataCleaning":
				AlDataClean alDataClean = alDataCleanService
					.getOne(Wrappers.lambdaQuery(AlDataClean.class).eq(AlDataClean::getId, alTask.getAlId()));
				alTask = algorithmJobService.startJob(alTask, alDataClean.getBackupAlUrl(), alDataClean.getAlUrl(),
						alDataClean.getAlShortName());
				break;
			case "dataMining":
				AlDataMining alDataMining = alDataMiningService
					.getOne(Wrappers.lambdaQuery(AlDataMining.class).eq(AlDataMining::getId, alTask.getAlId()));
				alTask = algorithmJobService.startJob(alTask, alDataMining.getBackupAlUrl(), alDataMining.getAlUrl(),
						alDataMining.getAlShortName());
				break;
			case "dimensionalityReduction":
				AlDataDimReduce alDataDimReduce = alDataDimReduceService
					.getOne(Wrappers.lambdaQuery(AlDataDimReduce.class).eq(AlDataDimReduce::getId, alTask.getAlId()));
				alTask = algorithmJobService.startJob(alTask, alDataDimReduce.getBackupAlUrl(),
						alDataDimReduce.getAlUrl(), alDataDimReduce.getAlShortName());
				break;
			case "featureExtraction":
				AlFeatureExtraction alFeatureExtraction = alFeatureExtractionService
					.getOne(Wrappers.lambdaQuery(AlFeatureExtraction.class)
						.eq(AlFeatureExtraction::getId, alTask.getAlId()));
				alTask = algorithmJobService.startJob(alTask, alFeatureExtraction.getBackupAlUrl(),
						alFeatureExtraction.getAlUrl(), alFeatureExtraction.getAlShortName());
				break;
			case "alFaultDiagnosis":
				AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisService
					.getOne(Wrappers.lambdaQuery(AlFaultDiagnosis.class).eq(AlFaultDiagnosis::getId, alTask.getAlId()));
				alTask = algorithmJobService.startConfiguredJob(alTask, alFaultDiagnosis.getBackupModelUrl(),
						alFaultDiagnosis.getModelUrl(), alFaultDiagnosis.getExecutorType(),
						alFaultDiagnosis.getExecutorConfig());
				break;
			case "alStateEvaluation":
				AlStateEvaluation alStateEvaluation = alStateEvaluationService.getOne(
						Wrappers.lambdaQuery(AlStateEvaluation.class).eq(AlStateEvaluation::getId, alTask.getAlId()));
				alTask = algorithmJobService.startConfiguredJob(alTask, alStateEvaluation.getBackupModelUrl(),
						alStateEvaluation.getModelUrl(), alStateEvaluation.getExecutorType(),
						alStateEvaluation.getExecutorConfig());
				break;
			case "conditionsClassification":
				AlConditionCategory alConditionCategory = alConditionCategoryService
					.getOne(Wrappers.lambdaQuery(AlConditionCategory.class)
						.eq(AlConditionCategory::getId, alTask.getAlId()));
				alTask = algorithmJobService.startJob(alTask, alConditionCategory.getBackupModelUrl(),
						alConditionCategory.getModelUrl(), alConditionCategory.getModelShortName());
				break;
			case "maintenanceDecisions":
				AlMaintDecision alMaintDecision = alMaintDecisionService
					.getOne(Wrappers.lambdaQuery(AlMaintDecision.class).eq(AlMaintDecision::getId, alTask.getAlId()));
				alTask = algorithmJobService.startJob(alTask, alMaintDecision.getBackupModelUrl(),
						alMaintDecision.getModelUrl(), alMaintDecision.getModelShortName());
				break;
			case "faultTransmit":
				AlFaultPropagation alFaultPropagation = alFaultPropagationService.getOne(
						Wrappers.lambdaQuery(AlFaultPropagation.class).eq(AlFaultPropagation::getId, alTask.getAlId()));
				alTask = algorithmJobService.startJob(alTask, alFaultPropagation.getBackupModelUrl(),
						alFaultPropagation.getModelUrl(), alFaultPropagation.getModelShortName());
				break;
			case "resourceScheduling":
				AlResourceScheduling alResourceScheduling = alResourceSchedulingService
					.getOne(Wrappers.lambdaQuery(AlResourceScheduling.class)
						.eq(AlResourceScheduling::getId, alTask.getAlId()));
				alTask = algorithmJobService.startJob(alTask, alResourceScheduling.getBackupModelUrl(),
						alResourceScheduling.getModelUrl(), alResourceScheduling.getModelShortName());
				break;
			case "domainModel":
				DomainModel domainModel = domainModelService.getById(alTask.getAlId());
				alTask = algorithmJobService.startJob(alTask, domainModel.getBackupModelUrl(),
						domainModel.getModelUrl(), domainModel.getModelShortName());
				break;
			case "autoTest":
				alTask = algorithmJobService.startAutoTest(alTask);
				break;
			case "autoGetMetrics":
				alTask = algorithmJobService.startAutoGetMetrics(alTask);
				break;
			case "onlineTrain":
				alTask = algorithmJobService.startOnlineTrain(alTask);
				break;
			default:
				throw new IllegalArgumentException("Unknown model type: " + alModelType);
		}

		alTask.setUseCase(useCase);
		alTask.setStartTime(LocalDateTime.now());
		alTask.setEndTime(LocalDateTime.now());
		alTask.setId(id);
		alTask.setAlClass(alModelType);
		return RestResult.success(alTaskService.updateById(alTask));
	}

	@PostMapping
	public RestResult save(@RequestBody AlTask alTask) {
		alTask.setTaskState(0);
		// alTask.setCreator(this.getUserName());
		alTask.setIsDeleted(0);
		return RestResult.success(alTaskService.save(alTask));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlTask alTask) {
		// alTask.setEditor(this.getUserName());
		return RestResult.success(alTaskService.updateById(alTask));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alTaskService.removeById(id));
	}

	@PostMapping("/DataCleaningTest")
	public RestResult DataCleaningTest(@RequestBody AlTask alTask) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("input", alTask.getTaskMsg());
		alTask.setTaskMsg(jsonObject.toJSONString());
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlDataClean alDataClean = alDataCleanService.getById(alTask.getAlId());
		alDataClean.setAlNum(alDataClean.getAlNum() + 1);
		alDataCleanService.updateById(alDataClean);

		List<Object> list = new ArrayList<>();
		list.add(alTask.getId());
		list.add("alDataCleaning");
		return RestResult.success(list);
	}

	@PostMapping("/dataMiningTest")
	public RestResult dataMiningTest(@RequestBody AlTask alTask) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("input", alTask.getTaskMsg());
		alTask.setTaskMsg(jsonObject.toJSONString());
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlDataMining alDataMining = alDataMiningService.getById(alTask.getAlId());
		alDataMining.setAlNum(alDataMining.getAlNum() + 1);
		alDataMiningService.updateById(alDataMining);

		List<Object> list = new ArrayList<>();
		list.add(alTask.getId());
		list.add("dataMining");
		return RestResult.success(list);
	}

	@PostMapping("/dimensionalityReductionTest")
	public RestResult dimensionalityReductionTest(@RequestBody AlTask alTask) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("input", alTask.getTaskMsg());
		alTask.setTaskMsg(jsonObject.toJSONString());
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlDataDimReduce alDataDimReduce = alDataDimReduceService.getById(alTask.getAlId());
		alDataDimReduce.setAlNum(alDataDimReduce.getAlNum() + 1);
		alDataDimReduceService.updateById(alDataDimReduce);

		List<Object> list = new ArrayList<>();
		list.add(alTask.getId());
		list.add("dimensionalityReduction");
		return RestResult.success(list);
	}

	@PostMapping("/featureExtractionTest")
	public RestResult featureExtractionTest(@RequestBody AlTask alTask) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("input", alTask.getTaskMsg());
		alTask.setTaskMsg(jsonObject.toJSONString());
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlFeatureExtraction alFeatureExtraction = alFeatureExtractionService.getById(alTask.getAlId());
		alFeatureExtraction.setAlNum(alFeatureExtraction.getAlNum() + 1);
		alFeatureExtractionService.updateById(alFeatureExtraction);

		List<Object> list = new ArrayList<>();
		list.add(alTask.getId());
		list.add("featureExtraction");
		return RestResult.success(list);
	}

	@PostMapping("/FaultDiagnosisTest")
	public RestResult FaultDiagnosisTest(@RequestBody AlTask alTask) {
		Long isjson = alTask.getIsjson();
		if (isjson == 1) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("input", alTask.getTaskMsg());
			alTask.setTaskMsg(jsonObject.toJSONString());
		}

		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlFaultDiagnosis alFaultDiagnosis = alFaultDiagnosisService.getbyId(alTask.getAlId());
		alFaultDiagnosis.setModelNum(alFaultDiagnosis.getModelNum() + 1);
		alFaultDiagnosisService.updateById(alFaultDiagnosis);

		String alModelType = "alFaultDiagnosis";
		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add(alModelType);
		return RestResult.success(objects);
	}

	@PostMapping("/StateEvaluationTest")
	public RestResult StateEvaluationTest(@RequestBody AlTask alTask) {
		Long isjson = alTask.getIsjson();
		if (isjson == 1) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("input", alTask.getTaskMsg());
			alTask.setTaskMsg(jsonObject.toJSONString());
		}

		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlStateEvaluation alStateEvaluation = alStateEvaluationService.getById(alTask.getAlId());
		alStateEvaluation.setModelNum(alStateEvaluation.getModelNum() + 1);
		alStateEvaluationService.updateById(alStateEvaluation);

		String alModelType = "alStateEvaluation";
		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add(alModelType);
		return RestResult.success(objects);
	}

	@PostMapping("/conditionsClassificationTest")
	public RestResult conditionsClassificationTest(@RequestBody AlTask alTask) {
		Long isjson = alTask.getIsjson();
		if (isjson == 1) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("input", alTask.getTaskMsg());
			alTask.setTaskMsg(jsonObject.toJSONString());
		}

		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlConditionCategory alConditionCategory = alConditionCategoryService.getbyId(alTask.getAlId());
		alConditionCategory.setModelNum(alConditionCategory.getModelNum() + 1);
		alConditionCategoryService.updateById(alConditionCategory);

		String alModelType = "conditionsClassification";
		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add(alModelType);
		return RestResult.success(objects);
	}

	@PostMapping("/maintenanceDecisionsTest")
	public RestResult maintenanceDecisionsTest(@RequestBody AlTask alTask) {
		Long isjson = alTask.getIsjson();
		if (isjson == 1) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("input", alTask.getTaskMsg());
			alTask.setTaskMsg(jsonObject.toJSONString());
		}

		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlMaintDecision alMaintDecision = alMaintDecisionService.getbyId(alTask.getAlId());
		alMaintDecision.setModelNum(alMaintDecision.getModelNum() + 1);
		alMaintDecisionService.updateById(alMaintDecision);

		String alModelType = "maintenanceDecisions";
		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add(alModelType);
		return RestResult.success(objects);
	}

	@PostMapping("/faultTransmitTest")
	public RestResult faultTransmitTest(@RequestBody AlTask alTask) {
		Long isjson = alTask.getIsjson();
		if (isjson == 1) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("input", alTask.getTaskMsg());
			alTask.setTaskMsg(jsonObject.toJSONString());
		}

		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlFaultPropagation alFaultPropagation = alFaultPropagationService.getbyId(alTask.getAlId());
		alFaultPropagation.setModelNum(alFaultPropagation.getModelNum() + 1);
		alFaultPropagationService.updateById(alFaultPropagation);

		String alModelType = "faultTransmit";
		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add(alModelType);
		return RestResult.success(objects);
	}

	@PostMapping("/resourceSchedulingTest")
	public RestResult resourceSchedulingTest(@RequestBody AlTask alTask) {
		Long isjson = alTask.getIsjson();
		if (isjson == 1) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("input", alTask.getTaskMsg());
			alTask.setTaskMsg(jsonObject.toJSONString());
		}

		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		AlResourceScheduling alResourceScheduling = alResourceSchedulingService.getbyId(alTask.getAlId());
		alResourceScheduling.setModelNum(alResourceScheduling.getModelNum() + 1);
		alResourceSchedulingService.updateById(alResourceScheduling);

		String alModelType = "resourceScheduling";
		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add(alModelType);
		return RestResult.success(objects);
	}

	@PostMapping("/domainModelTest")
	public RestResult domainModelTest(@RequestBody AlTask alTask) {
		Long isjson = alTask.getIsjson();
		if (isjson == 1) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("input", alTask.getTaskMsg());
			alTask.setTaskMsg(jsonObject.toJSONString());
		}

		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTaskService.save(alTask);
		DomainModel domainModel = domainModelService.getById(alTask.getAlId());
		domainModel.setModelNum(domainModel.getModelNum() + 1);
		domainModelService.updateById(domainModel);

		String alModelType = "domainModel";
		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add(alModelType);
		return RestResult.success(objects);
	}

	@GetMapping("/autoTest")
	public RestResult autoTest(@RequestParam(defaultValue = "") String programUrl) {
		AlTask alTask = new AlTask();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("programUrl", programUrl);
		alTask.setTaskMsg(jsonObject.toJSONString());
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTask.setUseCase("自动测试");
		alTaskService.save(alTask);

		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add("autoTest");
		return RestResult.success(objects);
	}

	// 自动获取评价指标
	@GetMapping("/autoGetMetrics")
	public RestResult autoGetMetrics(@RequestParam(defaultValue = "") String programUrl) {
		AlTask alTask = new AlTask();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("programUrl", programUrl);
		alTask.setTaskMsg(jsonObject.toJSONString());
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTask.setUseCase("自动获取评价指标");
		alTaskService.save(alTask);

		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add("autoGetMetrics");
		return RestResult.success(objects);
	}

	// 自动获取评价指标
	@PostMapping("/onlineTrain")
	public RestResult onlineTrain(MultipartFile trainDataset, MultipartFile testDataset,
			@RequestParam(defaultValue = "") String programUrl) {
		/* 将数据集传递到基础算法程序包中 */

		try {
			upload(trainDataset, testDataset, programUrl);
		}
		catch (Exception e) {
			return RestResult.error("上传失败，请重新上传");
		}

		/* 执行在线训练微服务 */
		AlTask alTask = new AlTask();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("programUrl", programUrl);
		alTask.setTaskMsg(jsonObject.toJSONString());
		alTask.setTaskState(0);
		alTask.setIsDeleted(0);
		alTask.setUseCase("在线训练");
		alTaskService.save(alTask);

		ArrayList<Object> objects = new ArrayList<>();
		objects.add(alTask.getId());
		objects.add("onlineTrain");
		return RestResult.success(objects);
	}

	public RestResult upload(MultipartFile trainDataset, MultipartFile testDataset, String path) throws Exception {
		Path workDir = Files.createTempDirectory("algorithm-upload-");
		String localFilePath = workDir.resolve("temp_file.zip").toString();
		String tempDirPath = workDir.resolve("temp_dir").toString();
		String newZipPath = workDir.resolve("modified_file.zip").toString();
		try {
			downloadFileFromMinio(path, localFilePath);
			unzip(localFilePath, tempDirPath);
			writeDatasetToFile(trainDataset, Paths.get(tempDirPath, "trainDataset.csv").toString());
			writeDatasetToFile(testDataset, Paths.get(tempDirPath, "testDataset.csv").toString());
			zip(tempDirPath, newZipPath);
			uploadFileToMinio(newZipPath, path);
			return RestResult.success("File uploaded successfully.");
		}
		finally {
			cleanUpTempFiles(localFilePath, tempDirPath, newZipPath);
			Files.deleteIfExists(workDir);
		}
	}

	// Step 1: 下载文件
	private void downloadFileFromMinio(String path, String localFilePath) throws Exception {
		// 获取文件内容（InputStream）
		InputStream fileInputStream = minioService.downloadResource1(path);
		// 获取目标文件路径
		Path localPath = Paths.get(localFilePath);
		// 检查父目录是否存在，如果不存在则创建
		Path parentDir = localPath.getParent();
		if (parentDir != null && !Files.exists(parentDir)) {
			Files.createDirectories(parentDir); // 创建父目录
		}
		// 将文件内容写入到本地路径 (path2)
		try (OutputStream os = Files.newOutputStream(localPath)) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = fileInputStream.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		}
		finally {
			fileInputStream.close();
		}
	}

	// Step 2: 上传文件
	private void uploadFileToMinio(String filePath, String path) throws Exception {
		this.filePath = filePath;
		this.path = path;
		Path file = Paths.get(filePath);

		// 获取文件大小
		long fileSize;
		try {
			fileSize = Files.size(file);
		}
		catch (IOException e) {
			throw new IOException("无法获取文件大小: " + filePath, e);
		}

		// 创建文件输入流
		try (InputStream inputStream = new FileInputStream(file.toFile())) {
			// 上传文件到 MinIO
			try {
				minioClient.putObject(PutObjectArgs.builder()
					.bucket("bigdata") // 替换为你的存储桶名称
					.object(path)
					.stream(inputStream, fileSize, -1)
					.build());
			}
			catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException
					| InvalidResponseException | NoSuchAlgorithmException | XmlParserException | ServerException e) {
				throw new Exception("上传文件到 MinIO 时发生错误: " + e.getMessage(), e);
			}
		}
		catch (IOException e) {
			throw new IOException("读取文件时发生错误: " + filePath, e);
		}
	}

	// Step 2: 解压文件
	private void unzip(String zipFilePath, String destDir) throws IOException {
		File dir = new File(destDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath))) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				File file = new File(destDir, entry.getName());
				if (entry.isDirectory()) {
					file.mkdirs();
				}
				else {
					try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
						byte[] buffer = new byte[1024];
						int len;
						while ((len = zipInputStream.read(buffer)) > 0) {
							out.write(buffer, 0, len);
						}
					}
				}
				zipInputStream.closeEntry();
			}
		}
	}

	// Step 3: 写入 dataset 数据到文件
	private void writeDatasetToFile(MultipartFile dataset, String filePath) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath))) {
			writer.write(new String(dataset.getBytes(), "UTF-8"));
		}
	}

	// Step 4: 压缩文件
	// 压缩文件或文件夹，不添加额外的根目录
	private void zip(String sourceDirPath, String zipFilePath) throws IOException {
		Path sourceDir = Paths.get(sourceDirPath);
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
			Files.walk(sourceDir)
				.filter(path -> !Files.isDirectory(path)) // 只压缩文件，跳过目录
				.forEach(path -> {
					try {
						// 计算相对于源目录的路径，避免包含额外的根目录
						Path relativePath = sourceDir.relativize(path);
						zos.putNextEntry(new ZipEntry(relativePath.toString()));
						Files.copy(path, zos);
						zos.closeEntry();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				});
		}
	}

	// Step 5: 上传文件到 MinIO

	// 清理临时文件
	private void cleanUpTempFiles(String localFilePath, String tempDirPath, String newZipPath) throws IOException {
		Files.deleteIfExists(Paths.get(localFilePath));
		Path tempDir = Paths.get(tempDirPath);
		if (Files.exists(tempDir)) {
			try (Stream<Path> paths = Files.walk(tempDir)) {
				paths.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
			}
		}
		Files.deleteIfExists(Paths.get(newZipPath));
	}

	@GetMapping("/getLog")
	public RestResult getLogTable(@RequestParam(name = "alId") int alId, @RequestParam(name = "alClass") String alClass,
			@RequestParam(name = "curPage", required = false, defaultValue = "1") Integer curPage,
			@RequestParam(name = "size", required = false, defaultValue = "10") Integer size) {
		Page<AlTask> page = new Page<>(curPage, size);
		LambdaQueryWrapper<AlTask> lambdaQueryWrapper = Wrappers.lambdaQuery(AlTask.class);
		lambdaQueryWrapper.eq(AlTask::getAlId, alId).eq(AlTask::getAlClass, alClass);
		lambdaQueryWrapper.orderByDesc(AlTask::getStartTime);
		alTaskService.page(page, lambdaQueryWrapper);
		return RestResult.success(page);
	}

	// 导入的两个语言模型，特殊
	@PostMapping("/testModel/{useCase}")
	public RestResult testModel(@PathVariable String useCase, @RequestBody AlKnowledgeExtraction alKnowledgeExtraction)
			throws JsonProcessingException {
		String backupModelUrl = alKnowledgeExtraction.getBackupAlUrl();
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(backupModelUrl);
		// 获取 "backup_urls" 数组节点
		JsonNode backupUrlsNode = rootNode.get("backup_urls");
		// 创建一个 List 来存储解析出来的 URL
		List<String> algorithmUrls = new ArrayList<>();
		// 遍历数组并添加到 List
		if (backupUrlsNode.isArray()) {
			for (JsonNode urlNode : backupUrlsNode) {
				algorithmUrls.add(urlNode.asText());
			}
		}
		algorithmUrls.add(alKnowledgeExtraction.getAlUrl());
		String reStr = "";
		AlTask alTask = new AlTask();
		for (String url : algorithmUrls) {
			try {
				reStr = restTemplate.getForObject(url + alKnowledgeExtraction.getInput(), String.class);

				// 处理成功的返回结果
				if (isValidJson(reStr)) {
					URL urls = new URL(url);
					String base_url = urls.getProtocol() + "://" + urls.getHost();

					alTask = AlTask.builder()
						.taskState(2)
						.taskUrl(url)
						.serverUrl(base_url)
						.alId(alKnowledgeExtraction.getId().longValue())
						.taskMsg(JsonUtil.toJson(alKnowledgeExtraction.getInput()))
						.taskResult(JsonUtil.toJson(reStr))
						.startTime(LocalDateTime.now())
						.endTime(LocalDateTime.now())
						.build();
					break; // 如果成功，则退出循环
				}
				else {
					continue;

				}
			}
			catch (RestClientException e) {
				// 记录错误信息
				alTask.setTaskUrl(url); // 记录实际使用的算法 URL
				alTask.setTaskState(3); // 设置任务状态为失败
				alTask.setTaskResult("Error: " + e.getMessage());
				alTask.setTaskMsg(JsonUtil.toJson(alKnowledgeExtraction.getInput()));
				alTask.setStartTime(LocalDateTime.now());
				alTask.setEndTime(LocalDateTime.now());

				// 继续处理下一个 URL
				continue;
			}
			catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		alTask.setAlId(Long.valueOf(alKnowledgeExtraction.getId()));
		alTask.setAlClass("alKnowledgeExtraction");
		alTask.setTaskReUrl(systemConfigService.getSystemConfigByKey(SysConfigEnum.ALGORITHM_CALLBACK_URL.getKey())
				+ "algorithm/job/algorithmJobCallback");
		alTask.setUseCase(useCase);
		alTaskService.save(alTask);

		AlKnowledgeExtraction alKnowledgeExtraction1 = alKnowledgeExtractionService
			.getById(alKnowledgeExtraction.getId());
		alKnowledgeExtraction1.setAlNum(alKnowledgeExtraction.getAlNum() + 1);
		alKnowledgeExtractionService.updateById(alKnowledgeExtraction1);
		return RestResult.success(reStr);

	}

	private boolean isValidJson(String json) {
		try {
			JsonUtil.fromJson(json, Object.class); // 尝试将其解析为 Object
			return true; // 如果没有抛出异常，说明是有效的 JSON
		}
		catch (Exception e) {
			return false; // 如果抛出异常，则不是有效的 JSON
		}
	}

	// 将字符串转成 JSON 字符串
	private String convertToJsonString(String res) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("message", res); // 将 res 作为值添加到 JSON 对象中
		return jsonObject.toString(); // 返回 JSON 字符串
	}

}
