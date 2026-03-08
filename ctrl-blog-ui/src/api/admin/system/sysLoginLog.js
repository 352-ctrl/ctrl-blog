import request from "@/utils/request.js";

const BASE_URL = '/api/v1/admin/monitor/login-log'

// 1. 分页查询
export function getLoginLogPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 2. 删除单个
export function deleteLoginLog(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 3. 批量删除
export function deleteLoginLogs(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}