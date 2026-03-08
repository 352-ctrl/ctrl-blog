import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/messages'

// 1. 分页查询消息列表 (这是上一步建议你后端补充的接口)
export function getMessagePage(params) {
    return request({
        url: `${BASE_URL}/list`,
        method: 'get',
        params: params
    })
}

// 2. 获取当前用户的未读消息总数
export function getUnreadCount() {
    return request({
        url: `${BASE_URL}/unread/count`,
        method: 'get'
    })
}

// 3. 标记单条消息为已读
export function markMessageAsRead(id) {
    return request({
        url: `${BASE_URL}/${id}/read`,
        method: 'put'
    })
}

// 4. 一键全部已读
export function markAllAsRead(type) {
    return request({
        url: `${BASE_URL}/read-status`,
        method: 'put',
        params: { type } // type 参数可选
    })
}