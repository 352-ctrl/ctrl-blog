import request from "@/utils/request.js";

const UPLOAD_URL = '/api/files';

/**
 * 通用文件上传
 * @param {FormData} data - 包含文件的 FormData 对象
 */
export function uploadFile(data) {
    return request({
        url: `${UPLOAD_URL}/upload`,
        method: 'post',
        data: data,
        headers: {
            // 显式声明 multipart/form-data，防止被拦截器默认的 JSON header 覆盖
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * Markdown / 富文本图片上传
 * @param {FormData} data
 */
export function uploadWangImage(data) {
    return request({
        url: `${UPLOAD_URL}/wang/upload`,
        method: 'post',
        data: data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}