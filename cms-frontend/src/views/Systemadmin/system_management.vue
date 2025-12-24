<template>
  <div class="system-maintenance-container">
    <div class="header">
      <h2>系统维护</h2>
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
      <el-table-column prop="operatorFullName" label="操作姓名" width="180"/>
      <el-table-column prop="operationType" label="操作类型" width="120" />
      <el-table-column prop="manuscriptId" label="稿件ID" width="100" />
      <el-table-column prop="description" label="操作描述" min-width="200" />
    </el-table>
    <div class="pagination-container" style="margin-top: 20px; display: flex; justify-content: flex-end;">
      <el-pagination
          v-model:current-page="logQuery.pageNum"
          v-model:page-size="logQuery.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalLogs"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleLogSizeChange"
          @current-change="handleLogPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeMount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'

// 添加路由实例
const router = useRouter()

// token验证函数
const checkAuth = () => {
  const token = localStorage.getItem('token')
  const userInfo = localStorage.getItem('userInfo')

  if (!token || !userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }

  try {
    const userData = JSON.parse(userInfo)
    // 只有系统管理员和超级管理员可以访问系统维护
    const allowedRoles = ['SuperAdmin', 'SystemAdmin']
    if (!allowedRoles.includes(userData.role)) {
      ElMessage.error('权限不足，无法访问系统维护')
      router.push('/unauthorized')
      return false
    }
    return true
  } catch (error) {
    console.error('解析用户信息失败:', error)
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
    return false
  }
}

// token过期处理函数
const handleTokenExpired = () => {
  ElMessage.error('登录已过期，请重新登录')
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

// 系统日志相关
const logList = ref([])
const logLoading = ref(false)
const totalLogs = ref(0)
const logQuery = reactive({
  operationType: '',
  dateRange: [],
  pageNum: 1,
  pageSize: 10
})

// 在组件加载前检查权限
onBeforeMount(() => {
  //checkAuth()
})

// 初始化
onMounted(() => {
  if (checkAuth()) {
    fetchLogs()
  }
})

// 获取日志
const fetchLogs = async () => {
  // 先检查token
  if (!checkAuth()) return

  logLoading.value = true
  try {
    const params = {
      operationType: logQuery.operationType || null,
      pageNum: logQuery.pageNum || 1,
      pageSize: logQuery.pageSize || 10
    }

    // 处理日期范围
    if (logQuery.dateRange && logQuery.dateRange.length === 2) {
      params.startDate = formatDate(logQuery.dateRange[0])
      params.endDate = formatDate(logQuery.dateRange[1])
    }

    const res = await request.get('/api/system-admin/logs', { params })
    if (res.code === 200) {
      logList.value = res.data.list || []
      totalLogs.value = res.data.total || 0
    } else {
      // 如果token过期或无效，后端通常会返回401
      if (res.code === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error(res.msg || '获取日志失败')
    }
  } catch (err) {
    // 网络错误或token过期
    if (err.response && err.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error('获取日志失败')
    console.error(err)
  } finally {
    logLoading.value = false
  }
}

// 日期格式化工具函数
const formatDate = (date) => {
  if (!date) return null
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 清空日志
const clearLogs = async () => {
  // 先检查token
  if (!checkAuth()) return

  try {
    await ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复。', '警告', {
      type: 'warning'
    })

    const res = await request.delete('/api/system-admin/logs')
    if (res.code === 200) {
      ElMessage.success('日志清空成功')
      fetchLogs()
    } else {
      if (res.code === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error(res.msg || '日志清空失败')
    }
  } catch (err) {
    if (err === 'cancel') {
      return
    }
    if (err.response && err.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error('日志清空失败')
    console.error(err)
  }
}

// 日志分页切换
const handleLogPageChange = (page) => {
  // 检查权限
  if (!checkAuth()) return

  logQuery.pageNum = page
  fetchLogs()
}

const handleLogSizeChange = (size) => {
  // 检查权限
  if (!checkAuth()) return

  logQuery.pageSize = size
  logQuery.pageNum = 1
  fetchLogs()
}
</script>

<style scoped>
.system-maintenance-container {
  padding: 20px;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #303133;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}
</style>