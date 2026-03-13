import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/reports'

// 后台分页查询举报列表
export function getReportPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 后台处理举报
export function processReport(data) {
    return request({
        url: `${BASE_URL}/process`,
        method: 'post',
        data: data
    })
}

// 删除单条举报记录
export function deleteReport(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 批量删除举报记录
export function deleteReports(ids) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: ids
    })
}