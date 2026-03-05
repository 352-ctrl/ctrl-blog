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

      <el-aside class="custom-aside hide-md-down" :style="{ width: isCollapse ? '64px' : '250px' }">
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
import { useDark, useToggle } from '@vueuse/core'; // 引入暗黑模式 API

const userStore = useUserStore()
const router = useRouter();
const drawer = ref(false)
const isCollapse = ref(window.innerWidth <= 992);
let lastSmallScreenState = window.innerWidth <= 992; // 记录上次的状态

// 实例化暗黑模式切换逻辑
const isDark = useDark();
const toggleDark = useToggle(isDark);

onMounted(() => {
  // 初始检测
  checkScreenSize();
  // 监听窗口大小变化
  window.addEventListener('resize', checkScreenSize);
});

// 菜单选择事件处理
const handleMenuSelect = () => {
  drawer.value = false // 关闭 drawer
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 检测屏幕宽度并更新状态
const checkScreenSize = () => {
  const width = window.innerWidth;
  const currentIsSmall = width <= 992;
  const isMobile = width <= 768;

  // 只有当小屏幕状态发生改变时（跨越992px阈值）才自动调整侧边栏
  if (currentIsSmall !== lastSmallScreenState) {
    isCollapse.value = currentIsSmall; // 小于等于992px隐藏，大于992px展开
    lastSmallScreenState = currentIsSmall; // 更新记录的状态
  }

  // 当屏幕宽度大于768px时，自动关闭抽屉
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
  transition: width 0.3s cubic-bezier(0.25, 0.8, 0.25, 1) !important; /* 让侧边栏收缩更丝滑 */
  overflow: hidden;
  height: 100vh;
  background-color: var(--el-bg-color-overlay);
  border-right: 1px solid var(--el-border-color-lighter); /* 使用较淡的边框色 */
}

/* 抽屉(Drawer)头部样式 */
.drawer-header {
  height: 60px;
  display: flex;
  align-items: center;
  padding-right: 20px;
}

.logo-img {
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
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
  border-bottom: 1px solid var(--el-border-color-lighter); /* 较淡的底部分割线 */
  padding: 0 20px;
  flex-shrink: 0; /* 防止头部被压缩 */
}

.header-left, .header-right {
  display: flex;
  align-items: center;
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
  color: var(--el-text-color-regular); /* 使用较柔和的图标颜色 */
  transition: color 0.3s;
  background-color: transparent !important;
  border: none !important;
}

.action-btn:hover {
  color: var(--el-color-primary); /* 悬浮时显示品牌主题色 */
  background-color: transparent !important;
}

.theme-toggle-btn {
  margin-right: 15px; /* 距离右侧头像下拉框一定的间距 */
}

/* ====================================
   下方内容容器 (Main)
   ==================================== */
.page-container {
  flex: 1;
  overflow-y: auto; /* 内容超出时显示滚动条 */
  background-color: var(--el-bg-color-page);
  padding: 15px; /* 可以给页面统一加内边距，根据需要调整 */
  margin: 0 !important;
}

/* ====================================
   媒体查询与深度覆盖 (:deep)
   ==================================== */
@media screen and (max-width: 992px) {
  .common-header {
    padding: 0 10px; /* 移动端减小 Header 的左右内边距 */
  }
}

/* 覆盖 Element Plus 抽屉组件的默认背景和边距 */
:deep(.el-drawer) {
  /* 确保抽屉背景色也和侧边栏一样淡一点 */
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

/* 修正菜单层级缩进 */
:deep(.el-menu--inline .el-menu-item) {
  padding-left: 48px !important;
}

/* ====================================
   侧边栏结构分离的专属容器
   ==================================== */
.aside-logo-container {
  height: 60px; /* 与 Header 高度保持一致 */
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