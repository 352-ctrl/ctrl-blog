<template>
  <el-breadcrumb separator="/" class="app-breadcrumb">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in breadcrumbList" :key="item.path">
        <span v-if="index === breadcrumbList.length - 1 || item.redirect === 'noRedirect'" class="no-redirect">
          {{ item.meta.title }}
        </span>
        <a v-else @click.prevent="handleLink(item)" class="redirect">
          {{ item.meta.title }}
        </a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const breadcrumbList = computed(() => {
  // 1. 获取当前路由匹配数组
  let matched = route.matched.filter(item => item.meta && item.meta.title);

  // 2. 判断首个节点是否为 Dashboard
  const first = matched[0];
  if (!isDashboard(first)) {
    // 如果不是，强行在头部加一个 Dashboard
    matched = [{ path: '/admin/dashboard', meta: { title: '仪表盘' } }].concat(matched);
  }

  return matched;
});

// 判断是否是首页（根据我们要之前的路由配置，name 是 AdminDashboard）
const isDashboard = (route) => {
  const name = route && route.name;
  if (!name) return false;
  return name.trim() === 'AdminDashboard';
};

const handleLink = (item) => {
  const { redirect, path } = item;
  if (redirect) {
    router.push(redirect);
    return;
  }
  router.push(path);
};
</script>

<style scoped>
.app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  margin-left: 8px;
}

.no-redirect {
  color: var(--el-text-color-secondary); /* 适配暗黑模式的次要文字颜色 */
  cursor: text;
}

.redirect {
  color: var(--el-text-color-regular); /* 适配暗黑模式的常规文字颜色 */
  font-weight: 600;
  cursor: pointer;
  transition: color 0.3s;
}

.redirect:hover {
  color: var(--el-color-primary); /* 悬浮显示主题色 */
}

/* 动画 */
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter-from,
.breadcrumb-leave-active {
  opacity: 0;
  transform: translateX(20px);
}

.breadcrumb-leave-active {
  position: absolute;
}
</style>