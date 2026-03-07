/**
 * 常用格式化工具 Hook
 */
export function useFormat() {

    const getMonth = (dateStr) => {
        if (!dateStr) return ''
        return new Date(dateStr).getMonth() + 1 + '月'
    }

    const getDay = (dateStr) => {
        if (!dateStr) return ''
        return new Date(dateStr).getDate()
    }

    const formatFullDate = (dateStr) => {
        if (!dateStr) return ''
        const date = new Date(dateStr)
        const y = date.getFullYear()
        const m = (date.getMonth() + 1).toString().padStart(2, '0')
        const d = date.getDate().toString().padStart(2, '0')
        return `${y}-${m}-${d}`
    }

    return {
        getMonth,
        getDay,
        formatFullDate
    }
}