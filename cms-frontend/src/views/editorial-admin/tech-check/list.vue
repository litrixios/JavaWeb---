<template>
  <el-card>
    <template #header>
      <h2>待形式审查稿件</h2>
    </template>

    <el-table
        class="tech-check-table"
        v-loading="loading"
        :data="pagedData"
        border
        size="small"
    >
      <el-table-column prop="manuscriptId" label="稿件ID" width="100" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="keywords" label="关键词" />
      <el-table-column prop="fundingInfo" label="项目资助情况" />
      <el-table-column label="作者" width="200">
        <template #default="{ row }">
          <!-- 先处理字符串格式的数组，再判断 -->
          <template v-if="row.authorList">
            {{
              // 尝试解析字符串为数组
              (() => {
                let authorData = row.authorList
                // 如果是字符串且以 [ 开头，尝试 JSON 解析
                if (typeof authorData === 'string' && authorData.startsWith('[')) {
                  try {
                    authorData = JSON.parse(authorData)
                  } catch (e) {
                    // 解析失败则保持原字符串
                    return authorData
                  }
                }
                // 再判断是否为数组
                return Array.isArray(authorData)
                    ? authorData.map(item => item.name || item).join('，')
                    : authorData
              })()
            }}
          </template>
          <template v-else>-</template>
        </template>
      </el-table-column>

      <el-table-column label="提交时间" width="190">
        <template #default="{ row }">
          {{ formatDateTime(row.submissionTime) }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="goDetail(row.manuscriptId)">
            形式审查
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
          v-model:current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTechCheckManuscripts } from '@/api/editorialAdmin'

const router = useRouter()
const loading = ref(false)

const rawData = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const pagedData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return rawData.value.slice(start, start + pageSize.value)
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getTechCheckManuscripts()
    if (res.success) {
      rawData.value = [...res.data].sort((a, b) => a.manuscriptId - b.manuscriptId)
      total.value = rawData.value.length
    }
  } finally {
    loading.value = false
  }
}

const goDetail = (manuscriptId) => {
  router.push({
    path: '/editorial-admin/tech-check/detail',
    query: { manuscriptId }
  })
}

onMounted(fetchList)

const formatDateTime = (val) => {
  if (!val) return '-'
  return String(val).replace('T', ' ').replace(/\.\d{3}\+.*$/, '')
}
</script>

<style>
.tech-check-table .el-table__cell {
  font-size: 15px;
  color: #303133;
}

.tech-check-table th.el-table__cell {
  font-size: 15px;
  font-weight: 600;
}

.el-table--small .el-table__row,
.el-table--small .el-table__empty-row {
  height: 44px;
}

.pagination-container {
  margin-top: 16px;
  text-align: right;
}
</style>
