import { createRouter, createWebHistory } from 'vue-router'
import {ElMessage} from "element-plus";
import {frontRoutes} from "@/router/modules/frontRoutes.js";
import {adminRoutes} from "@/router/modules/adminRoutes.js";
import { reportSiteVisit } from "@/api/front/system/visit.js";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [

    // 1. 后台路由 (优先匹配 /admin)
    adminRoutes,

    // 2. 前台路由 (匹配 / 下的路径)
    frontRoutes,

    // 3. 错误页处理
    {
      path: '/403',
      name: 'Forbidden',
      component: () => import('@/views/error/403.vue'),
      meta: { title: '权限不足' }
    },
    {
      path: '/notFound',
      name: 'NotFound',
      component: () => import('@/views/error/404.vue'),
      meta: { title: '页面不存在' }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/notFound'
    },
  ],
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
      next('/403');
      return;
    }
  }

  // 全部通过
  next();
});

// ================= 全局后置钩子 (主动上报访问量) =================
router.afterEach((to, from) => {
  // 只统计前台页面
  // 排除后台管理系统 (/admin)、错误页 (404, 403)
  const isFrontPage = !to.path.startsWith('/admin')
      && to.name !== 'NotFound'
      && to.name !== 'Forbidden';

  if (isFrontPage) {
    // 异步无阻塞发送上报请求，不管成功失败，绝不能影响用户页面的正常渲染
    reportSiteVisit().catch(err => {
      console.warn('访问量上报失败，', err);
    });
  }
});
export default router