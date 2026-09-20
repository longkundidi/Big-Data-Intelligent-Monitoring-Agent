package com.algorithm.web.service.impl.al;

import cn.hutool.json.JSONObject;
import com.algorithm.web.exceptions.BizException;
import com.algorithm.web.mapper.al.AlResumeDataMapper;
import com.algorithm.web.model.dto.al.AlResumeDataDto;
import com.algorithm.web.model.dto.al.SceneModelTree;
import com.algorithm.web.model.entity.al.AlResumeData;
import com.algorithm.web.service.al.AlResumeDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlResumeDataServiceImpl extends ServiceImpl<AlResumeDataMapper, AlResumeData>
		implements AlResumeDataService {

	@Override
	public ArrayList<JSONObject> saveBatch(List<AlResumeDataDto> alResumeDataDtoList) {
		ArrayList<JSONObject> proIdList = new ArrayList<>();

		for (AlResumeDataDto alResumeDataDto : alResumeDataDtoList) {
			// 查询是否存在
			LambdaQueryWrapper<AlResumeData> queryWrapper = new LambdaQueryWrapper<>();
			queryWrapper.eq(AlResumeData::getProject, alResumeDataDto.getProject())
				.eq(AlResumeData::getProductModel, alResumeDataDto.getProductModel());
			AlResumeData exitProject = this.getOne(queryWrapper);
			if (exitProject == null) {
				AlResumeData alResumeData = new AlResumeData();
				alResumeData.setProject(alResumeDataDto.getProject());
				alResumeData.setProductModel(alResumeDataDto.getProductModel());
				alResumeData.setBomModel("SBOM");
				this.save(alResumeData);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("pro_id", alResumeData.getId());
				jsonObject.put("productModel", alResumeData.getProductModel());
				proIdList.add(jsonObject);

			}
			else {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("pro_id", exitProject.getId());
				jsonObject.put("productModel", exitProject.getProductModel());
				proIdList.add(jsonObject);
			}
		}
		return proIdList;
	}

	@Override
	public Map<String, List<AlResumeData>> fetchProModelMap() {
		// 查询所有需要字段
		LambdaQueryWrapper<AlResumeData> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.select(AlResumeData::getId, AlResumeData::getProject, AlResumeData::getProductModel,
				AlResumeData::getBomModel);
		List<AlResumeData> alResumeDataList = this.getBaseMapper().selectList(queryWrapper);

		Map<String, List<AlResumeData>> map = new HashMap<>();
		// 获取所有场景名
		Set<String> projects = alResumeDataList.stream()
			.filter(e -> "GBOM".equals(e.getBomModel()))
			.map(AlResumeData::getProject)
			.collect(Collectors.toSet());

		return alResumeDataList.stream()
			.filter(e -> "MBOM".equals(e.getBomModel()) && projects.contains(e.getProject()))
			.collect(Collectors.groupingBy(AlResumeData::getProject));
	}

	@Override
	public boolean checkSceneNameExists(String name) {
		LambdaQueryWrapper<AlResumeData> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(AlResumeData::getProject, name);
		return this.count(wrapper) > 0;
	}

	@Override
	public boolean checkProductModelExists(String sceneName, String modelName) {
		LambdaQueryWrapper<AlResumeData> wrapper = new LambdaQueryWrapper<>();
		wrapper.eq(AlResumeData::getProductModel, modelName).eq(AlResumeData::getProject, sceneName);
		return this.count(wrapper) > 0;
	}

	@Override
	public List<SceneModelTree> buildSceneModelTree(List<AlResumeData> allData) {
		Map<String, SceneModelTree> gbomMap = new LinkedHashMap<>();
		List<SceneModelTree> tree = new ArrayList<>();

		// 1. 构建 GBOM 节点作为根
		for (AlResumeData data : allData) {
			if ("GBOM".equalsIgnoreCase(data.getBomModel())) {
				SceneModelTree gbomNode = new SceneModelTree();
				gbomNode.setId(data.getId());
				gbomNode.setLabel(data.getProject());
				gbomNode.setBomModel("GBOM");
				gbomMap.put(data.getProject(), gbomNode);
				tree.add(gbomNode);
			}
		}

		// 2. 构建 MBOM 子节点（仅附加到存在的 GBOM 节点下）
		for (AlResumeData data : allData) {
			if ("MBOM".equalsIgnoreCase(data.getBomModel())) {
				SceneModelTree parent = gbomMap.get(data.getProject());
				if (parent != null) {
					SceneModelTree mbomNode = new SceneModelTree();
					mbomNode.setId(data.getId());
					mbomNode.setLabel(data.getProductModel());
					mbomNode.setBomModel("MBOM");
					parent.getChildren().add(mbomNode);
				}
			}
		}
		return tree;
	}

	@Override
	public boolean deleteSceneNode(String sceneId) {
		// 1. 检查场景是否存在
		AlResumeData scene = this.getById(sceneId);
		if (scene == null) {
			throw new BizException("场景不存在");
		}

		// 2. 检查是否为GBOM类型（场景节点）
		if (!"GBOM".equals(scene.getBomModel())) {
			throw new BizException("只能删除场景节点");
		}

		// 3. 查询nodeId列表
		List<String> nodeIds = baseMapper.getNodeIdsBySceneId(sceneId);

		// 4. 删除 config_perceived_variable 中 node_id 对应数据
		if (nodeIds != null && !nodeIds.isEmpty()) {
			baseMapper.deletePerceivedByNodeIds(nodeIds);
		}

		// 5. 删除 config_gbom_tree 表中sceneId对应数据
		baseMapper.deleteConfigGbomTreeBySceneId(sceneId);

		// 6. 删除 config_bom_perceived_variable_template 表中 pro_id 对应数据
		baseMapper.deletePerceivedTemplateBySceneId(sceneId);

		// 7. 删除 al_resume_data 表中 id 对应数据
		return this.removeById(sceneId);
	}

	@Override
	public boolean deleteModelNode(String modelId) {
		// 1. 检查机型是否存在
		AlResumeData model = this.getById(modelId);
		if (model == null) {
			throw new BizException("机型不存在");
		}

		// 2. 检查是否为MBOM类型（机型节点）
		if (!"MBOM".equals(model.getBomModel())) {
			throw new BizException("只能删除机型节点");
		}

		// 查询该机型是否被使用
		if (baseMapper.getModelUsedCount(Long.valueOf(modelId)) > 0) {
			throw new BizException("该机型已被使用，不可删除");
		}

		// 处理used_count
		// 3.1. 根据modelId查询config_bom_tree_template表中的node_code
		List<String> nodeCodes = baseMapper.getNodeCodesByModelId(modelId);

		// 3.2. 根据modelId查询project，然后查找project相同且bom_model=GBOM的数据id
		String project = baseMapper.getProjectByModelId(modelId);
		List<Long> sceneIds = baseMapper.getSceneIdsByProject(project);
		// 3.3. 在config_gbom_tree表中查询node_code相等和scene_id对应数据的used_count，如果大于0则减1
		if (sceneIds != null && !sceneIds.isEmpty() && nodeCodes != null && !nodeCodes.isEmpty()) {
			String sceneId = sceneIds.get(0).toString(); // 转换为 String 以适配下游 SQL
			for (String nodeCode : nodeCodes) {
				Integer usedCount = baseMapper.getUsedCount(nodeCode, sceneId);
				if (usedCount != null && usedCount > 0) {
					baseMapper.updateUsedCount(nodeCode, sceneId, usedCount - 1);
				}
			}
		}

		// 4. 删除 config_bom_tree_template 表中 pro_id 对应数据
		baseMapper.deleteBomTreeTemplateByModelId(modelId);

		// 5. 删除 config_bom_perceived_variable_template 表中 pro_id 对应数据
		baseMapper.deletePerceivedTemplateByModelId(modelId);

		// 6. 删除al_resume_data表中id等于modelId的数据
		return this.removeById(modelId);
	}

}
