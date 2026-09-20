import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/al';

export function fetchList(query) {
  return request({
    url: `${baseUrl}/altask/page`,
    method: 'get',
    params: query
  })
}

export function getname(id) {
  return request({
    url: `${baseUrl}/altask/name/` + id,
    method: 'get'
  })
}

export function addObj(obj) {
  return request({
    url: `${baseUrl}/altask`,
    method: 'post',
    data: obj
  })
}

export function getObj(id) {
  return request({
    url: `${baseUrl}/altask/` + id,
    method: 'get'
  })
}

export function delObj(id) {
  return request({
    url: `${baseUrl}/altask/` + id,
    method: 'delete'
  })
}

export function putObj(obj) {
  return request({
    url: `${baseUrl}/altask`,
    method: 'put',
    data: obj
  })
}

export function startJob(id,alModelType,useCase) {
  return request({
    url: `${baseUrl}/altask/startJob`,
    params: {
      id: id,
      alModelType:alModelType,
      useCase:useCase
    },
    method: 'get',
    timeout: 60000
  })
}

export function DataCleaningTest(obj) {
  return request({
    url: `${baseUrl}/altask/DataCleaningTest`,
    method: 'post',
    data: obj
  })
}

export function dimensionalityReductionTest(obj) {
  return request({
    url: `${baseUrl}/altask/dimensionalityReductionTest`,
    method: 'post',
    data: obj
  })
}

export function featureExtractionTest(obj) {
  return request({
    url: `${baseUrl}/altask/featureExtractionTest`,
    method: 'post',
    data: obj
  })
}

export function dataMiningTest(obj) {
  return request({
    url: `${baseUrl}/altask/dataMiningTest`,
    method: 'post',
    data: obj
  })
}

export function textExtraction(useCase,obj) {
  return request({
    url: `${baseUrl}/altask/testModel/${useCase}`,
    method: 'post',
    data: obj
  })
}

export function FaultDiagnosisTest(obj) {
  return request({
    url: `${baseUrl}/altask/FaultDiagnosisTest`,
    method: 'post',
    data: obj
  })
}

export function StateEvaluationTest(obj) {
  return request({
    url: `${baseUrl}/altask/StateEvaluationTest`,
    method: 'post',
    data: obj
  })
}

export function runModelTest(obj) {
  return request({
    url: `${baseUrl}/model-test`,
    method: 'post',
    data: obj,
    timeout: 60000
  })
}

export function conditionsClassificationTest(obj) {
  return request({
    url: `${baseUrl}/altask/conditionsClassificationTest`,
    method: 'post',
    data: obj
  })
}

export function maintenanceDecisionsTest(obj) {
  return request({
    url: `${baseUrl}/altask/maintenanceDecisionsTest`,
    method: 'post',
    data: obj
  })
}

export function faultTransmitTest(obj) {
  return request({
    url: `${baseUrl}/altask/faultTransmitTest`,
    method: 'post',
    data: obj
  })
}

export function resourceSchedulingTest(obj) {
  return request({
    url: `${baseUrl}/altask/resourceSchedulingTest`,
    method: 'post',
    data: obj
  })
}

export function domainModelTest(obj) {
  return request({
    url: `${baseUrl}/altask/domainModelTest`,
    method: 'post',
    data: obj
  })
}

export function autoTest(programUrl) {
  return request({
    url: `${baseUrl}/altask/autoTest`,
    method: 'get',
    params: {
      programUrl: programUrl
    }
  })
}

export function getLog(query) {
  return request({
    url: `${baseUrl}/altask/getLog`,
    method: 'get',
    params: query
  })
}
