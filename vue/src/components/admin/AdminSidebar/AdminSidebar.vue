<template>
  <el-menu
      :default-active="currentPath"
      :collapse="isCollapse"
      :unique-opened="true"
      router
      class="sidebar-menu"
      @select="handleSelect"
  >
    <div v-if="showLogo" class="sidebar-logo-container">
      <img src="../../../assets/images/logo.png" class="logo-img" alt="logo">
      <span v-if="!isCollapse" class="logo-text">个人网站</span>
    </div>

    <template v-for="item in menuRoutes" :key="item.path">

      <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.path">
        <template #title>
          <el-icon v-if="item.meta?.icon">
            <component :is="item.meta.icon" />
          </el-icon>
          <span>{{ item.meta?.title }}</span>
        </template>

        <template v-for="child in item.children" :key="child.path">
          <el-sub-menu v-if="child.children && child.children.length > 0" :index="child.path">
            <template #title>
              <el-icon v-if="child.meta?.icon"><component :is="child.meta.icon" /></el-icon>
              <span>{{ child.meta?.title }}</span>
            </template>
            <el-menu-item
                v-for="grandChild in child.children"
                :key="grandChild.path"
                :index="grandChild.path"
            >
              <el-icon v-if="grandChild.meta?.icon"><component :is="grandChild.meta.icon" /></el-icon>
              {{ grandChild.meta?.title }}
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item v-else :index="child.path">
            <el-icon v-if="child.meta?.icon"><component :is="child.meta.icon" /></el-icon>
            {{ child.meta?.title }}
          </el-menu-item>
        </template>
      </el-sub-menu>

      <el-menu-item v-else :index="item.path">
        <el-icon v-if="item.meta?.icon">
          <component :is="item.meta.icon" />
        </el-icon>
        <span>{{ item.meta?.title }}</span>
      </el-menu-item>

    </template>

    <el-menu-item index="/">
      <el-icon><Guide /></el-icon>
      <span>官网</span>
    </el-menu-item>

  </el-menu>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const props = defineProps({
  isCollapse: {
    type: Boolean,
    default: false
  },
  showLogo: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click-menu'])
const router = useRouter()
const route = useRoute()

const currentPath = computed(() => route.path)

const handleSelect = () => {
  emit('click-menu')
}

// 辅助函数：路径拼接
const resolve = (base, path) => {
  if (path.startsWith('/')) return path
  const cleanBase = base.replace(/\/$/, '')
  const cleanPath = path.replace(/^\//, '')
  return `${cleanBase}/${cleanPath}`
}

// 核心修改：将扁平的循环改为递归函数，这样支持无限极菜单的路径解析
const buildMenuTree = (routes, basePath) => {
  return routes
      .filter(item => !item.meta?.hideSidebar) // 过滤隐藏项
      .map(item => {
        const currentPath = resolve(basePath, item.path)
        const newItem = { ...item, path: currentPath }

        // 如果有子节点，递归调用解析子节点
        if (item.children && item.children.length > 0) {
          newItem.children = buildMenuTree(item.children, currentPath)
        } else {
          newItem.children = null // 清除空数组，防止 v-if 误判
        }
        return newItem
      })
}

// 获取并生成菜单树
const menuRoutes = computed(() => {
  const adminRoute = router.options.routes.find(r => r.path === '/admin')
  if (!adminRoute || !adminRoute.children) return []
  return buildMenuTree(adminRoute.children, '/admin')
})
</script>

<style scoped>
/* ====================================
   侧边栏整体与 Logo 区域
   ==================================== */
.sidebar-menu {
  border-right: none;
  background-color: transparent; /* 背景透明，跟随父容器 Aside 的颜色 */
}

.sidebar-logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  overflow: hidden;
  border-bottom: 1px solid var(--el-border-color-lighter); /* 增加极淡的底部分割线 */
  background-color: transparent;
}

.logo-img {
  height: 32px; /* 稍微缩小一点 Logo 让其看起来更精致 */
  border-radius: 50%;
  padding: 0 5px;
}

.logo-text {
  font-weight: 600;
  font-size: 16px;
  white-space: nowrap;
  margin-left: 5px;
  color: var(--el-text-color-primary); /* 适配暗黑模式字体颜色 */
}

/* ====================================
   菜单项悬浮与激活态
   ==================================== */
:deep(.el-menu),
:deep(.el-menu--inline) {
  background-color: transparent !important;
}

/* 常规文本颜色适配 */
:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: var(--el-text-color-regular);
}

/* 悬浮态：极淡的背景色填充 */
:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  color: var(--el-color-primary) !important;
  background-color: var(--el-fill-color-light) !important;
}

/* 激活态：品牌色浅色背景 + 左侧边框高亮 */
:deep(.el-menu-item.is-active) {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  font-weight: bold;
  border-left: 3px solid var(--el-color-primary); /* 左边框高亮条 */
}

/* 针对暗黑模式修正激活态底色 */
html.dark :deep(.el-menu-item.is-active) {
  background-color: var(--el-color-primary-light-8);
}

/* 抵消掉由于左侧增加边框导致的内边距偏移 */
:deep(.el-menu-item) {
  border-left: 3px solid transparent;
}

/* ====================================
   子菜单层级缩进
   ==================================== */
/* 1. 二级菜单统一缩进（同时囊括“无子菜单的项”和“有子菜单的标题”） */
:deep(.el-menu--inline .el-menu-item),
:deep(.el-menu--inline .el-sub-menu__title) {
  padding-left: 48px !important;
}

/* 2. 三级菜单单独再往右缩进，形成明显的层级落差 */
:deep(.el-menu--inline .el-menu--inline .el-menu-item) {
  padding-left: 64px !important;
}
</style>