import request from '@/router/axios'
import axios from "axios";

export function getAlarmList(query) {
    return request({
        url: '/model3d/configalarminfo/page',
        method: 'post',
        params: query
    })
}

export function getDiagnosisList(query) {
    return request({
        url: '/model3d/configmodel/getDiagnosis',
        method: 'get',
        params: query
    })
}

export function getLocation(query) {
    return request({
        url: '/model3d/configmodel/getLocation',
        method: 'post',
        params: query
    })
}

export function runModel(obj) {
    return request({
        url: '/al/alFaultDiagnosisbase/operate',
        method: 'post',
        data: obj,
        timeout: 60000
    })
}

export function getUser() {
    return request({
        url: `/model3d/alInput/getUser`,
        method: 'get',
    })
}

export function signalAnalysis(obj) {
    return request({
        url: `/al/alFaultDiagnosisbase/signalAnalysis`,
        method: 'post',
        data: obj,
        headers: {
            'Content-Type': 'application/json'
        },
        timeout: 60000
    })
}

