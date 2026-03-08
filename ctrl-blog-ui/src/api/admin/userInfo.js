import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/profile'

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

// 4. 发送换绑邮箱验证码
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

// 5. 确认更换邮箱
export function changeEmail(data) {
    return request({
        url: `${BASE_URL}/email`,
        method: 'put',
        data: data
    })
}