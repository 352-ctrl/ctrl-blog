import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/front/site'

// 获取站长卡片信息
export function getWebmasterInfo() {
    return request({
        url: `${BASE_URL}/webmaster`,
        method: 'get'
    })
}