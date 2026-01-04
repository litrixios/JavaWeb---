<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="header-actions">
          <span>我的稿件</span>
          <el-button type="primary" @click="$router.push('/manuscript/submit')">
            <el-icon><Plus /></el-icon> 新建投稿
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="未完成 (Incomplete)" name="Incomplete" />
        <el-tab-pane label="处理中 (Processing)" name="Processing" />
        <el-tab-pane label="需修回 (Revision)" name="Revision" />
        <el-tab-pane label="已决议 (Decision)" name="Decided" />
      </el-tabs>

      <el-table v-loading="loading" :data="tableData" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="manuscriptId" label="ID" width="80" />
        <el-table-column prop="title" label="标题" show-overflow-tooltip />

        <el-table-column prop="submissionTime" label="提交时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.submissionTime) }}
          </template>
        </el-table-column>

        <el-table-column prop="status" label="当前状态">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="activeTab === 'Incomplete'"
                link type="primary"
                @click="handleEdit(row.manuscriptId)"
            >
              继续编辑
            </el-button>

            <el-button
                v-if="activeTab === 'Revision'"
                link type="warning"
                @click="handleDetail(row.manuscriptId, 'revision')"
            >
              提交修回
            </el-button>

            <el-button link type="primary" @click="handleDetail(row.manuscriptId, 'track')">
              追踪状态
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="getList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyManuscripts } from '@/api/manuscript'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()

// 修改点2: 默认值修改为 Processing
const activeTab = ref('Processing')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: 'Processing' // 修改点3: 默认查询参数也同步修改
})

const getStatusTag = (status) => {
  const map = {
    'Incomplete': 'info',
    'Processing': 'primary',
    'Revision': 'warning',
    'Decided': 'success'
  }
  return map[status] || 'info'
}

const formatDate = (dateStr) => {
  if(!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

const getList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      status: activeTab.value
    }
    const res = await getMyManuscripts(params)
    if (res.code === 200) {
      tableData.value = res.data.list
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

const handleTabChange = (name) => {
  queryParams.pageNum = 1
  queryParams.status = name
  getList()
}

const handleEdit = (id) => {
  router.push({ path: '/manuscript/submit', query: { id } })
}

const handleDetail = (id, tab = 'info') => {
  router.push({ path: `/manuscript/detail/${id}`, query: { tab } })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.app-container {
  padding: 20px;
}
</style>