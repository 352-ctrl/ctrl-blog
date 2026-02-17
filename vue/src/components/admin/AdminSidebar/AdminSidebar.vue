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

        <el-menu-item
            v-for="child in item.children"
            :key="child.path"
            :index="child.path"
        >
          <el-icon v-if="child.meta?.icon"><component :is="child.meta.icon" /></el-icon>
          {{ child.meta?.title }}
        </el-menu-item>
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

// 这里的 emit 用于手机端点击后关闭抽屉
const emit = defineEmits(['click-menu'])

const router = useRouter()
const route = useRoute()

// 获取当前激活菜单
const currentPath = computed(() => route.path)

// 实现处理函数，触发事件通知父组件
const handleSelect = () => {
  emit('click-menu')
}

// 递归生成带有“绝对路径”的菜单树
const menuRoutes = computed(() => {
  const adminRoute = router.options.routes.find(r => r.path === '/admin')
  if (!adminRoute || !adminRoute.children) return []

  // 基础路径
  const basePath = '/admin'

  return adminRoute.children
      .filter(item => !item.meta?.hideSidebar) // 过滤隐藏项
      .map(item => {
        // 计算一级菜单的绝对路径 (例如 dashboard -> /admin/dashboard)
        const itemPath = resolve(basePath, item.path)

        // 如果有子菜单，递归处理子菜单的路径
        let children = null
        if (item.children) {
          children = item.children.map(child => ({
            ...child,
            // 计算二级菜单的绝对路径 (例如 articles -> /admin/content/articles)
            path: resolve(itemPath, child.path)
          }))
        }

        return {
          ...item,
          path: itemPath, // 覆盖为绝对路径
          children
        }
      })
})

// 辅助函数：路径拼接 (处理 /admin + dashboard = /admin/dashboard)
const resolve = (base, path) => {
  if (path.startsWith('/')) return path // 已经是绝对路径
  // 移除 base 结尾的 / 和 path 开头的 /，然后拼接
  const cleanBase = base.replace(/\/$/, '')
  const cleanPath = path.replace(/^\//, '')
  return `${cleanBase}/${cleanPath}`
}
</script>

<style scoped>
/* 保持你原有的样式效果 */
.sidebar-menu {
  border-right: none;
}

.sidebar-logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  /* 避免菜单折叠时文字溢出 */
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

/* 修正菜单层级缩进 (从你的代码中提取) */
:deep(.el-menu-item) {
  /* 注意：el-sub-menu 下的 item 默认会有缩进，这里是你的自定义调整 */
}
/* 如果你想针对二级菜单强制缩进，可以保留你原来的 CSS */
:deep(.el-menu--inline .el-menu-item) {
  padding-left: 48px !important;
}
</style>