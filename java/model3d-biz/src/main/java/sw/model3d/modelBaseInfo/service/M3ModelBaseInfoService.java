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

package sw.model3d.modelBaseInfo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.common.response.OpenResponse;
import sw.model3d.modelBaseInfo.entity.M3ModelBaseInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
 * @author pig code generator
 * @date 2023-10-24 10:55:58
 */
public interface M3ModelBaseInfoService extends IService<M3ModelBaseInfo> {

	// 分页查询根节点
	IPage<M3ModelBaseInfo> getRootNodePage(Page page);

	List<M3ModelBaseInfo> getSonNode(String mbCode);

	// 添加节点
	M3ModelBaseInfo addNode(M3ModelBaseInfo m3ModelBaseInfo);

	// 删除模板信息及其关联的结构树(mo_metatree_info、mo_metatree)
	OpenResponse deleteModelBaseInfo(String mbId);

}
