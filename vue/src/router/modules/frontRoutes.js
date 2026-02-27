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
            path: 'user/profile',
            name: 'FrontUserProfile',
            component: () => import('@/views/profile/Profile.vue'),
            meta: { hideSidebar: true, title: '个人中心', requiresAuth: true }
        },
        {
            path: 'user/message',
            name: 'FrontUserMessage',
            component: () => import('@/views/front/Message.vue'),
            meta: { hideSidebar: true, title: '消息中心', requiresAuth: true }
        },
    ]
}