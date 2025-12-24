<template>
  <el-table :data="manuscriptList" v-loading="loading" border stripe style="width: 100%">
    <el-table-column prop="manuscriptId" label="ID" width="60" align="center" />

    <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip />

    <el-table-column label="关键词" min-width="220">
      <template #default="scope">
        <div style="display: flex; flex-wrap: wrap; gap: 4px;">
          <el-tag
              v-for="(kw, index) in (scope.row.keywords ? scope.row.keywords.split(/[;；,，]/) : [])"
              :key="index"
              size="small"
              type="info"
              effect="plain"
          >
            {{ kw.trim() }}
          </el-tag>
          <span v-if="!scope.row.keywords || scope.row.keywords.trim() === ''" style="color: #c0c4cc; font-size: 12px;">
            未设置关键词
          </span>
        </div>
      </template>
    </el-table-column>

    <el-table-column prop="status" label="状态" width="110" align="center">
      <template #default="scope">
        <el-tag :type="getStatusType(scope.row.status)" size="small">
          {{ scope.row.status }}
        </el-tag>
      </template>
    </el-table-column>

    <el-table-column prop="subStatus" label="子状态" width="140" align="center">
      <template #default="scope">
        <el-tag
            :type="getSubStatusType(scope.row.subStatus)"
            size="small"
            effect="light"
            style="border-radius: 10px; padding: 0 10px;"
        >
          {{ scope.row.subStatus }}
        </el-tag>
      </template>
    </el-table-column>

    <el-table-column label="操作" width="100" align="center">
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

// 辅助函数：根据状态返回不同的标签颜色
const getStatusType = (status) => {
  const map = {
    'Processing': 'primary',
    'Decided': 'success',
    'Rejected': 'danger',
    'WithEditor': 'warning'
  }
  return map[status] || 'info'
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await fetch('http://localhost:8080/api/editor/manuscripts', {
      headers: { 'Authorization': localStorage.getItem('token') }
    })
    const result = await res.json()

    if (result.code === 200) {
      // 核心修改：只保留 status 为 Processing 且 subStatus 为 WithEditor 的稿件
      manuscriptList.value = result.data.filter(item =>
          item.status === 'Processing' && item.subStatus === 'WithEditor'
      )

      console.log("过滤后展示的任务数量:", manuscriptList.value.length)
    }
  } catch (error) {
    console.error("加载列表失败:", error)
  } finally {
    loading.value = false
  }
}

// 辅助函数：定义子状态的颜色逻辑
const getSubStatusType = (subStatus) => {
  const map = {
    'UnderReview': 'warning',   // 橙色：审稿中，需要关注进度
    'PendingAssign': 'danger',  // 红色：待指派，紧急任务
    'WithEditor': 'primary',    // 蓝色：编辑处理中
    'Accepted': 'success',      // 绿色：已录用
    'Rejected': 'info',         // 灰色：已拒绝
    'Decided': 'success'        // 绿色：已定稿
  }
  // 如果匹配不到，默认给灰色
  return map[subStatus] || 'info'
}
const handleProcess = (id) => {
  router.push(`/editor/process?id=${id}`)
}

onMounted(fetchList)
</script>