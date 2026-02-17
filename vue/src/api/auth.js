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

// 3. 发送验证码
export function sendEmailCode(email) {
    return request({
        url: `${BASE_URL}/email/code`,
        method: 'post',
        data: { email: email }
    })
}