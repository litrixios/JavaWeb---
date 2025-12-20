<template>
  <div class="login-box">
    <h2>学术投稿系统登录</h2>
    <form @submit.prevent="handleLogin">
      <div>
        <label>用户名:</label>
        <input v-model="form.username" type="text" placeholder="superadmin / prof_zhang" required />
      </div>
      <div>
        <label>密码:</label>
        <input v-model="form.password" type="password" required />
      </div>
      <button type="submit">登录</button>
    </form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    // 对应 AuthController.login 接口
    const data = await request.post('/auth/login', form)

    // 这里的 data 就是后端返回的 map (包含 token 和 user)
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.user))

    alert('登录成功！')
    router.push('/dashboard') // 跳转到主页
  } catch (e) {
    console.error(e)
  }
}
</script>

<style scoped>
.login-box { width: 300px; margin: 100px auto; padding: 20px; border: 1px solid #ccc; }
input { display: block; width: 100%; margin-bottom: 10px; }
</style>