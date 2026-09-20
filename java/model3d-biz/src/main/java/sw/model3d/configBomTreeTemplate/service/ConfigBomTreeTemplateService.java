package sw.model3d.configBomTreeTemplate.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import sw.common.response.OpenResponse;
import sw.model3d.configBomTreeTemplate.entity.ConfigBomTreeTemplate;
import sw.model3d.configBomTreeTemplate.entity.vo.ConfigBomTreeTemplateVo;
import sw.model3d.configBomTreeTemplate.entity.vo.UploadDataSetVO;
import sw.model3d.configPerceivedVariable.entity.ConfigPerceivedVariable;

import java.util.List;
import java.util.Set;

/**
 * 项目风场不同机型风机结构表(ConfigBomTreeTemplate)表服务接口
 *
 * @author makejava
 * @since 2024-06-24 17:16:53
 */
public interface ConfigBomTreeTemplateService extends IService<ConfigBomTreeTemplate> {

	boolean createProTemplate(List<List<JSONObject>> list);

	List<ConfigBomTreeTemplateVo> getSonNodes(Long proId, String nodeCode);

	List<ConfigBomTreeTemplateVo> reqSonNodesByModelId(String modelId, String nodeLevel);

	List<ConfigBomTreeTemplateVo> getTreeNodes(Long proId, String nodeLevel);

	List<ConfigBomTreeTemplateVo> reqTreeNodesByModelId(String modelId, String nodeLevel);

	ConfigBomTreeTemplateVo addSonNode(Long proId, String parentNodeId, ConfigBomTreeTemplate configBomTreeTemplate);

	ConfigBomTreeTemplateVo editNodeInfo(Long proId, ConfigBomTreeTemplate nodeForm);

	void delNode(Long proId, String nodeCode);

	OpenResponse<ConfigBomTreeTemplateVo> copySonNode(Long proId, String nodeCode);

	ConfigBomTreeTemplate getNode(String proId, String nodeId);

	ConfigBomTreeTemplateVo moveNode(Long proId, String sourceNodeCode, String targetNodeCode);

	boolean createProInstance(Long proId, String nodeName);

	boolean createObjInstance(Long sceId, Long modelId, String nodeName);

	Object uploadSonNode(String nodeId, String datasetType, String datasetUrl, String provider, String datasetName);

	Object replaceSonNode(String nodeId, String datasetType, String datasetUrl, String provider,
			String replaceVersionType, String datasetName);

	Object revertDataset(String nodeId, String datasetType);

	Object getDataUrl(String objectId, String dataType);

	Object getAllDataUrl(String objectId, String dataType);

	// Object getDataProvider(String objectId);
	/**
	 * 根据元结构树节点ids创建机型结构树
	 * @param nodeIds 元结构树节点ids
	 * @return boolean
	 */
	boolean addProductModelTree(String proId, Set<String> nodeIds);

	boolean updateProductModelTree(String proId, Set<String> nodeIds);

	/**
	 * 根据机型复制一棵树， 同时复制数据集和感知变量
	 * @param copyName 新复制的机型名称
	 * @param newId 新复制的机型id
	 * @param proId 被复制的机型id
	 * @return
	 */
	boolean copyModelTreeAndVar(String copyName, String newId, String proId);

	boolean addProductModelVariables(String proId, List<ConfigPerceivedVariable> modelTreeVariable);

	boolean addProductModelDataSets(String proId, List<UploadDataSetVO> uploadDataSets);

	Object updateDatasetInfo(String nodeId, String type, ConfigBomTreeTemplate configBomTreeTemplate);

}
