import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/profile'

// 1. 获取当前用户信息
export function getUserProfile() {
    return request({
        url: BASE_URL,
        method: 'get'
    })
}

// 2. 获取用户总览看板数据
export function getUserDashboardData() {
    return request({
        url: `${BASE_URL}/dashboard`,
        method: 'get'
    })
}

// 3. 更新用户基本信息
export function updateProfile(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 4. 修改密码
export function changePassword(data) {
    return request({
        url: `${BASE_URL}/password`,
        method: 'put',
        data: data
    })
}

// 5. 发送换绑邮箱验证码
export function sendBindEmailCode(email, captchaToken) {
    return request({
        url: `${BASE_URL}/email/verification-codes`,
        method: 'post',
        data: { email: email },
        headers: {
            'captchaVerification': captchaToken
        }
    })
}

// 6. 确认更换邮箱
export function changeEmail(data) {
    return request({
        url: `${BASE_URL}/email`,
        method: 'put',
        data: data
    })
}

// 7. 分页获取我的收藏列表
export function getUserFavorites(params) {
    return request({
        url: `${BASE_URL}/favorites`,
        method: 'get',
        params: params // 传入 pageNum 和 pageSize
    })
}

// 8. 分页获取我的点赞列表
export function getUserLikes(params) {
    return request({
        url: `${BASE_URL}/likes`,
        method: 'get',
        params: params
    })
}

// 9. 分页获取我的评论列表
export function getUserComments(params) {
    return request({
        url: `${BASE_URL}/comments`,
        method: 'get',
        params: params
    })
}

// 10. 申请注销账号
export function requestAccountDeletion() {
    return request({
        url: `${BASE_URL}/cancel`,
        method: 'post'
    })
}