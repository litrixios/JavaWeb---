import { createRouter, createWebHistory } from 'vue-router'
// 确保 Layout 路径正确，如果报错找不到文件，请检查 src/layout/index.vue 是否存在
import Layout from '@/layout/index.vue'
import Editorsidebar from '@/layout/EditorSIdebar.vue'
import Layout2 from '@/layout/admin_index.vue'
import Layout3 from '@/layout/systemadminSidebar.vue'
import Router from '@/layout/eic.vue'
import EditorialAdminSidebar from '@/layout/editorial-adminSidebar.vue'
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
        component: Layout3,
        children: [{

            path: 'systemadmin', // 匹配 /dashboard/systemadmin 路径

            name: 'Systemadmin',

            component: () => import('@/views/Systemadmin/systemadmin.vue'),

            meta: {

                title: '系统管理',

                icon: 'Setting',

                roles: ['SystemAdmin']

            }},
            {

                path: 'systemadmin_management', // 匹配 /dashboard/systemadmin 路径

                name: 'Systemadmin_management',

                component: () => import('@/views/Systemadmin/system_management.vue'),

                meta: {

                    title: '系统管理',

                    icon: 'Setting',

                    roles: ['SystemAdmin']

                }}


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
            },
            {
                // 确保这里的 path 是小写 'tracking'
                path: 'tracking',
                name: 'Tracking',
                // 确保文件名 ReviewTracking.vue 路径正确
                component: () => import('@/views/editor/ReviewTracking.vue'),
                meta: { title: '审稿进度', roles: ['Editor'] }
            }
        ]
    },

    {
        path: '/reviewer',
        component: () => import('@/layout/index.vue'), // 确保使用你的主布局组件
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
            }
        ]
    },
// ✅ 编辑部管理员（改成最新 sidebar 布局）
    // 编辑部管理员
    {
        path: '/editorial-admin',
        name: 'EditorialAdmin',
        component: EditorialAdminSidebar, // ✅ layout 目录下的布局
        meta: { role: 'EDITORIAL_ADMIN' },
        redirect: '/editorial-admin/tech-check',
        children: [
            // 形式审查
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

            // 新闻管理
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


    // 【新增】消息中心 (所有人可见)
    {
        path: '/message',
        component: Layout,
        redirect: '/message/index',
        children: [
            {
                path: 'index',
                name: 'MessageCenter',
                component: () => import('@/views/message/index.vue'),
                meta: { title: '消息中心', icon: 'ChatDotRound' } // 确保图标已在 main.ts 注册或使用 Text
            }
        ]
    },


]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router