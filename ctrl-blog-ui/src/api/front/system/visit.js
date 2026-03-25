import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/visit'

// 站点访问量主动上报
export function reportSiteVisit() {
    return request({
        url: BASE_URL,
        method: 'post',
        silent: true
    })
}