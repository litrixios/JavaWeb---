<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ref, onMounted } from 'vue' // 新增：用于处理用户名显示
const route = useRoute()
const router = useRouter()

// 新增：获取用户名逻辑
const userName = ref('Admin')
onMounted(() => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (userInfoStr) {
    try {
      const userInfo = JSON.parse(userInfoStr)
      userName.value = userInfo.fullName || userInfo.username || 'Admin'
    } catch (e) {
      console.error('用户信息解析失败', e)
    }
  }
})

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<template>
  <!-- 新增外层容器，使用flex布局实现左右结构 -->
  <div class="app-wrapper">
    <!-- 左侧侧边栏 -->
    <div class="sidebar-container">
      <h3 style="color:white; text-align:center; padding: 20px 0;">编辑部管理员</h3>
      <el-menu
          :default-active="route.path"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
      >
        <el-menu-item index="/editorial-admin/tech-check">形式审查</el-menu-item>
        <el-menu-item index="/editorial-admin/news">新闻管理</el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧主内容区域 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <div class="navbar">
        <div class="right-menu">
          用户: {{ userName }}
          <el-button link type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>

      <!-- 页面内容区域 -->
      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>


<style scoped>
/* 外层容器使用flex布局，占满整个视口 */
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

/* 左侧侧边栏样式 */
.sidebar-container {
  width: 210px; /* 固定宽度 */
  background-color: #304156;
  height: 100%;
  overflow-y: auto; /* 内容过多时可滚动 */
}

/* 右侧主内容区域 */
.main-container {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column; /* 纵向排列导航栏和内容 */
  overflow: hidden;
  background-color: #f0f2f5;
}

/* 顶部导航栏样式 */
.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  align-items: center;
  justify-content: flex-end; /* 内容靠右显示 */
  padding-right: 20px;
}

/* 右侧菜单（用户名和退出按钮）样式 */
.right-menu {
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 页面内容区域样式 */
.app-main {
  flex: 1; /* 占据剩余空间 */
  padding: 20px;
  overflow-y: auto; /* 内容过多时可滚动 */
}
</style>