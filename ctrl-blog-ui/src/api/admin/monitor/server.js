import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/monitor/server'

// 1. 获取服务器监控信息
export function getServerInfo() {
    return request({
        url: `${BASE_URL}/info`,
        method: 'get'
    })
}