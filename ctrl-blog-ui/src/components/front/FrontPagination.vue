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
  margin-top: 20px;
  display: flex;
  justify-content: center; /* 核心：居中对齐 */
  width: 100%;
}

/* ==========================================
 * 前台专属分页样式定制 (完美适配明暗模式)
 * ========================================== */

/* 隐藏默认背景，重置按钮基础样式 */
:deep(.front-pagination.is-background .el-pager li),
:deep(.front-pagination.is-background .btn-prev),
:deep(.front-pagination.is-background .btn-next) {
  background-color: transparent !important; /* 透明背景 */
  border: none !important;
  color: var(--el-text-color-regular) !important; /* 替换 #666，自动适配明暗的常规文本色 */
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
  /* 使用 Element Plus 原生极淡主题色变量，暗黑模式下会自动转换为深邃的微光背景 */
  background-color: var(--el-color-primary-light-9) !important;
}

/* 激活(当前页)的样式 */
:deep(.front-pagination.is-background .el-pager li.is-active) {
  background-color: var(--el-color-primary) !important;
  color: var(--el-color-white) !important; /* 替换 #fff */
  /* 使用原生阴影颜色变量，并带有主题色倾向 */
  box-shadow: 0 4px 10px var(--el-color-primary-light-5) !important;
  transform: translateY(-2px); /* 轻微上浮 */
}

/* 针对暗黑模式单独优化激活时的弥散阴影，避免刺眼 */
html.dark :deep(.front-pagination.is-background .el-pager li.is-active) {
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.4) !important;
}

/* 禁用状态的箭头 */
:deep(.front-pagination.is-background .btn-prev:disabled),
:deep(.front-pagination.is-background .btn-next:disabled) {
  color: var(--el-text-color-disabled) !important; /* 替换 #c0c4cc */
  background-color: transparent !important;
}
</style>