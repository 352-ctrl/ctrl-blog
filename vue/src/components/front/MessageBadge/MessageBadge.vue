<template>
  <div class="message-badge-wrapper" @click="goToMessage">
    <el-badge
        :value="userStore.unreadCount"
        :hidden="userStore.unreadCount <= 0"
        :show-zero="false"
        :max="99"
        class="custom-badge"
    >
      <el-icon class="bell-icon"><Bell /></el-icon>
    </el-badge>
  </div>
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
   外层包裹容器优化
   ==================================== */
.message-badge-wrapper {
  position: relative;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.message-badge-wrapper:hover {
  background-color: var(--el-fill-color-light);
  transform: translateY(-2px);
}

.message-badge-wrapper:active {
  transform: scale(0.92);
}

/* ====================================
   图标样式
   ==================================== */
.bell-icon {
  font-size: 24px;
  color: var(--el-text-color-regular);
  transition: color 0.3s ease;
}

.message-badge-wrapper:hover .bell-icon {
  color: var(--el-color-primary);
}

/* ====================================
   小红点精确微调
   ==================================== */
:deep(.custom-badge .el-badge__content.is-fixed) {
  top: 6px;
  right: 8px;
  transform: translateY(-50%) translateX(50%) scale(0.85);
  border: none;
  box-shadow: 0 2px 4px var(--el-color-danger-light-5);
}
</style>