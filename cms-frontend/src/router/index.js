import { createRouter, createWebHistory } from 'vue-router'
// 确保 Layout 路径正确，如果报错找不到文件，请检查 src/layout/index.vue 是否存在
import Layout from '@/layout/index.vue'
import Editorsidebar from '@/layout/EditorSIdebar.vue'
import Layout2 from '@/layout/admin_index.vue'
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

    // ================== 新增：主编功能模块 ==================
    {
        path: '/eic',
        component: Layout,
        redirect: '/eic/reviewer',
        meta: { title: '主编工作台', icon: 'UserFilled' }, // 需确保引入了 UserFilled 图标，或者换成 'User'
        children: [
            // {
            //     path: 'reviewer',
            //     name: 'ReviewerManager',
            //     // 对应下面第三步创建的文件
            //     component: () => import('@/views/eic/ReviewerManager.vue'),
            //     meta: { title: '审稿人管理' }
            // },
            {
                path: 'audit',
                name: 'ManuscriptAudit',
                // 暂时预留，你可以之后再创建这个文件
                component: () => import('@/views/eic/ManuscriptAudit.vue'),
                meta: { title: '稿件全览与决策' }
            }
        ]
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
    {

        path: '/SuperAdmin', // 匹配 /dashboard/systemadmin 路径
        component: Layout2,
        children: [{

            path: 'superadmin', // 匹配 /dashboard/systemadmin 路径

            name: 'Superadmin',

            component: () => import('@/views/SuperAdmin/superadmin.vue'),

            meta: {

                title: '系统管理',

                icon: 'Setting',

                roles: ['SuperAdmin']

            }
            },
            {

                path: 'superadmin_management', // 匹配 /dashboard/systemadmin 路径

                name: 'Superadmin_management',

                component: () => import('@/views/SuperAdmin/management.vue'),

                meta: {

                    title: '系统管理',

                    icon: 'Setting1',

                    roles: ['SuperAdmin']

                }
            }

        ]

    },
    {

        path: '/Systemadmin', // 匹配 /dashboard/systemadmin 路径
       // component: Layout,
        children: [{

            path: 'systemadmin', // 匹配 /dashboard/systemadmin 路径

            name: 'Systemadmin',

            component: () => import('@/views/Systemadmin/systemadmin.vue'),

            meta: {

                title: '系统管理',

                icon: 'Setting',

                roles: ['SystemAdmin']

            }},


        ]

    },
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
                path: 'detail/:id',
                name: 'ManuscriptDetail',
                component: () => import('@/views/manuscript/detail.vue'),
                meta: { title: '稿件详情', hidden: true }
            }
        ]
    },
    //编辑路由
    {
        path: '/editor',
        component: Editorsidebar,
        meta: { title: '稿件处理', icon: 'Edit', role: 'Editor' },
        children: [
            {
                path: 'my-manuscripts',
                name: 'EditorManuscripts',
                component: () => import('@/views/editor/list.vue'),
                meta: { title: '我负责的稿件' } // 功能1
            },
            {
                path: 'process/:id',
                name: 'ProcessManuscript',
                component: () => import('@/views/editor/process.vue'),
                meta: { title: '稿件处理详情', hidden: true } // 功能2,3,4,5
            },
            {
                path: '/editor/process',
                name: 'ManuscriptProcess',
                component: () => import('@/views/editor/process.vue') // 确保文件名和路径正确
            },
            {
                // 确保这里的 path 是小写 'monitoring'
                path: 'monitoring',
                name: 'Monitoring',
                // 确保文件名 Monitoring.vue 路径正确
                component: () => import('@/views/editor/Monitoring.vue'),
                meta: { title: '审稿监控', roles: ['Editor'] }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router