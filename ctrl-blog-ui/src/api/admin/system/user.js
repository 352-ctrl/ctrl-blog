import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/users'

// 1. 获取详细信息
export function getUserById(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'get'
    })
}

// 2. 分页查询
export function getUserPage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 3. 新增用户
export function addUser(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}

// 4. 修改用户
export function updateUser(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 5. 删除单个
export function deleteUser(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 6. 批量删除 (传数组)
export function deleteUsers(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}

// 7. 管理员重置用户密码
export function resetPassword(data) {
    return request({
        url: `${BASE_URL}/password`,
        method: 'put',
        data: data
    })
}