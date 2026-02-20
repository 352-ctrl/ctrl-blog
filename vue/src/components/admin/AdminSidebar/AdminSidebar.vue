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
.sidebar-menu {
  border-right: none;
}

.sidebar-logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  overflow: hidden;
}

.logo-img {
  height: 40px;
  border-radius: 50%;
  padding: 0 5px;
}

.logo-text {
  font-weight: bold;
  white-space: nowrap;
  margin-left: 5px;
}

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