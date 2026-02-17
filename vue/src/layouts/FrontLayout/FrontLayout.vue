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
                :default-openeds="['1']"
                :default-active="route.path"
                @select="handleMenuSelect"
            >
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/categories">分类</el-menu-item>
              <el-menu-item index="/tags">标签</el-menu-item>
              <el-menu-item index="/archive">归档</el-menu-item>
              <el-menu-item v-if="userStore.isAdmin" index="/admin/dashboard">后台入口</el-menu-item>
              <el-menu-item index="/">关于本站</el-menu-item>
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
        <el-dropdown trigger="click">
          <div class="user-avatar-trigger">
            <img v-if="userStore.avatar" class="user-avatar" :src="userStore.avatar" alt="avatar">
            <img v-else class="user-avatar" src="../../assets/images/default-avatar.png" alt="default avatar">
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <template v-if="userStore.userInfo.id">
                <el-dropdown-item disabled class="dropdown-nickname">
                  {{ userStore.nickname }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="router.push('/user/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="userStore.logout()">退出登录</el-dropdown-item>
              </template>
              <template v-else>
                <el-dropdown-item @click="router.push('/login')">立即登录</el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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
            :default-openeds="['1']"
            :default-active="route.path"
            class="top-menu"
            mode="horizontal"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/categories">分类</el-menu-item>
          <el-menu-item index="/tags">标签</el-menu-item>
          <el-menu-item index="/archive">归档</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin/dashboard">后台入口</el-menu-item>
          <el-menu-item index="/">关于本站</el-menu-item>
        </el-menu>
      </div>

      <div class="header-right-tools">
        <SearchModal />
        <el-dropdown trigger="click">
          <div class="user-avatar-trigger">
            <img v-if="userStore.avatar" class="user-avatar" :src="userStore.avatar" alt="avatar">
            <img v-else class="user-avatar" src="../../assets/images/default-avatar.png" alt="default avatar">
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <template v-if="userStore.userInfo.id">
                <el-dropdown-item disabled class="dropdown-nickname">
                  {{ userStore.nickname }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="router.push('/user/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="userStore.logout()">退出登录</el-dropdown-item>
              </template>
              <template v-else>
                <el-dropdown-item @click="router.push('/login')">立即登录</el-dropdown-item>
              </template>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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

    <el-backtop target=".page-scroll-view" :right="50" :bottom="50" />

  </el-container>
</template>

<script setup>
import {computed, ref} from "vue";
import { useUserStore } from '@/store/user.js'
import { useRoute, useRouter } from "vue-router";
import IconHamburger from "@/components/common/Icon/IconHamburger.vue";

// 初始化路由和状态库
const route = useRoute();
const router = useRouter();
const userStore = useUserStore()

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
  flex-shrink: 0; /* 禁止头部被压缩 */
  z-index: 100;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08); /* 增加阴影体现层次感 */

  /* 保持原有布局对齐 */
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
   2. 布局容器样式
   ==================================== */

/* 锁定整个视口，禁止 body 滚动 */
.layout-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 关键：防止双重滚动条 */
}

/* 滚动容器 (el-main) */
.page-scroll-view {
  flex: 1; /* 占据剩余所有高度 */
  overflow-y: auto; /* 关键：滚动条只在这里出现 */
  overflow-x: hidden;
  padding: 0; /* 重置 padding，由内部容器控制 */
  background-color: #f5f7fa;
  scroll-behavior: smooth; /* 平滑滚动 */
  position: relative; /* 确保 backtop 定位正确 */
}

/* 应用主容器：限制最大宽度，Flex布局 */
.app-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 10px; /* 给上下一些间距 */
  width: 100%;
  display: flex;
  box-sizing: border-box;
  min-height: 100%; /* 确保内容少时也能撑开背景 */
}

/* 左侧内容区：自适应宽度 */
.main-content-wrapper {
  flex: 1;
  min-width: 0; /* 防止内容撑破 flex 容器 */
}

/* ====================================
   3. 组件样式 (从 Template 提取)
   ==================================== */

/* 图标按钮通用样式 */
.icon-btn {
  padding: 0;
}

/* 抽屉头部布局 */
.drawer-header-row {
  height: 60px;
  display: flex;
  align-items: center;
  padding-right: 20px;
}

/* 品牌 Logo 样式 */
.brand-logo {
  height: 40px;
  border-radius: 50%;
}

/* 品牌文字样式 */
.brand-text {
  font-weight: bold;
  white-space: nowrap;
}

/* 左侧盒子，给按钮留出空间 */
.header-left-box {
  display: flex;
  align-items: center;
  z-index: 2; /* 确保层级高于居中的 Logo */
}

/* 移动端 Logo 容器绝对居中 */
.mobile-brand-center {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%); /* 核心：向左向上偏移自身的一半 */
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none; /* 防止遮挡两侧按钮的点击事件 */
  z-index: 1;
}

/* 右侧工具栏，确保层级 */
.header-right-tools {
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 用户头像触发器 */
.user-avatar-trigger {
  cursor: pointer;
}

/* 下拉菜单中的昵称项 */
.dropdown-nickname {
  text-align: center;
  cursor: default;
  color: #333;
  font-weight: bold;
}

/* 桌面端 Logo 容器 */
.desktop-logo-wrapper {
  display: flex;
  align-items: center;
  padding-right: 20px;
}

/* 桌面端菜单容器 */
.desktop-menu-wrapper {
  flex: 1;
}

/* ====================================
   4. 响应式适配
   ==================================== */

@media screen and (max-width: 992px) {
  /* 移动端调整顶部内边距 */
  :global(.page-container-md-down) {
    padding-top: 0 !important;
  }

  .app-container {
    padding-top: 10px !important;
  }
}
</style>