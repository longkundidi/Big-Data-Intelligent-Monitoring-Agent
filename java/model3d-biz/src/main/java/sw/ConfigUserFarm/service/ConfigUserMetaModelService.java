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

package sw.ConfigUserFarm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import sw.ConfigUserFarm.entity.ConfigUserMetaModel;

/**
 * 元模型-用户权限对应表
 *
 * @author pig code generator
 * @date 2025-03-10 14:51:27
 */
public interface ConfigUserMetaModelService extends IService<ConfigUserMetaModel> {

	ConfigUserMetaModel getMetaModelIdByUserId(Long userId, String userRole);

}
