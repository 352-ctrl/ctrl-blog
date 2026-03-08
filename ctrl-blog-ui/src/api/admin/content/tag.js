import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/tags'

// 1. 获取详细信息
export function getTagById(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'get'
    })
}

// 2. 分页查询
export function getTagPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 3. 新增标签
export function addTag(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}

// 4. 修改标签
export function updateTag(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 5. 删除单个
export function deleteTag(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 6. 批量删除 (传数组)
export function deleteTags(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}

// 7. 获取标签列表 (用于下拉选择)
export function getTagList() {
    return request({
        url: `${BASE_URL}/options`,
        method: 'get'
    })
}