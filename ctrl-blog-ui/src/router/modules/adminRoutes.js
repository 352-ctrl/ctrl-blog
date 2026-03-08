import { h } from 'vue'
import { RouterView } from 'vue-router'

// 空布局：用于渲染子路由的占位符
const BlankLayout = { render: () => h(RouterView) }

/**
 * 后台路由配置
 */
export const adminRoutes = {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
        {
            path: 'dashboard',
            name: 'AdminDashboard',
            component: () => import('@/views/admin/dashboard/Dashboard.vue'),
            meta: { title: '仪表盘', icon: 'Odometer' }
        },
        {
            path: 'content',
            component: BlankLayout,
            meta: { title: '内容管理', icon: 'Folder' },
            redirect: '/admin/content/articles',
            children: [
                {
                    path: 'articles',
                    name: 'AdminArticleList',
                    component: () => import('@/views/admin/content/article/ArticleList.vue'),
                    meta: { title: '文章管理', icon: 'Document' }
                },
                {
                    path: 'categories',
                    name: 'AdminCategoryList',
                    component: () => import('@/views/admin/content/category/CategoryList.vue'),
                    meta: { title: '分类管理', icon: 'Files' }
                },
                {
                    path: 'tags',
                    name: 'AdminTagList',
                    component: () => import('@/views/admin/content/tag/TagList.vue'),
                    meta: { title: '标签管理', icon: 'CollectionTag' }
                },
            ]
        },
        {
            path: 'operation',
            component: BlankLayout,
            meta: { title: '运营管理', icon: 'ChatDotRound' },
            redirect: '/admin/operation/comments',
            children: [
                {
                    path: 'comments',
                    name: 'AdminCommentList',
                    component: () => import('@/views/admin/operation/comment/CommentList.vue'),
                    meta: { title: '评论管理', icon: 'ChatLineRound' }
                },
                {
                    path: 'feedbacks',
                    name: 'AdminFeedbackList',
                    component: () => import('@/views/admin/operation/feedback/FeedbackList.vue'),
                    meta: { title: '意见反馈', icon: 'Service' }
                },
                {
                    path: 'reports',
                    name: 'AdminReportList',
                    component: () => import('@/views/admin/operation/report/ReportList.vue'),
                    meta: { title: '举报管理', icon: 'Warning' }
                },
                {
                    path: 'notices',
                    name: 'AdminNoticeList',
                    component: () => import('@/views/admin/operation/notice/NoticeList.vue'),
                    meta: { title: '公告管理', icon: 'Bell' }
                },
                {
                    path: 'danmakus',
                    name: 'AdminDanmakuList',
                    component: () => import('@/views/admin/operation/danmaku/DanmakuList.vue'),
                    meta: { title: '弹幕管理', icon: 'VideoCamera' }
                }
            ]
        },
        {
            path: 'system',
            component: BlankLayout,
            meta: { title: '系统管理', icon: 'Setting' },
            redirect: '/admin/system/users',
            children: [
                {
                    path: 'users',
                    name: 'AdminUserList',
                    component: () => import('@/views/admin/system/user/UserList.vue'),
                    meta: { title: '用户管理', icon: 'User' }
                },
                {
                    path: 'sensitive-words',
                    name: 'AdminSensitiveWordList',
                    component: () => import('@/views/admin/system/word/SensitiveWordList.vue'),
                    meta: { title: '敏感词管理', icon: 'CircleClose' }
                },
                {
                    path: 'log',
                    name: 'SystemLog',
                    component: BlankLayout,
                    meta: { title: '日志管理', icon: 'Document' },
                    redirect: '/admin/system/log/login',
                    children: [
                        {
                            path: 'login',
                            name: 'AdminLoginLogList',
                            component: () => import('@/views/admin/system/log/loginLog/LoginLogList.vue'),
                            meta: { title: '登录日志', icon: 'Key' }
                        },
                        {
                            path: 'operate',
                            name: 'AdminOperLogList',
                            component: () => import('@/views/admin/system/log/operLog/OperLogList.vue'),
                            meta: { title: '操作日志', icon: 'Operation' }
                        }
                    ]
                }
            ]
        },
        {
            path: 'monitor',
            component: BlankLayout,
            meta: { title: '系统监控', icon: 'Monitor' },
            redirect: '/admin/monitor/jobs',
            children: [
                {
                    path: 'jobs',
                    name: 'AdminJobList',
                    component: () => import('@/views/admin/monitor/job/JobList.vue'),
                    meta: { title: '定时任务', icon: 'Timer' }
                },
                {
                    path: 'server',
                    name: 'AdminServerMonitor',
                    component: () => import('@/views/admin/monitor/server/Server.vue'),
                    meta: { title: '服务监控', icon: 'DataLine' }
                },
                {
                    path: 'redis',
                    name: 'AdminRedisMonitor',
                    component: () => import('@/views/admin/monitor/redis/Redis.vue'),
                    meta: { title: '缓存监控', icon: 'Coin' }
                }
            ]
        },
        {
            path: 'profile',
            name: 'AdminProfile',
            component: () => import('@/views/admin/profile/AdminProfile.vue'),
            meta: { title: '个人中心', hideSidebar: true }
        },
    ]
}