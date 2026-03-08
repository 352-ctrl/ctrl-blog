import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/dashboard'

// 1. 获取仪表盘数据
export function getDashboardData() {
    return request({
        url: BASE_URL,
        method: 'get'
    })
}