package com.algorithm.web.service.al;

import cn.hutool.json.JSONObject;
import com.algorithm.web.model.dto.al.AlResumeDataDto;
import com.algorithm.web.model.dto.al.SceneModelTree;
import com.algorithm.web.model.entity.al.AlResumeData;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface AlResumeDataService extends IService<AlResumeData> {

	ArrayList<JSONObject> saveBatch(List<AlResumeDataDto> alResumeDataDtoList);

	/**
	 * 获取全部的场景机型map
	 * @return Map<String, List<AlResumeData>>
	 */
	Map<String, List<AlResumeData>> fetchProModelMap();

	/**
	 * 检查场景名称是否存在
	 * @param name 场景名称
	 * @return 是否存在
	 */
	boolean checkSceneNameExists(String name);

	/**
	 * 检查该型号是否已经存在
	 * @param sceneName 场景名称
	 * @param modelName 型号名称
	 * @return 是否存在
	 */
	boolean checkProductModelExists(String sceneName, String modelName);

	/**
	 * 构造场景型号结构树
	 * @return
	 */
	List<SceneModelTree> buildSceneModelTree(List<AlResumeData> allData);

	/**
	 * 删除场景节点
	 * @param sceneId 场景ID
	 * @return 是否删除成功
	 */
	boolean deleteSceneNode(String sceneId);

	/**
	 * 删除模型节点
	 * @param modelId 模型ID
	 * @return 是否删除成功
	 */

	boolean deleteModelNode(String modelId);

}
