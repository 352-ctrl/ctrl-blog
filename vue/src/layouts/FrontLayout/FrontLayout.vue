<template>
  <el-container class="layout-container">
    <el-header class="common-header show-md-down hide-md-up">
      <div class="header-left-box">
        <el-button text @click="drawer = true" class="icon-btn">
          <IconHamburger size="24"/>
        </el-button>

        <el-drawer v-model="drawer" direction="ltr" :show-close="false" :size="250">
          <template #header>
            <div class="drawer-header-row">
              <img src="../../assets/images/logo.png" class="brand-logo" alt="logo">
              <span class="brand-text">个人网站</span>
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
                <el-icon><HomeFilled /></el-icon>
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

              <el-sub-menu index="more">
                <template #title>
                  <el-icon><MoreFilled /></el-icon>
                  <span>更多</span>
                </template>
                <el-menu-item v-if="userStore.isAdmin" index="/admin/dashboard">
                  <el-icon><Setting /></el-icon>
                  <span>后台管理</span>
                </el-menu-item>
                <el-menu-item index="/about">
                  <el-icon><InfoFilled /></el-icon>
                  <span>关于本站</span>
                </el-menu-item>
              </el-sub-menu>
            </el-menu>
          </template>
        </el-drawer>
      </div>

      <div class="mobile-brand-center">
        <img src="../../assets/images/logo.png" class="brand-logo" alt="logo">
        <span class="brand-text">个人网站</span>
      </div>

      <div class="header-right-tools">
        <SearchModal />
        <MessageBadge />
        <UserDropdown />
      </div>
    </el-header>

    <el-header class="common-header hide-md-down show-md-up">
      <div class="desktop-logo-wrapper">
        <img src="../../assets/images/logo.png" class="brand-logo" alt="logo">
        <span class="brand-text">个人网站</span>
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
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>

          <el-sub-menu index="explore" popper-class="auto-width-popper">
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

          <el-sub-menu index="more" popper-class="auto-width-popper">
            <template #title>
              <el-icon><MoreFilled /></el-icon>
              <span>更多</span>
            </template>
            <el-menu-item v-if="userStore.isAdmin" index="/admin/dashboard">
              <el-icon><Setting /></el-icon>
              <span>后台管理</span>
            </el-menu-item>
            <el-menu-item index="/about">
              <el-icon><InfoFilled /></el-icon>
              <span>关于本站</span>
            </el-menu-item>
          </el-sub-menu>
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
          <RouterView :key="$route.fullPath"/>
        </div>
        <RightSideBar v-if="isShowSidebar" />
      </div>
    </el-main>

    <div class="theme-toggle-wrapper">
      <el-button
          circle
          @click="toggleDark()"
          :icon="isDark ? 'Sunny' : 'Moon'"
          class="theme-btn custom-float-btn"
      />
    </div>

    <el-backtop target=".page-scroll-view" :right="50" :bottom="50" class="custom-float-btn" />

    <AuthDialog />

  </el-container>
</template>

<script setup>
import { computed, ref } from "vue";
import { useUserStore } from '@/store/user.js'
import { useRoute, useRouter } from "vue-router";
import { useDark, useToggle } from '@vueuse/core'
import AuthDialog from "@/components/front/AuthDialog/AuthDialog.vue";
import IconHamburger from "@/components/common/Icon/IconHamburger.vue";

// 初始化路由和状态库
const route = useRoute();
const router = useRouter();
const userStore = useUserStore()

// 控制移动端抽屉开关
const drawer = ref(false)

const isDark = useDark()
const toggleDark = useToggle(isDark)

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
  box-shadow: var(--el-box-shadow-light);
  transition: background-color 0.3s, box-shadow 0.3s;

  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
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

/* 桌面端水平菜单优化 */
.top-menu {
  border-bottom: none !important; /* 移除底部灰线 */
  background-color: transparent !important; /* 确保背景透明，跟随 header */
  height: 60px;
}

/* 修改桌面端菜单项间距、悬浮和激活态 */
.top-menu :deep(.el-menu-item),
.top-menu :deep(.el-sub-menu__title) {
  font-weight: 500;
  font-size: 15px;
  /* ✨ 核心修改：将默认的 20px 左右内边距缩小到 12px，让菜单更紧凑 */
  padding: 0 12px !important;
  background-color: transparent !important;
  transition: color 0.3s;
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
  scroll-behavior: smooth;
  position: relative;

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
  padding: 20px 10px 0 10px;
  width: 100%;
  display: flex;
  box-sizing: border-box;
  min-height: 100%;
}

.main-content-wrapper {
  flex: 1;
  min-width: 0;
  padding-bottom: 20px;
}

/* ====================================
   4. 组件样式
   ==================================== */

.icon-btn {
  padding: 0;
}

.drawer-header-row {
  height: 60px;
  display: flex;
  align-items: center;
  padding-right: 20px;
}

.brand-logo {
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}

.brand-text {
  font-weight: bold;
  font-size: 18px;
  white-space: nowrap;
  color: var(--el-text-color-primary);
  transition: color 0.3s;
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
  justify-content: center;
  pointer-events: none;
  z-index: 1;
}

.header-right-tools {
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 16px;
}

.desktop-logo-wrapper {
  display: flex;
  align-items: center;
  padding-right: 40px; /* 拉开 logo 和 菜单 的距离 */
}

.desktop-menu-wrapper {
  flex: 1;
  display: flex;
  justify-content: flex-start; /* 菜单靠左对齐，如果想居中可以改成 center */
}

/* ====================================
   5. 悬浮按钮统一设计
   ==================================== */

.theme-toggle-wrapper {
  position: fixed;
  right: 50px;
  bottom: 110px;
  z-index: 99;
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
}

.custom-float-btn:hover,
:deep(.el-backtop.custom-float-btn:hover) {
  transform: translateY(-4px) scale(1.05) !important;
  color: var(--el-color-primary) !important;
  box-shadow: var(--el-box-shadow) !important;
  background-color: var(--el-bg-color-overlay) !important;
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

  .theme-toggle-wrapper {
    right: 20px;
    bottom: 80px;
  }

  :deep(.el-backtop) {
    right: 20px !important;
    bottom: 20px !important;
  }
}

/* 覆盖 el-menu 下拉框默认的 min-width: 200px */
:global(.auto-width-popper .el-menu--popup) {
  min-width: max-content !important;
  /* 可以稍微调整下拉框的上下内边距 */
  padding: 5px 0 !important;
}

/* 确保下拉菜单里的每个 Item 也能自适应内容，并保持舒适的左右留白 */
:global(.auto-width-popper .el-menu-item) {
  min-width: max-content !important;
  padding: 0 20px !important;
}
</style>