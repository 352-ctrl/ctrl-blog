import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/categories'

// 获取分类列表
export function getCategoryList() {
    return request({
        url: BASE_URL,
        method: 'get'
    })
}