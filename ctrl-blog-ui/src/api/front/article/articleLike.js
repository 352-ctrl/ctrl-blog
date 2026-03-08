import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/articles'

// 点赞文章
export function likeArticle(articleId) {
    return request({
        url: `${BASE_URL}/${articleId}/likes`,
        method: 'post'
    })
}

// 取消点赞文章
export function cancelLikeArticle(articleId) {
    return request({
        url: `${BASE_URL}/${articleId}/likes`,
        method: 'delete'
    })
}