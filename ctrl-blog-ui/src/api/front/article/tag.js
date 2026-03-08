import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/tags'

// 获取标签列表
export function getTagList() {
    return request({
        url: BASE_URL,
        method: 'get'
    })
}