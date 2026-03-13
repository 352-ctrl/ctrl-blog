import { reactive, ref, toRaw } from 'vue'

/**
 * 分页列表封装 Hook
 * @param {Function} apiFunc 分页API
 * @param {Object} initQuery 初始查询条件
 */
export function useTable(apiFunc, initQuery = {}) {
    const loading = ref(false)
    const list = ref([])
    const total = ref(0)

    const query = reactive({
        pageNum: 1,
        pageSize: 10,
        ...initQuery
    })

    const loadData = async () => {
        loading.value = true
        try {
            const res = await apiFunc(toRaw(query))
            if (res.code === 200) {
                // 兼容不同的分页返回格式
                list.value = res.data.records || res.data.list || res.data || []
                total.value = res.data.total || 0
            }
        } finally {
            loading.value = false
        }
    }

    const handlePageChange = async () => {
        await loadData()
    }

    const resetQuery = async () => {
        Object.assign(query, {
            pageNum: 1,
            pageSize: 10,
            ...initQuery
        })
        await loadData()
    }

    return {
        loading,
        list,
        total,
        query,
        loadData,
        handlePageChange,
        resetQuery
    }
}