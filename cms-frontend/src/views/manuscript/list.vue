<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="全部稿件" name="all" />
      <el-tab-pane label="待提交/草稿" name="incomplete" />
      <el-tab-pane label="审稿中" name="processing" />
      <el-tab-pane label="需要修回" name="revision" />
      <el-tab-pane label="已决议" name="decision" />
    </el-tabs>

    <el-card>
      <div class="filter-container">
        <el-button type="primary" icon="Plus" @click="$router.push('/manuscript/submit')">
          新建投稿
        </el-button>
      </div>

      <el-table :data="filteredList" v-loading="loading" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="id" label="稿件ID" width="100" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="submitTime" label="提交时间" width="180" />

        <el-table-column label="当前状态" width="150">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'Incomplete'" link type="primary" @click="handleEdit(row.id)">
              继续编辑
            </el-button>
            <el-button link type="primary" @click="handleTrack(row.id)">
              追踪状态
            </el-button>
            <el-button v-if="row.status === 'Need Revision'" link type="danger" @click="handleRevise(row.id)">
              提交修回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyManuscripts } from '@/api/author'

const router = useRouter()
const list = ref([])
const loading = ref(false)
const activeTab = ref('all')

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyManuscripts()
    list.value = res.data || []
  } finally {
    loading.value = false
  }
}

// 前端过滤模拟不同Tab的数据展示 (实际建议后端支持 status 参数过滤)
const filteredList = computed(() => {
  if (activeTab.value === 'all') return list.value

  // 这里需要根据你后端具体的 Status 字符串或者字典值来过滤
  // 假设后端返回的状态是英文单词，如 'Incomplete', 'Under Review' 等
  return list.value.filter(item => {
    if (activeTab.value === 'incomplete') return item.status === 'Incomplete'
    if (activeTab.value === 'processing') return ['With Editor', 'Under Review'].includes(item.status)
    if (activeTab.value === 'revision') return item.status === 'Need Revision'
    if (activeTab.value === 'decision') return ['Accepted', 'Rejected'].includes(item.status)
    return true
  })
})

const getStatusType = (status) => {
  const map = {
    'Accepted': 'success',
    'Rejected': 'danger',
    'Incomplete': 'info',
    'Under Review': 'warning'
  }
  return map[status] || ''
}

const handleEdit = (id) => router.push(`/manuscript/submit?id=${id}`)
const handleTrack = (id) => router.push(`/manuscript/detail?id=${id}`)
const handleRevise = (id) => router.push(`/manuscript/submit?id=${id}&type=revise`)

onMounted(fetchData)
</script>