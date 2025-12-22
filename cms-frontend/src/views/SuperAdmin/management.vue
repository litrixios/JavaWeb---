<!-- views/system/SystemMaintenance.vue -->
<template>
  <div class="system-maintenance-container">
    <div class="header">
      <h2>系统维护</h2>
      <el-button type="primary" @click="$router.push('/system/user-management')">
        切换到用户管理
      </el-button>
    </div>

    <div class="search-bar">
      <el-select
          v-model="logQuery.operationType"
          placeholder="日志类型"
          style="width: 200px; margin-right: 10px;"
      >
        <el-option label="所有日志" value="" />
        <el-option label="登录日志" value="Login" />
        <el-option label="提交日志" value="Submit" />
        <el-option label="系统日志" value="System" />
        <el-option label="用户操作" value="User" />
        <el-option label="审稿操作" value="Review" />
      </el-select>
      <el-date-picker
          v-model="logQuery.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 300px; margin-right: 10px;"
      />
      <el-button type="primary" @click="fetchLogs">查询</el-button>
      <el-button type="warning" @click="clearLogs" :disabled="logList.length === 0">清空日志</el-button>
    </div>

    <el-table
        :data="logList"
        border
        style="width: 100%; margin-top: 15px;"
        v-loading="logLoading"
        empty-text="暂无日志数据"
    >
      <el-table-column prop="logId" label="日志ID" width="100" />
      <el-table-column prop="operationTime" label="操作时间" width="180" />
      <el-table-column prop="operatorName" label="操作人" width="120" />
      <el-table-column prop="operationType" label="操作类型" width="120" />
      <el-table-column prop="manuscriptId" label="稿件ID" width="100" />
      <el-table-column prop="description" label="操作描述" min-width="200" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const router = useRouter()

// 系统日志相关
const logList = ref([])
const logLoading = ref(false)
const logQuery = reactive({
  operationType: '',
  dateRange: [],
})

onMounted(() => {
  fetchLogs()
})

// 获取系统日志
const fetchLogs = async () => {
  logLoading.value = true
  try {
    const params = {
      ...logQuery,
      startDate: logQuery.dateRange?.[0] ? new Date(logQuery.dateRange[0]).toISOString() : null,
      endDate: logQuery.dateRange?.[1] ? new Date(logQuery.dateRange[1]).toISOString() : null,
      operationType: logQuery.operationType || null
    }

    const res = await request.get('/api/system-admin/logs', { params })
    if (res.code === 200) {
      logList.value = res.data
    } else {
      ElMessage.error(res.msg || '获取日志失败')
    }
  } catch (err) {
    ElMessage.error('获取日志失败')
    console.error(err)
  } finally {
    logLoading.value = false
  }
}

// 清空日志
const clearLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复。', '警告', {
      type: 'warning'
    })

    const res = await request.delete('/api/system-admin/logs')
    if (res.code === 200) {
      ElMessage.success('日志清空成功')
      fetchLogs()
    } else {
      ElMessage.error(res.msg || '日志清空失败')
    }
  } catch (err) {
    if (err === 'cancel') return
    ElMessage.error('日志清空失败')
    console.error(err)
  }
}
</script>

<style scoped>
.system-maintenance-container {
  padding: 20px;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}
</style>