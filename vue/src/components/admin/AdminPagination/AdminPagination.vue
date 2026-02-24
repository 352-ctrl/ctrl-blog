<template>
  <div class="pagination-container" v-if="total > 0">
    <el-pagination
        class="custom-pagination"
        background
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @update:current-page="handleCurrentPageChange"
        @update:page-size="handlePageSizeChange"
        @change="handleChange"
    />
  </div>
</template>

<script setup>
/**
 * =========================================================================
 * Props 定义
 * =========================================================================
 */
const props = defineProps({
  currentPage: { type: Number, required: true, default: 1 },
  pageSize: { type: Number, required: true, default: 10 },
  total: { type: Number, required: true, default: 0 }
})

/**
 * =========================================================================
 * Events 定义
 * =========================================================================
 */
const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change'])

/**
 * =========================================================================
 * 交互逻辑
 * =========================================================================
 */
const handleCurrentPageChange = (newPage) => {
  emit('update:currentPage', newPage)
}

const handlePageSizeChange = (newSize) => {
  emit('update:pageSize', newSize)
}

const handleChange = () => {
  emit('change')
}
</script>

<style scoped>
/* ==========================================
 * 基础容器样式
 * ========================================== */
.pagination-container {
  margin-top: 24px;
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  width: 100%;
  box-sizing: border-box;
  overflow-x: auto;
  border-radius: 8px;     /* 可选：容器圆角 */
}

/* 隐藏默认滚动条 */
.pagination-container::-webkit-scrollbar {
  display: none;
}

/* ==========================================
 * 现代化按钮样式优化 (UI/UX 增强，完美适配暗黑)
 * ========================================== */

/* 文字颜色自适应（总条数、跳页等辅助文字） */
:deep(.custom-pagination .el-pagination__total),
:deep(.custom-pagination .el-pagination__jump) {
  color: var(--el-text-color-regular);
}

/* 统一按钮圆角、过渡动画与自适应背景 */
:deep(.custom-pagination.is-background .el-pager li),
:deep(.custom-pagination.is-background .btn-prev),
:deep(.custom-pagination.is-background .btn-next) {
  border-radius: 6px !important; /* 现代感圆角 */
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important; /* 平滑动效 */
  background-color: var(--el-fill-color-light) !important; /* 柔和的填充色，亮色浅灰/暗色深灰 */
  color: var(--el-text-color-regular) !important; /* 字体颜色适配暗黑 */
  border: 1px solid transparent;
}

/* 悬浮状态 (Hover) */
:deep(.custom-pagination.is-background .el-pager li:not(.is-disabled):not(.is-active):hover),
:deep(.custom-pagination.is-background .btn-prev:not(.is-disabled):hover),
:deep(.custom-pagination.is-background .btn-next:not(.is-disabled):hover) {
  color: var(--el-color-primary) !important;
  background-color: var(--el-color-primary-light-9) !important; /* Element 会自动反转暗色系下的 light-9 */
}

/* 激活状态 (Active) */
:deep(.custom-pagination.is-background .el-pager li.is-active) {
  background-color: var(--el-color-primary) !important;
  color: var(--el-color-white) !important; /* 保持白色，因为背景已经是实心主题色 */
  font-weight: 600;
  box-shadow: 0 3px 8px var(--el-color-primary-light-5) !important; /* 动态发光投影 */
  transform: translateY(-1px); /* 选中项微微上浮 */
}

/* 下拉框和输入框的圆角统一 */
:deep(.custom-pagination .el-select .el-input__wrapper),
:deep(.custom-pagination .el-pagination__editor.el-input .el-input__wrapper) {
  border-radius: 6px;
}

/* ==========================================
 * 响应式屏幕适配
 * ========================================== */

@media screen and (max-width: 992px) {
  .pagination-container {
    justify-content: center;
    padding: 12px 0;
  }
}

@media screen and (max-width: 768px) {
  :deep(.custom-pagination .el-pagination__jump),
  :deep(.custom-pagination .el-pagination__sizes) {
    display: none !important;
  }
}

@media screen and (max-width: 480px) {
  :deep(.custom-pagination .el-pagination__total) {
    display: none !important;
  }

  /* 进一步优化超小屏幕下的按钮尺寸 */
  :deep(.custom-pagination.is-background .el-pager li),
  :deep(.custom-pagination.is-background .btn-prev),
  :deep(.custom-pagination.is-background .btn-next) {
    margin: 0 3px !important;
    min-width: 32px !important;
    height: 32px !important;
    line-height: 32px !important;
    border-radius: 4px !important; /* 小屏幕适当减小圆角 */
  }
}
</style>