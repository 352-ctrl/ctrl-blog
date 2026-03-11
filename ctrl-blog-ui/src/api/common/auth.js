import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/auth'

// 1. 登录 (创建令牌)
export function login(data) {
    return request({
        url: `${BASE_URL}/token`,
        method: 'post',
        data: data
    })
}

// 2. 注册 (创建账户)
export function register(data) {
    return request({
        url: `${BASE_URL}/accounts`,
        method: 'post',
        data: data
    })
}

// 3. 发送注册邮箱验证码
export function sendRegisterEmailCode({ email, captchaToken }) {
    return request({
        url: `${BASE_URL}/register/verification-codes`,
        method: 'post',
        data: { email: email },
        headers: {
            'captchaVerification': captchaToken
        }
    })
}

// 4. 发送找回密码邮箱验证码
export function sendForgotPwdEmailCode({ email, captchaToken }) {
    return request({
        url: `${BASE_URL}/password-reset/verification-codes`,
        method: 'post',
        data: { email: email },
        headers: {
            'captchaVerification': captchaToken
        }
    })
}

// 5. 通过邮箱验证码重置密码 (更新密码资源)
export function resetPasswordByEmail(data) {
    return request({
        url: `${BASE_URL}/password`,
        method: 'put',
        data: data
    })
}

// 6. 退出登录 (销毁令牌)
export function logout() {
    return request({
        url: `${BASE_URL}/token`,
        method: 'delete'
    })
}