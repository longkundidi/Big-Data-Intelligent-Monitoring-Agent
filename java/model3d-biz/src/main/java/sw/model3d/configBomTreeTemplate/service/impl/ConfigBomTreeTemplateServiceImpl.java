package sw.model3d.configBomTreeTemplate.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.core.util.R;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import sw.AlResumeData.entity.AlResumeData;
import sw.AlResumeData.mapper.AlResumeDataMapper;
import sw.common.exception.MyException;
import sw.common.exception.MyExceptionEnum;
import sw.common.response.OpenResponse;
import sw.common.response.OpenResponseCode;
import sw.common.strUtils;
import sw.model3d.ConfigBomPerceivedVariable.entity.ConfigBomPerceivedVariable;
import sw.model3d.ConfigBomPerceivedVariable.service.ConfigBomPerceivedVariableService;
import sw.model3d.configBomPerceivedVariableTemplate.entity.ConfigBomPerceivedVariableTemplate;
import sw.model3d.configBomPerceivedVariableTemplate.mapper.ConfigBomPerceivedVariableTemplateMapper;
import sw.model3d.configBomPerceivedVariableTemplate.service.ConfigBomPerceivedVariableTemplateService;
import sw.model3d.configBomTreeTemplate.entity.ConfigBomTreeTemplate;
import sw.model3d.configBomTreeTemplate.entity.vo.ConfigBomTreeTemplateVo;
import sw.model3d.configBomTreeTemplate.entity.vo.UploadDataSetVO;
import sw.model3d.configBomTreeTemplate.mapper.ConfigBomTreeTemplateMapper;
import sw.model3d.configBomTreeTemplate.service.ConfigBomTreeTemplateService;
import sw.model3d.configGbomTree.entity.ConfigGbomTree;
import sw.model3d.configGbomTree.mapper.ConfigGbomTreeMapper;
import sw.model3d.configModel.entity.ConfigBomTree;
import sw.model3d.configModel.service.ConfigBomTreeService;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;
import sw.utils.codecTag;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 项目风场不同机型风机结构表(ConfigBomTreeTemplate)表服务实现类
 *
 * @author makejava
 * @since 2024-06-24 17:16:53
 */
@Service
public class ConfigBomTreeTemplateServiceImpl extends ServiceImpl<ConfigBomTreeTemplateMapper, ConfigBomTreeTemplate>
		implements ConfigBomTreeTemplateService {

	@Autowired
	private ConfigBomTreeService configBomTreeService;

	@Autowired
	private ConfigBomPerceivedVariableService configBomPerceivedVariableService;

	@Autowired
	private ConfigBomPerceivedVariableTemplateMapper configBomPerceivedVariableTemplateMapper;

	@Autowired
	private ConfigGbomTreeMapper configGbomTreeMapper;

	@Autowired
	private ConfigBomTreeTemplateMapper configBomTreeTemplateMapper;

	@Autowired
	private AlResumeDataMapper alResumeDataMapper;

	@Autowired
	private ConfigBomPerceivedVariableTemplateService configBomPerceivedVariableTemplateService;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@Override
	public boolean createProTemplate(List<List<JSONObject>> list) {
		// 先删除已有的该机型的风机结构记录
		for (List<JSONObject> listItem : list) {
			JSONObject root = listItem.get(0);
			String proId = root.getString("pro_id");
			baseMapper.delTempalteByProId(proId);

		}

		// 再插入该机型的风机结构
		for (List<JSONObject> listItem : list) {
			JSONObject root = listItem.get(0);

			for (JSONObject object : listItem) {
				ConfigBomTreeTemplate configBomTreeTemplate = new ConfigBomTreeTemplate();
				configBomTreeTemplate.setProId(object.getString("pro_id"));
				configBomTreeTemplate.setNodeName(object.getString("node_name"));
				configBomTreeTemplate.setNodeCode(object.getString("node_code"));
				configBomTreeTemplate.setNodeType(object.getString("node_type"));

				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				configBomTreeTemplate.setUpdateTime(formatter.format(new Date()));

				// 计算node_level
				String nodeCode = (String) object.get("node_code");
				char targetChar = '-';
				int count = 0;
				int index = 0;

				while ((index = nodeCode.indexOf(targetChar, index)) != -1) {
					count++; // 每找到一个 '-' 就加一
					index++; // 移动到下一个字符，避免重复计数
				}
				configBomTreeTemplate.setNodeLevel(count + 1); // 层级 = '-'数量 + 1

				/* 一种机型有多种风机结构的情况 */
				// 查询当前机型下的所有不重复的turbinecode，并取出最后一个字段最大的turbinecode返回
				// String turbineCode=
				// baseMapper.turbineCodeMaxByProductModel((object.getString("pro_id")));
				// String[] parts = turbineCode.split("-");
				// String lastPart = parts[parts.length - 1];
				// String numStr = lastPart.replace("\\D+", "");
				// configBomTreeTemplate.setTurbineCode(root.getString("node_name")+"-"+"template"+(Integer.parseInt(numStr)+1));

				configBomTreeTemplate
					.setTurbineCode(object.getString("pro_id") + "-" + root.getString("node_name") + "-" + "template1");

				// "TM1-1-10"
				if (object.getString("node_type").equals("Root")) {
					configBomTreeTemplate.setNodeNo(1);
				}
				else {
					int lastIndex = object.getString("node_code").lastIndexOf("-");// 返回最后一个-的index
					int length = object.getString("node_code").length();// 获取长度
					String node_code = object.getString("node_code").substring(lastIndex + 1, length);// 截取从最后一个index+1到length之间的字符

					configBomTreeTemplate.setNodeNo(Integer.parseInt(node_code));
					configBomTreeTemplate.setSwsort(Float.parseFloat(node_code));
				}
				this.save(configBomTreeTemplate);
			}
		}
		return true;
	}

	@Override
	public boolean createProInstance(Long proId, String nodeName) {
		String turbineCode = createInstanceBom(proId, nodeName);
		return createInstanceVar(proId, turbineCode);
	}

	@Override
	public boolean createObjInstance(Long sceId, Long modelId, String nodeName) {
		// 根据具体型号modelId创建该型号的实例对象
		String turbineCode = createInstanceBom(modelId, nodeName);
		// 由于感知变量被挂载到场景上，根据场景sceId创建实例对象的感知变量
		return createInstanceVar(sceId, turbineCode);
	}

	String createInstanceBom(Long proId, String nodeName) {
		String turbineCode = "";
		// 获取指定proId的所有结构
		LambdaQueryWrapper<ConfigBomTreeTemplate> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigBomTreeTemplate::getProId, proId);
		List<ConfigBomTreeTemplate> templateList = this.getBaseMapper().selectList(queryWrapper);
		for (ConfigBomTreeTemplate template : templateList) {
			template.setNodeId(null);

			LambdaQueryWrapper<ConfigBomTree> queryWrapper1 = new LambdaQueryWrapper<>();
			queryWrapper1.eq(ConfigBomTree::getProId, proId);
			List<ConfigBomTree> configBomTreeList = configBomTreeService.getBaseMapper().selectList(queryWrapper1);

			if (template.getNodeType().equals("Root")) {
				template.setNodeName(nodeName);
				if (configBomTreeList.size() == 0) {
					turbineCode = template.getTurbineCode().replace("template1", "WT1");

				}
				else {
					ArrayList<Integer> list = new ArrayList<>();
					for (ConfigBomTree configBomTree : configBomTreeList) {
						String turbineCodestr = configBomTree.getTurbineCode();
						String pstr = turbineCodestr.substring(0, turbineCodestr.lastIndexOf("WT"));
						String num_part = turbineCodestr.replace(pstr, "");

						list.add(Integer.parseInt(num_part.replace("WT", "")));

					}
					List<Integer> nums = list.stream().distinct().collect(Collectors.toList());
					Integer current_Maxnum = Collections.max(nums) + 1;
					turbineCode = template.getTurbineCode().replace("template1", "WT" + current_Maxnum);
				}
			}
			template.setTurbineCode(turbineCode);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			template.setUpdateTime(formatter.format(new Date()));
			ConfigBomTree configBomTree = new ConfigBomTree();
			BeanUtils.copyProperties(template, configBomTree);
			configBomTreeService.save(configBomTree);
		}
		return turbineCode;
	}

	boolean createInstanceVar(Long proId, String turbineCode) {
		// 获取指定proId的所有感知变量
		LambdaQueryWrapper<ConfigBomPerceivedVariableTemplate> val = new LambdaQueryWrapper<>();
		val.eq(ConfigBomPerceivedVariableTemplate::getProId, proId);
		List<ConfigBomPerceivedVariableTemplate> valList = configBomPerceivedVariableTemplateMapper.selectList(val);
		for (ConfigBomPerceivedVariableTemplate item : valList) {
			ConfigBomPerceivedVariable configBomPerceivedVariable = new ConfigBomPerceivedVariable();
			BeanUtils.copyProperties(item, configBomPerceivedVariable);
			configBomPerceivedVariable.setTurbineCode(turbineCode);
			configBomPerceivedVariable.setId(null);
			configBomPerceivedVariableService.save(configBomPerceivedVariable);
		}
		return true;
	}

	@Override
	public List<ConfigBomTreeTemplateVo> getSonNodes(Long proId, String nodeCode) {
		return baseMapper.getSonNodes(proId, nodeCode);
	}

	@Override
	public List<ConfigBomTreeTemplateVo> reqSonNodesByModelId(String modelId, String nodeCode) {
		return baseMapper.reqSonNodesByModelId(modelId, nodeCode);
	}

	@Override
	public List<ConfigBomTreeTemplateVo> getTreeNodes(Long proId, String nodeLevel) {
		return baseMapper.getTreeNodes(proId, nodeLevel);
	}

	@Override
	public List<ConfigBomTreeTemplateVo> reqTreeNodesByModelId(String modelId, String nodeLevel) {
		return baseMapper.reqTreeNodesByModelId(modelId, nodeLevel);
	}

	public Object uploadDataSet(ConfigBomTreeTemplate record, String datasetType, String datasetUrl, String provider,
			String datasetName) {
		if ("SCADA_train".equals(datasetType)) {
			// 更新 SCADA 数据集 URL
			record.setScadaTrainPreviousDataset(record.getScadaTrainLatestDataset()); // 将当前最新值赋给上一次
			record.setScadaTrainLatestDataset(datasetUrl); // 设置新的最新值
			// 更新数据集名称和提供者
			record.setScadaDatasetName(datasetName);
			record.setScadaProvider(provider);
		}
		else if ("SCADA_test".equals(datasetType)) {
			// 更新 CMS 数据集 URL
			record.setScadaTestPreviousDataset(record.getScadaTestLatestDataset()); // 将当前最新值赋给上一次
			record.setScadaTestLatestDataset(datasetUrl); // 设置新的最新值
			// 更新数据集名称和提供者
			record.setScadaDatasetName(datasetName);
			record.setScadaProvider(provider);
		}
		else if ("CMS_train".equals(datasetType)) {
			// 更新 CMS 数据集 URL
			record.setCmsTrainPreviousDataset(record.getCmsTrainLatestDataset()); // 将当前最新值赋给上一次
			record.setCmsTrainLatestDataset(datasetUrl); // 设置新的最新值
			// 更新数据集名称和提供者
			record.setCmsDatasetName(datasetName);
			record.setCmsProvider(provider);
		}
		else if ("CMS_test".equals(datasetType)) {
			// 更新 CMS 数据集 URL
			record.setCmsTestPreviousDataset(record.getCmsTestLatestDataset()); // 将当前最新值赋给上一次
			record.setCmsTestLatestDataset(datasetUrl); // 设置新的最新值
			// 更新数据集名称和提供者
			record.setCmsDatasetName(datasetName);
			record.setCmsProvider(provider);
		}
		else {
			throw new RuntimeException("不支持的 datasetType: " + datasetType);
		}
		// 保存更新后的记录
		this.baseMapper.updateById(record);
		return record; // 返回更新后的记录，或根据需求返回其他值
	}

	public Object replaceDataSet(ConfigBomTreeTemplate record, String datasetType, String datasetUrl, String provider,
			String replaceVersionType, String datasetName) {
		if ("SCADA_train".equals(datasetType)) {
			if ("train_latest".equals(replaceVersionType)) {
				record.setScadaTrainLatestDataset(datasetUrl);
			}
			else {
				record.setScadaTrainPreviousDataset(datasetUrl);
			}
			// 更新数据集名称和提供者
			record.setScadaDatasetName(datasetName);
			record.setScadaProvider(provider);
		}
		else if ("SCADA_test".equals(datasetType)) {
			if ("test_latest".equals(replaceVersionType)) {
				record.setScadaTestLatestDataset(datasetUrl);
			}
			else {
				record.setScadaTestPreviousDataset(datasetUrl);
			}
			// 更新数据集名称和提供者
			record.setScadaDatasetName(datasetName);
			record.setScadaProvider(provider);
		}
		else if ("CMS_train".equals(datasetType)) {
			if ("train_latest".equals(replaceVersionType)) {
				record.setCmsTrainLatestDataset(datasetUrl);
			}
			else {
				record.setCmsTrainPreviousDataset(datasetUrl);
			}
			// 更新数据集名称和提供者
			record.setCmsDatasetName(datasetName);
			record.setCmsProvider(provider);
		}
		else if ("CMS_test".equals(datasetType)) {
			if ("test_latest".equals(replaceVersionType)) {
				record.setCmsTestLatestDataset(datasetUrl);
			}
			else {
				record.setCmsTestPreviousDataset(datasetUrl);
			}
			// 更新数据集名称和提供者
			record.setCmsDatasetName(datasetName);
			record.setCmsProvider(provider);
		}
		else {
			throw new RuntimeException("不支持的 datasetType: " + datasetType);
		}
		// 保存更新后的记录
		this.baseMapper.updateById(record);
		return record; // 返回更新后的记录，或根据需求返回其他值
	}

	@Override
	public Object uploadSonNode(String nodeId, String datasetType, String datasetUrl, String provider,
			String datasetName) {
		// 根据 nodeId 查找记录
		QueryWrapper<ConfigBomTreeTemplate> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("node_id", nodeId); // 假设数据库字段是 node_id
		ConfigBomTreeTemplate record = this.getBaseMapper().selectOne(queryWrapper);
		if (record == null) {
			throw new RuntimeException("未找到 nodeId 为 " + nodeId + " 的记录");
		}
		return uploadDataSet(record, datasetType, datasetUrl, provider, datasetName);
	}

	@Override
	public Object replaceSonNode(String nodeId, String datasetType, String datasetUrl, String provider,
			String replaceVersionType, String datasetName) {
		// 根据 nodeId 查找记录
		QueryWrapper<ConfigBomTreeTemplate> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("node_id", nodeId); // 假设数据库字段是 node_id
		ConfigBomTreeTemplate record = this.getBaseMapper().selectOne(queryWrapper);
		if (record == null) {
			throw new RuntimeException("未找到 nodeId 为 " + nodeId + " 的记录");
		}
		return replaceDataSet(record, datasetType, datasetUrl, provider, replaceVersionType, datasetName);
	}

	@Override
	public Object revertDataset(String nodeId, String datasetType) {
		QueryWrapper<ConfigBomTreeTemplate> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("node_id", nodeId);
		ConfigBomTreeTemplate record = this.getBaseMapper().selectOne(queryWrapper);
		if (record == null) {
			throw new RuntimeException("未找到 nodeId 为 " + nodeId + " 的记录");
		}
		if ("SCADA_train".equals(datasetType)) {
			String previousDataset = record.getScadaTrainPreviousDataset();
			if (previousDataset == null) {
				return R.failed("无可用的历史版本");
			}
			else {
				record.setScadaTrainLatestDataset(previousDataset);
			}

		}
		else if ("SCADA_test".equals(datasetType)) {
			String previousDataset = record.getScadaTestPreviousDataset();
			if (previousDataset == null) {
				return R.failed("无可用的历史版本");
			}
			else {
				record.setScadaTestLatestDataset(previousDataset);
			}
		}
		else if ("CMS_train".equals(datasetType)) {
			String previousDataset = record.getCmsTrainPreviousDataset();
			if (previousDataset == null) {
				return R.failed("无可用的历史版本");
			}
			else {
				record.setCmsTrainLatestDataset(previousDataset);
			}
		}
		else if ("CMS_test".equals(datasetType)) {
			String previousDataset = record.getCmsTestPreviousDataset();
			if (previousDataset == null) {
				return R.failed("无可用的历史版本");
			}
			else {
				record.setCmsTestLatestDataset(previousDataset);
			}
		}
		else {
			throw new RuntimeException("不支持的 datasetType: " + datasetType);
		}
		// 保存更新后的记录
		this.baseMapper.updateById(record);
		return record; // 返回更新后的记录，或根据需求返回其他值
	}

	@Override
	public Object getDataUrl(String objectId, String dataType) {
		if (dataType.equals("scada"))
			return this.baseMapper.getScadaDataUrl(objectId);
		if (dataType.equals("cms"))
			return this.baseMapper.getCmsDataUrl(objectId);

		return null;
	}

	@Override
	public Object getAllDataUrl(String objectId, String dataType) {
		if (dataType.equals("scada"))
			return this.baseMapper.getAllScadaDataUrl(objectId);
		if (dataType.equals("cms"))
			return this.baseMapper.getAllCmsDataUrl(objectId);
		return null;
	}

	// @Override
	// public Object getDataProvider(String objectId){
	// return this.baseMapper.getDataProvider(objectId);
	// }

	@Override
	public ConfigBomTreeTemplateVo addSonNode(Long proId, String parentNodeId,
			ConfigBomTreeTemplate configBomTreeTemplate) {
		ConfigBomTreeTemplateVo vo = new ConfigBomTreeTemplateVo();
		ConfigBomTreeTemplate parentNode = this.getNode(proId.toString(), parentNodeId);

		if (parentNode != null) {
			Integer nodeNo = baseMapper.getSonMaxNo(parentNode.getNodeCode(), proId) + 1;
			configBomTreeTemplate.setNodeNo(nodeNo);

			if (!configBomTreeTemplate.getNodeType().equals("Bom")) {
				configBomTreeTemplate.setNodeCode(sonNodeCode(parentNode.getNodeCode().replace("TM", "PM"), nodeNo));
			}
			else {
				configBomTreeTemplate.setNodeCode(sonNodeCode(parentNode.getNodeCode(), nodeNo));
			}

			configBomTreeTemplate.setTurbineCode(parentNode.getTurbineCode());
			if (configBomTreeTemplate.getSwsort() == null || configBomTreeTemplate.getSwsort().equals(0f)) { // 是数字0
				configBomTreeTemplate.setSwsort((float) (nodeNo));
			}
			configBomTreeTemplate.setNodeLevel(parentNode.getNodeLevel() + 1);
			configBomTreeTemplate.setProId(proId.toString());

			if (!configBomTreeTemplate.getNodeType().equals("Bom")) {
				if (configBomTreeTemplate.getNodeType().equals("SCADA_param")) {
					configBomTreeTemplate.setNodeType("SCADA_param");
				}
				else {
					configBomTreeTemplate.setNodeType("CMS_param");
				}
			}
			else {
				configBomTreeTemplate.setNodeType("Leaf");
			}

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			configBomTreeTemplate.setUpdateTime(formatter.format(new Date()));

			// 修改父节点的leveltype
			try {
				this.save(configBomTreeTemplate);
				// 更改父节点的node_type属性
				if (("Root-Leaf").equals(parentNode.getNodeType()) || ("Root").equals(parentNode.getNodeType())) {// 本身是root-leaf或者root
					parentNode.setNodeType("Root");
				}
				else { // 如果父节点原来是叶节点
					parentNode.setNodeType("Mid");
				}
				this.updateById(parentNode);

				// 返回子节点信息
				BeanUtils.copyProperties(configBomTreeTemplate, vo);
				vo.setLeaf(true);
				vo.setId(configBomTreeTemplate.getNodeId());
				vo.setName(configBomTreeTemplate.getNodeName());
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new MyException(MyExceptionEnum.DATABASE_EXCEPTION); // 子节点添加失败，事务回滚
			}

		}

		return vo;

	}

	@Override
	public ConfigBomTreeTemplateVo editNodeInfo(Long proId, ConfigBomTreeTemplate nodeForm) {

		LambdaUpdateWrapper<ConfigBomTreeTemplate> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.eq(ConfigBomTreeTemplate::getNodeId, nodeForm.getNodeId())
			.eq(ConfigBomTreeTemplate::getProId, proId)
			.set(ConfigBomTreeTemplate::getNodeName, nodeForm.getNodeName())
			.set(ConfigBomTreeTemplate::getSwsort, nodeForm.getSwsort())
			.set(ConfigBomTreeTemplate::getMemo, nodeForm.getMemo());
		update(nodeForm, updateWrapper);
		ConfigBomTreeTemplate updated = this.getNode(proId.toString(), nodeForm.getNodeId());
		ConfigBomTreeTemplateVo res = new ConfigBomTreeTemplateVo();
		BeanUtils.copyProperties(updated, res);
		res.setId(updated.getNodeId());
		res.setName(updated.getNodeName());
		if (updated.getNodeType().equals("Leaf"))
			res.setLeaf(true);
		return res;
	}

	@Override
	public void delNode(Long proId, String nodeCode) {
		// 删除root节点对应的模板感知变量
		LambdaQueryWrapper<ConfigBomTreeTemplate> queryWrapper = new LambdaQueryWrapper<>();
		queryWrapper.eq(ConfigBomTreeTemplate::getProId, proId).eq(ConfigBomTreeTemplate::getNodeCode, nodeCode);
		ConfigBomTreeTemplate template = this.getBaseMapper().selectOne(queryWrapper);
		if (template.getNodeType().equals("Root")) {
			LambdaQueryWrapper<ConfigBomPerceivedVariableTemplate> queryWrapper1 = new LambdaQueryWrapper<>();
			queryWrapper1.eq(ConfigBomPerceivedVariableTemplate::getProId, proId);
			configBomPerceivedVariableTemplateMapper.delete(queryWrapper1);
		}

		// 删除节点
		List<ConfigBomTreeTemplate> delList = baseMapper.selectList(
				new QueryWrapper<ConfigBomTreeTemplate>().lambda().eq(ConfigBomTreeTemplate::getProId, proId).and(i -> // 开始一个内嵌的AND块，确保下面的OR条件都是在proId已经匹配的基础上执行
				i.likeRight(ConfigBomTreeTemplate::getNodeCode, nodeCode + "-") // 第一个或条件：以nodeCode+“-”%
					.or()
					.likeRight(ConfigBomTreeTemplate::getNodeCode, nodeCode.replace("TM", "PM") + "-") // 第二个或条件：替换后以nodeCode+“-”%
					.or()
					.eq(ConfigBomTreeTemplate::getNodeCode, nodeCode) // 第三个或条件：精确匹配nodeCode
				));

		List<String> dels = new ArrayList<>();
		for (ConfigBomTreeTemplate node : delList) {
			dels.add(node.getNodeId());
		}
		this.removeBatchByIds(dels); // 批量删除节点
		if (nodeCode.contains(codecTag.joinStr))
			updatePNodeTypeWhileDel(proId, nodeCode);

	}

	@Override
	public OpenResponse<ConfigBomTreeTemplateVo> copySonNode(Long proId, String nodeCode) {
		OpenResponse<ConfigBomTreeTemplateVo> response = new OpenResponse<>(OpenResponseCode.ERROR);
		if (nodeCode != null) {
			ConfigBomTreeTemplateVo configBomTreeTemplateVo = copyNodeByNodeCode(proId, nodeCode);
			if (configBomTreeTemplateVo == null) {
				response.setCode(4008);
				response.setMessage("复制失败 or 根节点不可复制");
				return response;
			}
			response.setData(configBomTreeTemplateVo);
			response.setCode(200);
			response.setMessage("复制成功");
		}
		return response;

	}

	@Override
	public ConfigBomTreeTemplate getNode(String proId, String nodeId) {
		return this.getOne(new QueryWrapper<ConfigBomTreeTemplate>().eq("node_id", nodeId).eq("pro_id", proId));
	}

	@Override
	public ConfigBomTreeTemplateVo moveNode(Long proId, String sourceNodeCode, String targetNodeCode) {
		OpenResponse response = new OpenResponse(OpenResponseCode.SUCCESS);
		ConfigBomTreeTemplate gbomTreeNode = baseMapper.selectOne(new QueryWrapper<ConfigBomTreeTemplate>().lambda()
			.eq(ConfigBomTreeTemplate::getNodeCode, sourceNodeCode)
			.eq(ConfigBomTreeTemplate::getProId, proId)); // 找到待移动的节点对象
		ConfigBomTreeTemplate PgbomTreeNode = baseMapper.selectOne(new QueryWrapper<ConfigBomTreeTemplate>().lambda()
			.eq(ConfigBomTreeTemplate::getProId, proId)
			.eq(ConfigBomTreeTemplate::getNodeCode, targetNodeCode)); // 找到目标节点对象
		List<ConfigBomTreeTemplate> nodeList = baseMapper.selectList(new QueryWrapper<ConfigBomTreeTemplate>().lambda()
			.likeRight(ConfigBomTreeTemplate::getNodeCode, sourceNodeCode + "-")
			.eq(ConfigBomTreeTemplate::getProId, proId));
		Integer oldNodeLevel = gbomTreeNode.getNodeLevel();
		// 1、生成（待移动节点）新的节点编码nodeCode
		gbomTreeNode.setNodeNo(baseMapper.getSonMaxNo(targetNodeCode, proId) + 1);
		gbomTreeNode.setNodeLevel(PgbomTreeNode.getNodeLevel() + 1);
		gbomTreeNode.setNodeType((nodeList.size() == 0) ? "Leaf" : "Mid");
		gbomTreeNode.setNodeCode(sonNodeCode(targetNodeCode, gbomTreeNode.getNodeNo()));

		try {
			// 2、更新目标节点的nodeType属性
			if ("Leaf".equals(PgbomTreeNode.getNodeType()) || "Root-Leaf".equals(PgbomTreeNode.getNodeType())) {
				PgbomTreeNode.setNodeType("Mid");
				this.update(PgbomTreeNode,
						new QueryWrapper<ConfigBomTreeTemplate>().eq("node_id", PgbomTreeNode.getNodeId())
							.eq("pro_id", proId));
			}
			// 3、原节点的父亲节点类型更新
			if (sourceNodeCode.contains("-")) {
				String pNodeCode = getParentNodeCode(sourceNodeCode); // 根据当前节点编码，提取父节点编码
				List<String> sonNodeIds = baseMapper.getNextLevelNodeIdsByNodeCode(pNodeCode,
						strUtils.getCount(pNodeCode, "-") + 2, proId.toString()); // 查询父节点下一层级是否存在其他子节点
				if (sonNodeIds.size() == 1) { // 如果父节点下，没有其他子节点，则更改父节点node_type属性.注意：这里采用了事务处理，因此这里仅剩的2个节点分别是父节点和它的一个子节点
					ConfigBomTreeTemplate parentProjTree = this.getOne(
							new QueryWrapper<ConfigBomTreeTemplate>().eq("node_code", pNodeCode).eq("pro_id", proId)); // 根据节点编码，查询父节点
					parentProjTree.setNodeType(setParentNodeTypeAfterDel(parentProjTree.getNodeType())); // 设置父节点的node_type属性
					this.update(parentProjTree,
							new QueryWrapper<ConfigBomTreeTemplate>().eq("node_id", parentProjTree.getNodeId())
								.eq("pro_id", proId)); // 更新源节点的父节点node_type属性
				}
			}
			// 4、更新子树根节点
			this.update(gbomTreeNode, new QueryWrapper<ConfigBomTreeTemplate>().eq("node_id", gbomTreeNode.getNodeId())
				.eq("pro_id", proId));

			// 5、批量更新树的子节点
			if (nodeList.size() > 0) {
				int ind = strUtils.getIndexOf(nodeList.get(0).getNodeCode(), "-", oldNodeLevel);
				String pNodeCode = gbomTreeNode.getNodeCode();
				for (ConfigBomTreeTemplate sonNode : nodeList) {
					sonNode.setNodeCode(pNodeCode + sonNode.getNodeCode().substring(ind));
					sonNode.setNodeLevel(strUtils.getCount(sonNode.getNodeCode(), "-") + 1);
					this.update(sonNode, new QueryWrapper<ConfigBomTreeTemplate>().eq("node_id", sonNode.getNodeId())
						.eq("pro_id", proId));
				}
			}
		}
		catch (Exception e) {
			response.setCode(OpenResponseCode.ERROR);
			response.setMessage("移动失败");
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly(); // 事务回滚
		}
		ConfigBomTreeTemplate newNode = this.getNode(proId.toString(), gbomTreeNode.getNodeId());

		ConfigBomTreeTemplateVo res = new ConfigBomTreeTemplateVo();
		BeanUtils.copyProperties(newNode, res);
		res.setId(newNode.getNodeId());
		res.setName(newNode.getNodeName());
		return res;
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

	// 更新被删除节点的父节点类型
	private void updatePNodeTypeWhileDel(Long proId, String nodeCode) {
		String pNode = nodeCode.substring(0, nodeCode.lastIndexOf("-"));
		ConfigBomTreeTemplate pGbomTree = baseMapper.selectOne(new QueryWrapper<ConfigBomTreeTemplate>().lambda()
			.eq(ConfigBomTreeTemplate::getProId, proId)
			.eq(ConfigBomTreeTemplate::getNodeCode, pNode));
		List<ConfigBomTreeTemplateVo> sonNodes = baseMapper.getSonNodes(proId, pNode);
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

	public ConfigBomTreeTemplateVo copyNodeByNodeCode(Long proId, String nodeCode) {

		if (hasParentNode(proId, nodeCode)) { // 如果存在父节点，就执行节点复制操作
			List<ConfigBomTreeTemplate> addNodes = new ArrayList<>();
			String projCode = getProjCode(nodeCode);// 提取项目编码
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 格式化日期
			String strDate = formatter.format(new Date());
			Pair<ConfigBomTreeTemplate, ConfigBomTreeTemplate> pair = buildParentNodeForCopyTree(proId, nodeCode,
					strDate);
			ConfigBomTreeTemplate parentNode = pair.getRight();
			addNodes.add(parentNode);
			List<ConfigBomTreeTemplate> sonNodes = buildSonNodesForCopyTree(proId, nodeCode,
					parentNode.getTurbineCode(), parentNode.getNodeCode(), strDate);
			addNodes.addAll(sonNodes);
			if (!addNodes.isEmpty())
				this.saveBatch(addNodes);

			ConfigBomTreeTemplate newPNode = addNodes.get(0);
			ConfigBomTreeTemplateVo res = new ConfigBomTreeTemplateVo();
			BeanUtils.copyProperties(newPNode, res);

			if (Objects.equals(res.getNodeType(), "Leaf"))
				res.setLeaf(true);
			res.setId(newPNode.getNodeId());
			res.setName(newPNode.getNodeName());
			return res;
		}
		return null;
	}

	private boolean hasParentNode(Long proId, String nodeCode) {
		if (!nodeCode.contains("-"))
			return false;
		String pCode = nodeCode.substring(0, nodeCode.lastIndexOf("-"));
		ConfigBomTreeTemplate pGbomTree = baseMapper.selectOne(new QueryWrapper<ConfigBomTreeTemplate>().lambda()
			.eq(ConfigBomTreeTemplate::getProId, proId)
			.eq(ConfigBomTreeTemplate::getNodeCode, pCode));
		if (pGbomTree != null)// 存在父节点
			return true;
		else
			return false;
	}

	// 从节点编码中提取项目编码
	private String getProjCode(String nodeCode) {
		String[] split = nodeCode.split("-", 2);
		return split[0];
	}

	private String getParentNodeCode(String nodeCode) {
		return nodeCode.substring(0, nodeCode.lastIndexOf("-"));
	}

	private String sonNodeCode(String pNodeCode, Integer nodeNo) {
		return pNodeCode + "-" + nodeNo; // 子节点编码 = 父节点编码 + "-" + 当前节点的最大编号
	}

	private String updateSonNodeCode(String sonNodeCode, String pNodeCode, String pNodeNewCode) {
		return sonNodeCode.replace(pNodeCode, pNodeNewCode);
	}

	public String addCopySuffix(Long proId, String input, String nodeCode) {
		String suffix = "-复制";
		int indexP = input.indexOf("-复制");
		String pre = null;
		if (indexP != -1) {
			pre = input.substring(0, indexP);
		}
		else {
			pre = new String(input);
		}
		List<ConfigBomTreeTemplate> lists = this.list(new QueryWrapper<ConfigBomTreeTemplate>().lambda()
			.eq(ConfigBomTreeTemplate::getProId, proId)
			.likeRight(ConfigBomTreeTemplate::getNodeName, pre)
			.likeRight(ConfigBomTreeTemplate::getNodeCode, nodeCode.substring(0, nodeCode.length() - 2)));
		int count = 0;
		Pattern pattern = Pattern.compile("-复制(\\d+)");
		for (ConfigBomTreeTemplate list : lists) {
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

	private Pair<ConfigBomTreeTemplate, ConfigBomTreeTemplate> buildParentNodeForCopyTree(Long proId, String nodeCode,
			String strUpdateTime) {
		ConfigBomTreeTemplate gbomTreeNode = this
			.getOne(new QueryWrapper<ConfigBomTreeTemplate>().eq("pro_id", proId).eq("node_code", nodeCode)); // 查询待复制的节点
		ConfigBomTreeTemplate originalNode = new ConfigBomTreeTemplate();
		BeanUtil.copyProperties(gbomTreeNode, originalNode); // 复制一个拷贝
		gbomTreeNode.setNodeId(null);
		ConfigBomTreeTemplate parentNode = this.getOne(new QueryWrapper<ConfigBomTreeTemplate>().eq("pro_id", proId)
			.eq("node_code", getParentNodeCode(nodeCode))); // 获取父节点

		Integer nodeNo = baseMapper.getSonMaxNo(parentNode.getNodeCode(), proId) + 1; // 计算子树根节点的编号
		gbomTreeNode.setNodeNo(nodeNo);
		gbomTreeNode.setNodeCode(sonNodeCode(parentNode.getNodeCode(), nodeNo)); // 生成新节点的编码
		gbomTreeNode.setTurbineCode(parentNode.getTurbineCode());

		gbomTreeNode.setNodeName(addCopySuffix(proId, gbomTreeNode.getNodeName(), nodeCode)); // 修改节点名称
		gbomTreeNode.setSwsort((float) nodeNo);
		gbomTreeNode.setUpdateTime(strUpdateTime); // 设置日期日期
		Pair<ConfigBomTreeTemplate, ConfigBomTreeTemplate> pair = Pair.of(originalNode, gbomTreeNode);
		return pair;
	}

	private List<ConfigBomTreeTemplate> buildSonNodesForCopyTree(Long proId, String nodeCode,
			String newParentTurbineCode, String newParentNodeCode, String strUpdateTime) {
		List<ConfigBomTreeTemplate> sonNodes = baseMapper.getAllSubNodesByNodeCode(proId, nodeCode);

		for (ConfigBomTreeTemplate node : sonNodes) {
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
	public boolean addProductModelTree(String proId, Set<String> nodeIds) {
		// 先删除已有的该机型的风机结构记录
		// 要考虑修改used_count
		List<String> nodeCodes = baseMapper.getNodeCodesByProId(proId);
		if (nodeCodes != null && !nodeCodes.isEmpty()) {
			for (String nodeId : nodeIds) {
				configGbomTreeMapper.decrementUsedCountByNodeId(nodeId);
			}
		}
		baseMapper.delTempalteByProId(proId);

		List<ConfigGbomTree> configGbomTrees = configGbomTreeMapper
			.selectList(new LambdaQueryWrapper<ConfigGbomTree>().in(ConfigGbomTree::getNodeId, nodeIds));
		// 修改used_count，使其值增加1
		List<String> nodeIdList = configGbomTrees.stream().map(ConfigGbomTree::getNodeId).collect(Collectors.toList());
		for (String nodeId : nodeIdList) {
			configGbomTreeMapper.incrementUsedCountByNodeId(nodeId);
		}
		AlResumeData data = alResumeDataMapper
			.selectOne(new LambdaQueryWrapper<AlResumeData>().eq(AlResumeData::getId, proId));
		String productModelName = data != null ? data.getProductModel() : null;
		ConfigBomTreeTemplate configBomTreeTemplate = null;
		for (ConfigGbomTree configGbomTree : configGbomTrees) {
			String nodeCode = configGbomTree.getNodeCode();
			configBomTreeTemplate = new ConfigBomTreeTemplate();
			configBomTreeTemplate.setProId(proId);
			configBomTreeTemplate.setNodeName(configGbomTree.getNodeName());
			configBomTreeTemplate.setNodeCode(nodeCode);
			configBomTreeTemplate.setNodeType(configGbomTree.getNodeType());

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			configBomTreeTemplate.setUpdateTime(formatter.format(new Date()));

			// 计算node_level
			char targetChar = '-';
			int count = 0;
			int index = 0;

			while ((index = nodeCode.indexOf(targetChar, index)) != -1) {
				count++; // 每找到一个 '-' 就加一
				index++; // 移动到下一个字符，避免重复计数
			}
			configBomTreeTemplate.setNodeLevel(count + 1); // 层级 = '-'数量 + 1

			/* 一种机型有多种风机结构的情况 */
			// 查询当前机型下的所有不重复的turbinecode，并取出最后一个字段最大的turbinecode返回
			// String turbineCode=
			// baseMapper.turbineCodeMaxByProductModel((object.getString("pro_id")));
			// String[] parts = turbineCode.split("-");
			// String lastPart = parts[parts.length - 1];
			// String numStr = lastPart.replace("\\D+", "");
			// configBomTreeTemplate.setTurbineCode(root.getString("node_name")+"-"+"template"+(Integer.parseInt(numStr)+1));

			configBomTreeTemplate.setTurbineCode(proId + "-" + productModelName + "-" + "template1");

			// "TM1-1-10"
			if (configGbomTree.getNodeType().equals("Root")) {
				configBomTreeTemplate.setNodeNo(1);
				configBomTreeTemplate.setNodeName(productModelName);
			}
			else {
				int lastIndex = nodeCode.lastIndexOf("-");// 返回最后一个-的index
				int length = configGbomTree.getNodeCode().length();// 获取长度
				String node_code = nodeCode.substring(lastIndex + 1, length);// 截取从最后一个index+1到length之间的字符

				configBomTreeTemplate.setNodeNo(Integer.parseInt(node_code));
				configBomTreeTemplate.setSwsort(Float.parseFloat(node_code));
			}
			this.save(configBomTreeTemplate);
		}
		return true;
	}

	@Override
	public boolean addProductModelVariables(String proId, List<ConfigPerceivedVariable> modelTreeVariable) {
		// 先删除已有的数据
		configBomPerceivedVariableTemplateMapper.deleteByProId(Integer.valueOf(proId));

		ConfigBomPerceivedVariableTemplate configBomPerceivedVariableTemplate = null;
		for (ConfigPerceivedVariable configPerceivedVariable : modelTreeVariable) {
			configBomPerceivedVariableTemplate = new ConfigBomPerceivedVariableTemplate();
			BeanUtils.copyProperties(configPerceivedVariable, configBomPerceivedVariableTemplate);

			String gBomNodeId = configPerceivedVariable.getNodeId();
			String nodeCode = configGbomTreeMapper.selectById(gBomNodeId).getNodeCode();
			ConfigBomTreeTemplate configBomTreeTemplate = configBomTreeTemplateMapper
				.selectOne(new LambdaQueryWrapper<ConfigBomTreeTemplate>().eq(ConfigBomTreeTemplate::getProId, proId)
					.eq(ConfigBomTreeTemplate::getNodeCode, nodeCode));
			if (Objects.isNull(configBomTreeTemplate)) {
				return false;
			}
			String bomNodeId = configBomTreeTemplate.getNodeId();

			configBomPerceivedVariableTemplate.setNodeId(bomNodeId);
			configBomPerceivedVariableTemplate.setUsable("1");
			configBomPerceivedVariableTemplate.setProId(Long.valueOf(proId));

			configBomPerceivedVariableTemplateMapper.insert(configBomPerceivedVariableTemplate);
		}
		return true;
	}

	public boolean addProductModelDataSets(String proId, List<UploadDataSetVO> uploadDataSets) {
		for (UploadDataSetVO uploadDataSetVO : uploadDataSets) {
			ConfigBomTreeTemplate record = configBomTreeTemplateMapper
				.selectOne(new LambdaQueryWrapper<ConfigBomTreeTemplate>()
					.eq(ConfigBomTreeTemplate::getNodeCode, uploadDataSetVO.getNode().getNodeCode())
					.eq(ConfigBomTreeTemplate::getProId, proId));
			if (record == null) {
				throw new RuntimeException("未找到 对应的节点 的记录");
			}
			uploadDataSet(record, uploadDataSetVO.getDatasetType(), uploadDataSetVO.getDatasetUrl(),
					uploadDataSetVO.getProvider(), uploadDataSetVO.getDatasetName());
		}
		return true;
	}

	@Override
	public Object updateDatasetInfo(String nodeId, String type, ConfigBomTreeTemplate config) {

		String prefix = type.equalsIgnoreCase("cms") ? "cms" : "scada";
		// 构建更新参数Map
		Map<String, Object> params = new HashMap<>();

		// 以下字段带前缀
		params.put("prefix", prefix);
		params.put("nodeId", nodeId);
		if (prefix.equals("cms")) {
			// 以下都带前缀
			params.put("fieldCount", config.getCmsFieldCount());
			params.put("collectionStartDate", config.getCmsCollectionStartDate());
			params.put("collectionEndDate", config.getCmsCollectionEndDate());
			params.put("collectionFrequency", config.getCmsCollectionFrequency());
			params.put("dataType", config.getCmsDataType());
			params.put("missingCondition", config.getCmsMissingCondition());
			params.put("storageSize", config.getCmsStorageSize());
			params.put("datasetName", config.getCmsDatasetName());
			params.put("provider", config.getCmsProvider());
			params.put("source", config.getCmsSource());
		}
		else {
			// 以下都带前缀
			params.put("fieldCount", config.getScadaFieldCount());
			params.put("collectionStartDate", config.getScadaCollectionStartDate());
			params.put("collectionEndDate", config.getScadaCollectionEndDate());
			params.put("collectionFrequency", config.getScadaCollectionFrequency());
			params.put("dataType", config.getScadaDataType());
			params.put("missingCondition", config.getScadaMissingCondition());
			params.put("storageSize", config.getScadaStorageSize());
			params.put("datasetName", config.getScadaDatasetName());
			params.put("provider", config.getScadaProvider());
			params.put("source", config.getScadaSource());
		}

		// 执行更新
		return configBomTreeTemplateMapper.updateDatasetInfo(params);

	}

	/**
	 * 增量更新机型结构树模板
	 * @param proId 机型ID
	 * @param nodeIds 结构树节点ID集合
	 * @return 是否成功
	 */
	public boolean updateProductModelTree(String proId, Set<String> nodeIds) {
		AlResumeData data = alResumeDataMapper
			.selectOne(new LambdaQueryWrapper<AlResumeData>().eq(AlResumeData::getId, proId));
		String productModelName = data != null ? data.getProductModel() : null;
		ConfigBomTreeTemplate rootNode = configBomTreeTemplateMapper
			.selectOne(new LambdaQueryWrapper<ConfigBomTreeTemplate>().eq(ConfigBomTreeTemplate::getProId, proId)
				.eq(ConfigBomTreeTemplate::getNodeType, "Root"));
		rootNode.setNodeName(productModelName);
		configBomTreeTemplateMapper.updateById(rootNode);
		// 1. 查询现有模板树节点
		List<ConfigBomTreeTemplate> existingTemplates = configBomTreeTemplateMapper
			.selectList(new LambdaQueryWrapper<ConfigBomTreeTemplate>().eq(ConfigBomTreeTemplate::getProId, proId));
		Map<String, ConfigBomTreeTemplate> existingNodeCodeMap = existingTemplates.stream()
			.collect(Collectors.toMap(ConfigBomTreeTemplate::getNodeCode, t -> t));
		Set<String> existingNodeCodes = existingNodeCodeMap.keySet();

		// 2. 查询最新的结构树节点
		List<ConfigGbomTree> gbomTrees = configGbomTreeMapper
			.selectList(new LambdaQueryWrapper<ConfigGbomTree>().in(ConfigGbomTree::getNodeId, nodeIds));
		String sceneId = gbomTrees.get(0).getSceneId();
		Map<String, ConfigGbomTree> gbomNodeCodeMap = gbomTrees.stream()
			.collect(Collectors.toMap(ConfigGbomTree::getNodeCode, t -> t));
		Set<String> newNodeCodes = gbomNodeCodeMap.keySet();

		// 3. 需要删除的节点
		Set<String> toDelete = new HashSet<>(existingNodeCodes);
		toDelete.removeAll(newNodeCodes);

		// 4. 需要新增的节点
		Set<String> toAdd = new HashSet<>(newNodeCodes);
		toAdd.removeAll(existingNodeCodes);

		// 5. 删除多余的模板节点，并减usedCount
		for (String nodeCode : toDelete) {
			ConfigBomTreeTemplate template = existingNodeCodeMap.get(nodeCode);
			if (template != null) {
				configBomTreeTemplateMapper.deleteById(template.getNodeId());
				// 结构树模板的nodeId和gbom的nodeId不一定一致，这里用nodeCode反查ConfigGbomTree
				ConfigGbomTree gbom = configGbomTreeMapper.getNodeByCodeAndSceneId(nodeCode, sceneId);
				if (gbom != null) {
					configGbomTreeMapper.decrementUsedCountByNodeId(gbom.getNodeId());
				}
			}
		}

		// 6. 新增缺失的模板节点，并加usedCount
		for (String nodeCode : toAdd) {
			ConfigGbomTree gbomTree = gbomNodeCodeMap.get(nodeCode);
			if (gbomTree != null) {
				ConfigBomTreeTemplate configBomTreeTemplate = new ConfigBomTreeTemplate();
				configBomTreeTemplate.setProId(proId);
				configBomTreeTemplate.setNodeName(gbomTree.getNodeName());
				configBomTreeTemplate.setNodeCode(nodeCode);
				configBomTreeTemplate.setNodeType(gbomTree.getNodeType());
				configBomTreeTemplate.setMemo(gbomTree.getMemo());
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				configBomTreeTemplate.setUpdateTime(formatter.format(new Date()));
				// 计算node_level
				char targetChar = '-';
				int count = 0;
				int index = 0;
				while ((index = nodeCode.indexOf(targetChar, index)) != -1) {
					count++;
					index++;
				}
				configBomTreeTemplate.setNodeLevel(count + 1);
				configBomTreeTemplate.setTurbineCode(proId + "-" + productModelName + "-" + "template1");

				int lastIndex = nodeCode.lastIndexOf("-");
				int length = nodeCode.length();
				String node_code = nodeCode.substring(lastIndex + 1, length);
				configBomTreeTemplate.setNodeNo(Integer.parseInt(node_code));
				configBomTreeTemplate.setSwsort(Float.parseFloat(node_code));

				configBomTreeTemplateMapper.insert(configBomTreeTemplate);
				configGbomTreeMapper.incrementUsedCountByNodeId(gbomTree.getNodeId());
			}
		}
		return true;
	}

	@Override
	public boolean copyModelTreeAndVar(String copyName, String newId, String proId) {
		// 查询原始结构树和变量
		List<ConfigBomTreeTemplate> treeNodes = configBomTreeTemplateMapper
			.selectList(new LambdaQueryWrapper<ConfigBomTreeTemplate>().eq(ConfigBomTreeTemplate::getProId, proId));
		List<ConfigBomPerceivedVariableTemplate> vars = configBomPerceivedVariableTemplateMapper
			.selectList(new LambdaQueryWrapper<ConfigBomPerceivedVariableTemplate>()
				.eq(ConfigBomPerceivedVariableTemplate::getProId, proId));
		// 构建 treeNodes：nodeId -> nodeCode 的映射
		Map<String, String> oldNodeIdToNodeCodeMap = treeNodes.stream()
			.collect(Collectors.toMap(ConfigBomTreeTemplate::getNodeId, ConfigBomTreeTemplate::getNodeCode));

		treeNodes.forEach(node -> {
			node.setNodeId(null); // 置空 nodeId
			node.setTurbineCode(newId + "-" + copyName + "-template1"); // 设置 turbineCode
			node.setUpdateTime(LocalDateTime.now().format(FORMATTER));
			node.setProId(newId);
			if ("Root".equals(node.getNodeType())) {
				node.setNodeName(copyName); // 如果是 Root 节点，设置 nodeName
			}
		});
		this.saveBatch(treeNodes);

		// 2.查询刚保存的结构树
		List<ConfigBomTreeTemplate> copyNodes = configBomTreeTemplateMapper
			.selectList(new LambdaQueryWrapper<ConfigBomTreeTemplate>()
				.select(ConfigBomTreeTemplate::getNodeId, ConfigBomTreeTemplate::getNodeCode)
				.eq(ConfigBomTreeTemplate::getProId, newId));

		// 构建 copyNodes 的 nodeCode -> nodeId 映射
		Map<String, String> codeToCopyNodeIdMap = copyNodes.stream()
			.collect(Collectors.toMap(ConfigBomTreeTemplate::getNodeCode, ConfigBomTreeTemplate::getNodeId));

		vars.forEach(var -> {
			var.setId(null);
			String nodeCode = oldNodeIdToNodeCodeMap.get(var.getNodeId());
			if (nodeCode == null) {
				log.warn("无法找到原变量 [{" + var.getId() + "}] 的 nodeCode");
				return;
			}
			String newNodeId = codeToCopyNodeIdMap.get(nodeCode);
			if (newNodeId == null) {
				log.warn("无法找到 nodeCode [{" + nodeCode + "}] 对应的新 nodeId");
				return;
			}
			var.setNodeId(newNodeId);
			var.setNodeId(codeToCopyNodeIdMap.get(nodeCode));
			var.setProId(Long.valueOf(newId));
		});
		configBomPerceivedVariableTemplateService.saveBatch(vars);

		return true;
	}

}
