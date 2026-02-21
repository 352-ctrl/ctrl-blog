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
              <span class="nickname">{{ userStore.nickname }}</span>
              <span class="role-tag admin-tag" v-if="userStore.isAdmin">管理员</span>
              <span class="role-tag user-tag" v-else>Lv1 普通用户</span>
            </div>
          </el-dropdown-item>

          <el-dropdown-item divided @click="router.push('/user/profile')">
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
      登录 / 注册
    </el-button>
  </div>
</template>

<script setup>
import { useUserStore } from '@/store/user.js';
import { useRouter } from 'vue-router';
import { User, SwitchButton } from '@element-plus/icons-vue';

const userStore = useUserStore();
const router = useRouter();
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
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e3e5e7;
  transition: transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1), border-color 0.3s ease;
}

.user-avatar-trigger:hover .user-avatar {
  transform: scale(1.15) translateY(2px);
  border-color: #c9ccd0;
}

/* ================= 登录按钮样式 (未登录) ================= */
.login-action-btn {
  padding: 8px 14px;
  font-weight: bold;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2); /* 淡淡的蓝色发光阴影，更吸引眼球 */
  transition: all 0.3s ease;
}

.login-action-btn:hover {
  transform: translateY(-2px); /* 鼠标悬浮时微微上浮 */
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.3);
}

/* ================= 下拉菜单全局覆盖 ================= */
.custom-dropdown-menu {
  border-radius: 8px;
  padding: 6px 0;
  box-shadow: 0 8px 24px rgba(18, 18, 18, 0.1);
}

/* 菜单项的基础样式和交互 */
:deep(.el-dropdown-menu__item) {
  padding: 10px 24px;
  font-size: 14px;
  color: #515767;
  transition: background-color 0.2s ease, color 0.2s ease;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background-color: #f2f3f5;
  color: #1e80ff;
}

:deep(.el-dropdown-menu__item .el-icon) {
  margin-right: 10px;
  font-size: 16px;
}

/* ================= 掘金风格：已登录卡片 ================= */
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
  border: 1px solid #e4e6eb;
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
  color: #252933;
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

.admin-tag {
  color: #1e80ff;
  background: rgba(30, 128, 255, 0.1);
}

.user-tag {
  color: #515767;
  background: #f2f3f5;
}
</style>