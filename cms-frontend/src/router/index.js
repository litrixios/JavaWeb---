import { createRouter, createWebHistory } from 'vue-router'
// 确保 Layout 路径正确，如果报错找不到文件，请检查 src/layout/index.vue 是否存在
import Layout from '@/layout/index.vue'
import Router from '@/layout/eic.vue'
const routes = [
    // 1. 登录页
    {
        path: '/login',
        component: () => import('@/views/login/index.vue'),
        hidden: true
    }, // <--- 注意这里要有逗号
    //注册页
    {
        path: '/register',
        name: 'register',
        component: () => import('@/views/login/register.vue')
    },
    // 2. 首页 (Dashboard)
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '工作台', icon: 'Dashboard' }
            }
        ]
    }, // <--- 注意这里要有逗号

    // 3. 作者核心功能
    {
        path: '/manuscript',
        component: Layout,
        redirect: '/manuscript/list',
        meta: {title: '稿件管理', icon: 'Document'},
        children: [
            {
                path: 'submit',
                name: 'SubmitManuscript',
                component: () => import('@/views/manuscript/submit.vue'),
                meta: {title: '在线投稿'}
            }, // <--- 注意这里要有逗号
            {
                path: 'list',
                name: 'MyManuscripts',
                component: () => import('@/views/manuscript/list.vue'),
                meta: {title: '我的稿件'}
            }, // <--- 注意这里要有逗号
            {
                path: 'detail/:id',
                name: 'ManuscriptDetail',
                component: () => import('@/views/manuscript/detail.vue'),
                meta: {title: '稿件详情', hidden: true}
            }
        ]
    },






    //编辑部管理员
    {
        path: '/editorial-admin',
        name: 'EditorialAdmin',
        component: () => import('@/layout/AdminLayout.vue'),
        meta: { role: 'EDITORIAL_ADMIN' },
        redirect: '/editorial-admin/tech-check',  // 添加默认重定向
        children: [
            // 形式审查路由
            {
                path: 'tech-check',
                name: 'TechCheckList',
                component: () => import('@/views/editorial-admin/tech-check/list.vue')
            },
            {
                path: 'tech-check/detail',
                name: 'TechCheckDetail',
                component: () => import('@/views/editorial-admin/tech-check/detail.vue')
            },
            // 新闻管理路由
            {
                path: 'news',
                name: 'NewsList',
                component: () => import('@/views/editorial-admin/news/list.vue')
            },
            {
                path: 'news/add',
                name: 'AddNews',
                component: () => import('@/views/editorial-admin/news/form.vue')
            },
            {
                path: 'news/edit',
                name: 'EditNews',
                component: () => import('@/views/editorial-admin/news/form.vue')
            }
        ]
    },
    // ================== 新增：主编功能模块 ==================
    {
        path: '/eic',
        component: Router,
        redirect: '/eic/reviewer',
        meta: { title: '主编工作台', icon: 'UserFilled' }, // 需确保引入了 UserFilled 图标，或者换成 'User'
        children: [
            {
                path: 'reviewer',
                name: 'ReviewerManager',
                // 对应下面第三步创建的文件
                component: () => import('@/views/eic/ReviewerManager.vue'),
                meta: { title: '审稿人管理' }
            },
            {
                path: 'audit',
                name: 'ManuscriptAudit',
                // 暂时预留，你可以之后再创建这个文件
                component: () => import('@/views/eic/ManuscriptAudit.vue'),
                meta: { title: '稿件全览与决策' }
            }
        ]
    }


]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router