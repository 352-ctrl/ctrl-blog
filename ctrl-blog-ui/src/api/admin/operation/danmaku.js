import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/danmakus'

// 1. 分页查询弹幕
export function getDanmakuPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 2. 删除单个弹幕
export function deleteDanmaku(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 3. 批量删除弹幕 (传数组)
export function deleteDanmakus(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}