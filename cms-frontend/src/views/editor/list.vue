<template>
  <el-table :data="manuscriptList" v-loading="loading" border stripe>
    <el-table-column prop="manuscriptId" label="ID" width="80" />
    <el-table-column prop="title" label="标题" />
    <el-table-column prop="status" label="状态" width="120" />
    <el-table-column prop="subStatus" label="子状态" width="120" />
    <el-table-column label="操作" width="120">
      <template #default="scope">
        <el-button type="primary" size="small" @click="handleProcess(scope.row.manuscriptId)">
          处理
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const manuscriptList = ref([])
const loading = ref(false)

const fetchList = async () => {
  loading.value = true
  try {
    const res = await fetch('http://localhost:8080/api/editor/manuscripts', {
      headers: { 'Authorization': localStorage.getItem('token') }
    })
    const result = await res.json()
    // 关键调试：如果这里打印 []，说明数据库查询没查到 CurrentEditorID 匹配的数据
    console.log("前端收到的数据:", result.data)
    if (result.code === 200) manuscriptList.value = result.data
  } finally {
    loading.value = false
  }
}

const handleProcess = (id) => {
  router.push(`/editor/process?id=${id}`)
}

onMounted(fetchList)
</script>