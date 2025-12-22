<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>用户注册</span>
        </div>
      </template>
      <el-form-item>
        <el-button style="width: 100%;" @click="returnto">返回到主界面</el-button>
      </el-form-item>
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="姓名" prop="fullName">
          <el-input v-model="registerForm.fullName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择身份" style="width: 100%">
            <el-option label="审稿人" value="Reviewer" />
            <el-option label="作者" value="Author" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属单位" prop="affiliation">
          <el-input v-model="registerForm.affiliation" placeholder="请输入所属单位" />
        </el-form-item>
        <el-form-item label="研究方向" prop="researchDirection">
          <el-input v-model="registerForm.researchDirection" placeholder="请输入研究方向" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%;" @click="handleRegister" :loading="loading">注册</el-button>
        </el-form-item>
        <el-form-item>
          <el-button style="width: 100%;" @click="goToLogin">返回登录</el-button>
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
const registerFormRef = ref()

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  fullName: '',
  role: '',
  affiliation: '',
  researchDirection: ''
})

// 表单验证规则
const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择身份', trigger: 'change' }
  ]
})

const handleRegister = async () => {
  if (!registerFormRef.value) return

  // 表单验证
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true

  try {
    const response = await fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(registerForm)
    })

    const result = await response.json()

    if (result.code === 200) {
      ElMessage.success('注册成功！')
      // 自动登录
      localStorage.setItem('token', result.data.token)
      localStorage.setItem('userInfo', JSON.stringify(result.data.user))
      router.push('/login')
    } else {
      ElMessage.error(result.msg || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
const returnto = () =>{
  window.location.href = 'http://localhost:8080/index'
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #2d3a4b;
}
.register-card {
  width: 500px;
}
.card-header {
  text-align: center;
  font-weight: bold;
  color: #333;
}
</style>