<template>
  <div class="app-wrapper">
    <div class="sidebar-container">
      <div class="logo-wrapper">
        <h3 class="logo-title">CMS 系统</h3>
      </div>

      <el-menu
          :default-active="activeMenu"
          active-text-color="#409EFF"
          background-color="#304156"
          text-color="#bfcbd9"
          :unique-opened="true"
          router
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <template v-for="item in currentRoleMenus" :key="item.index">
          <el-sub-menu v-if="item.children" :index="item.index">
            <template #title>
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.title }}</span>
            </template>
            <el-menu-item
                v-for="child in item.children"
                :key="child.index"
                :index="child.index"
            >
              <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item v-else :index="item.index">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </div>

    <div class="main-container">
      <div class="navbar">
        <div class="breadcrumb">
          <span style="font-weight: bold; color: #666;">{{ currentRoleName }} 端</span>
        </div>
        <div class="right-menu">
          <span class="user-name">用户: {{ displayName }}</span>
          <el-button link type="danger" size="small" @click="handleLogout">
            <el-icon style="margin-right: 4px"><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </div>

      <div class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
// 引入需要的图标
import {
  Document, Edit, User, Avatar, Search,
  Odometer, Setting, Monitor, SwitchButton,
  Management // 注意：如果图标库没有 Management，请换成其他如 List
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 用户状态
const userInfo = ref({})
const displayName = ref('User')
const userRole = ref('')

// 1. 定义各角色的菜单配置
// 格式：{ index: 路由路径, title: 显示名称, icon: 图标组件名, children: [] }
const menuMap = {
  // 作者
  Author: [
    { index: '/manuscript/list', title: '我的稿件', icon: 'Document' },
    { index: '/manuscript/submit', title: '在线投稿', icon: 'Edit' }
  ],
  // 编辑
  Editor: [
    { index: '/editor/my-manuscripts', title: '我负责的稿件', icon: 'Document' },
    { index: '/editor/monitoring', title: '审稿监控', icon: 'Monitor' }
  ],
  // 审稿人
  Reviewer: [
    { index: '/reviewer/dashboard', title: '我的审稿任务', icon: 'Edit' }
  ],
  // 超级管理员
  SuperAdmin: [
    { index: '/SuperAdmin/superadmin', title: '系统管理', icon: 'Setting' },
    { index: '/SuperAdmin/superadmin_management', title: '管理员管理', icon: 'User' }
  ],
  // 系统管理员
  SystemAdmin: [
    { index: '/Systemadmin/systemadmin', title: '系统配置', icon: 'Setting' }
  ],
  // 主编 (根据你提供的旧代码保留，如果路由已注释请自行调整)
  EditorInChief: [
    {
      index: '/eic',
      title: '主编管理',
      icon: 'User',
      children: [
        { index: '/eic/reviewer', title: '审稿人管理', icon: 'Avatar' },
        { index: '/eic/audit', title: '稿件全览', icon: 'Search' }
      ]
    }
  ]
}

// 2. 初始化加载用户信息
onMounted(() => {
  const infoStr = localStorage.getItem('userInfo')
  if (infoStr) {
    try {
      const parsed = JSON.parse(infoStr)
      userInfo.value = parsed
      userRole.value = parsed.role || 'Author' // 默认回退到 Author
      // 优先显示全名，没有则显示用户名
      displayName.value = parsed.fullName || parsed.username || 'User'
    } catch (e) {
      console.error('用户信息解析失败', e)
      handleLogout()
    }
  } else {
    // 如果没有登录信息，强制跳回登录
    router.push('/login')
  }
})

// 3. 计算当前角色的菜单
const currentRoleMenus = computed(() => {
  return menuMap[userRole.value] || []
})

// 显示当前角色的中文名（用于顶部展示）
const currentRoleName = computed(() => {
  const map = {
    Author: '作者',
    Editor: '责任编辑',
    Reviewer: '审稿人',
    SuperAdmin: '超级管理员',
    SystemAdmin: '系统管理员',
    EditorInChief: '主编'
  }
  return map[userRole.value] || ''
})

// 当前激活的菜单项（高亮）
const activeMenu = computed(() => {
  return route.path
})

// 4. 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

/* 侧边栏样式 */
.sidebar-container {
  width: 210px;
  background-color: #304156;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止侧边栏出现双重滚动条 */
}

.logo-wrapper {
  height: 50px;
  line-height: 50px;
  background-color: #2b2f3a;
  text-align: center;
}

.logo-title {
  color: white;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

/* 覆盖 el-menu 默认样式，使其占满剩余空间并可滚动 */
.sidebar-container .el-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

/* 主体容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0; /* 防止 flex 子项溢出 */
  background-color: #f0f2f5;
}

/* 顶部导航 */
.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 9;
}

.right-menu {
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
}

.user-name {
  margin-right: 15px;
  font-weight: 500;
}

/* 内容区 */
.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto; /* 内容溢出时滚动 */
  position: relative;
}

/* 简单的路由切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>