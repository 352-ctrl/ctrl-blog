import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/admin/articles'

// 1. 获取详细信息
export function getArticleById(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'get'
    })
}

// 2. 分页查询
export function getArticlePage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 3. 新增文章
export function addArticle(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}

// 4. 修改文章
export function updateArticle(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 5. 删除单个
export function deleteArticle(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 6. 批量删除 (传数组)
export function deleteArticles(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}

// 7. AI生成摘要
export function generateArticleSummary(data) {
    return request({
        url: `${BASE_URL}/generate-summary`,
        method: 'post',
        data: data
    })
}