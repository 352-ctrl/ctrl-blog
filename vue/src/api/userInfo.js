import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/user'

// 1. 获取当前用户信息
export function getUserProfile() {
    return request({
        url: BASE_URL,
        method: 'get'
    })
}

// 2. 更新用户基本信息
export function updateProfile(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 3. 修改密码
export function changePassword(data) {
    return request({
        url: `${BASE_URL}/password`,
        method: 'put',
        data: data
    })
}