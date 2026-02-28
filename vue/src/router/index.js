import { createRouter, createWebHistory } from 'vue-router'
import {ElMessage} from "element-plus";
import {frontRoutes} from "@/router/modules/frontRoutes.js";
import {adminRoutes} from "@/router/modules/adminRoutes.js";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [

    // 1. 后台路由 (优先匹配 /admin)
    adminRoutes,

    // 2. 前台路由 (匹配 / 下的路径)
    frontRoutes,

    // 3. 错误页处理
    {
      path: '/notFound',
      name: 'NotFound',
      component: () => import('@/views/error/404.vue'),
      meta: { title: '页面不存在' }
    },
    // 捕获所有未匹配路径，跳转到 404
    {
      path: '/:pathMatch(.*)*',
      redirect: '/notFound'
    },
  ],
  // 切换路由时滚动条回到顶部
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 全局路由守卫：权限校验（适配父路由meta继承）
router.beforeEach((to, from, next) => {
  // 获取 token
  const token = localStorage.getItem('token');

  const getUserRole = () => {
    try {
      return JSON.parse(localStorage.getItem('userInfo') || '{}').role;
    } catch {
      return '';
    }
  };

  // 不需要权限的页面直接放行
  if (!to.matched.some(record => record.meta.requiresAuth)) {
    next();
    return;
  }

  // 需要权限，但没 Token -> 去登录
  if (!token) {
    ElMessage.warning('请先登录');
    next('/login');
    return;
  }

  // 有 Token，校验角色
  const role = getUserRole();
  if (to.meta.role) {
    // 兼容 meta.role 可能是字符串或数组
    const allowedRoles = Array.isArray(to.meta.role) ? to.meta.role : [to.meta.role];

    // 核心修改：如果当前角色不在允许列表中，且当前角色也不是 SUPER_ADMIN，才拦截
    if (!allowedRoles.includes(role) && role !== 'SUPER_ADMIN') {
      ElMessage.error('无权访问该页面');
      next('/');
      return;
    }
  }

  // 全部通过
  next();
});
export default router