<template>
  <el-container class="layout-container">
    <el-header class="common-header show-md-down hide-md-up mobile-header">
      <div class="header-left-box">
        <el-button text @click="drawer = true" class="icon-btn">
          <IconHamburger size="24"/>
        </el-button>

        <el-drawer
            v-model="drawer"
            direction="ltr"
            :show-close="false"
            :size="220"
            append-to-body
        >
          <template #header>
            <div class="drawer-header-row">
              <AppLogo/>
            </div>
          </template>
          <template #default>
            <el-menu
                router
                :default-active="route.path"
                @select="handleMenuSelect"
                class="mobile-menu"
            >
              <el-menu-item index="/">
                <el-icon><House /></el-icon>
                <span>首页</span>
              </el-menu-item>

              <el-sub-menu index="explore">
                <template #title>
                  <el-icon><Compass /></el-icon>
                  <span>探索</span>
                </template>
                <el-menu-item index="/categories">
                  <el-icon><Folder /></el-icon>
                  <span>分类</span>
                </el-menu-item>
                <el-menu-item index="/tags">
                  <el-icon><CollectionTag /></el-icon>
                  <span>标签</span>
                </el-menu-item>
                <el-menu-item index="/archive">
                  <el-icon><Box /></el-icon>
                  <span>归档</span>
                </el-menu-item>
              </el-sub-menu>

              <el-menu-item index="/board">
                <el-icon><ChatDotRound /></el-icon>
                <span>留言板</span>
              </el-menu-item>

              <el-menu-item index="/about">
                <el-icon><Warning /></el-icon>
                <span>关于我</span>
              </el-menu-item>
            </el-menu>
          </template>
        </el-drawer>
      </div>

      <div class="mobile-brand-center">
        <AppLogo/>
      </div>

      <div class="header-right-tools mobile-right-tools">
        <SearchModal />
        <MessageBadge />
        <UserDropdown />
      </div>
    </el-header>

    <el-header class="common-header hide-md-down show-md-up">
      <div class="desktop-logo-wrapper">
        <AppLogo/>
      </div>

      <div class="desktop-menu-wrapper">
        <el-menu
            router
            :default-active="route.path"
            class="top-menu"
            mode="horizontal"
            :ellipsis="false"
        >
          <el-menu-item index="/">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-sub-menu index="explore" popper-class="auto-width-popper">
            <template #title>
              <el-icon><Guide /></el-icon>
              <span>探索</span>
            </template>
            <el-menu-item index="/categories">
              <el-icon><FolderOpened /></el-icon>
              <span>分类</span>
            </el-menu-item>
            <el-menu-item index="/tags">
              <el-icon><PriceTag /></el-icon>
              <span>标签</span>
            </el-menu-item>
            <el-menu-item index="/archive">
              <el-icon><TakeawayBox /></el-icon>
              <span>归档</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/board">
            <el-icon><ChatDotRound /></el-icon>
            <span>留言板</span>
          </el-menu-item>

          <el-menu-item index="/about">
            <el-icon><Warning /></el-icon>
            <span>关于我</span>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="header-right-tools">
        <SearchModal />
        <MessageBadge />
        <UserDropdown />
      </div>
    </el-header>

    <el-main class="page-scroll-view page-container-md-down">
      <div class="app-container">
        <div class="main-content-wrapper">
          <RouterView :key="route.fullPath"/>
        </div>
        <RightSideBar v-if="isShowSidebar" />
      </div>
      <FrontFooter />
    </el-main>

    <div class="feedback-toggle-wrapper custom-float-btn">
      <FeedbackDialog />
    </div>

    <div class="theme-toggle-wrapper custom-float-btn">
      <ThemeToggle />
    </div>

    <el-backtop target=".page-scroll-view" :right="50" :bottom="50" class="custom-float-btn">
      <el-icon><Top /></el-icon>
    </el-backtop>

    <AuthDialog />
    <ReportDialog />

  </el-container>
</template>

<script setup>
import { computed, ref } from "vue";
import { useUserStore } from '@/store/user.js'
import { useRoute, useRouter } from "vue-router";
import { useDark, useToggle } from '@vueuse/core'
import AuthDialog from "./components/AuthDialog/AuthDialog.vue";
import ReportDialog from "../../components/front/ReportDialog.vue";
import RightSideBar from "./components/RightSidebar/RightSideBar.vue";
import FeedbackDialog from "./components/FeedbackDialog.vue";
import MessageBadge from "./components/MessageBadge.vue";
import SearchModal from "./components/SearchModal.vue";
import ThemeToggle from "./components/ThemeToggle.vue";
import IconHamburger from "@/components/common/Icon/IconHamburger.vue";
import FrontFooter from "@/layouts/FrontLayout/components/FrontFooter.vue";

// 初始化路由和状态库
const route = useRoute();

// 控制移动端抽屉开关
const drawer = ref(false)

/**
 * 处理移动端菜单点击事件
 * 点击菜单项后自动关闭抽屉
 */
const handleMenuSelect = () => {
  drawer.value = false // 关闭 drawer
}

/**
 * 计算属性：是否显示右侧侧边栏
 * 根据路由 meta 中的 hideSidebar 字段判断
 */
const isShowSidebar = computed(() => !route.meta.hideSidebar);

</script>

<style scoped>
/* ====================================
   1. 样式穿透 (Element Plus Overrides)
   ==================================== */

/* 头部固定定位，层级提升 */
:deep(.common-header) {
  flex-shrink: 0;
  z-index: 100;
  background-color: var(--el-bg-color);
  box-shadow: none !important;
  border-bottom: 1px solid var(--el-border-color-light);

  transition: background-color 0.3s, border-color 0.3s;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
}

html.dark :deep(.common-header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

:deep(.mobile-header) {
  padding: 0 14px !important;
}

/* 重置抽屉 Body 内边距 */
:deep(.el-drawer__body) {
  padding: 0 !important;
}

/* 重置抽屉 Header 样式 */
:deep(.el-drawer__header) {
  margin-bottom: 0 !important;
  padding: 0 10px !important;
  border-bottom: none !important;
  height: auto !important;
  min-height: 0 !important;
}

/* ====================================
   2. 菜单专属样式优化
   ==================================== */

/* 1. 普通菜单项：保持你想要的紧凑间距 */
.top-menu :deep(.el-menu-item) {
  font-weight: 500;
  font-size: 15px;
  padding: 0 12px !important;
  background-color: transparent !important;
  transition: color 0.3s;
}

/* 2. 子菜单标题：左侧保持 12px(不改变整体间距)，右侧稍微加大一点给箭头腾出空间 */
.top-menu :deep(.el-sub-menu__title) {
  font-weight: 500;
  font-size: 15px;
  padding-left: 12px !important;
  padding-right: 26px !important; /* 专为箭头预留的位置 */
  background-color: transparent !important;
  transition: color 0.3s;
}

/* 3. 把默认在 right: 20px 的箭头往右侧边缘移动 */
.top-menu :deep(.el-sub-menu__icon-arrow) {
  right: 8px !important;
}

.top-menu :deep(.el-menu-item:hover),
.top-menu :deep(.el-sub-menu__title:hover) {
  color: var(--el-color-primary) !important;
}

/* 移动端垂直菜单优化 */
.mobile-menu {
  border-right: none !important; /* 移除右侧灰线 */
  background-color: transparent !important;
}

.mobile-menu :deep(.el-menu-item .el-icon),
.mobile-menu :deep(.el-sub-menu__title .el-icon) {
  font-size: 16px !important;
  margin-right: 8px !important;
}

.mobile-menu :deep(.el-menu-item),
.mobile-menu :deep(.el-sub-menu__title) {
  font-weight: 500;
  font-size: 15px;
  border-radius: 0 20px 20px 0; /* 右侧圆角，看起来更现代 */
  margin-right: 15px; /* 右侧留白 */
  margin-bottom: 4px;
}

/* 移动端菜单激活状态 */
.mobile-menu :deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: bold;
}

/* 针对暗黑模式修正移动端选中菜单的底色 */
html.dark .mobile-menu :deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary-light-8);
}

/* ====================================
   3. 布局容器样式
   ==================================== */

.layout-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-scroll-view {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 0;
  position: relative;

  display: flex;
  flex-direction: column;

  /* 基础底色 */
  background-color: var(--el-bg-color-page);

  background-image:
      linear-gradient(var(--el-border-color-light) 1px, transparent 1px),
      linear-gradient(90deg, var(--el-border-color-light) 1px, transparent 1px);
  /* 控制网格格子的大小 */
  background-size: 32px 32px;

  transition: background-color 0.3s ease, background-image 0.3s ease;
}

/* 🌙 暗黑模式下的网格 */
html.dark .page-scroll-view {
  background-color: #252529;

  background-image:
      linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px);
}

.app-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 10px 40px 10px;
  width: 100%;
  display: flex;
  box-sizing: border-box;
  flex: 1 0 auto;
}

.main-content-wrapper {
  flex: 1;
  min-width: 0;
}

/* ====================================
   4. 组件组件与 Header 样式
   ==================================== */

.icon-btn {
  padding: 0;
}

.drawer-header-row {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-left-box {
  display: flex;
  align-items: center;
  z-index: 2;
}

.mobile-brand-center {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  gap: 8px;
  pointer-events: none;
  z-index: 1;
  white-space: nowrap;
}

.mobile-brand-center .brand-logo {
  height: 28px;
  width: 28px;
  margin: 0;
}

.mobile-brand-center .brand-text {
  font-size: 16px; /* 移动端字体稍微缩小 */
}

.header-right-tools {
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 移动端右侧工具栏优化 */
.mobile-right-tools {
  gap: 14px;
  transform: scale(0.85);
  transform-origin: right center;
}

.desktop-logo-wrapper {
  display: flex;
  align-items: center;
  padding-right: 20px;
}

.desktop-menu-wrapper {
  flex: 1;
  display: flex;
  justify-content: flex-start;
}

/* ====================================
   5. 悬浮按钮统一设计
   ==================================== */

.feedback-toggle-wrapper {
  position: fixed;
  right: 50px;
  bottom: 170px;
  z-index: 99;
  padding: 0;
}

.theme-toggle-wrapper {
  position: fixed;
  right: 50px;
  bottom: 110px;
  z-index: 99;
  padding: 0;
}

.custom-float-btn,
:deep(.el-backtop.custom-float-btn) {
  width: 44px !important;
  height: 44px !important;
  border-radius: 50% !important;
  background-color: var(--el-bg-color-overlay) !important;
  border: 1px solid var(--el-border-color-light) !important;
  color: var(--el-text-color-regular) !important;
  box-shadow: var(--el-box-shadow-light) !important;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px !important;
  cursor: pointer;
  z-index: 99 !important;
}

.custom-float-btn:hover,
:deep(.el-backtop.custom-float-btn:hover) {
  transform: translateY(-4px) scale(1.05) !important;
  color: var(--el-color-primary) !important;
  box-shadow: var(--el-box-shadow) !important;
  background-color: var(--el-bg-color-overlay) !important;
}

:deep(.custom-float-btn .el-button) {
  width: 100%;
  height: 100%;
  border: none;
  background: transparent;
  color: inherit;
  font-size: inherit;
  margin: 0;
  padding: 0;
}

/* ====================================
   6. 响应式适配
   ==================================== */

@media screen and (max-width: 992px) {
  :global(.page-container-md-down) {
    padding-top: 0 !important;
  }

  .app-container {
    padding-top: 10px !important;
  }

  .feedback-toggle-wrapper {
    right: 20px;
    bottom: calc(200px + env(safe-area-inset-bottom));
  }

  .theme-toggle-wrapper {
    right: 20px;
    bottom: calc(140px + env(safe-area-inset-bottom));
  }

  :deep(.el-backtop) {
    right: 20px !important;
    bottom: calc(80px + env(safe-area-inset-bottom)) !important;
  }
}

/* 覆盖 el-menu 下拉框默认的 min-width: 200px */
:global(.auto-width-popper .el-menu--popup) {
  min-width: max-content !important;
  padding: 5px 0 !important;
}

/* 确保下拉菜单里的每个 Item 也能自适应内容，并保持舒适的左右留白 */
:global(.auto-width-popper .el-menu-item) {
  min-width: max-content !important;
  padding: 0 20px !important;
}
</style>