import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/danmakus'

// 1. 获取所有弹幕列表
export function getDanmakuList() {
    return request({
        url: BASE_URL,
        method: 'get'
    })
}

// 2. 发送新弹幕
export function addDanmaku(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}