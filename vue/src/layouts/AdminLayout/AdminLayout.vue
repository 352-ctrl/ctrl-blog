<template>
  <div>
    <el-container style="display: flex; flex-direction: row">

      <el-drawer v-model="drawer" direction="ltr" :show-close="false" class="hide-md-up" :size="250">
        <template #header>
          <div style="height: 60px; display: flex; align-items: center; padding-right: 20px">
            <img src="../../assets/images/logo.png" style="height: 40px; border-radius: 50%" >
            <span style="font-weight: bold; white-space: nowrap">个人网站</span>
          </div>
        </template>
        <template #default>
          <AdminSidebar :is-collapse="false" :show-logo="false" @click-menu="handleMenuSelect" />
        </template>
      </el-drawer>

      <el-aside class="custom-aside hide-md-down" :style="{ width: isCollapse ? '64px' : '250px' }">
        <AdminSidebar :is-collapse="isCollapse" :show-logo="true" />
      </el-aside>

      <el-container style="flex: 1">
        <el-header class="common-header">
          <div style="display: flex; align-items: center">
            <el-button class="hide-md-down" text @click="toggleCollapse" :icon="isCollapse ? 'Fold' : 'Expand'" style="padding: 0; font-size: 20px"></el-button>
            <el-button class="hide-md-up" text @click="drawer = true" :icon="isCollapse ? 'Fold' : 'Expand'" style="padding: 0; font-size: 20px"></el-button>
            <AdminBreadcrumb />
          </div>
          <div style="flex: 1"></div>
          <div>
            <UserDropdown />
          </div>
        </el-header>

        <el-main class="page-container">
          <RouterView :key="$route.fullPath"/>
        </el-main>

      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {ElMessage} from "element-plus";
import { useUserStore } from '@/store/user.js'
import {useRouter} from "vue-router";

const userStore = useUserStore()
const router = useRouter();
const drawer = ref(false)
const isCollapse = ref(window.innerWidth <= 992);
let lastSmallScreenState = window.innerWidth <= 992; // 记录上次的状态

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
  const currentIsSmall  = width <= 992;
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
:deep(.page-container) {
  margin-top: 0 !important;
}
.custom-aside {
  transition: width 0.2s ease-in-out !important;
  overflow: hidden;
  height: 100vh;
  border-right: 1px solid #dcdfe6; /* 添加右侧边框 */
}

/* 修正菜单层级缩进 */
.el-menu--inline .el-menu-item {
  padding-left: 48px !important;
}

.page-container {
  overflow-y: auto; /* 内容超出时显示滚动条，否则隐藏 */
  background-color: #f5f5f5;
  height: calc(100vh - 60px); /* 容器高度 = 视口高度 - 菜单栏高度（60px），顶部刚好对齐菜单栏底部 */
  margin-top: 60px; /* 容器顶部距离页面顶部60px，和菜单栏高度一致 */
}

@media screen and (max-width: 992px) {
  .common-header {
    /* 移动端减小 Header 的左右内边距 */
    padding: 0 10px;
  }
}
</style>