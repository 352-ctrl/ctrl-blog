import request from '@/utils/request.js'

// 基础路径
const BASE_URL = '/api/v1/admin/sensitive-words'

// 分页查询敏感词列表
export function getSensitiveWordPage(params) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: params
    })
}

// 获取敏感词详情
export function getSensitiveWordById(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'get'
    })
}

// 新增敏感词
export function addSensitiveWord(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}

// 更新敏感词
export function updateSensitiveWord(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 单个删除敏感词
export function deleteSensitiveWord(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 批量删除敏感词
export function deleteSensitiveWords(ids) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: ids
    })
}