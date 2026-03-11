import { ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 基础请求封装 Hook
 * @param {Function} apiFunc API函数
 * @param {Object} options 配置项 { successMsg, errorMsg, immediate, params }
 */
export function useRequest(apiFunc, options = {}) {
    const { successMsg = null, errorMsg = null, immediate = false, params = null } = options

    const loading = ref(false)
    const data = ref(null)
    const error = ref(null)

    const execute = async (...args) => {
        loading.value = true
        error.value = null
        try {
            // 如果调用时没传参，则使用初始化时的 params
            const res = await apiFunc(...(args.length > 0 ? args : [params]))
            if (res.code === 200) {
                data.value = res.data
                if (successMsg) ElMessage.success(successMsg)
                return res.data
            } else {
                throw new Error(res.msg || '请求失败')
            }
        } catch (err) {
            error.value = err
            if (errorMsg) {
                ElMessage.error(errorMsg)
            }
            throw err
        } finally {
            loading.value = false
        }
    }

    if (immediate) {
        // 使用自执行函数或直接调用并捕获错误，避免被识别为 ignored promise
        execute().catch(() => {})
    }

    return { loading, data, error, execute }
}