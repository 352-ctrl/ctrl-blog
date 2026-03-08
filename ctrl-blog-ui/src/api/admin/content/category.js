import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/categories'

// 1. 获取详细信息
export function getCategoryById(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'get'
    })
}

// 2. 分页查询
export function getCategoryPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 3. 新增分类
export function addCategory(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}

// 4. 修改分类
export function updateCategory(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 5. 删除单个
export function deleteCategory(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 6. 批量删除 (传数组)
export function deleteCategories(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}

// 7. 获取分类列表 (用于下拉选择)
export function getCategoryList() {
    return request({
        url: `${BASE_URL}/options`,
        method: 'get'
    })
}