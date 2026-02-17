import xss from 'xss'

/**
 * 过滤HTML字符串，防XSS攻击 + 白名单控制
 * @param {string} htmlStr - 待过滤的HTML字符串
 * @returns {string} 过滤后的安全HTML
 */
export function sanitizeHtml(htmlStr) {
    if (!htmlStr) return ''
    // 扩展白名单：保留业务需要的合法标签，仅允许安全属性
    const xssOptions = {
        whiteList: {
            // 原有基础标签
            p: [],
            strong: [],
            em: [],
            br: [],
            div: [],
            span: ['style'], // 仅允许span的style属性
            // 补充测试HTML中的合法标签
            h1: [],
            h2: [],
            ul: [],
            li: [],
            blockquote: [],
            // 特殊标签：仅允许安全属性，过滤恶意属性
            img: ['src'], // 仅保留img的src属性，过滤onerror等
            button: [], // 保留button标签，过滤onclick等
            a: ['href'], // 保留a标签的href属性，但会自动过滤javascript:协议
        },
        // 强制过滤所有on*事件属性（onclick/onerror/onload等）
        onTagAttr: function(tag, name, value) {
            return !name.startsWith('on')
        },
        // 过滤a标签的javascript:伪协议（xss库默认已处理，此处强化）
        safeAttrValue: function(tag, name, value) {
            if (tag === 'a' && name === 'href') {
                // 移除javascript:开头的链接
                if (value.toLowerCase().startsWith('javascript:')) {
                    return ''
                }
            }
            return value
        },
        // 完全移除script/iframe等危险标签（不在白名单，默认已过滤）
    }
    return xss(htmlStr, xssOptions)
}