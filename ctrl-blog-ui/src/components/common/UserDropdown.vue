<template>
  <div class="user-action-container">
    <el-dropdown v-if="userStore.userInfo?.id" trigger="hover" class="user-dropdown" :hide-timeout="100">
      <div class="user-avatar-trigger">
        <img class="user-avatar" :src="userStore.avatar" alt="avatar">
      </div>

      <template #dropdown>
        <el-dropdown-menu class="custom-dropdown-menu">
          <el-dropdown-item disabled class="header-card">
            <img class="card-avatar" :src="userStore.avatar" alt="avatar">
            <div class="card-info">
              <el-tooltip
                  :content="userStore.nickname"
                  placement="top"
                  effect="dark"
                  :show-after="300"
                  :disabled="(userStore.nickname || '').length <= 8"
              >
                <span class="nickname">{{ userStore.nickname }}</span>
              </el-tooltip>
              <span class="role-tag super-admin-tag" v-if="userStore.userInfo?.role === 'SUPER_ADMIN'">超级管理员</span>
              <span class="role-tag admin-tag" v-else-if="userStore.userInfo?.role === 'ADMIN'">管理员</span>
              <span class="role-tag user-tag" v-else>普通用户</span>
            </div>
          </el-dropdown-item>

          <el-dropdown-item
              divided
              v-if="userStore.isAdmin && !isAdminRoute"
              @click="router.push('/admin')"
          >
            <el-icon><Monitor /></el-icon>管理后台
          </el-dropdown-item>

          <el-dropdown-item
              divided
              v-if="userStore.isAdmin && isAdminRoute"
              @click="router.push('/')"
          >
            <el-icon><House /></el-icon>博客首页
          </el-dropdown-item>

          <el-dropdown-item divided @click="goToProfile">
            <el-icon><User /></el-icon>个人中心
          </el-dropdown-item>
          <el-dropdown-item @click="userStore.logout()">
            <el-icon><SwitchButton /></el-icon>退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

    <el-button
        v-else
        type="primary"
        round
        class="login-action-btn"
        @click="userStore.openAuthDialog('login')"
    >
      <span class="text-long">登录 / 注册</span>
      <span class="text-short">登录</span>
    </el-button>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/store/user.js';
import { useRouter, useRoute } from 'vue-router';

const userStore = useUserStore();
const router = useRouter();
const route = useRoute();

const isAdminRoute = computed(() => route.path.startsWith('/admin'));

const goToProfile = () => {
  // 判断当前页面是否在后台环境
  if (isAdminRoute.value || route.path.startsWith('/profile')) {
    router.push({ name: 'AdminProfile' }); // 进入后台个人中心
  } else {
    router.push('/user'); // 进入前台个人中心
  }
};
</script>

<style scoped>
/* 容器，保证内部元素垂直居中 */
.user-action-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ================= 头像触发区样式 (已登录) ================= */
.user-avatar-trigger {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2px;
  border-radius: 50%;
  outline: none;
}

:deep(.el-tooltip__trigger) {
  outline: none !important;
}

.user-avatar {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--el-border-color); /* 适配暗黑模式的边框 */
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1), border-color 0.3s ease;
}

.user-avatar-trigger:hover .user-avatar {
  transform: scale(1.15) translateY(2px);
  border-color: var(--el-border-color-darker);
}

/* ================= 登录按钮样式 (未登录) ================= */
.login-action-btn {
  padding: 8px 14px;
  font-weight: bold;
  letter-spacing: 0.5px;
  /* 阴影使用带有透明度的主题色变量 */
  box-shadow: 0 4px 12px var(--el-color-primary-light-5);
  transition: all 0.3s ease;
}

.login-action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px var(--el-color-primary-light-3);
}

/* ================= 下拉菜单全局覆盖 ================= */
.custom-dropdown-menu {
  border-radius: 8px;
  padding: 6px 0;
  box-shadow: var(--el-box-shadow-light); /* Element Plus 官方浅色/深色自适应阴影 */
}

/* 菜单项的基础样式和交互 */
:deep(.el-dropdown-menu__item) {
  padding: 10px 24px;
  font-size: 14px;
  color: var(--el-text-color-regular); /* 自适应文字颜色 */
  transition: background-color 0.2s ease, color 0.2s ease;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background-color: var(--el-fill-color-light); /* 悬浮背景色 */
  color: var(--el-color-primary); /* 悬浮文字颜色 */
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 10px;
  font-size: 16px;
}

:deep(.el-dropdown-menu__item.is-disabled.header-card) {
  cursor: default !important;
}

/* ================= 已登录卡片 ================= */
.header-card {
  cursor: default !important;
  background-color: transparent !important;
  padding: 16px 24px 12px 24px !important;
  display: flex;
  align-items: center;
  opacity: 1 !important;
}

.card-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid var(--el-border-color-light);
  margin-right: 16px;
}

.card-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  gap: 6px;
}

.nickname {
  color: var(--el-text-color-primary); /* 主要文字颜色，亮色下黑，暗色下白 */
  font-size: 16px;
  font-weight: 600;
  max-width: 130px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.role-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  line-height: 1.2;
  font-weight: 500;
}

.super-admin-tag {
  color: var(--el-color-danger);
  background: var(--el-color-danger-light-9);
}

.admin-tag {
  color: var(--el-color-primary); /* 主题色文字 */
  background: var(--el-color-primary-light-9); /* 主题色的极浅背景，Element Plus在暗黑模式下会自动把 light-9 转换为深色背景 */
}

.user-tag {
  color: var(--el-text-color-secondary); /* 次级文字颜色 */
  background: var(--el-fill-color); /* 填充背景色，亮色浅灰，暗色深灰 */
}
/* ================= 移动端按钮响应式优化 ================= */
.text-short {
  display: none; /* 默认隐藏短文本 */
}

@media screen and (max-width: 768px) {
  .text-long {
    display: none; /* 小屏幕下隐藏长文本 */
  }
  .text-short {
    display: inline; /* 小屏幕下显示短文本 */
  }
  .login-action-btn {
    padding: 6px 12px; /* 减小按钮的内边距 */
    font-size: 13px;   /* 稍微减小字体大小 */
  }
}
</style>