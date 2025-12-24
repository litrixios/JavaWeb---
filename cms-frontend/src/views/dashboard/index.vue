<template>
  <div class="dashboard-container">
    <div class="welcome-card">
      <div class="greeting-content">
        <div class="icon-wrapper">
          <el-icon :size="60" color="#409EFF"><Sunrise /></el-icon>
        </div>

        <h1>欢迎使用学术稿件管理系统</h1>
        <p class="subtitle">CMS - Academic Manuscript Management System</p>

        <div class="user-info" v-if="user.fullName">
          <h2>你好，{{ user.fullName }}！</h2>
          <p class="date-info">今天是 {{ currentDate }}</p>
        </div>
        <div class="user-info" v-else>
          <h2>你好，欢迎回来！</h2>
          <p class="date-info">今天是 {{ currentDate }}</p>
        </div>

        <div class="tips">
          <p>请通过左侧菜单栏选择您需要进行的操作。</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Sunrise } from '@element-plus/icons-vue' // 确保引入图标

const user = ref({})
const currentDate = ref('')

onMounted(() => {
  // 1. 获取当前时间
  const now = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  currentDate.value = now.toLocaleDateString('zh-CN', options)

  // 2. 尝试获取用户信息 (假设登录时存储了 userInfo)
  const storedUser = localStorage.getItem('userInfo')
  if (storedUser) {
    try {
      user.value = JSON.parse(storedUser)
    } catch (e) {
      console.error('用户信息解析失败', e)
    }
  }
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 100px); /* 减去头部导航的高度 */
  background-color: #f0f2f5;
  padding: 20px;
}

.welcome-card {
  background: white;
  padding: 60px 80px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  text-align: center;
  max-width: 800px;
  width: 100%;
  transition: all 0.3s ease;
}

.welcome-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.icon-wrapper {
  margin-bottom: 20px;
}

h1 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
  font-weight: 600;
}

.subtitle {
  font-size: 16px;
  color: #909399;
  margin-bottom: 40px;
  letter-spacing: 1px;
}

.user-info {
  margin-bottom: 40px;
  padding: 20px;
  background-color: #ecf5ff;
  border-radius: 8px;
  display: inline-block;
  min-width: 300px;
}

.user-info h2 {
  color: #409EFF;
  margin: 0 0 10px 0;
}

.date-info {
  color: #606266;
  margin: 0;
}

.tips {
  color: #909399;
  font-size: 14px;
}
</style>