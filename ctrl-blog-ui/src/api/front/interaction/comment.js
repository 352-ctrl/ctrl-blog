import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/comments'

// 1. 分页查询
export function getCommentPage(params) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: params
    })
}

// 2. 新增评论
export function addComment(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}

// 3. 删除单个
export function deleteComment(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 4. 获取评论所在的页码
export function getCommentLocatorPage(id, pageSize = 10) {
    return request({
        url: `${BASE_URL}/${id}/locator`,
        method: 'get',
        params: { pageSize }
    })
}

// 5. 点赞评论
export function likeComment(commentId) {
    return request({
        url: `${BASE_URL}/${commentId}/likes`,
        method: 'post'
    })
}

// 6. 取消点赞评论
export function cancelLikeComment(commentId) {
    return request({
        url: `${BASE_URL}/${commentId}/likes`,
        method: 'delete'
    })
}