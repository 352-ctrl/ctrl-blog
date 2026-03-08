import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/monitor/redis'

// 1. 获取缓存监控信息
export function getRedisInfo() {
    return request({
        url: `${BASE_URL}/info`,
        method: 'get'
    })
}

// 2. 分页搜索键值列表
export function getRedisKeys(params) {
    return request({
        url: BASE_URL + '/keys',
        method: 'get',
        params: params
    })
}