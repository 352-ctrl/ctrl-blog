import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/front/report'

// 提交内容举报
export function addReport(data, captchaToken) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data,
        headers: {
            'captchaVerification': captchaToken
        }
    })
}