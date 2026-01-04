<template>
  <div class="register-container">
    <div class="bg-pattern"></div>

    <div class="register-card-wrapper">
      <el-card class="register-card">
        <template #header>
          <div class="card-header">
            <div class="system-logo">
              <el-icon :size="36"><DocumentAdd /></el-icon>
            </div>
            <h2>CMS系统</h2>
            <p class="subtitle">用户注册</p>
          </div>
        </template>

        <el-form
            :model="registerForm"
            :rules="registerRules"
            ref="registerFormRef"
            label-width="100px"
            class="register-form"
        >
          <!-- 用户名 + 邮箱：一行两列 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input
                    v-model="registerForm.username"
                    placeholder="请输入用户名"
                    prefix-icon="User"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input
                    v-model="registerForm.email"
                    placeholder="请输入邮箱"
                    prefix-icon="Message"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 密码 + 确认密码：一行两列 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="请输入密码"
                    show-password
                    prefix-icon="Lock"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入密码"
                    show-password
                    prefix-icon="Lock"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 姓名 + 身份：一行两列（核心修正） -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="姓名" prop="fullName">
                <el-input
                    v-model="registerForm.fullName"
                    placeholder="请输入真实姓名"
                    prefix-icon="UserFilled"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="身份" prop="role">
                <el-select
                    v-model="registerForm.role"
                    placeholder="请选择身份"
                    style="width: 100%"
                    prefix-icon="User"
                >
                  <el-option label="作者" value="Author" />
                  <el-option label="审稿人" value="Reviewer" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 所属单位：单独一行 -->
          <el-form-item label="所属单位" prop="affiliation">
            <el-input
                v-model="registerForm.affiliation"
                placeholder="请输入所属单位"
                prefix-icon="OfficeBuilding"
            />
          </el-form-item>

          <!-- 研究方向：单独一行 -->
          <el-form-item label="研究方向" prop="researchDirection">
            <el-input
                v-model="registerForm.researchDirection"
                placeholder="请输入研究方向"
                prefix-icon="Reading"
            />
          </el-form-item>

          <el-form-item class="form-actions">
            <el-button
                type="primary"
                style="width: 100%;"
                @click="handleRegister"
                :loading="loading"
                class="primary-btn"
            >
              完成注册
            </el-button>
          </el-form-item>

          <div class="button-group">
            <el-button
                type="default"
                @click="returnto"
                class="secondary-btn"
            >
              返回首页
            </el-button>
            <el-button
                type="text"
                @click="goToLogin"
                class="link-btn"
            >
              已有账号？返回登录
            </el-button>
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
import { DocumentAdd, User, Lock, Message, UserFilled, OfficeBuilding, Reading } from '@element-plus/icons-vue'

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
      ElMessage.success('注册成功！请登录')
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

const returnto = () => {
  window.location.href = 'http://localhost:8080/index'
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  padding: 40px 0;
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
  background-size: 40px 40px;
  background-position: 0 0, 20px 20px;
  z-index: 0;
}

.register-card-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 760px;
}

.register-card {
  width: 100%;
  padding: 28px 32px;
  border-radius: 12px;
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.08);
  border: none;
}

.card-header {
  text-align: center;
  margin-bottom: 20px;
}

.system-logo {
  margin-bottom: 15px;
  color: #245d9c;
}

.card-header h2 {
  margin: 0 0 8px 0;
  color: #1D2129;
  font-size: 22px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #86909C;
  font-size: 14px;
}

.register-form {
  margin-top: 20px;
}

.el-form-item {
  margin-bottom: 16px;
}

.el-input, .el-select {
  height: 40px;
  border-radius: 6px;
}

.el-row {
  margin-bottom: 8px;
}

.form-actions {
  margin-top: 25px;
  margin-bottom: 15px;
}

.button-group {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  gap: 8px;
}

@media (max-width: 640px) {
  .register-card {
    padding: 22px 18px;
  }

  .button-group {
    flex-direction: column;
    align-items: stretch;
  }

  .secondary-btn {
    width: 100%;
    justify-content: center;
  }

  /* 移动端自动转为单列布局 */
  .el-col {
    span: 24 !important;
  }
}

.primary-btn {
  height: 44px;
  font-size: 16px;
  background-color: #245d9c;
  border-color: #245d9c;
  transition: all 0.3s ease;
}

.primary-btn:hover {
  background-color: #1e4f86;
  border-color: #1e4f86;
}

.secondary-btn {
  padding: 8px 16px;
  background-color: #F2F3F5;
  color: #4E5969;
  border: none;
}

.link-btn {
  color: #245d9c;
  padding: 8px 12px;
}

.copyright {
  text-align: center;
  margin-top: 25px;
  color: #86909C;
  font-size: 12px;
}
</style>