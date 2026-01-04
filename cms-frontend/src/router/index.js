import { createRouter, createWebHistory } from 'vue-router'
// 确保 Layout 路径正确
import Layout from '@/layout/index.vue'
import Editorsidebar from '@/layout/EditorSIdebar.vue'
import Layout2 from '@/layout/admin_index.vue'
import Layout3 from '@/layout/systemadminSidebar.vue'
import EicLayout from '@/layout/eic.vue'
import EditorialAdminSidebar from '@/layout/editorial-adminSidebar.vue'

// === 1. 定义通用的消息中心组件 (复用核心) ===
const MessageCenter = () => import('@/views/message/index.vue')

const routes = [
    // === 登录/注册 ===
    {
        path: '/login',
        component: () => import('@/views/login/index.vue'),
        hidden: true
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('@/views/login/register.vue')
    },

    // ================== 主编 (EIC) ==================
    {
        path: '/eic',
        component: EicLayout,
        redirect: '/eic/reviewer',
        meta: { title: '主编工作台', icon: 'UserFilled' },
        children: [
            {
                path: 'reviewer',
                name: 'ReviewerManager',
                component: () => import('@/views/eic/ReviewerManager.vue'),
                meta: { title: '审稿人管理' }
            },
            {
                path: 'audit',
                name: 'ManuscriptAudit',
                component: () => import('@/views/eic/ManuscriptAudit.vue'),
                meta: { title: '稿件全览与决策' }
            },
            // [主编] 消息中心 -> /eic/message
            {
                path: 'message',
                name: 'EicMessage',
                component: MessageCenter,
                meta: { title: '消息中心' }
            }
        ]
    },

    // ================== 首页 / 工作台 (通用 Layout) ==================
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
    },

    // ================== 超级管理员 (SuperAdmin) ==================
    {
        path: '/SuperAdmin',
        component: Layout2,
        children: [
            {
                path: 'superadmin',
                name: 'Superadmin',
                component: () => import('@/views/SuperAdmin/superadmin.vue'),
                meta: { title: '系统管理', icon: 'Setting', roles: ['SuperAdmin'] }
            },
            {
                path: 'superadmin_management',
                name: 'Superadmin_management',
                component: () => import('@/views/SuperAdmin/management.vue'),
                meta: { title: '系统管理', icon: 'Setting1', roles: ['SuperAdmin'] }
            },
            // [超级管理员] 消息中心 -> /SuperAdmin/message
            {
                path: 'message',
                name: 'SuperAdminMessage',
                component: MessageCenter,
                meta: { title: '消息中心' }
            }
        ]
    },

    // ================== 系统管理员 (SystemAdmin) ==================
    {
        path: '/Systemadmin',
        component: Layout3,
        children: [
            {
                path: 'systemadmin',
                name: 'Systemadmin',
                component: () => import('@/views/Systemadmin/systemadmin.vue'),
                meta: { title: '系统管理', icon: 'Setting', roles: ['SystemAdmin'] }
            },
            {
                path: 'systemadmin_management',
                name: 'Systemadmin_management',
                component: () => import('@/views/Systemadmin/system_management.vue'),
                meta: { title: '系统管理', icon: 'Setting', roles: ['SystemAdmin'] }
            },
            // [系统管理员] 消息中心 -> /Systemadmin/message
            {
                path: 'message',
                name: 'SystemAdminMessage',
                component: MessageCenter,
                meta: { title: '消息中心' }
            }
        ]
    },

    // ================== 作者核心功能 (Layout) ==================
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
            },
            {
                path: 'list',
                name: 'MyManuscripts',
                component: () => import('@/views/manuscript/list.vue'),
                meta: { title: '我的稿件' }
            },
            {
                path: 'detail/:id',
                name: 'ManuscriptDetail',
                component: () => import('@/views/manuscript/detail.vue'),
                meta: { title: '稿件详情', hidden: true }
            },
            // === 变动点：将作者的消息中心移到这里 ===
            // [作者] 消息中心 -> /manuscript/message
            {
                path: 'message',
                name: 'AuthorMessage',
                component: MessageCenter,
                meta: { title: '消息中心' }
            }
        ]
    },

    // ================== 编辑 (Editor) ==================
    {
        path: '/editor',
        component: Editorsidebar,
        meta: { title: '稿件处理', icon: 'Edit', role: 'Editor' },
        children: [
            {
                path: 'my-manuscripts',
                name: 'EditorManuscripts',
                component: () => import('@/views/editor/list.vue'),
                meta: { title: '我负责的稿件' }
            },
            {
                path: 'process/:id',
                name: 'ProcessManuscript',
                component: () => import('@/views/editor/process.vue'),
                meta: { title: '稿件处理详情', hidden: true }
            },
            {
                path: 'monitoring',
                name: 'Monitoring',
                component: () => import('@/views/editor/Monitoring.vue'),
                meta: { title: '审稿监控', roles: ['Editor'] }
            },
            {
                path: 'tracking',
                name: 'Tracking',
                component: () => import('@/views/editor/ReviewTracking.vue'),
                meta: { title: '审稿进度', roles: ['Editor'] }
            },
            // [编辑] 消息中心 -> /editor/message
            {
                path: 'message',
                name: 'EditorMessage',
                component: MessageCenter,
                meta: { title: '消息中心', roles: ['Editor'] }
            }
        ]
    },

    // ================== 审稿人 (Reviewer) ==================
    {
        path: '/reviewer',
        component: ReviewerSidebar,
        redirect: '/reviewer/dashboard',
        meta: { title: '审稿工作台', icon: 'Edit', roles: ['Reviewer'] },
        children: [
            {
                path: 'dashboard',
                name: 'ReviewerDashboard',
                component: () => import('@/views/reviewer/dashboard.vue'),
                meta: { title: '我的审稿任务' }
            },
            {
                path: 'process/:reviewId',
                name: 'ReviewProcess',
                component: () => import('@/views/reviewer/process.vue'),
                meta: { title: '审稿详情', hidden: true }
            },
            // [审稿人] 消息中心 -> /reviewer/message
            {
                path: 'message',
                name: 'ReviewerMessage',
                component: MessageCenter,
                meta: { title: '消息中心' }
            }
        ]
    },

    // ================== 编辑部管理员 (Editorial Admin) ==================
    {
        path: '/editorial-admin',
        name: 'EditorialAdmin',
        component: EditorialAdminSidebar,
        meta: { role: 'EDITORIAL_ADMIN' },
        redirect: '/editorial-admin/tech-check',
        children: [
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
            },
            // [编辑部管理员] 消息中心 -> /editorial-admin/message
            {
                path: 'message',
                name: 'EditorialAdminMessage',
                component: MessageCenter,
                meta: { title: '消息中心' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router