<template>
  <div class="admin-layout">
    <el-container class="layout-wrapper">

      <el-drawer v-model="drawer" direction="ltr" :show-close="false" class="hide-md-up" :size="250">
        <template #header>
          <div class="drawer-header">
            <AppLogo/>
          </div>
        </template>
        <template #default>
          <AdminSidebar :is-collapse="false" :show-logo="false" @click-menu="handleMenuSelect" />
        </template>
      </el-drawer>

      <el-aside class="custom-aside hide-md-down" :class="{ 'is-collapsed': isCollapse }">
        <div class="aside-logo-container">
          <AppLogo :is-collapse="isCollapse" />
        </div>

        <el-scrollbar class="aside-menu-scrollbar">
          <AdminSidebar :is-collapse="isCollapse" :show-logo="false" />
        </el-scrollbar>
      </el-aside>

      <el-container class="main-container">

        <el-header class="common-header">
          <div class="header-left">
            <el-button class="hide-md-down action-btn" text @click="toggleCollapse" :icon="isCollapse ? 'Expand' : 'Fold'"></el-button>
            <el-button class="hide-md-up action-btn" text @click="drawer = true" icon="Expand"></el-button>

            <div class="breadcrumb-nav">
              <AdminBreadcrumb />
            </div>
          </div>

          <div class="header-right">
            <el-button
                class="action-btn theme-toggle-btn"
                text
                @click="toggleDark()"
                :icon="isDark ? 'Sunny' : 'Moon'"
            ></el-button>

            <UserDropdown />
          </div>
        </el-header>

        <el-main class="page-container">
          <RouterView :key="router.fullPath"/>
        </el-main>

      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { useUserStore } from '@/store/user.js'
import { useRouter } from "vue-router";
import { useDark, useToggle } from '@vueuse/core';
import AdminBreadcrumb from "./components/AdminBreadcrumb.vue"
import AdminSidebar from "./components/AdminSidebar.vue"

const userStore = useUserStore()
const router = useRouter();
const drawer = ref(false)
const isCollapse = ref(window.innerWidth <= 992);
let lastSmallScreenState = window.innerWidth <= 992;

const isDark = useDark();
const toggleDark = useToggle(isDark);

onMounted(() => {
  checkScreenSize();
  window.addEventListener('resize', checkScreenSize);
});

const handleMenuSelect = () => {
  drawer.value = false
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const checkScreenSize = () => {
  const width = window.innerWidth;
  const currentIsSmall = width <= 992;
  const isMobile = width <= 768;

  if (currentIsSmall !== lastSmallScreenState) {
    isCollapse.value = currentIsSmall;
    lastSmallScreenState = currentIsSmall;
  }

  if (!isMobile && drawer.value) {
    drawer.value = false;
  }
};

const logout = () => {
  userStore.logout();
}
</script>

<style scoped>
/* ====================================
   全局外层容器布局
   ==================================== */
.admin-layout {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.layout-wrapper {
  height: 100%;
  display: flex;
}

/* ====================================
   左侧侧边栏 (Aside)
   ==================================== */
.custom-aside {
  width: 250px; /* 默认展开宽度 */
  transition: width 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important;
  overflow: hidden;
  height: 100vh;
  background-color: var(--el-bg-color-overlay);
  border-right: 1px solid var(--el-border-color-lighter);
}

/* 折叠状态的宽度控制 */
.custom-aside.is-collapsed {
  width: 64px;
}

/* 抽屉(Drawer)头部样式 */
.drawer-header {
  height: 60px;
  display: flex;
  align-items: center;
  padding-right: 20px;
}

/* ====================================
   右侧区域 (Container & Header)
   ==================================== */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column; /* 让头部和主体上下自动排布 */
  overflow: hidden;
}

.common-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  background-color: var(--el-bg-color-overlay);
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 0 20px;
  flex-shrink: 0; /* 防止头部被压缩 */
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.breadcrumb-nav {
  margin-left: 15px;
  display: flex;
  align-items: center;
}

/* ====================================
   通用操作按钮样式 (包括折叠和暗黑切换)
   ==================================== */
.action-btn {
  padding: 0;
  font-size: 24px;
  color: var(--el-text-color-regular);
  transition: color 0.3s;
  background-color: transparent !important;
  border: none !important;
}

.action-btn:hover {
  color: var(--el-color-primary); /* 悬浮时显示品牌主题色 */
  background-color: transparent !important;
}

/* ====================================
   下方内容容器 (Main)
   ==================================== */
.page-container {
  flex: 1;
  overflow-y: auto; /* 内容超出时显示滚动条 */
  background-color: var(--el-bg-color-page);
  padding: 15px;
  margin: 0 !important;
}

/* ====================================
   媒体查询与深度覆盖 (:deep)
   ==================================== */
@media screen and (max-width: 992px) {
  .common-header {
    padding: 0 10px; /* 移动端减小 Header 的左右内边距 */
  }

  .header-right {
    transform: scale(0.85);
    transform-origin: right center;
    gap: 10px; /* 缩小间距 */
  }

  .action-btn {
    font-size: 20px;
  }
}

@media screen and (max-width: 768px) {
  .breadcrumb-nav {
    display: none;
  }
}

:deep(.el-drawer) {
  background-color: var(--el-bg-color-overlay) !important;
}

:deep(.el-drawer__body) {
  padding: 0 !important;
}

:deep(.el-drawer__header) {
  margin-bottom: 0 !important;
  padding: 0 10px !important;
  border-bottom: none !important;
  height: auto !important;
  min-height: 0 !important;
}

:deep(.el-menu--inline .el-menu-item) {
  padding-left: 48px !important;
}

/* ====================================
   侧边栏结构分离的专属容器
   ==================================== */
.aside-logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--el-border-color-lighter);
  overflow: hidden;
  white-space: nowrap;
}

.aside-menu-scrollbar {
  height: calc(100vh - 60px);
}

.aside-menu-scrollbar :deep(.el-scrollbar__bar) {
  display: none !important;
}
</style>