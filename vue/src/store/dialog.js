import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useDialogStore = defineStore('dialog', () => {
    // 举报弹窗的状态
    const reportVisible = ref(false);
    const reportTargetId = ref(null);
    const reportTargetType = ref('COMMENT'); // 默认是评论，也可以是 'USER'

    // 唤起举报弹窗的全局方法
    const openReportDialog = (targetId, targetType = 'COMMENT') => {
        reportTargetId.value = targetId;
        reportTargetType.value = targetType;
        reportVisible.value = true;
    };

    // 关闭举报弹窗
    const closeReportDialog = () => {
        reportVisible.value = false;
        reportTargetId.value = null; // 清空目标，防止数据残留
    };

    return {
        reportVisible,
        reportTargetId,
        reportTargetType,
        openReportDialog,
        closeReportDialog
    };
});