import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/notices'

// 获取公告列表
export function getHomepageNotices() {
    return request({
        url: BASE_URL,
        method: 'get'
    })
}