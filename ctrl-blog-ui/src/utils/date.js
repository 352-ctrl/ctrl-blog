/**
 * 将时间字符串格式化为“相对时间”（如：刚刚、5分钟前）
 * @param {String|Date} timeStr - 时间字符串或 Date 对象
 * @returns {String} 格式化后的相对时间
 */
export const formatTimeAgo = (timeStr) => {
    if (!timeStr) return '未知时间';

    const date = new Date(timeStr).getTime();
    const now = Date.now();
    const diff = (now - date) / 1000;

    if (diff < 60) return '刚刚';
    if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前';
    if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前';
    if (diff < 2592000) return Math.floor(diff / 86400) + ' 天前';
    if (diff < 31536000) return Math.floor(diff / 2592000) + ' 个月前';
    return Math.floor(diff / 31536000) + ' 年前';
}