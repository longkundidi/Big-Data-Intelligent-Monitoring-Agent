/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */
package sw.model3d.configModel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import sw.AlResumeData.entity.AlResumeData;
import sw.AlResumeData.mapper.AlResumeDataMapper;
import sw.ConfigUserFarm.mapper.ConfigUserFarmMapper;
import sw.common.exception.MyException;
import sw.common.exception.MyExceptionEnum;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.common.strUtils;
import sw.model3d.ConfigBomPerceivedVariable.entity.ConfigBomPerceivedVariable;
import sw.model3d.ConfigBomPerceivedVariable.mapper.ConfigBomPerceivedVariableMapper;
import sw.model3d.configAlarmInfo.mapper.ConfigAlarmInfoMapper;
import sw.model3d.configBomPerceivedVariableTemplate.entity.ConfigBomPerceivedVariableTemplate;
import sw.model3d.configBomPerceivedVariableTemplate.mapper.ConfigBomPerceivedVariableTemplateMapper;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.model3d.configModel.entity.ConfigBomTree;
import sw.model3d.configModel.entity.ConfigPerceivedTask;
import sw.model3d.configModel.entity.ConfigPerceivedTaskVariable;
import sw.model3d.configModel.entity.vo.ConfigBomTreeVo;
import sw.model3d.configModel.entity.vo.ConfigBomTreeWithStatus;
import sw.model3d.configModel.entity.vo.ConfigComponentMatchVo;
import sw.model3d.configModel.mapper.ConfigBomTreeMapper;
import sw.model3d.configModel.mapper.ConfigPerceivedTaskMapper;
import sw.model3d.configModel.mapper.ConfigPerceivedTaskVariableMapper;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.model3d.configPerceivedVariable.mapper.ConfigPerceivedVariableMapper;
import sw.model3d.configModel.service.ConfigBomTreeService;
import sw.utils.entity.TreeEntity;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-05-09 11:36:07
 */
@Service
public class ConfigBomTreeServiceImpl extends ServiceImpl<ConfigBomTreeMapper, ConfigBomTree>
		implements ConfigBomTreeService {

	@Autowired
	private ConfigBomTreeMapper configBomTreeMapper;

	@Autowired
	private ConfigGbomTreeMapper configGbomTreeMapper;

	@Autowired
	private ConfigBomPerceivedVariableTemplateMapper configBomPerceivedVariableTemplateMapper;

	@Autowired
	private ConfigBomPerceivedVariableMapper configBomPerceivedVariableMapper;

	@Autowired
	private ConfigPerceivedVariableMapper configPerceivedVariableMapper;

	@Autowired
	private ConfigPerceivedTaskMapper configPerceivedTaskMapper;

	@Autowired
	private ConfigPerceivedTaskVariableMapper configPerceivedTaskVariableMapper;

	@Autowired
	private ConfigAlarmInfoMapper configAlarmInfoMapper;

	@Autowired
	private AlResumeDataMapper alResumeDataMapper;

	@Autowired
	private ConfigUserFarmMapper configUserFarmMapper;

	@Override
	public List<ConfigBomTree> findAllTurbine(String proName, String productModel) {
		Long proId = configBomPerceivedVariableTemplateMapper.getProId(proName, productModel);
		return this.list(new QueryWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getProId, proId)
			.and(wrapper -> wrapper.eq(ConfigBomTree::getIsBomPassed, 0).or().isNull(ConfigBomTree::getIsBomPassed))
			.eq(ConfigBomTree::getNodeLevel, 1));

	}

	@Override
	public ConfigComponentMatchVo componentMatch(List<String> componentCodes, ConfigBomTree turbine, String sceneId) {

		ConfigComponentMatchVo configComponentMatchVo = new ConfigComponentMatchVo();

		// 每次匹配前先清空该实例全部节点的结构匹配状态，避免历史结果干扰当前场景
		this.update(new UpdateWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getTurbineCode, turbine.getTurbineCode())
			.set(ConfigBomTree::getIsBomPassed, 0));

		// 1. 拿到GBom树节点的编码列表
		List<String> GBomTreeNodeCode = Optional.ofNullable(configGbomTreeMapper.getNodeCode(sceneId))
			.orElse(Collections.emptyList());
		Set<String> gBomNodeSet = GBomTreeNodeCode.stream().filter(Objects::nonNull).collect(Collectors.toSet());
		Set<String> componentNodeSet = Optional.ofNullable(componentCodes)
			.orElse(Collections.emptyList())
			.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		Set<String> requiredNodeCodeSet = getRequiredGbomNodeCodes(sceneId);

		// 2. 有感知变量的节点全部存在即可通过；
		// 若当前场景尚未配置任何感知变量节点，则保留原有的全量一致判定。
		boolean fullMatched = !gBomNodeSet.isEmpty() && gBomNodeSet.equals(componentNodeSet);
		boolean requiredNodesMatched = !requiredNodeCodeSet.isEmpty()
				&& componentNodeSet.containsAll(requiredNodeCodeSet);
		boolean bomMatched = requiredNodeCodeSet.isEmpty() ? fullMatched : requiredNodesMatched;
		List<String> matchedNodeCodes = GBomTreeNodeCode.stream()
			.filter(componentNodeSet::contains)
			.collect(Collectors.toList());

		// 3. 匹配通过后再回写通过节点
		if (!bomMatched) {
			configComponentMatchVo.setIsBomPassed(0);
		}
		else {
			configComponentMatchVo.setIsBomPassed(1);
			List<TreeEntity> bomList = new ArrayList<>();
			for (String code : matchedNodeCodes) {
				TreeEntity node = configBomTreeMapper.getNodeVobyCode(code, turbine.getTurbineCode(),
						turbine.getProId());
				if (node != null) {
					bomList.add(node);
				}
			}
			configComponentMatchVo.setBomList(bomList);
			for (String code : matchedNodeCodes) {
				ConfigBomTree configBomTree = this.getOne(new QueryWrapper<ConfigBomTree>().lambda()
					.eq(ConfigBomTree::getNodeCode, code)
					.eq(ConfigBomTree::getTurbineCode, turbine.getTurbineCode()));
				if (configBomTree != null) {
					configBomTree.setIsBomPassed(1);
					this.updateById(configBomTree);
				}
			}
		}

		return configComponentMatchVo;
	}

	private Set<String> getRequiredGbomNodeCodes(String sceneId) {
		List<String> gbomNodeIds = configGbomTreeMapper.getNodeIdsBySceneId(sceneId);
		if (gbomNodeIds == null || gbomNodeIds.isEmpty()) {
			return Collections.emptySet();
		}

		List<ConfigPerceivedVariable> perceivedVariables = configPerceivedVariableMapper
			.selectList(new LambdaQueryWrapper<ConfigPerceivedVariable>().select(ConfigPerceivedVariable::getNodeId)
				.in(ConfigPerceivedVariable::getNodeId, gbomNodeIds));
		Set<String> requiredNodeIds = perceivedVariables.stream()
			.map(ConfigPerceivedVariable::getNodeId)
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		if (requiredNodeIds.isEmpty()) {
			return Collections.emptySet();
		}

		return configGbomTreeMapper.selectBatchIds(requiredNodeIds)
			.stream()
			.filter(Objects::nonNull)
			.map(ConfigGbomTree::getNodeCode)
			.filter(Objects::nonNull)
			.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	@Override
	public List<ConfigBomTreeVo> getComponentNodes(Long proId, String nodeLevel) {
		// 获取当前项目当前机型的所有通过匹配的实例风机
		List<String> turbineCodes = configBomTreeMapper.getAllMatchedTurbineCode(proId);
		if (turbineCodes.size() == 0) {
			return Collections.emptyList();
		}
		return configBomTreeMapper.getComponentNodes(proId, nodeLevel, turbineCodes);
	}

	@Override
	public List<String> findComponentCodes(String turbineCode) {
		return configBomTreeMapper.findComponentCodes(turbineCode);
	}

	@Override
	public List<ConfigBomTreeVo> getSonNodes(String nodeCode, String turbineCode) {

		return baseMapper.getSonNodes(nodeCode, turbineCode);
	}

	@Override
	public List<ConfigBomTreeVo> getTreeNodes(Long proId, String nodeLevel) {
		return baseMapper.getTreeNodes(proId, nodeLevel);
	}

	@Override
	public boolean uploadUnitData(List<List<JSONObject>> list) {
		Map<String, List<String>> map = new HashMap<>();
		String proId = null;
		for (List<JSONObject> listItem : list) {
			JSONObject root = listItem.get(0);

			String rootNodeName = null;
			for (JSONObject obj : listItem) {
				if ("Root".equals(obj.getString("node_type"))) {
					rootNodeName = obj.getString("node_name");
					break;
				}
			}
			List<String> nodeCodes = new ArrayList<>();
			String turbineCode = null;
			for (JSONObject object : listItem) {
				ConfigBomTree configBomTree = new ConfigBomTree();
				configBomTree.setProId(object.getString("pro_id"));
				configBomTree.setNodeName(object.getString("node_name"));
				configBomTree.setNodeCode(object.getString("node_code"));
				configBomTree.setNodeType(object.getString("node_type"));
				nodeCodes.add(configBomTree.getNodeCode());

				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				configBomTree.setUpdateTime(formatter.format(new Date()));

				// 计算node_level
				String nodeCode = (String) object.get("node_code");
				char targetChar = '-';
				int count = 0;
				int index = 0;

				while ((index = nodeCode.indexOf(targetChar, index)) != -1) {
					count++; // 每找到一个 '-' 就加一
					index++; // 移动到下一个字符，避免重复计数
				}
				configBomTree.setNodeLevel(count + 1); // 层级 = '-'数量 + 1

				AlResumeData data = alResumeDataMapper.selectOne(
						new LambdaQueryWrapper<AlResumeData>().eq(AlResumeData::getId, configBomTree.getProId()));
				String productModelName = data != null ? data.getProductModel() : null;
				if (data != null) {
					proId = String.valueOf(data.getProductModelId());
				}

				configBomTree.setTurbineCode(object.getString("pro_id") + "-" + productModelName + "-" + rootNodeName);
				turbineCode = configBomTree.getTurbineCode();

				// "TM1-1-10"
				if (object.getString("node_type").equals("Root")) {
					configBomTree.setNodeNo(1);
				}
				else {
					int lastIndex = object.getString("node_code").lastIndexOf("-");// 返回最后一个-的index
					int length = object.getString("node_code").length();// 获取长度
					String node_code = object.getString("node_code").substring(lastIndex + 1, length);// 截取从最后一个index+1到length之间的字符

					configBomTree.setNodeNo(Integer.parseInt(node_code));
					configBomTree.setSwsort(Float.parseFloat(node_code));
				}
				configBomTree.setProductionDate(object.getString("production_date") == null ? null : LocalDate
					.parse(object.getString("production_date"), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

				configBomTree.setPurchaseDate(object.getString("purchase_date") == null ? null : LocalDate
					.parse(object.getString("purchase_date"), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

				configBomTree.setInstallationDate(object.getString("installation_date") == null ? null : LocalDate
					.parse(object.getString("installation_date"), DateTimeFormatter.ofPattern("yyyy-MM-dd")));

				configBomTree.setInstallationLeader(object.getString("installation_leader"));
				configBomTree.setResponsiblePerson(object.getString("responsible_person"));
				configBomTree.setComponentModel(object.getString("component_model"));
				configBomTree.setManufacturer(object.getString("manufacturer"));
				configBomTree.setMaintenanceRecord(object.getString("maintenance_record"));
				configBomTree.setMaintenancePerson(object.getString("maintenance_person"));
				configBomTree.setFaultRecord(object.getString("fault_record"));
				configBomTree.setFaultReporter(object.getString("fault_reporter"));
				configBomTree.setReplacementRecord(object.getString("replacement_record"));
				configBomTree.setReplacementPerson(object.getString("replacement_person"));

				this.save(configBomTree);
			}
			map.put(turbineCode, nodeCodes);
		}
		addBomVariable(proId, map);
		return true;
	}

	@Override
	public boolean updateUnitData(ConfigBomTreeVo configBomTreeVo) {
		// 1. 创建 UpdateWrapper，仅更新指定字段
		UpdateWrapper<ConfigBomTree> wrapper = new UpdateWrapper<>();
		wrapper.eq("node_id", configBomTreeVo.getId())
			.set("production_date", configBomTreeVo.getProductionDate())
			.set("purchase_date", configBomTreeVo.getPurchaseDate())
			.set("installation_date", configBomTreeVo.getInstallationDate())
			.set("installation_leader", configBomTreeVo.getInstallationLeader())
			.set("responsible_person", configBomTreeVo.getResponsiblePerson())
			.set("component_model", configBomTreeVo.getComponentModel())
			.set("manufacturer", configBomTreeVo.getManufacturer())
			.set("maintenance_record", configBomTreeVo.getMaintenanceRecord())
			.set("maintenance_person", configBomTreeVo.getMaintenancePerson())
			.set("fault_record", configBomTreeVo.getFaultRecord())
			.set("fault_reporter", configBomTreeVo.getFaultReporter())
			.set("replacement_record", configBomTreeVo.getReplacementRecord())
			.set("replacement_person", configBomTreeVo.getReplacementPerson())
			.set("brake_type", configBomTreeVo.getBrakeType())
			.set("rated_load", configBomTreeVo.getRatedLoad())
			.set("rated_speed", configBomTreeVo.getRatedSpeed())
			.set("rated_power", configBomTreeVo.getRatedPower());

		// 2. 执行更新
		return this.update(wrapper);
	}

	private void addBomVariable(String proId, Map<String, List<String>> map) {
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			List<String> nodeIds = configBomTreeMapper.getNodeIdsByProIdAndNodeCodes(proId, entry.getValue());
			List<ConfigBomPerceivedVariableTemplate> variableTemplates = configBomPerceivedVariableTemplateMapper
				.selectList(new LambdaQueryWrapper<ConfigBomPerceivedVariableTemplate>()
					.in(ConfigBomPerceivedVariableTemplate::getNodeId, nodeIds));
			for (ConfigBomPerceivedVariableTemplate variableTemplate : variableTemplates) {
				ConfigBomPerceivedVariable variable = new ConfigBomPerceivedVariable();
				BeanUtil.copyProperties(variableTemplate, variable);
				variable.setId(null);
				variable.setNodeId(null);
				variable.setTurbineCode(entry.getKey());
				configBomPerceivedVariableMapper.insert(variable);
			}
		}
	}

	@Override
	public ConfigBomTree getNode(String nodeId) {
		return this.getOne(new QueryWrapper<ConfigBomTree>().eq("node_id", nodeId));

	}

	@Override
	public ConfigBomTreeVo editNodeInfo(ConfigBomTree nodeForm) {

		LambdaUpdateWrapper<ConfigBomTree> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(ConfigBomTree::getNodeId, nodeForm.getNodeId())
			.set(ConfigBomTree::getNodeName, nodeForm.getNodeName())
			.set(ConfigBomTree::getSwsort, nodeForm.getSwsort())
			.set(ConfigBomTree::getMemo, nodeForm.getMemo());
		update(nodeForm, updateWrapper);
		ConfigBomTree updated = this.getNode(nodeForm.getNodeId());
		ConfigBomTreeVo res = new ConfigBomTreeVo();
		BeanUtils.copyProperties(updated, res);
		res.setId(updated.getNodeId());
		res.setName(updated.getNodeName());
		if (updated.getNodeType().equals("Leaf"))
			res.setLeaf(true);
		return res;
	}

	// 更新被删除节点的父节点类型
	private void updatePNodeTypeWhileDel(Long proId, String turbineCode, String nodeCode) {
		String pNode = nodeCode.substring(0, nodeCode.lastIndexOf("-"));
		ConfigBomTree pGbomTree = baseMapper.selectOne(new QueryWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getTurbineCode, turbineCode)
			.eq(ConfigBomTree::getNodeCode, pNode));
		List<ConfigBomTreeVo> sonNodes = baseMapper.getSonNodes(pNode, pGbomTree.getTurbineCode());
		if (sonNodes.isEmpty()) { // 删除子节点后，没有其他子节点,则更新父节点type
			if (pGbomTree.getNodeLevel() == 1) { // 如果为根节点
				pGbomTree.setNodeType("Root-Leaf");
				this.updateById(pGbomTree);
			}
			else { // 如果为级别的节点
				pGbomTree.setNodeType("Leaf");
				this.updateById(pGbomTree);
			}
		}
	}

	@Override
	public void delNode(Long proId, String turbineCode, String nodeCode) {

		// 如果是删除root节点，则需要先删除该风机的感知变量
		LambdaQueryWrapper<ConfigBomTree> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigBomTree::getTurbineCode, turbineCode).eq(ConfigBomTree::getNodeCode, nodeCode);
		ConfigBomTree wind = this.getBaseMapper().selectOne(queryWrapper);
		if (wind.getNodeType().equals("Root")) {
			// 删除该风机所有叶子节点的状态感知任务及其变量
			List<ConfigBomTree> leafNodes = baseMapper.selectList(new QueryWrapper<ConfigBomTree>().lambda()
				.eq(ConfigBomTree::getTurbineCode, turbineCode)
				.eq(ConfigBomTree::getNodeType, "Leaf")); // 查询待删除叶子节点列表

			deleteTasksAndVariables(leafNodes);

			// 删除该风机的所有感知变量
			LambdaQueryWrapper<ConfigBomPerceivedVariable> perceivedVarWrapper = new LambdaQueryWrapper<>();
			perceivedVarWrapper.eq(ConfigBomPerceivedVariable::getTurbineCode, turbineCode);
			configBomPerceivedVariableMapper.delete(perceivedVarWrapper);
		}

		// 如果是叶子节点，还需要判断是否需要删除状态感知任务及其变量
		if (wind.getNodeType().equals("Leaf")) {
			List<ConfigBomTree> singleNodeList = new ArrayList<>();
			singleNodeList.add(wind);
			deleteTasksAndVariables(singleNodeList);
		}

		// 删除风机节点
		List<ConfigBomTree> delList = baseMapper.selectList(
				new QueryWrapper<ConfigBomTree>().lambda().eq(ConfigBomTree::getTurbineCode, turbineCode).and(i -> // 开始一个内嵌的AND块，确保下面的OR条件都是在proId已经匹配的基础上执行
				i.likeRight(ConfigBomTree::getNodeCode, nodeCode + "-") // 第一个或条件：以nodeCode+“-”%
					.or()
					.likeRight(ConfigBomTree::getNodeCode, nodeCode.replace("TM", "PM") + "-") // 第二个或条件：替换后以nodeCode+“-”%
					.or()
					.eq(ConfigBomTree::getNodeCode, nodeCode) // 第三个或条件：精确匹配nodeCode
				));

		List<String> dels = new ArrayList<>();
		for (ConfigBomTree node : delList) {
			dels.add(node.getNodeId());
			List<String> tasks = configPerceivedTaskMapper.getTaskId(node.getNodeId());
			if (!tasks.isEmpty()) {
				configPerceivedTaskMapper.deleteBatchIds(tasks);
			}
		}
		this.removeBatchByIds(dels); // 批量删除节点
		if (nodeCode.contains("-"))
			updatePNodeTypeWhileDel(proId, turbineCode, nodeCode);

	}

	private void deleteTasksAndVariables(List<ConfigBomTree> nodes) {
		for (ConfigBomTree node : nodes) {
			List<ConfigPerceivedTask> tasks = configPerceivedTaskMapper
				.selectList(new QueryWrapper<ConfigPerceivedTask>().lambda()
					.eq(ConfigPerceivedTask::getNodeId, node.getNodeId()));

			List<Long> taskIds = tasks.stream().map(ConfigPerceivedTask::getTaskId).collect(Collectors.toList());

			if (!taskIds.isEmpty()) {
				// 批量删除感知任务变量
				LambdaQueryWrapper<ConfigPerceivedTaskVariable> varWrapper = new LambdaQueryWrapper<>();
				varWrapper.in(ConfigPerceivedTaskVariable::getTaskId, taskIds);
				configPerceivedTaskVariableMapper.delete(varWrapper);

				// 批量删除感知任务
				configPerceivedTaskMapper.deleteBatchIds(taskIds);
			}
		}
	}

	private boolean hasParentNode(String turbineCode, String nodeCode) {
		if (!nodeCode.contains("-"))
			return false;
		String pCode = nodeCode.substring(0, nodeCode.lastIndexOf("-"));
		ConfigBomTree pGbomTree = baseMapper.selectOne(new QueryWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getTurbineCode, turbineCode)
			.eq(ConfigBomTree::getNodeCode, pCode));
		if (pGbomTree != null)// 存在父节点
			return true;
		else
			return false;
	}

	private String getParentNodeCode(String nodeCode) {
		return nodeCode.substring(0, nodeCode.lastIndexOf("-"));
	}

	public String addCopySuffix(String turbineCode, String input, String nodeCode) {
		String suffix = "-复制";
		int indexP = input.indexOf("-复制");
		String pre = null;
		if (indexP != -1) {
			pre = input.substring(0, indexP);
		}
		else {
			pre = new String(input);
		}
		List<ConfigBomTree> lists = this.list(new QueryWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getTurbineCode, turbineCode)
			.likeRight(ConfigBomTree::getNodeName, pre)
			.likeRight(ConfigBomTree::getNodeCode, nodeCode.substring(0, nodeCode.length() - 2)));
		int count = 0;
		Pattern pattern = Pattern.compile("-复制(\\d+)");
		for (ConfigBomTree list : lists) {
			String nodeName = list.getNodeName();
			Matcher matcher = pattern.matcher(nodeName);
			if (matcher.find()) {
				String numberString = matcher.group(1);
				count = Integer.max(count, Integer.parseInt(numberString));
			}
		}
		count++;
		suffix = suffix + count;

		return pre + suffix;
	}

	private Pair<ConfigBomTree, ConfigBomTree> buildParentNodeForCopyTree(String turbineCode, String nodeCode,
			String strUpdateTime) {
		ConfigBomTree gbomTreeNode = this
			.getOne(new QueryWrapper<ConfigBomTree>().eq("turbine_code", turbineCode).eq("node_code", nodeCode)); // 查询待复制的节点
		ConfigBomTree originalNode = new ConfigBomTree();
		BeanUtil.copyProperties(gbomTreeNode, originalNode); // 复制一个拷贝
		gbomTreeNode.setNodeId(null);
		ConfigBomTree parentNode = this.getOne(new QueryWrapper<ConfigBomTree>().eq("turbine_code", turbineCode)
			.eq("node_code", getParentNodeCode(nodeCode))); // 获取父节点

		Integer nodeNo = baseMapper.getSonMaxNo(parentNode.getNodeCode(), parentNode.getTurbineCode()) + 1; // 计算子树根节点的编号
		gbomTreeNode.setNodeNo(nodeNo);
		gbomTreeNode.setNodeCode(sonNodeCode(parentNode.getNodeCode(), nodeNo)); // 生成新节点的编码
		gbomTreeNode.setTurbineCode(parentNode.getTurbineCode());

		gbomTreeNode.setNodeName(addCopySuffix(turbineCode, gbomTreeNode.getNodeName(), nodeCode)); // 修改节点名称
		gbomTreeNode.setSwsort((float) nodeNo);
		gbomTreeNode.setUpdateTime(strUpdateTime); // 设置日期日期
		Pair<ConfigBomTree, ConfigBomTree> pair = Pair.of(originalNode, gbomTreeNode);
		return pair;
	}

	private String updateSonNodeCode(String sonNodeCode, String pNodeCode, String pNodeNewCode) {
		return sonNodeCode.replace(pNodeCode, pNodeNewCode);
	}

	private List<ConfigBomTree> buildSonNodesForCopyTree(String proId, String turbineCode, String nodeCode,
			String newParentTurbineCode, String newParentNodeCode, String strUpdateTime) {
		List<ConfigBomTree> sonNodes = baseMapper.getAllSubNodesByNodeCode(turbineCode, nodeCode);

		for (ConfigBomTree node : sonNodes) {
			node.setNodeId(null);
			String newNodeCode = updateSonNodeCode(node.getNodeCode(), nodeCode, newParentNodeCode);
			node.setNodeCode(newNodeCode); // 修改节点编码
			node.setTurbineCode(newParentTurbineCode);
			node.setUpdateTime(strUpdateTime); // 设置日期日期
			node.setProId(proId.toString());

		}
		return sonNodes;
	}

	@Override
	public OpenResponse<ConfigBomTreeVo> copySonNode(String turbineCode, String nodeCode) {

		OpenResponse<ConfigBomTreeVo> response = new OpenResponse<>(OpenResponseCode.ERROR);
		if (nodeCode != null) {
			ConfigBomTreeVo configBomTreeVo = copyNodeByNodeCode(turbineCode, nodeCode);
			if (configBomTreeVo == null) {
				response.setCode(4008);
				response.setMessage("复制失败 or 根节点不可复制");
				return response;
			}
			response.setData(configBomTreeVo);
			response.setCode(200);
			response.setMessage("复制成功");
		}
		return response;

	}

	private String setParentNodeTypeAfterDel(String pNodeType) {
		String nodeType = null;
		if (!(pNodeType == null || pNodeType.equals(""))) {
			if (pNodeType.equals("Root"))
				nodeType = "Root-Leaf";
			else if (pNodeType.equals("Mid"))
				nodeType = "Leaf";
		}
		return nodeType;
	}

	@Override
	public ConfigBomTreeVo moveNode(String turbineCode, String sourceNodeCode, String targetNodeCode) {
		OpenResponse response = new OpenResponse(OpenResponseCode.SUCCESS);
		ConfigBomTree gbomTreeNode = baseMapper.selectOne(new QueryWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getNodeCode, sourceNodeCode)
			.eq(ConfigBomTree::getTurbineCode, turbineCode)); // 找到待移动的节点对象
		ConfigBomTree PgbomTreeNode = baseMapper.selectOne(new QueryWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getTurbineCode, turbineCode)
			.eq(ConfigBomTree::getNodeCode, targetNodeCode)); // 找到目标节点对象
		List<ConfigBomTree> nodeList = baseMapper.selectList(new QueryWrapper<ConfigBomTree>().lambda()
			.likeRight(ConfigBomTree::getNodeCode, sourceNodeCode + "-")
			.eq(ConfigBomTree::getTurbineCode, turbineCode));
		Integer oldNodeLevel = gbomTreeNode.getNodeLevel();
		// 1、生成（待移动节点）新的节点编码nodeCode
		gbomTreeNode.setNodeNo(baseMapper.getSonMaxNo(targetNodeCode, turbineCode) + 1);
		gbomTreeNode.setNodeLevel(PgbomTreeNode.getNodeLevel() + 1);
		gbomTreeNode.setNodeType((nodeList.size() == 0) ? "Leaf" : "Mid");
		gbomTreeNode.setNodeCode(sonNodeCode(targetNodeCode, gbomTreeNode.getNodeNo()));

		try {
			// 2、更新目标节点的nodeType属性
			if ("Leaf".equals(PgbomTreeNode.getNodeType()) || "Root-Leaf".equals(PgbomTreeNode.getNodeType())) {
				PgbomTreeNode.setNodeType("Mid");
				this.update(PgbomTreeNode, new QueryWrapper<ConfigBomTree>().eq("node_id", PgbomTreeNode.getNodeId())
					.eq("turbine_code", turbineCode));
			}
			// 3、原节点的父亲节点类型更新
			if (sourceNodeCode.contains("-")) {
				String pNodeCode = getParentNodeCode(sourceNodeCode); // 根据当前节点编码，提取父节点编码
				List<String> sonNodeIds = baseMapper.getNextLevelNodeIdsByNodeCode(pNodeCode,
						strUtils.getCount(pNodeCode, "-") + 2, turbineCode); // 查询父节点下一层级是否存在其他子节点
				if (sonNodeIds.size() == 1) { // 如果父节点下，没有其他子节点，则更改父节点node_type属性.注意：这里采用了事务处理，因此这里仅剩的2个节点分别是父节点和它的一个子节点
					ConfigBomTree parentProjTree = this
						.getOne(new QueryWrapper<ConfigBomTree>().eq("node_code", pNodeCode)
							.eq("turbine_code", turbineCode)); // 根据节点编码，查询父节点
					parentProjTree.setNodeType(setParentNodeTypeAfterDel(parentProjTree.getNodeType())); // 设置父节点的node_type属性
					this.update(parentProjTree,
							new QueryWrapper<ConfigBomTree>().eq("node_id", parentProjTree.getNodeId())
								.eq("turbine_code", turbineCode)); // 更新源节点的父节点node_type属性
				}
			}
			// 4、更新子树根节点
			this.update(gbomTreeNode, new QueryWrapper<ConfigBomTree>().eq("node_id", gbomTreeNode.getNodeId())
				.eq("turbine_code", turbineCode));

			// 5、批量更新树的子节点
			if (nodeList.size() > 0) {
				int ind = strUtils.getIndexOf(nodeList.get(0).getNodeCode(), "-", oldNodeLevel);
				String pNodeCode = gbomTreeNode.getNodeCode();
				for (ConfigBomTree sonNode : nodeList) {
					sonNode.setNodeCode(pNodeCode + sonNode.getNodeCode().substring(ind));
					sonNode.setNodeLevel(strUtils.getCount(sonNode.getNodeCode(), "-") + 1);
					this.update(sonNode, new QueryWrapper<ConfigBomTree>().eq("node_id", sonNode.getNodeId())
						.eq("turbine_code", turbineCode));
				}
			}
		}
		catch (Exception e) {
			response.setCode(OpenResponseCode.ERROR);
			response.setMessage("移动失败");
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); // 事务回滚
		}
		ConfigBomTree newNode = this.getNode(gbomTreeNode.getNodeId());

		ConfigBomTreeVo res = new ConfigBomTreeVo();
		BeanUtils.copyProperties(newNode, res);
		res.setId(newNode.getNodeId());
		res.setName(newNode.getNodeName());
		res.setNodeLevel(newNode.getNodeLevel().toString());
		return res;
	}

	@Override
	public List<ConfigBomTree> findAllBomMatchedTurbine(String proName, String productModel) {
		Long proId = configBomPerceivedVariableTemplateMapper.getProId(proName, productModel);
		return this.list(new QueryWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getProId, proId)
			.eq(ConfigBomTree::getIsBomPassed, 1)
			.and(wrapper -> wrapper.eq(ConfigBomTree::getIsValPassed, 0).or().isNull(ConfigBomTree::getIsValPassed))
			.eq(ConfigBomTree::getNodeLevel, 1));
	}

	@Override
	public List<ConfigBomTreeVo> getBomSons(String nodeCode, String turbineCode) {
		return baseMapper.getBomSons(nodeCode, turbineCode);
	}

	ConfigBomTreeVo copyNodeByNodeCode(String turbineCode, String nodeCode) {
		if (hasParentNode(turbineCode, nodeCode)) { // 如果存在父节点，就执行节点复制操作
			List<ConfigBomTree> addNodes = new ArrayList<>();

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 格式化日期
			String strDate = formatter.format(new Date());
			Pair<ConfigBomTree, ConfigBomTree> pair = buildParentNodeForCopyTree(turbineCode, nodeCode, strDate);
			ConfigBomTree parentNode = pair.getRight();
			addNodes.add(parentNode);
			List<ConfigBomTree> sonNodes = buildSonNodesForCopyTree(parentNode.getProId(), turbineCode, nodeCode,
					parentNode.getTurbineCode(), parentNode.getNodeCode(), strDate);
			addNodes.addAll(sonNodes);
			if (!addNodes.isEmpty())
				this.saveBatch(addNodes);

			ConfigBomTree newPNode = addNodes.get(0);
			ConfigBomTreeVo res = new ConfigBomTreeVo();
			BeanUtils.copyProperties(newPNode, res);

			if (Objects.equals(res.getNodeType(), "Leaf"))
				res.setLeaf(true);
			res.setId(newPNode.getNodeId());
			res.setName(newPNode.getNodeName());
			return res;
		}
		return null;
	}

	private String sonNodeCode(String pNodeCode, Integer nodeNo) {
		return pNodeCode + "-" + nodeNo; // 子节点编码 = 父节点编码 + "-" + 当前节点的最大编号
	}

	@Override
	public ConfigBomTreeVo addSonNode(String parentNodeId, ConfigBomTree configBomTree) {
		ConfigBomTreeVo vo = new ConfigBomTreeVo();
		ConfigBomTree parentNode = this.getNode(parentNodeId);

		if (parentNode != null) {
			Integer nodeNo = baseMapper.getSonMaxNo(parentNode.getNodeCode(), parentNode.getTurbineCode()) + 1;
			configBomTree.setNodeNo(nodeNo);

			if (!configBomTree.getNodeType().equals("Bom")) {
				configBomTree.setNodeCode(sonNodeCode(parentNode.getNodeCode().replace("TM", "PM"), nodeNo));
			}
			else {
				configBomTree.setNodeCode(sonNodeCode(parentNode.getNodeCode(), nodeNo));
			}

			configBomTree.setTurbineCode(parentNode.getTurbineCode());
			if (configBomTree.getSwsort() == null || configBomTree.getSwsort().equals(0f)) { // 是数字0
				configBomTree.setSwsort((float) (nodeNo));
			}
			configBomTree.setNodeLevel(parentNode.getNodeLevel() + 1);
			configBomTree.setProId(parentNode.getProId().toString());
			if (!configBomTree.getNodeType().equals("Bom")) {
				if (configBomTree.getNodeType().equals("SCADA_param")) {
					configBomTree.setNodeType("SCADA_param");
				}
				else {
					configBomTree.setNodeType("CMS_param");
				}
			}
			else {
				configBomTree.setNodeType("Leaf");
			}
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			configBomTree.setUpdateTime(formatter.format(new Date()));

			// 修改父节点的leveltype
			try {
				this.save(configBomTree);
				// 更改父节点的node_type属性
				if (("Root-Leaf").equals(parentNode.getNodeType()) || ("Root").equals(parentNode.getNodeType())) {// 本身是root-leaf或者root
					parentNode.setNodeType("Root");
				}
				else { // 如果父节点原来是叶节点
					parentNode.setNodeType("Mid");
				}
				this.updateById(parentNode);

				// 返回子节点信息
				BeanUtils.copyProperties(configBomTree, vo);
				vo.setLeaf(true);
				vo.setId(configBomTree.getNodeId());
				vo.setName(configBomTree.getNodeName());
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new MyException(MyExceptionEnum.DATABASE_EXCEPTION); // 子节点添加失败，事务回滚
			}

		}

		return vo;
	}

	@Override
	public List<String> getLocation(String turbineCode, String nodeCode) {
		ConfigBomTree node = configBomTreeMapper.getNodebyCode(nodeCode, turbineCode);
		if (node == null || node.getNodeId() == null) {
			return Collections.emptyList();
		}

		List<ConfigPerceivedTask> taskList = configPerceivedTaskMapper.selectList(
				new LambdaQueryWrapper<ConfigPerceivedTask>().eq(ConfigPerceivedTask::getNodeId, node.getNodeId())
					.eq(ConfigPerceivedTask::getModelType, "perceived")
					.in(ConfigPerceivedTask::getStatus, Arrays.asList(0, 1)));
		if (taskList == null || taskList.isEmpty()) {
			return Collections.emptyList();
		}

		Set<String> locationSet = new LinkedHashSet<>();
		for (ConfigPerceivedTask task : taskList) {
			List<ConfigPerceivedTaskVariable> varList = configPerceivedTaskVariableMapper
				.getVarsByTaskId(task.getTaskId().toString());
			if (varList == null) {
				continue;
			}
			for (ConfigPerceivedTaskVariable variable : varList) {
				if (variable.getVarName() != null && !variable.getVarName().trim().isEmpty()) {
					locationSet.add(variable.getVarName());
				}
			}
		}

		return new ArrayList<>(locationSet);
	}

	@Override
	public List<ConfigBomTreeWithStatus> getStateOfTree(String farmName, String turbineModel) {
		Long proId = configBomPerceivedVariableTemplateMapper.getProId(farmName, turbineModel);
		if (proId == null) {
			return Collections.emptyList();
		}
		List<ConfigBomTree> treeList = this.list(new QueryWrapper<ConfigBomTree>().lambda()
			.eq(ConfigBomTree::getProId, proId)
			.eq(ConfigBomTree::getNodeType, "Root"));
		if (treeList == null || treeList.isEmpty()) {
			return Collections.emptyList();
		}
		return buildStateOfTreeResult(treeList);
	}

	@Override
	public List<ConfigBomTreeWithStatus> getStateOfTreeByFarm(String farmName) {
		List<AlResumeData> projectList = alResumeDataMapper
			.selectList(new QueryWrapper<AlResumeData>().eq("project", farmName).eq("bom_model", "SBOM"));
		if (projectList == null || projectList.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> proIds = projectList.stream().map(AlResumeData::getId).collect(Collectors.toList());
		if (proIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<ConfigBomTree> treeList = this.list(new QueryWrapper<ConfigBomTree>().lambda()
			.in(ConfigBomTree::getProId, proIds)
			.eq(ConfigBomTree::getNodeType, "Root"));
		if (treeList == null || treeList.isEmpty()) {
			return Collections.emptyList();
		}
		return buildStateOfTreeResult(treeList);
	}

	@Override
	public List<ConfigBomTreeWithStatus> getAccessStateOfTreeByFarm(String farmName) {
		List<AlResumeData> projectList = alResumeDataMapper
			.selectList(new QueryWrapper<AlResumeData>().eq("project", farmName).eq("bom_model", "SBOM"));
		if (projectList == null || projectList.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> proIds = projectList.stream().map(AlResumeData::getId).collect(Collectors.toList());
		if (proIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<ConfigBomTree> treeList = this.list(new QueryWrapper<ConfigBomTree>().lambda()
			.in(ConfigBomTree::getProId, proIds)
			.eq(ConfigBomTree::getNodeType, "Root"));
		if (treeList == null || treeList.isEmpty()) {
			return Collections.emptyList();
		}
		return buildStateOfTreeResult(treeList, configPerceivedTaskMapper.selectRunningNodeIdsByProIds(proIds));
	}

	private List<ConfigBomTreeWithStatus> buildStateOfTreeResult(List<ConfigBomTree> treeList) {
		return buildStateOfTreeResult(treeList, configPerceivedTaskMapper.selectDistinctNodeIds());
	}

	private List<ConfigBomTreeWithStatus> buildStateOfTreeResult(List<ConfigBomTree> treeList, List<String> nodeIds) {
		if (nodeIds == null || nodeIds.isEmpty()) {
			return treeList.stream()
				.map(tree -> new ConfigBomTreeWithStatus(tree.getNodeName(), tree.getTurbineCode(), "unaccessible",
						tree.getProducer()))
				.collect(Collectors.toList());
		}

		List<String> turbineCodes = configBomTreeMapper.selectTurbineCodesByNodeIds(nodeIds);
		Set<String> turbineCodeSet = (turbineCodes == null) ? Collections.emptySet() : new HashSet<>(turbineCodes);

		List<ConfigBomTreeWithStatus> resultList = treeList.stream()
			.map(tree -> new ConfigBomTreeWithStatus(tree.getNodeName(), tree.getTurbineCode(),
					turbineCodeSet.contains(tree.getTurbineCode()) ? "accessible" : "unaccessible", tree.getProducer()))
			.collect(Collectors.toList());

		List<String> taskIds = configAlarmInfoMapper.selectDistinctTaskIdsLast24Hours();
		if (taskIds == null || taskIds.isEmpty()) {
			resultList.forEach(tree -> {
				if ("accessible".equals(tree.getStatus())) {
					tree.setStatus("normal");
				}
			});
			return resultList;
		}

		List<String> alarmNodeIds = configPerceivedTaskMapper.selectNodeIdsByTaskIds(taskIds);
		if (alarmNodeIds == null || alarmNodeIds.isEmpty()) {
			resultList.forEach(tree -> {
				if ("accessible".equals(tree.getStatus())) {
					tree.setStatus("normal");
				}
			});
			return resultList;
		}

		List<String> alarmTurbineCodes = configBomTreeMapper.selectTurbineCodesByNodeIds(alarmNodeIds);
		Set<String> alarmTurbineCodeSet = (alarmTurbineCodes == null) ? Collections.emptySet()
				: new HashSet<>(alarmTurbineCodes);
		resultList.forEach(tree -> {
			if ("accessible".equals(tree.getStatus())) {
				tree.setStatus(alarmTurbineCodeSet.contains(tree.getTurbineCode()) ? "abnormal" : "normal");
			}
		});

		return resultList;
	}

	@Override
	public Object getOverviewOfFarms(Long userId) {
		// 1. 获取用户关联的风场名称
		List<String> farmNames = configUserFarmMapper.getFarmNamesByUserId(userId);
		if (farmNames.isEmpty()) {
			return Collections.emptyList();
		}

		// 2. 获取符合风场名的 AlResumeData 记录
		List<AlResumeData> projectList = alResumeDataMapper
			.selectList(new QueryWrapper<AlResumeData>().in("project", farmNames).eq("bom_model", "SBOM"));
		if (projectList.isEmpty()) {
			return Collections.emptyList();
		}
		List<Long> proIds = projectList.stream().map(proj -> proj.getId()).collect(Collectors.toList());

		// 2. 获取所有 pro_id 对应的 nodeName（仅限 node_type='Root'）
		List<Map<String, Object>> nodeList = configBomTreeMapper.getRootNodes(proIds);
		Map<Long, List<String>> nodeMap = nodeList.stream()
			.collect(Collectors.groupingBy(node -> Long.parseLong(node.get("pro_id").toString()),
					Collectors.mapping(node -> (String) node.get("node_name"), Collectors.toList())));

		// 3. 组装 JSON 结构
		List<Map<String, Object>> treeData = new ArrayList<>();
		Map<String, Map<String, Object>> projectMap = new HashMap<>();

		for (AlResumeData project : projectList) {
			String projectName = project.getProject();
			String productModel = project.getProductModel();
			Long proId = project.getId();

			// 获取风机节点
			List<String> nodeNames = nodeMap.getOrDefault(proId, new ArrayList<>());

			// 组装 product_model 节点
			Map<String, Object> productModelNode = new HashMap<>();
			productModelNode.put("label", productModel);
			productModelNode.put("children", nodeNames.stream().map(node -> {
				Map<String, Object> nodeMapEntry = new HashMap<>();
				nodeMapEntry.put("label", node);
				return nodeMapEntry;
			}).collect(Collectors.toList()));

			// 添加到 project
			projectMap.computeIfAbsent(projectName, k -> {
				Map<String, Object> projectNode = new HashMap<>();
				projectNode.put("label", projectName);
				projectNode.put("children", new ArrayList<Map<String, Object>>());
				treeData.add(projectNode);
				return projectNode;
			});

			((List<Map<String, Object>>) projectMap.get(projectName).get("children")).add(productModelNode);
		}

		return treeData;
	}

	@Override
	public Object searchNode(String searchKey) {
		QueryWrapper<AlResumeData> queryWrapper = new QueryWrapper<>();
		List<AlResumeData> projectList = alResumeDataMapper.selectList(queryWrapper.like("project", searchKey));
		if (projectList.isEmpty()) {
			return Collections.emptyList();
		}
		List<Long> proIds = projectList.stream().map(proj -> proj.getId()).collect(Collectors.toList());

		// 2. 获取所有 pro_id 对应的 nodeName（仅限 node_type='Root'）
		List<Map<String, Object>> nodeList = configBomTreeMapper.getRootNodes(proIds);
		Map<Long, List<String>> nodeMap = nodeList.stream()
			.collect(Collectors.groupingBy(node -> Long.parseLong(node.get("pro_id").toString()),
					Collectors.mapping(node -> (String) node.get("node_name"), Collectors.toList())));

		// 3. 组装 JSON 结构
		List<Map<String, Object>> treeData = new ArrayList<>();
		Map<String, Map<String, Object>> projectMap = new HashMap<>();

		for (AlResumeData project : projectList) {
			String projectName = project.getProject();
			String productModel = project.getProductModel();
			Long proId = project.getId();

			// 获取风机节点
			List<String> nodeNames = nodeMap.getOrDefault(proId, new ArrayList<>());

			// 组装 product_model 节点
			Map<String, Object> productModelNode = new HashMap<>();
			productModelNode.put("label", productModel);
			productModelNode.put("children", nodeNames.stream().map(node -> {
				Map<String, Object> nodeMapEntry = new HashMap<>();
				nodeMapEntry.put("label", node);
				return nodeMapEntry;
			}).collect(Collectors.toList()));

			// 添加到 project
			projectMap.computeIfAbsent(projectName, k -> {
				Map<String, Object> projectNode = new HashMap<>();
				projectNode.put("label", projectName);
				projectNode.put("children", new ArrayList<Map<String, Object>>());
				treeData.add(projectNode);
				return projectNode;
			});

			((List<Map<String, Object>>) projectMap.get(projectName).get("children")).add(productModelNode);
		}

		return treeData;
	}

}
