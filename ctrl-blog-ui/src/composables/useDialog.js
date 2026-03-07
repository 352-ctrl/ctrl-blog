import { ref } from 'vue'

/**
 * 通用弹窗状态管理 Hook
 */
export function useDialog() {
    const visible = ref(false)
    const title = ref('')
    const type = ref('add') // 'add' | 'edit' | 'view'
    const rowData = ref({}) // 临时保存当前操作的行数据

    /**
     * 打开弹窗
     * @param {String} dialogTitle - 弹窗标题
     * @param {String} dialogType - 操作类型
     * @param {Object} data - 需要传递的行数据
     */
    const openDialog = (dialogTitle = '操作', dialogType = 'add', data = {}) => {
        title.value = dialogTitle
        type.value = dialogType
        rowData.value = JSON.parse(JSON.stringify(data)) // 深拷贝，防止污染表格数据
        visible.value = true
    }

    const closeDialog = () => {
        visible.value = false
        rowData.value = {}
    }

    return {
        visible,
        title,
        type,
        rowData,
        openDialog,
        closeDialog
    }
}