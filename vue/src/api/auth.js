import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/auth'

// 1. 登录
export function login(data) {
    return request({
        url: `${BASE_URL}/login`,
        method: 'post',
        data: data
    })
}

// 2. 注册
export function register(data) {
    return request({
        url: `${BASE_URL}/register`,
        method: 'post',
        data: data
    })
}

// 3. 发送注册邮箱验证码
export function sendRegisterEmailCode(data) {
    return request({
        url: `${BASE_URL}/email/code/register`,
        method: 'post',
        data: data
    })
}

// 4. 发送找回密码邮箱验证码
export function sendForgotPwdEmailCode(data) {
    return request({
        url: `${BASE_URL}/email/code/forgot`,
        method: 'post',
        data: data
    })
}

// 5. 通过邮箱验证码重置密码
export function resetPasswordByEmail(data) {
    return request({
        url: `${BASE_URL}/password/reset`,
        method: 'post',
        data: data
    })
}

// 6. 退出登录
export function logout() {
    return request({
        url: `${BASE_URL}/logout`,
        method: 'post'
    })
}