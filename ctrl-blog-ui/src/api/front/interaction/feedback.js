import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/feedbacks'

// 提交意见反馈
export function addFeedback(data, captchaToken) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data,
        headers: {
            'captchaVerification': captchaToken
        }
    })
}