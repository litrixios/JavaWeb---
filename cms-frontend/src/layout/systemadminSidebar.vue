<template>
  <div class="app-wrapper">
    <div class="sidebar-container">
      <h3 style="color:white; text-align:center; padding: 20px 0;">CMS 系统</h3>
      <el-menu
          active-text-color="#409EFF"
          background-color="#304156"
          text-color="#bfcbd9"
          default-active="1"
          router
      >
        <el-menu-item index="/Systemadmin/systemadmin">
          <el-icon><Document /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/Systemadmin/systemadmin_management">
          <el-icon><Edit /></el-icon>
          <span>系统维护</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main-container">
      <div class="navbar">
        <div class="right-menu">
          用户: {{ userName }}
          <el-button link type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>

      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Edit } from '@element-plus/icons-vue'

const router = useRouter()
// 定义响应式变量存储用户名
const userName = ref('User')

onMounted(() => {
  // 1. 从 localStorage 获取用户信息字符串
  const userInfoStr = localStorage.getItem('userInfo')

  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      // 2. 优先显示 fullName (例如 "张三")，如果为空则显示 username (例如 "zhangsan")
      userName.value = userInfo.fullName || userInfo.username || 'User'
    } catch (e) {
      console.error('用户信息解析失败', e)
    }
  }
})

const handleLogout = () => {
  // 3. 清除 Token 和用户信息
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  // 4. 跳转回登录页
  router.push('/login') // 假设你的登录路由是 /login
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  width: 210px;
  background-color: #304156;
  height: 100%;
  overflow-y: auto;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f0f2f5;
}

.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 20px;
}

.right-menu {
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px; /* 让名字和按钮之间有点间距 */
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>