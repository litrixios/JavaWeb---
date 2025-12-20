<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <h2>欢迎回来，张三</h2>
      <p>今天是 2025年12月20日，祝你科研顺利！</p>
    </div>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>我的投稿</span>
              <el-tag type="primary">Total</el-tag>
            </div>
          </template>
          <div class="card-num">3</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>待处理修回</span>
              <el-tag type="danger">Action</el-tag>
            </div>
          </template>
          <div class="card-num">1</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>已录用</span>
              <el-tag type="success">Accepted</el-tag>
            </div>
          </template>
          <div class="card-num">1</div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>消息通知</span>
            </div>
          </template>
          <div class="card-num">5</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;" header="快捷操作">
      <el-button type="primary" icon="Edit" @click="$router.push('/manuscript/submit')">发起新投稿</el-button>
      <el-button icon="Search" @click="$router.push('/manuscript/list')">查看审稿进度</el-button>
    </el-card>
  </div>
</template>

<script setup>
// 这里可以导入图标，目前直接使用 ElButton 的 icon 属性
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}
.welcome-section {
  background: white;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-num {
  font-size: 24px;
  font-weight: bold;
  color: #304156;
}
</style>