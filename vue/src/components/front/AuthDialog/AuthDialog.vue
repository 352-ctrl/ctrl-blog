<template>
  <el-dialog
      v-model="dialogVisible"
      width="400px"
      destroy-on-close
      center
      class="auth-dialog-custom"
      :show-close="true"
  >
    <template #header>
      <div class="auth-header-nav" v-if="mode === 'login' || mode === 'register'">
        <span
            :class="['nav-item', { active: mode === 'login' }]"
            @click="mode = 'login'"
        >登 录</span>
        <span class="nav-divider"></span>
        <span
            :class="['nav-item', { active: mode === 'register' }]"
            @click="mode = 'register'"
        >注 册</span>
      </div>
    </template>

    <div class="auth-body">
      <transition name="fade-slide" mode="out-in">
        <LoginForm
            v-if="mode === 'login'"
            @success="closeDialog"
        />

        <RegisterForm
            v-else-if="mode === 'register'"
            @success="closeDialog"
        />

        <ForgotPwdForm
            v-else-if="mode === 'forgotPwd'"
            @success="mode = 'login'"
            @switch-mode="(m) => mode = m"
        />
      </transition>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue';
import { useUserStore } from '@/store/user.js';
import LoginForm from './LoginForm.vue';
import RegisterForm from './RegisterForm.vue';
import ForgotPwdForm from './ForgotPwdForm.vue'; // 引入找回密码组件

const userStore = useUserStore();

const dialogVisible = computed({
  get: () => userStore.showAuthDialog,
  set: (val) => { userStore.showAuthDialog = val; }
});

const mode = computed({
  get: () => userStore.authDialogMode,
  set: (val) => { userStore.authDialogMode = val; }
});

const closeDialog = () => {
  dialogVisible.value = false;
};
</script>

<style scoped>
/* 原有的 scoped 样式保持不变 */
.auth-header-nav {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30px;
  margin-top: 10px;
}

.nav-item {
  font-size: 18px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  position: relative;
  padding-bottom: 6px;
}

.nav-item:hover {
  color: var(--el-color-primary-light-3);
}

.nav-item.active {
  color: var(--el-color-primary);
  font-weight: 600;
  font-size: 20px;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 16px;
  height: 3px;
  background-color: var(--el-color-primary);
  border-radius: 2px;
}

.nav-divider {
  width: 1px;
  height: 16px;
  background-color: var(--el-border-color-lighter);
  border-radius: 1px;
}

.auth-body {
  position: relative;
  min-height: 200px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
</style>

<style>
/* 原有的全局覆盖样式保持不变 */
.auth-dialog-custom {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--el-box-shadow-dark);
}

.auth-dialog-custom .el-dialog__header {
  padding-top: 25px;
  padding-bottom: 0;
  margin-right: 0;
}

.auth-dialog-custom .el-dialog__body {
  padding: 30px 40px 25px 40px;
}

.auth-dialog-custom .el-dialog__headerbtn {
  top: 15px;
  right: 15px;
}

.auth-dialog-custom .el-input__wrapper {
  background-color: var(--el-fill-color-light);
  box-shadow: none;
  border-radius: 8px;
  padding: 2px 15px;
  transition: all 0.3s ease;
}

.auth-dialog-custom .el-input__wrapper.is-focus {
  background-color: var(--el-bg-color);
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

.auth-dialog-custom .submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 4px;
  box-shadow: 0 4px 12px var(--el-color-primary-light-5);
  transition: all 0.3s ease;
}

.auth-dialog-custom .submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px var(--el-color-primary-light-3);
}
</style>