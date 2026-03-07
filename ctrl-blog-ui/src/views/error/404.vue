<template>
  <div class="not-found-container">
    <el-result
        icon="warning"
        title="404"
        sub-title="抱歉，您访问的页面就像断了线的风筝，找不到了~"
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
.not-found-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-bg-color-page); /* 完美适配暗黑模式底色 */
}

/* ====================================
   按钮与图标细节样式
   ==================================== */
/* 提取出来的图标边距样式，告别行内样式 */
.btn-icon {
  margin-right: 5px;
}

/* ====================================
   文本排版与暗黑模式适配覆盖 (:deep)
   ==================================== */
/* 让 404 标题更大一点，并适配主文本颜色 */
:deep(.el-result__title p) {
  font-size: 48px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

/* 显式声明副标题颜色，确保暗黑模式下足够柔和清晰 */
:deep(.el-result__subtitle p) {
  font-size: 16px;
  color: var(--el-text-color-secondary);
}
</style>