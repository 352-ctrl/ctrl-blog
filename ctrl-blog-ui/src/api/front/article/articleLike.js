import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/front/articles'

// 点赞文章
export function likeArticle(articleId) {
    return request({
        url: `${BASE_URL}/${articleId}/like`,
        method: 'post'
    })
}

// 取消点赞文章
export function cancelLikeArticle(articleId) {
    return request({
        url: `${BASE_URL}/${articleId}/cancel-like`,
        method: 'post'
    })
}