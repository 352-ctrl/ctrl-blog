import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/front/comments'

// 点赞评论
export function likeComment(commentId) {
    return request({
        url: `${BASE_URL}/${commentId}/like`,
        method: 'post'
    })
}

// 取消点赞评论
export function cancelLikeComment(commentId) {
    return request({
        url: `${BASE_URL}/${commentId}/cancel-like`,
        method: 'post'
    })
}