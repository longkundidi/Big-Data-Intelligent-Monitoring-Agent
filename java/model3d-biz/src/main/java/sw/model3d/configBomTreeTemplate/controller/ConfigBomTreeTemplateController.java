package sw.model3d.configBomTreeTemplate.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sw.AlResumeData.entity.AlResumeData;
import sw.AlResumeData.mapper.AlResumeDataMapper;
import sw.AlResumeData.service.AlResumeDataService;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.model3d.ConfigBomPerceivedVariable.entity.ConfigBomPerceivedVariable;
import sw.model3d.ConfigBomPerceivedVariable.service.ConfigBomPerceivedVariableService;
import sw.model3d.configBomTreeTemplate.entity.ConfigBomTreeTemplate;
import sw.model3d.configBomTreeTemplate.entity.vo.ConfigBomTreeTemplateVo;
import sw.model3d.configBomTreeTemplate.entity.vo.UploadDataSetVO;
import sw.model3d.configBomTreeTemplate.service.ConfigBomTreeTemplateService;
import sw.model3d.configGbomTree.entity.ConfigGbomTreeDTO;
import sw.model3d.configGbomTree.service.ConfigGbomTreeService;
import sw.model3d.configModel.entity.ConfigBomTree;
import sw.model3d.configModel.service.ConfigBomTreeService;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 项目风场不同机型风机结构表(ConfigBomTreeTemplate)表控制层
 *
 * @author makejava
 * @since 2024-06-24 17:16:50
 */
@RestController
@RequestMapping("/configBomTreeTemplate")
@CrossOrigin
public class ConfigBomTreeTemplateController {

	/**
	 * 服务对象
	 */
	@Autowired
	private ConfigBomTreeTemplateService configBomTreeTemplateService;

	@Autowired
	private ConfigBomPerceivedVariableService configBomPerceivedVariableService;

	@Autowired
	private ConfigBomTreeService configBomTreeService;

	@Autowired
	private ConfigGbomTreeService configGbomTreeService;

	@Autowired
	private AlResumeDataService alResumeDataService;

	/**
	 * 项目风机结构导入：项目名称、风机结构信息数组-数组-json
	 */
	@Operation(summary = "项目风机结构导入", description = "项目风机结构导入")
	@PostMapping("/createProTempalte")
	public R createProTempalte(@RequestBody List<List<JSONObject>> list) {
		return R.ok(configBomTreeTemplateService.createProTemplate(list));
	}

	/**
	 * 创建风机实例：项目号proId、风机名称nodeName
	 */
	@Operation(summary = "创建风机实例", description = "创建风机实例")
	@PostMapping("/createProInstance")
	public R createProInstance(@RequestParam Long proId, @RequestParam String nodeName) {
		return R.ok(configBomTreeTemplateService.createProInstance(proId, nodeName), "创建风机实例成功");
	}

	/**
	 * 根据机型生成实例对象
	 * @param sceId 场景id
	 * @param modelId 具体型号id
	 * @param nodeName 实例对象名称
	 */
	@Operation(summary = "创建实例对象", description = "创建实例对象")
	@PostMapping("/createObjInstance")
	public R createObjInstance(@RequestParam Long sceId, @RequestParam Long modelId, @RequestParam String nodeName) {
		return R.ok(configBomTreeTemplateService.createObjInstance(sceId, modelId, nodeName), "创建实例对象成功");
	}

	@Operation(summary = "根据项目名、机型和节点编码查询子节点", description = "根据项目名、机型和节点编码查询子节点")
	@GetMapping("/getSonNodes/{proId}")
	public R getSonNodes(@PathVariable Long proId, @RequestParam String nodeCode) {
		return R.ok(configBomTreeTemplateService.getSonNodes(proId, nodeCode), "查询成功");
	}

	@Operation(summary = "根据场景和节点编码查询子节点", description = "根据场景和节点编码查询子节点")
	@GetMapping("/reqSonNodesByScene/{proId}")
	public R reqSonNodesByScene(@PathVariable Long proId, @RequestParam String nodeCode) {
		Long tureId = alResumeDataService.getById(proId).getProductModelId();
		return R.ok(configBomTreeTemplateService.getSonNodes(tureId, nodeCode), "查询成功");
	}

	@Operation(summary = "根据机型和节点编码查询子节点", description = "根据机型和节点编码查询子节点")
	@GetMapping("/reqSonNodesByModelId")
	public R reqSonNodesByModelId(@RequestParam String modelId, @RequestParam String nodeCode) {
		return R.ok(configBomTreeTemplateService.reqSonNodesByModelId(modelId, nodeCode), "查询成功");
	}

	@Operation(summary = "根据项目名、机型和节点级别获取所有树节点", description = "根据项目名、机型和节点级别获取所有树节点")
	@GetMapping("/getTreeNodes/{proId}")
	public R getTreeNodes(@PathVariable Long proId, @RequestParam String nodeLevel) {
		return R.ok(configBomTreeTemplateService.getTreeNodes(proId, nodeLevel), "查询成功");
	}

	@Operation(summary = "根据场景机型获得模型Bom树", description = "根根据场景机型获得模型Bom树")
	@GetMapping("/getTreeNodesByScene/{proId}")
	public R getTreeNodesByScene(@PathVariable Long proId, @RequestParam String nodeLevel) {
		Long tureId = alResumeDataService.getById(proId).getProductModelId();
		return R.ok(configBomTreeTemplateService.getTreeNodes(tureId, nodeLevel), "查询成功");
	}

	@Operation(summary = "根据机型和节点级别获取所有树节点", description = "根据机型和节点级别获取所有树节点")
	@GetMapping("/reqTreeNodesByModelId")
	public R reqTreeNodesByModelId(@RequestParam String modelId, @RequestParam String nodeLevel) {
		return R.ok(configBomTreeTemplateService.reqTreeNodesByModelId(modelId, nodeLevel), "查询成功");
	}

	@Operation(summary = "根据节点id和proid获取模板对象", description = "根据节点id获取模板对象")
	@GetMapping("/getConfigBomTreeTemp/{proId}/{nodeId}")
	public R getConfigBomTreeTemp(@PathVariable String proId, @PathVariable String nodeId) {
		return R.ok(configBomTreeTemplateService.getNode(proId, nodeId), "查询成功");
	}

	@Operation(summary = "上传数据集", description = "上传数据集")
	@PostMapping("/uploadSonNode/{nodeId}")
	public R uploadSonNode(@PathVariable String nodeId, @RequestParam String datasetType,
			@RequestParam String datasetUrl, @RequestParam String provider, @RequestParam String datasetName) {
		return R.ok(configBomTreeTemplateService.uploadSonNode(nodeId, datasetType, datasetUrl, provider, datasetName),
				"上传数据集成功");
	}

	@Operation(summary = "替换数据集", description = "替换数据集")
	@PostMapping("/replaceSonNode/{nodeId}")
	public R replaceSonNode(@PathVariable String nodeId, @RequestParam String datasetType,
			@RequestParam String datasetUrl, @RequestParam String provider, @RequestParam String replaceVersionType,
			@RequestParam String datasetName) {
		return R.ok(configBomTreeTemplateService.replaceSonNode(nodeId, datasetType, datasetUrl, provider,
				replaceVersionType, datasetName), "上传数据集成功");
	}

	@Operation(summary = "回溯数据集", description = "回溯数据集")
	@PostMapping("/revertDataset/{nodeId}")
	public R revertDataset(@PathVariable String nodeId, @RequestParam String datasetType) {
		return R.ok(configBomTreeTemplateService.revertDataset(nodeId, datasetType));
	}

	@Operation(summary = "根据项目号、父节点id (parentNodeId)、子节点的节点信息（ConfigBomTreeTemplate）添加子节点",
			description = "根据项目名、机型和节点编码查询子节点")
	@PostMapping("/addSonNode/{proId}/{parentNodeId}")
	public R addSonNode(@PathVariable Long proId, @PathVariable String parentNodeId,
			@RequestBody ConfigBomTreeTemplate configBomTreeTemplate) {

		return R.ok(configBomTreeTemplateService.addSonNode(proId, parentNodeId, configBomTreeTemplate), "新增成功");
	}

	@Operation(summary = "根据节点id和proId编辑节点信息", description = "根据节点id编辑节点信息")
	@PutMapping("/editNodeInfo/{proId}")
	public R editNodeInfo(@PathVariable Long proId, @RequestBody ConfigBomTreeTemplate nodeForm) {
		return R.ok(configBomTreeTemplateService.editNodeInfo(proId, nodeForm), "编辑成功");
	}

	@Operation(summary = "根据节点nodeCode和proid删除节点及其子节点", description = "根据节点nodecode删除节点及其子节点")
	@GetMapping("/delNode")
	public R delNode(@RequestParam Long proId, @RequestParam String nodeCode) {
		configBomTreeTemplateService.delNode(proId, nodeCode);
		return R.ok("删除成功");
	}

	@Operation(summary = "根据proid和节点编码nodeCode复制一棵子树", description = "根据proid和节点编码nodeCode复制一棵子树")
	@PutMapping("/copySonNode/{proId}")
	public OpenResponse copySonNode(@PathVariable Long proId, @RequestParam String nodeCode) {
		return configBomTreeTemplateService.copySonNode(proId, nodeCode);
	}

	@Operation(summary = "根据proId、原节点编码nodeCode和目标节点编码nodeCode移动节点",
			description = "根据proId、原节点编码nodeCode和目标节点编码nodeCode移动节点")
	@PostMapping("/moveNode/{proId}")
	public OpenResponse moveNode(@PathVariable Long proId, @RequestParam String sourceNodeCode,
			@RequestParam String targetNodeCode) {
		// 修改移动节点的nodecode、level、type、nodeno、swort
		// 更新子节点的nodecode
		// 修改原根节点的type
		// 修改目标根节点的type
		// 修改其他兄弟节点的nodecode和nodeno、swort

		OpenResponse<ConfigBomTreeTemplateVo> response = new OpenResponse<>(OpenResponseCode.ERROR);
		response.setMessage("移动失败");
		ConfigBomTreeTemplateVo configBomTreeTemplateVo = configBomTreeTemplateService.moveNode(proId, sourceNodeCode,
				targetNodeCode);
		if (configBomTreeTemplateVo != null) {
			response.setData(configBomTreeTemplateVo);
			response.setCode(200);
			response.setMessage("移动成功");
		}
		return response;

	}

	@Operation(summary = "根据节点Id获取数据集地址")
	@GetMapping("/getDataUrl")
	public R getDataUrl(@RequestParam String objectId, @RequestParam String dataType) {
		return R.ok(configBomTreeTemplateService.getDataUrl(objectId, dataType));

	}

	@Operation(summary = "根据节点Id获取最新和历史数据集地址")
	@GetMapping("/getAllDataUrl")
	public R getAllDataUrl(@RequestParam String objectId, @RequestParam String dataType) {
		return R.ok(configBomTreeTemplateService.getAllDataUrl(objectId, dataType));
	}

	// @Operation(summary = "获取数据集提供者", description = "获取数据集提供者")
	// @GetMapping("/getDataProvider")
	// public R getDataProvider(@RequestParam String objectId) {
	// return R.ok(configBomTreeTemplateService.getDataProvider(objectId));
	// }

	@Operation(summary = "添加机型结构树", description = "添加机型结构树")
	@SysLog("添加机型结构树")
	@PostMapping("/addProductModelTree/{proId}")
	public R addProductModelTree(@PathVariable String proId, @RequestBody List<ConfigGbomTreeDTO> selectNodes) {
		Set<String> nodeIds = configGbomTreeService.getAllNodIdsBySelectNodes(selectNodes);
		return R.ok(configBomTreeTemplateService.addProductModelTree(proId, nodeIds));
	}

	@Operation(summary = "修改机型结构树", description = "修改机型结构树")
	@SysLog("修改机型结构树")
	@PostMapping("/updateProductModelTree/{proId}")
	public R updateProductModelTree(@PathVariable String proId, @RequestBody List<ConfigGbomTreeDTO> selectNodes) {
		Set<String> nodeIds = configGbomTreeService.getAllNodIdsBySelectNodes(selectNodes);
		return R.ok(configBomTreeTemplateService.updateProductModelTree(proId, nodeIds));
	}

	@Operation(summary = "复制机型结构树", description = "复制机型结构树")
	@SysLog("复制机型结构树")
	@GetMapping("/copyModelTreeAndVar")
	public R copyModelTreeAndVar(@RequestParam String copyName, @RequestParam String newId,
			@RequestParam String proId) {
		copyName = copyName.trim();
		return R.ok(configBomTreeTemplateService.copyModelTreeAndVar(copyName, newId, proId));
	}

	@Operation(summary = "添加机型感知变量", description = "添加机型感知变量")
	@SysLog("添加机型感知变量")
	@PostMapping("/addProductModelVariables/{proId}")
	public R addProductModelVariables(@PathVariable String proId,
			@RequestBody List<ConfigPerceivedVariable> modelTreeVariable) {
		return R.ok(configBomTreeTemplateService.addProductModelVariables(proId, modelTreeVariable));
	}

	@Operation(summary = "批量上传机型数据集", description = "批量上传机型数据集")
	@SysLog("批量上传机型数据集")
	@PostMapping("/addProductModelDataSets/{proId}")
	public R addProductModelDataSets(@PathVariable String proId, @RequestBody List<UploadDataSetVO> uploadDataSets) {
		return R.ok(configBomTreeTemplateService.addProductModelDataSets(proId, uploadDataSets));
	}

	@Operation(summary = "获取某个节点的信息", description = "获取某个节点的信息")
	@GetMapping("/getObj/{nodeId}")
	public R getObj(@PathVariable String nodeId) {
		return R.ok(configBomTreeTemplateService.getById(nodeId));
	}

	@Operation(summary = "修改数据集信息", description = "修改数据集信息")
	@SysLog("修改数据集信息")
	@PostMapping("/updateDatasetInfo/{nodeId}")
	public R updateDatasetInfo(@PathVariable String nodeId, @RequestParam String type,
			@RequestBody ConfigBomTreeTemplate configBomTreeTemplate) {
		return R.ok(configBomTreeTemplateService.updateDatasetInfo(nodeId, type, configBomTreeTemplate));
	}

	@Operation(summary = "根据风场机型id查询机型结构", description = "根据风场机型id查询机型结构")
	@GetMapping("/getModelStructure")
	public R getModelStructure(@RequestParam Long projectId) {
		// 1. 根据 projectId 查找 al_resume_data 表中的记录
		AlResumeData resumeData = alResumeDataService.getById(projectId);
		if (resumeData == null || resumeData.getProductModelId() == null) {
			return R.failed("未找到对应的机型结构信息");
		}

		Long productModelId = resumeData.getProductModelId();

		// 2. 查询指定字段
		List<ConfigBomTreeTemplate> structureList = configBomTreeTemplateService
			.list(new LambdaQueryWrapper<ConfigBomTreeTemplate>().eq(ConfigBomTreeTemplate::getProId, productModelId));

		// 3. 筛选 nodeType 为 Root、Mid、Leaf，并提取字段
		List<Map<String, String>> result = structureList.stream().filter(item -> {
			String type = item.getNodeType();
			return "Root".equals(type) || "Mid".equals(type) || "Leaf".equals(type);
		}).map(item -> {
			Map<String, String> map = new HashMap<>();
			map.put("nodeCode", item.getNodeCode());
			map.put("nodeName", item.getNodeName());
			map.put("nodeType", item.getNodeType());
			return map;
		}).collect(Collectors.toList());

		return R.ok(result);
	}

	@Operation(summary = "删除场景相关节点", description = "删除多个场景ID对应的所有相关数据")
	@PostMapping("/deleteSceneNode")
	public R deleteSceneNode(@RequestBody Map<String, List<String>> params) {
		List<String> sceneIds = params.get("sceneIds");

		if (sceneIds == null || sceneIds.isEmpty()) {
			return R.failed("sceneIds不能为空");
		}

		for (String sceneId : sceneIds) {
			configBomPerceivedVariableService.remove(new LambdaQueryWrapper<ConfigBomPerceivedVariable>()
				.apply("SUBSTRING_INDEX(turbine_code, '-', 1) = {0}", sceneId));
			configBomTreeService.remove(new LambdaQueryWrapper<ConfigBomTree>().eq(ConfigBomTree::getProId, sceneId));
			alResumeDataService.remove(new LambdaQueryWrapper<AlResumeData>().eq(AlResumeData::getId, sceneId));
		}

		return R.ok("删除成功");
	}

}
