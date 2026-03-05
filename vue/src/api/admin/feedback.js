import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/admin/feedback'

// 分页查询反馈列表
export function getFeedbackPage(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
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