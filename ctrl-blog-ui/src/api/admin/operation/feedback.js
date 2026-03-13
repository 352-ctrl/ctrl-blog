import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/feedbacks'

// 分页查询反馈列表
export function getFeedbackPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 后台处理反馈
export function processFeedback(data) {
    return request({
        url: `${BASE_URL}/process`,
        method: 'post',
        data: data
    })
}

// 删除单条反馈记录
export function deleteFeedback(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 批量删除反馈记录
export function deleteFeedbacks(ids) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: ids
    })
}