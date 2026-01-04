<template>
  <div class="login-container">
    <div class="bg-pattern"></div>

    <div class="login-card-wrapper">
      <el-card class="login-card">
        <div class="top-actions">
          <el-button type="text" class="back-link" @click="returnto">返回首页</el-button>
        </div>

        <div class="card-header">
          <div class="system-logo">
            <el-icon :size="36"><Document /></el-icon>
          </div>
          <h2>CMS系统</h2>
          <p class="subtitle">用户登录</p>
        </div>

        <el-form :model="form" label-position="top" class="login-form">
          <el-form-item label="账号">
            <el-input v-model="form.username" placeholder="请输入用户名" clearable />
          </el-form-item>

          <el-form-item label="密码">
            <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                show-password
                clearable
            />
          </el-form-item>

          <el-form-item label="身份">
            <el-select v-model="form.role" placeholder="请选择身份" style="width: 100%">
              <el-option label="作者" value="Author" />
              <el-option label="审稿人" value="Reviewer" />
              <el-option label="编辑" value="Editor" />
              <el-option label="主编" value="EditorInChief" />
              <el-option label="超级管理员" value="SuperAdmin" />
              <el-option label="系统管理员" value="SystemAdmin" />
              <el-option label="编辑部管理员" value="EditorialAdmin" />
            </el-select>
          </el-form-item>

          <el-form-item class="form-actions">
            <el-button type="primary" class="primary-btn" @click="handleLogin" :loading="loading">
              登录
            </el-button>
          </el-form-item>

          <div class="bottom-actions">
            <el-button type="text" class="link-btn" @click="goToRegister">没有账号？立即注册</el-button>
          </div>
        </el-form>
      </el-card>

      <div class="copyright">
        © {{ new Date().getFullYear() }} CMS系统 | 版权所有
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  role: ''
})

const handleLogin = async () => {
  // 表单验证
  if (!form.username || !form.password || !form.role) {
    ElMessage.error('请填写完整的登录信息')
    return
  }

  loading.value = true

  try {
    // 1. 调用登录接口
    const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form)
    });
    const loginResult = await loginResponse.json();

    if (loginResult.code !== 200) {
      ElMessage.error(loginResult.message || '登录失败');
      return;
    }

    const userData = loginResult.data.user;
    const token = loginResult.data.token;

    // 验证用户状态（1为启用）
    if (userData.status !== 1) {
      ElMessage.error('账号已被禁用，请联系管理员');
      return;
    }

    // 2. 调用权限接口获取用户权限
    const permissionResponse = await fetch(
        `http://localhost:8080/api/system-admin/users/${userData.userId}/permissions`,
        {
          headers: { 'Authorization': `Bearer ${token}` } // 携带token验证
        }
    );
    const permissionResult = await permissionResponse.json();

    if (permissionResult.code !== 200) {
      ElMessage.warning('获取权限信息失败，将使用默认权限');
    }
    const permissions = permissionResult.data || {}; // 默认为空对象

    // 3. 存储登录信息和权限
    localStorage.setItem('token', token);
    localStorage.setItem('userInfo', JSON.stringify(userData));
    localStorage.setItem('permissions', JSON.stringify(permissions));

    // 4. 根据角色跳转页面（保持原逻辑）
    const userRole = userData.role;
    switch (userRole) {
      case 'SuperAdmin':
        router.push('/SuperAdmin/superadmin')
        break
      case 'SystemAdmin':
        router.push('/Systemadmin/systemadmin')
        break
      case 'Editor':
        router.push('/editor/my-manuscripts')
        break
      case 'Author':
        router.push('/manuscript/list')
        break
      case 'Reviewer':
        router.push('/reviewer/dashboard')
        break
      case 'EditorialAdmin':
        router.push('/editorial-admin')
        break
      case 'EditorInChief':
        router.push('/eic/audit')
        break
      default:
        router.push('/manuscript/list')
    }

    ElMessage.success('登录成功');

  } catch (error) {
    console.error('登录错误:', error);
    ElMessage.error('网络错误，请检查后端服务是否启动');
  } finally {
    loading.value = false;
  }
}
const goToRegister = () => {
  router.push('/register')
}
const returnto = () =>{
  window.location.href = 'http://localhost:8080/index'
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  padding: 40px 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #2d3a4b;
  position: relative;
  box-sizing: border-box;
}

/* 背景装饰 */
.bg-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  background-image:
      radial-gradient(rgba(120, 130, 150, 0.30) 2px, transparent 1px),
      radial-gradient(rgba(120, 130, 150, 0.30) 2px, transparent 1px);
  background-size: 48px 48px;
  background-position: 0 0, 24px 24px;
  z-index: 0;
}

.login-card-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
}

.login-card {
  width: 100%;
  padding: 28px 32px;
  border-radius: 12px;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
  border: none;
}

.top-actions {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 6px;
}

.back-link {
  padding: 0;
}

.card-header {
  text-align: center;
  margin: 8px 0 16px;
}

.system-logo {
  margin-bottom: 10px;
  color: #245d9c;
}

.card-header h2 {
  margin: 0 0 6px 0;
  color: #1D2129;
  font-size: 22px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #86909C;
  font-size: 13px;
}

.login-form {
  margin-top: 14px;
}

.el-form-item {
  margin-bottom: 14px;
}

.el-input,
.el-select {
  height: 40px;
  border-radius: 6px;
}

.form-actions {
  margin-top: 18px;
  margin-bottom: 8px;
}

.primary-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background-color: #245d9c;
  border-color: #245d9c;
  transition: all 0.25s ease;
}

.primary-btn:hover {
  background-color: #245d9c;
  border-color: #245d9c;
}

.bottom-actions {
  display: flex;
  justify-content: center;
}

.link-btn {
  padding: 6px 10px;
  color: #245d9c;
}

.copyright {
  text-align: center;
  margin-top: 16px;
  color: #86909C;
  font-size: 12px;
}
</style>
