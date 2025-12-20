<template>
  <div class="container">
    <h1>我的稿件列表</h1>
    <div class="user-info" v-if="user">
      当前用户: {{ user.fullName }} ({{ user.role }})
      <button @click="logout">退出登录</button>
    </div>

    <button @click="goToSubmit">我要投稿</button>

    <table border="1" cellpadding="10" style="margin-top: 20px; width: 100%">
      <thead>
      <tr>
        <th>ID</th>
        <th>标题</th>
        <th>状态</th>
        <th>提交时间</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in manuscripts" :key="item.manuscriptId">
        <td>{{ item.manuscriptId }}</td>
        <td>{{ item.title }}</td>
        <td>{{ item.status }}</td>
        <td>{{ formatDate(item.submissionTime) }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const manuscripts = ref([])
const user = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 获取稿件列表
const fetchList = async () => {
  try {
    // 对应 ManuscriptController.getMyList
    const data = await request.get('/manuscript/my-list')
    manuscripts.value = data
  } catch (e) {
    console.error(e)
  }
}

const logout = () => {
  localStorage.clear()
  router.push('/login')
}

const goToSubmit = () => {
  // 这里可以跳转到投稿页面，你自己实现 SubmitManuscript.vue
  alert("请实现投稿页面组件！")
}

const formatDate = (dateStr) => {
  if(!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.container { padding: 20px; }
.user-info { margin-bottom: 20px; color: #666; }
</style>