import request from '@/utils/request.js'

// 分页查询敏感词列表
export function getSensitiveWordPage(params) {
    return request({
        url: '/api/admin/sensitive-words',
        method: 'get',
        params: params
    })
}

// 获取敏感词详情
export function getSensitiveWordById(id) {
    return request({
        url: `/api/admin/sensitive-words/${id}`,
        method: 'get'
    })
}

// 新增敏感词
export function addSensitiveWord(data) {
    return request({
        url: '/api/admin/sensitive-words',
        method: 'post',
        data: data
    })
}

// 更新敏感词
export function updateSensitiveWord(data) {
    return request({
        url: '/api/admin/sensitive-words',
        method: 'put',
        data: data
    })
}

// 单个删除敏感词
export function deleteSensitiveWord(id) {
    return request({
        url: `/api/admin/sensitive-words/${id}`,
        method: 'delete'
    })
}

// 批量删除敏感词
export function deleteSensitiveWords(ids) {
    return request({
        url: '/api/admin/sensitive-words/batch',
        method: 'delete',
        data: ids
    })
}