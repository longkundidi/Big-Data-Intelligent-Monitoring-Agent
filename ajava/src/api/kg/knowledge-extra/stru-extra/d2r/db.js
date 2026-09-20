import request from "@/router/axios"
import Qs from "qs";

const baseUrl = '/kg';

export function dbConnect(obj) {
    return request({
        url: `${baseUrl}/db/connect`,
        method: 'post',
        data: obj
    })
}

export function tableInfo(query) {
    return request({
        url: `${baseUrl}/d2r/tables`,
        method: 'get',
        params: query
    })
}


export function getTableView(dbDatasourceName, tableName) {
    return request({
        url: `${baseUrl}/d2r/tableInfo`,
        method: 'get',
        params: {
            dbDatasourceName,
            tableName
        }
    })
}

export function getOntologyInfos(ontologyName) {
    return request({
        url: `${baseUrl}/ontology/ontologyInfo`,
        method: 'get',
        params: {
            ontologyName,
        }
    })
}

export function getOntologyNodes(ontologyName) {
    return request({
        url: `${baseUrl}/ontology/ontologyNodes`,
        method: 'get',
        params: {
            ontologyName,
        }
    })
}


export function getOntologyMapping(mappingJson, connectName) {
    return request({
        url: `${baseUrl}/ontology/ontologyMapping`,
        method: 'post',
        params: {
            connectName
        },
        data: {"mappingJson": mappingJson}
    })
}


export function doCypherPost(cypher) {
    return request({
        url: `${baseUrl}/ontology/doCypher`,
        method: 'post',
        data: {cypher:cypher}
    })
}

export function doCypherAllPost(cypher) {
    return request({
        url: `${baseUrl}/ontology/doCypherAll`,
        method: 'post',
        data: cypher
    })
}
