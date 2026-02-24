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
      <div class="auth-header-nav">
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
            v-else
            @success="closeDialog"
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
.auth-header-nav {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30px;
  margin-top: 10px;
}

.nav-item {
  font-size: 18px;
  color: var(--el-text-color-secondary); /* 使用文字次要颜色，自适应暗黑模式 */
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
  color: var(--el-color-primary); /* 使用主题色 */
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
  background-color: var(--el-color-primary); /* 使用主题色 */
  border-radius: 2px;
}

.nav-divider {
  width: 1px;
  height: 16px;
  background-color: var(--el-border-color-lighter); /* 使用边框颜色 */
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
/* 针对弹窗顶层类名进行样式覆盖 (不加 scoped) */
.auth-dialog-custom {
  border-radius: 12px;
  overflow: hidden;
  /* 阴影使用 Element Plus 变量适配暗黑模式 */
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
  /* 使用 Element 的填充背景色变量，亮色下是浅灰，暗色下是深灰 */
  background-color: var(--el-fill-color-light);
  box-shadow: none;
  border-radius: 8px;
  padding: 2px 15px;
  transition: all 0.3s ease;
}

.auth-dialog-custom .el-input__wrapper.is-focus {
  /* 聚焦时使用纯背景色 */
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
  /* 按钮阴影使用含透明度的主题色 */
  box-shadow: 0 4px 12px var(--el-color-primary-light-5);
  transition: all 0.3s ease;
}

.auth-dialog-custom .submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px var(--el-color-primary-light-3);
}
</style>