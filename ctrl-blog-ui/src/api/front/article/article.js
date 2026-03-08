import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/articles'

// 1. 根据ID查询 (获取详情)
export function getArticleById(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'get'
    })
}

// 2. 获取文章归档
export function getArticleArchive() {
    return request({
        url: `${BASE_URL}/archive`,
        method: 'get'
    })
}

// 3. 分页查询 (前台文章列表)
export function getArticlePage(params) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: params
    })
}

// 4. 全站搜索索引
export function getSearchIndex() {
    return request({
        url: `${BASE_URL}/search/index`,
        method: 'get'
    })
}

// 5. 获取首页轮播图列表
export function getArticleCarousel() {
    return request({
        url: `${BASE_URL}/carousel`,
        method: 'get'
    })
}

// 6. 获取热门文章
export function getHotArticles() {
    return request({
        url: `${BASE_URL}/hot`,
        method: 'get'
    })
}

// 7. 增加文章阅读量
export function incrementArticleView(id) {
    return request({
        url: `${BASE_URL}/${id}/view`,
        method: 'post'
    })
}