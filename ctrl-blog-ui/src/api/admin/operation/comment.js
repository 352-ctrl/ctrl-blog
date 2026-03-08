import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/comments'

// 1. 分页查询
export function getCommentPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 2. 删除单个
export function deleteComment(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 3. 批量删除 (传数组)
export function deleteComments(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}