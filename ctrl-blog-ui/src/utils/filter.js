import xss from 'xss'

/**
 * 过滤HTML字符串，防XSS攻击 + 白名单控制
 * @param {string} htmlStr - 待过滤的HTML字符串
 * @returns {string} 过滤后的安全HTML
 */
export function sanitizeHtml(htmlStr) {
    if (!htmlStr) return ''

    const xssOptions = {
        whiteList: {
            // --- 1. 基础排版标签 ---
            p: [], br: [], div: ['class', 'id'],
            span: ['style', 'class'],

            // 允许 h 标签带 id，保证文章目录(TOC)正常锚点跳转
            h1: ['id'], h2: ['id'], h3: ['id'], h4: ['id'], h5: ['id'], h6: ['id'],

            // --- 2. 文本格式化标签 ---
            strong: [], b: [], em: [], i: [], u: [], s: [], strike: [], del: [],
            sub: [], sup: [], blockquote: [],

            // 代码块允许带 class，保证 Highlight.js 语法高亮不丢失
            code: ['class'], pre: ['class', 'style'],

            // --- 3. 列表与表格 ---
            ul: ['class'], ol: ['class'], li: ['class'],
            table: [], thead: [], tbody: [], tr: [], th: ['style', 'align'], td: ['style', 'align'],

            // 允许只读的 input，保证 Markdown 任务列表 ([x] Task) 正常显示
            input: ['type', 'disabled', 'checked'],

            // --- 4. 媒体与交互 ---
            img: ['src', 'alt', 'title', 'width', 'height', 'class', 'style'],
            button: ['class'],
            a: ['href', 'target', 'title', 'rel', 'class'],
        },
        // 强制过滤所有 on* 事件属性（onclick/onerror/onload等）防跨站脚本
        onTagAttr: function(tag, name, value) {
            if (name.startsWith('on')) {
                return '';
            }
        },
        // 过滤 a 标签的 javascript: 伪协议
        safeAttrValue: function(tag, name, value) {
            if (tag === 'a' && name === 'href') {
                if (value.toLowerCase().startsWith('javascript:')) {
                    return '';
                }
            }
            return value;
        }
    }
    return xss(htmlStr, xssOptions)
}