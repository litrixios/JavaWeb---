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

    // 作者
    {
        path: '/manuscript',
        name: 'Manuscript',
        // 假设你有 Layout 组件，这里需要包裹在 Layout 中
        component: () => import('@/layout/index.vue'),
        meta: { title: '稿件管理' },
        children: [
            {
                path: 'list',
                name: 'ManuscriptList',
                component: () => import('@/views/manuscript/list.vue'),
                meta: { title: '我的稿件' }
            },
            {
                path: 'submit', // 如果是编辑草稿，可以通过 query 传参 ?id=xxx
                name: 'ManuscriptSubmit',
                component: () => import('@/views/manuscript/submit.vue'),
                meta: { title: '在线投稿' }
            },
            {
                path: 'detail/:id',
                name: 'ManuscriptDetail',
                component: () => import('@/views/manuscript/detail.vue'),
                meta: { title: '稿件详情' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router