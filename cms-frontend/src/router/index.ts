import { createRouter, createWebHistory } from 'vue-router'
// 确保 Layout 路径正确，如果报错找不到文件，请检查 src/layout/index.vue 是否存在
import Layout from '@/layout/index.vue'

const routes = [
    // 1. 登录页
    {
        path: '/login',
        component: () => import('@/views/login/index.vue'),
        hidden: true
    }, // <--- 注意这里要有逗号

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
        meta: { title: '稿件管理', icon: 'Document' },
        children: [
            {
                path: 'submit',
                name: 'SubmitManuscript',
                component: () => import('@/views/manuscript/submit.vue'),
                meta: { title: '在线投稿' }
            }, // <--- 注意这里要有逗号
            {
                path: 'list',
                name: 'MyManuscripts',
                component: () => import('@/views/manuscript/list.vue'),
                meta: { title: '我的稿件' }
            }, // <--- 注意这里要有逗号
            {
                path: 'detail',
                name: 'ManuscriptDetail',
                component: () => import('@/views/manuscript/detail.vue'),
                meta: { title: '稿件详情', hidden: true }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router