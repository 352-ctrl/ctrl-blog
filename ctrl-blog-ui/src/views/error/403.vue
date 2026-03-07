<template>
  <div class="forbidden-container">
    <el-result
        icon="error"
        title="403"
        sub-title="抱歉，您没有权限访问该页面或执行此操作~"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">
          <el-icon class="btn-icon"><HomeFilled /></el-icon>
          返回主页
        </el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
/* ====================================
   全局外层容器布局
   ==================================== */
.forbidden-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-bg-color-page); /* 适配暗黑模式底色 */
}

/* ====================================
   按钮与图标细节样式
   ==================================== */
.btn-icon {
  margin-right: 5px;
}

/* ====================================
   文本排版与暗黑模式适配覆盖 (:deep)
   ==================================== */
:deep(.el-result__title p) {
  font-size: 48px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

:deep(.el-result__subtitle p) {
  font-size: 16px;
  color: var(--el-text-color-secondary);
}
</style>