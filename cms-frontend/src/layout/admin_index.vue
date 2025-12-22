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
        <el-menu-item index="/SuperAdmin/superadmin">
          <el-icon><Document /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/SuperAdmin/superadmin_management">
          <el-icon><Edit /></el-icon>
          <span>系统维护</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main-container">
      <div class="navbar">
        <div class="right-menu">
          用户: 张三 <el-button link type="danger" size="small">退出</el-button>
        </div>
      </div>

      <div class="app-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { Document, Edit, User, Avatar, Search } from '@element-plus/icons-vue'
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

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>