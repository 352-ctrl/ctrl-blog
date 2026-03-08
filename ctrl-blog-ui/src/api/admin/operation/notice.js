import request from "@/utils/request.js";

// 基础路径
const BASE_URL = '/api/v1/admin/notices'

// 1. 获取详细信息
export function getNoticeById(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'get'
    })
}

// 2. 分页查询
export function getNoticePage(query) {
    return request({
        url: BASE_URL,
        method: 'get',
        params: query
    })
}

// 3. 新增公告
export function addNotice(data) {
    return request({
        url: BASE_URL,
        method: 'post',
        data: data
    })
}

// 4. 修改公告
export function updateNotice(data) {
    return request({
        url: BASE_URL,
        method: 'put',
        data: data
    })
}

// 5. 删除单个
export function deleteNotice(id) {
    return request({
        url: `${BASE_URL}/${id}`,
        method: 'delete'
    })
}

// 6. 批量删除 (传数组)
export function deleteNotices(selectedIds) {
    return request({
        url: `${BASE_URL}/batch`,
        method: 'delete',
        data: selectedIds
    })
}