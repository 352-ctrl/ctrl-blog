<template>
  <div class="front-pagination-container" v-if="total > 0">
    <el-pagination
        class="front-pagination"
        background
        :current-page="currentPage"
        :page-size="pageSize"
        layout="prev, pager, next"
        :total="total"
        @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
const props = defineProps({
  currentPage: { type: Number, required: true, default: 1 },
  pageSize: { type: Number, required: true, default: 10 },
  total: { type: Number, required: true, default: 0 }
})

const emit = defineEmits(['update:currentPage', 'change'])

const handleCurrentChange = (newPage) => {
  emit('update:currentPage', newPage)
  emit('change', newPage)
}
</script>

<style scoped>
/* 居中容器 */
.front-pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center; /* 核心：居中对齐 */
  width: 100%;
}

/* ==========================================
 * 前台专属分页样式定制
 * ========================================== */

/* 隐藏默认背景，重置按钮基础样式 */
:deep(.front-pagination.is-background .el-pager li),
:deep(.front-pagination.is-background .btn-prev),
:deep(.front-pagination.is-background .btn-next) {
  background-color: transparent !important; /* 透明背景 */
  border: none !important;
  color: #666 !important;
  font-weight: 500;
  border-radius: 10px !important;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  transition: all 0.3s ease;
}

/* 悬浮时的样式 */
:deep(.front-pagination.is-background .el-pager li:not(.is-disabled):hover),
:deep(.front-pagination.is-background .btn-prev:not(.is-disabled):hover),
:deep(.front-pagination.is-background .btn-next:not(.is-disabled):hover) {
  color: var(--el-color-primary) !important;
  background-color: #f0f5ff !important; /* 极淡的主题色背景 */
}

/* 激活(当前页)的样式 */
:deep(.front-pagination.is-background .el-pager li.is-active) {
  background-color: var(--el-color-primary) !important;
  color: #fff !important;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3) !important; /* 弥散阴影 */
  transform: translateY(-2px); /* 轻微上浮 */
}

/* 禁用状态的箭头 */
:deep(.front-pagination.is-background .btn-prev:disabled),
:deep(.front-pagination.is-background .btn-next:disabled) {
  color: #c0c4cc !important;
  background-color: transparent !important;
}
</style>