<template>
  <el-badge
      :value="userStore.unreadCount"
      :hidden="userStore.unreadCount <= 0"
      :show-zero="false"
      :max="99"
      class="custom-badge"
      @click="goToMessage"
  >
    <el-icon class="bell-icon"><Bell /></el-icon>
  </el-badge>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user.js'

const router = useRouter()
const userStore = useUserStore()

const goToMessage = () => {
  router.push('/user/message')
}

onMounted(() => {
  userStore.startUnreadPolling()

  // 监听兄弟组件(消息页面)发来的已读事件，立刻通知 store 刷新未读数
  window.addEventListener('update-unread-count', userStore.fetchUnreadCount)
})

onUnmounted(() => {
  window.removeEventListener('update-unread-count', userStore.fetchUnreadCount)
})
</script>

<style scoped>
/* ====================================
   徽标容器
   ==================================== */
.custom-badge {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* ====================================
   图标基础样式
   ==================================== */
.bell-icon {
  font-size: 24px;
  color: var(--el-text-color-regular);
  transition: color 0.3s ease;
}

/* ✨ 鼠标悬浮在组件上时，图标变色 */
.custom-badge:hover .bell-icon {
  color: var(--el-color-primary);
}

/* ====================================
   小红点精确微调 (相对于图标自身)
   ==================================== */
:deep(.el-badge__content.is-fixed) {
  top: 2px;
  right: 4px;
  transform: translateY(-50%) translateX(50%) scale(0.85);
  border: none;
  box-shadow: 0 2px 4px var(--el-color-danger-light-5);
}
</style>