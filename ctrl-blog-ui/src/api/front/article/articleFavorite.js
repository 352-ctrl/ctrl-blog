import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/articles'

// 收藏文章
export function favoriteArticle(articleId) {
    return request({
        url: `${BASE_URL}/${articleId}/favorites`,
        method: 'post'
    })
}

// 取消收藏文章
export function cancelFavoriteArticle(articleId) {
    return request({
        url: `${BASE_URL}/${articleId}/favorites`,
        method: 'delete'
    })
}