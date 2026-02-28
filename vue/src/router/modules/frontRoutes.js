/**
 * 前台路由配置
 */
export const frontRoutes = {
    path: '/',
    component: () => import('@/layouts/FrontLayout/FrontLayout.vue'),
    children: [
        {
            path: '',
            name: 'FrontHome',
            component: () => import('@/views/front/Home.vue'),
            meta: { title: '主页' }
        },
        {
            path: 'archive',
            name: 'ArticleArchive',
            component: () => import('@/views/front/ArchiveList.vue'),
            meta: { title: '文章归档' }
        },
        {
            path: 'categories',
            name: 'FrontCategories',
            component: () => import('@/views/front/CategoryList.vue'),
            meta: { title: '全部分类' }
        },
        {
            path: 'tags',
            name: 'FrontTags',
            component: () => import('@/views/front/TagList.vue'),
            meta: { title: '标签墙' }
        },
        {
            path: 'post/:id',
            name: 'FrontArticleDetail',
            component: () => import('@/views/front/ArticleDetail.vue'),
            meta: { title: '文章详情' }
        },
        {
            path: 'user',
            component: () => import('@/layouts/UserLayout/UserLayout.vue'),
            redirect: '/user/dashboard',
            meta: { requiresAuth: true, hideSidebar: true },
            children: [
                { path: 'dashboard', component: () => import('@/views/front/user/UserDashboard.vue') },
                { path: 'collections', component: () => import('@/views/front/user/Collections.vue') },
                { path: 'likes', component: () => import('@/views/front/user/Likes.vue') },
                { path: 'comments', component: () => import('@/views/front/user/Comments.vue') },
                { path: 'settings', component: () => import('@/views/front/user/Settings.vue') },
            ]
        },
        {
            path: 'user/message',
            name: 'FrontUserMessage',
            component: () => import('@/views/front/Message.vue'),
            meta: { hideSidebar: true, title: '消息中心', requiresAuth: true }
        },
    ]
}