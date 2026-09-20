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

package sw.model3d.configModel.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import sw.common.response.OpenResponse;
import sw.model3d.configModel.entity.ConfigBomTree;
import sw.model3d.configModel.entity.vo.ConfigBomTreeVo;
import sw.model3d.configModel.entity.vo.ConfigBomTreeWithStatus;
import sw.model3d.configModel.entity.vo.ConfigComponentMatchVo;

import java.util.List;

/**
 * GBOM树
 *
 * @author pig code generator
 * @date 2024-05-09 11:36:07
 */
public interface ConfigBomTreeService extends IService<ConfigBomTree> {

	List<ConfigBomTree> findAllTurbine(String proName, String productModel);

	ConfigComponentMatchVo componentMatch(List<String> componentCodes, ConfigBomTree turbine, String sceneId);

	List<ConfigBomTreeVo> getComponentNodes(Long proId, String nodeLevel);

	List<String> findComponentCodes(String turbineCode);

	List<ConfigBomTreeVo> getSonNodes(String nodeCode, String turbineCode);

	List<ConfigBomTreeVo> getTreeNodes(Long proId, String nodeLevel);

	boolean uploadUnitData(List<List<JSONObject>> list);

	boolean updateUnitData(ConfigBomTreeVo configBomTreeVo);

	ConfigBomTreeVo addSonNode(String parentNodeId, ConfigBomTree configBomTreeTemplate);

	ConfigBomTree getNode(String nodeId);

	ConfigBomTreeVo editNodeInfo(ConfigBomTree nodeForm);

	void delNode(Long proId, String turbineCode, String nodeCode);

	OpenResponse<ConfigBomTreeVo> copySonNode(String turbineCode, String nodeCode);

	ConfigBomTreeVo moveNode(String turbineCode, String sourceNodeCode, String targetNodeCode);

	List<ConfigBomTree> findAllBomMatchedTurbine(String proName, String productModel);

	List<ConfigBomTreeVo> getBomSons(String nodeCode, String turbineCode);

	List<String> getLocation(String turbineCode, String nodeCode);

	List<ConfigBomTreeWithStatus> getStateOfTree(String farmName, String turbineModel);

	List<ConfigBomTreeWithStatus> getStateOfTreeByFarm(String farmName);

	List<ConfigBomTreeWithStatus> getAccessStateOfTreeByFarm(String farmName);

	Object getOverviewOfFarms(Long userId);

	Object searchNode(String searchKey);

}
