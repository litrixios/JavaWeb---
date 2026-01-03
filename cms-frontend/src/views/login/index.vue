<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>系统登录</span>
        </div>
      </template>
      <el-form-item>
        <el-button style="width: 100%;" @click="returnto">返回到主界面</el-button>
      </el-form-item>
      <el-form :model="form" label-width="60px">
        <el-form-item label="账号">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="身份">
          <el-select v-model="form.role" placeholder="请选择身份" style="width: 100%">
            <el-option label="超级管理员" value="SuperAdmin" />
            <el-option label="管理员" value="SystemAdmin" />
            <el-option label="编辑部管理员" value="EditorialAdmin" />
            <el-option label="主编" value="EditorInChief" />
            <el-option label="编辑" value="Editor" />
            <el-option label="审稿人" value="Reviewer" />
            <el-option label="作者" value="Author" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%;" @click="handleLogin" :loading="loading">登录</el-button>
        </el-form-item>
        <el-form-item>
          <el-button style="width: 100%;" @click="goToRegister">没有账号？立即注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

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
    // 调用后端登录接口
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(form)
    })

    const result = await response.json()

    console.log('完整的登录响应:', result) // 查看完整的响应结构

    if (result.code==200) {
      // 根据实际返回的数据结构调整
      // 后端返回的是 {user: ..., token: ...}，不是 {data: {user: ..., token: ...}}
      const userData = result.data.user // 直接取 user
      const token = result.data.token   // 直接取 token

      console.log('用户数据:', userData)
      console.log('token:', token)
      if(userData.status==1) {
        if (userData && token) {
          ElMessage.success('登录成功')
          // 保存token和用户信息到本地存储
          localStorage.setItem('token', token)
          localStorage.setItem('userInfo', JSON.stringify(userData))

          // 根据用户角色跳转到不同页面
          const userRole = userData.role
          console.log('用户角色:', userRole)

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
              router.push('/layout/index')
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
        } else {
          ElMessage.error('返回的用户数据为空')
        }
      }else {
        ElMessage.error('登录失败')
      }
    } else {
      ElMessage.error(result.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误:', error)
    ElMessage.error('网络错误，请检查后端服务是否启动')
  } finally {
    loading.value = false
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
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #2d3a4b;
}
.login-card {
  width: 400px;
}
.card-header {
  text-align: center;
  font-weight: bold;
  color: #333;
}
</style>