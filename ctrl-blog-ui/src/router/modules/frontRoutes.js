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
            component: () => import('@/views/front/home/Home.vue'),
            meta: { title: '主页' }
        },
        {
            path: 'archive',
            name: 'ArticleArchive',
            component: () => import('@/views/front/article/ArchiveList.vue'),
            meta: { title: '文章归档' }
        },
        {
            path: 'categories',
            name: 'FrontCategories',
            component: () => import('@/views/front/article/CategoryList.vue'),
            meta: { title: '全部分类' }
        },
        {
            path: 'tags',
            name: 'FrontTags',
            component: () => import('@/views/front/article/TagList.vue'),
            meta: { title: '标签墙' }
        },
        {
            path: 'board',
            name: 'MessageBoard',
            component: () => import('@/views/front/board/DanmakuBoard.vue'),
            meta: { title: '留言板', hideSidebar: true }
        },
        {
            path: 'post/:id',
            name: 'FrontArticleDetail',
            component: () => import('@/views/front/article/ArticleDetail.vue'),
            meta: { title: '文章详情' }
        },
        {
            path: 'about',
            name: 'FrontAbout',
            component: () => import('@/views/front/about/About.vue'),
            meta: { title: '关于我', hideSidebar: true }
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
            component: () => import('@/views/front/message/Message.vue'),
            meta: { hideSidebar: true, title: '消息中心', requiresAuth: true }
        },
    ]
}