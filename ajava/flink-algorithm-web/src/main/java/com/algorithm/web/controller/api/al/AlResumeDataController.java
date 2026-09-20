package com.algorithm.web.controller.api.al;

import com.algorithm.web.common.RestResult;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.model.dto.al.AlResumeDataDto;
import com.algorithm.web.model.entity.al.AlResumeData;
import com.algorithm.web.service.al.AlResumeDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alresumedata")

public class AlResumeDataController {

	private final AlResumeDataService alResumeDataService;

	private static final Logger log = LoggerFactory.getLogger(AlResumeDataController.class);

	@GetMapping("/page")
	public RestResult getAlResumeDataPage(Page page, AlResumeData alResumeData) {
		return RestResult.success(alResumeDataService.page(page, Wrappers.query(alResumeData)));
	}

	@GetMapping("/{id:\\d+}")
	public RestResult getById(@PathVariable("id") Long id) {
		return RestResult.success(alResumeDataService.getById(id));
	}

	// TODO 增加新增项目接口

	/**
	 * @param alResumeDataDtoList（project，productModel）
	 * @return
	 */
	@PostMapping
	public RestResult save(@RequestBody List<AlResumeDataDto> alResumeDataDtoList) {
		return RestResult.success(alResumeDataService.saveBatch(alResumeDataDtoList));
	}

	@PutMapping
	public RestResult updateById(@RequestBody AlResumeData alResumeData) {
		return RestResult.success(alResumeDataService.updateById(alResumeData));
	}

	@DeleteMapping("/{id}")
	public RestResult removeById(@PathVariable Long id) {
		return RestResult.success(alResumeDataService.removeById(id));
	}

	// 根据元模型查询场景
	@GetMapping("/getProjectsByMetaModelId")
	public RestResult getProjectAndModel(Long metaModelId) {
		LambdaQueryWrapper<AlResumeData> wrapper = Wrappers.lambdaQuery();
		wrapper.eq(AlResumeData::getBomModel, "SBOM").eq(AlResumeData::getMetaModelId, metaModelId);
		Set<String> sceneNames = alResumeDataService.list(wrapper)
			.stream()
			.map(AlResumeData::getProject)
			.collect(Collectors.toSet());
		return RestResult.success(sceneNames);
	}

	// 查询项目信息
	@GetMapping("/getProjects")
	public RestResult getProjectAndModel() {
		LambdaQueryWrapper<AlResumeData> wrapper = Wrappers.lambdaQuery();
		wrapper.eq(AlResumeData::getBomModel, "SBOM");
		return RestResult.success(alResumeDataService.list(wrapper));
	}

	// 根据元模型，得到产品机型的数据
	@GetMapping("/getProductModels/{name}")
	public RestResult getProductModels(@PathVariable("name") String projectName) {
		LambdaQueryWrapper<AlResumeData> lambdaQueryWrapper = Wrappers.lambdaQuery(AlResumeData.class);
		lambdaQueryWrapper.eq(AlResumeData::getProject, projectName).eq(AlResumeData::getBomModel, "MBOM");
		return RestResult.success(alResumeDataService.list(lambdaQueryWrapper));
	}

	// 根据场景名称，得到产品机型的数据
	@GetMapping("/getProductModelBySceneName/{name}")
	public RestResult getProductModelBySceneName(@PathVariable("name") String projectName) {
		LambdaQueryWrapper<AlResumeData> lambdaQueryWrapper = Wrappers.lambdaQuery(AlResumeData.class);
		lambdaQueryWrapper.eq(AlResumeData::getProject, projectName).eq(AlResumeData::getBomModel, "SBOM");
		return RestResult.success(alResumeDataService.list(lambdaQueryWrapper));
	}

	// 根据项目和产品机型,得到BOM信息
	@GetMapping("/getBOMs/{name}/{modelId}")
	public RestResult getBOMs(@PathVariable("name") String projectName, @PathVariable("modelId") String modelId) {
		LambdaQueryWrapper<AlResumeData> lambdaQueryWrapper = Wrappers.lambdaQuery(AlResumeData.class);
		lambdaQueryWrapper.eq(AlResumeData::getProject, projectName).eq(AlResumeData::getProductModel, modelId);

		return RestResult.success(alResumeDataService.list(lambdaQueryWrapper));
	}

	@GetMapping("/getProjectId")
	public RestResult getProjectId(@RequestParam String project, @RequestParam String productModel) {
		LambdaQueryWrapper<AlResumeData> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(AlResumeData::getProject, project)
			.eq(AlResumeData::getProductModel, productModel)
			.eq(AlResumeData::getBomModel, "SBOM");
		Long id = alResumeDataService.getBaseMapper().selectOne(queryWrapper).getId();
		return RestResult.success(id);
	}

	/**
	 * 获取所有场景
	 * @return
	 */
	@GetMapping("/getAllScene")
	public RestResult getAllScene() {
		LambdaQueryWrapper<AlResumeData> wrapper = Wrappers.lambdaQuery();
		wrapper.eq(AlResumeData::getBomModel, "GBOM");
		return RestResult.success(alResumeDataService.list(wrapper));
	}

	/**
	 * 新增场景
	 * @return
	 */
	@PostMapping("/addScene")
	public RestResult addScene(@RequestBody AlResumeData alResumeData) {
		if (alResumeDataService.checkSceneNameExists(alResumeData.getProject())) {
			throw new BizException("该场景名称已存在");
		}
		alResumeData.setBomModel("GBOM");
		try {
			alResumeDataService.save(alResumeData);
			return RestResult.success(alResumeData.getId());
		}
		catch (Exception e) {
			throw new BizException("新增场景失败");
		}
	}

	/**
	 * 新增机型
	 * @return
	 */
	@GetMapping("/addProductModel")
	public RestResult addProductModel(@RequestParam String id, @RequestParam String productModelName) {
		productModelName = productModelName.trim();
		String sceneName = alResumeDataService.getById(id).getProject();
		if (StringUtils.isEmpty(sceneName)) {
			throw new BizException("没有对应的场景名称");
		}
		if (alResumeDataService.checkProductModelExists(sceneName, productModelName)) {
			throw new BizException("该型号名称已存在");
		}

		AlResumeData productModel = new AlResumeData();
		productModel.setProject(sceneName);
		productModel.setBomModel("MBOM");
		productModel.setProductModel(productModelName);
		try {
			alResumeDataService.save(productModel);
			return RestResult.success(productModel.getId());
		}
		catch (Exception e) {
			throw new BizException("新增机型失败");
		}
	}

	@GetMapping("/updateProductModel")
	public RestResult updateProductModel(@RequestParam String modelId, @RequestParam String productModelName) {
		productModelName = productModelName.trim();
		AlResumeData productModel = alResumeDataService.getById(modelId);
		if (Objects.isNull(productModel)) {
			throw new BizException("没有对应的机型");
		}
		if (productModel.getProductModel().equals(productModelName)) {
			return RestResult.success();
		}
		if (alResumeDataService.checkProductModelExists(productModel.getProject(), productModelName)) {
			throw new BizException("该型号名称已存在");
		}
		productModel.setProductModel(productModelName);
		try {
			return RestResult.success(alResumeDataService.updateById(productModel));
		}
		catch (Exception e) {
			throw new BizException("修改机型失败");
		}
	}

	/**
	 * 获取全部的场景机型map
	 * @return Map<String, List < AlResumeData>>
	 */
	@GetMapping("/fetchProModelMap")
	public RestResult fetchProModelMap() {
		return RestResult.success(alResumeDataService.fetchProModelMap());
	}

	/**
	 * 构造场景型号结构树
	 * @return
	 */
	@GetMapping("/buildSceneModelTree")
	public RestResult buildSceneModelTree() {
		List<AlResumeData> allData = alResumeDataService.list();
		return RestResult.success(alResumeDataService.buildSceneModelTree(allData));
	}

	/**
	 * 删除场景节点
	 * @param sceneId 删除场景节点并返回操作结果
	 * @return RestResult 操作结果
	 */

	@PostMapping("/deleteSceneNode")
	public RestResult deleteSceneNode(@RequestParam String sceneId) {
		log.info("删除场景节点，sceneId: {}", sceneId);
		try {
			if (StringUtils.isBlank(sceneId)) {
				return RestResult.error("sceneId 不能为空");
			}
			boolean result = alResumeDataService.deleteSceneNode(sceneId);
			return result ? RestResult.success() : RestResult.error("删除场景失败");
		}
		catch (Exception e) {
			log.error("删除场景失败，sceneId: {}", sceneId, e); // 现在使用 SLF4J 的 log
			throw new BizException("删除场景失败");
		}
	}

	/**
	 * 删除型号节点
	 * @param
	 * @return
	 */
	@PostMapping("/deleteModelNode")
	public RestResult deleteModelNode(@RequestParam String modelId) {
		log.info("删除机型节点，modelId: {}", modelId);
		if (StringUtils.isBlank(modelId)) {
			return RestResult.error("modelId 不能为空");
		}

		// 不 catch BizException，直接返回失败原因
		try {
			boolean result = alResumeDataService.deleteModelNode(modelId);
			return result ? RestResult.success() : RestResult.error("删除机型失败");
		}
		catch (BizException e) {
			// 可以打印日志，但不要吞掉具体信息
			log.warn("业务异常：{}", e.getMessage());
			return RestResult.error(e.getMessage());
		}
		catch (Exception e) {
			log.error("系统异常，modelId: {}", modelId, e);
			return RestResult.error("系统异常，删除失败");
		}
	}

	/**
	 * 查询所有的场景元模型
	 * @return
	 */
	@GetMapping("/getAllSceneMetaModels")
	public RestResult getAllSceneMetaModels() {
		List<AlResumeData> list = alResumeDataService
			.list(new LambdaQueryWrapper<AlResumeData>().select(AlResumeData::getId, AlResumeData::getProject)
				.eq(AlResumeData::getBomModel, "GBOM"));
		return RestResult.success(list);
	}

	/**
	 * 保存场景
	 */
	@GetMapping("/saveScene")
	public RestResult saveScene(@RequestParam String sceneName, @RequestParam List<Long> modelIds,
			@RequestParam String metaModelName) {
		Long metaModelId = alResumeDataService
			.getOne(new LambdaQueryWrapper<AlResumeData>().eq(AlResumeData::getProject, metaModelName)
				.eq(AlResumeData::getBomModel, "GBOM"))
			.getId();
		AlResumeData alResumeData = null;
		for (Long modelId : modelIds) {
			String productModelName = alResumeDataService.getById(modelId).getProductModel();
			alResumeData = new AlResumeData();
			alResumeData.setProductModelId(modelId);
			alResumeData.setProductModel(productModelName);
			alResumeData.setProject(sceneName);
			alResumeData.setBomModel("SBOM");
			alResumeData.setMetaModelId(metaModelId);
			alResumeDataService.save(alResumeData);
		}
		return RestResult.success();
	}

}
