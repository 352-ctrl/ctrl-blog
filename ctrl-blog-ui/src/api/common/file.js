import request from "@/utils/request.js";

const UPLOAD_URL = '/api/v1/files';

/**
 * 通用文件上传 (POST /api/v1/files)
 * @param {FormData} data - 包含文件的 FormData 对象
 * @param {String} type - 业务类型（可选，例如 'avatar', 'cover'）
 */
export function uploadFile(data, type) {
    let url = UPLOAD_URL;
    if (type) {
        url = `${UPLOAD_URL}?type=${type}`; // 拼接到 URL 参数中
    }

    return request({
        url: url,
        method: 'post',
        data: data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

/**
 * WangEditor / 富文本图片上传 (POST /api/v1/files/wang)
 * @param {FormData} data
 */
export function uploadWangImage(data) {
    return request({
        url: `${UPLOAD_URL}/wang`,
        method: 'post',
        data: data,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}