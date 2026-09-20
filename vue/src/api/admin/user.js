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

import request from '@/router/axios'

export function fetchList(query) {
  return request({
    url: '/admin/user/page',
    method: 'get',
    params: query
  })
}

export function addObj(obj) {
  return request({
    url: '/admin/user',
    method: 'post',
    data: obj
  })
}

export function getObj(id) {
  return request({
    url: '/admin/user/' + id,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: '/admin/user/' + id,
    method: 'delete'
  })
}

export function putObj(obj) {
  return request({
    url: '/admin/user',
    method: 'put',
    data: obj
  })
}

export function isExist(params) {
  return request({
    url: '/admin/user/check/exist',
    method: 'get',
    params: params
  })
}

// 更改个人信息
export function editInfo(obj) {
  return request({
    url: '/admin/user/edit',
    method: 'put',
    data: obj
  })
}

// 1.获取风场列表的接口
export function getProjects() {
  return request({
    url: `al/alresumedata/getProjects`,
    method: 'get'
  })
}

//获取场景列表
export function getProjectsByMetaModelId(metaModelId) {
  return request({
    url: `al/alresumedata/getProjectsByMetaModelId`,
    method: 'get',
    params: {metaModelId}
  })
}

export function getProjectById(id) {
  return request({
    url: `al/alresumedata/${id}`,
    method: 'get'
  })
}


// 2.新增用户和风场对应关系的接口
export function batchAdd(obj) {
  return request({
    url: '/model3d/configuserfarm/batchAdd',
    method: 'post',
    data: obj  // 包括userId和farmNames 数组
  });
}

// 3.编辑用户和风场对应关系的接口
export function batchUpdate(obj) {
  return request({
    url: '/model3d/configuserfarm/batchUpdate',
    method: 'put',
    data: obj  // 包括userId和farmNames 数组
  });
}

// 4.根据用户名获取风场列表的接口
export function getFarmsByUserId(userId) {
  return request({
    url: '/model3d/configuserfarm/getFarmsByUserId',
    method: 'get',
    params: {userId}  // 通过 params 传递 userId
  });
}

// 5.删除用户名获取风场列表的接口
export function deleteByUserId(userId) {
  return request({
    url: `/model3d/configuserfarm/deleteByUserId/` + userId,
    method: 'delete',
  });
}

export function getIdByFarmName(farmName) {
  return request({
    url: `/model3d/alresumedata/getIdByFarmName/` + farmName,
    method: 'get',
  });
}
