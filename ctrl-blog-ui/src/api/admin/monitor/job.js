import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/jobs'

// 1. 获取详细信息
export function getJobById(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'get'
    })
}

// 2. 分页查询
export function getJobPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 3. 新增任务
export function addJob(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}

// 4. 修改任务
export function updateJob(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 5. 删除单个
export function deleteJob(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 6. 批量删除 (传数组)
export function deleteJobs(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}

// 7. 立即执行一次
export function run(id) {
    return request({
        url: `${BASE_URL}/${id}/run`,
        method: 'post'
    })
}

// 8. 状态修改
export function changeStatus(id, status) {
    return request({
        url: `${BASE_URL}/${id}/status`,
        method: 'post',
        data: { status: status }
    })
}