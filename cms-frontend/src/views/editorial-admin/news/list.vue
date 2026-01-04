<template>
  <el-card>
    <!-- header -->
    <template #header>
      <div class="header">
        <h2>新闻管理</h2>
        <el-button
            type="primary"
            icon="Plus"
            @click="$router.push('/editorial-admin/news/add')"
        >
          新增新闻
        </el-button>
      </div>
    </template>

    <!-- search -->
    <el-form :inline="true" :model="searchForm" class="mb-4">
      <el-form-item label="关键词">
        <el-input
            v-model="searchForm.keyword"
            placeholder="请输入标题关键词"
            style="width: 200px"
        />
      </el-form-item>

      <el-form-item label="日期范围">
        <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- table -->
    <el-table
        v-loading="loading"
        :data="currentPageData"
        border
        style="width: 100%"
    >
      <el-table-column prop="newsId" label="ID" width="80" />

      <el-table-column label="标题">
        <template #default="{ row }">
          {{ row.__empty ? '-' : row.title }}
        </template>
      </el-table-column>

      <el-table-column label="发布人ID" width="120">
        <template #default="{ row }">
          {{ row.__empty ? '-' : (row.publisherId ?? row.publisherName ?? '-') }}
        </template>
      </el-table-column>

      <el-table-column label="发布时间" width="180">
        <template #default="{ row }">
          {{ row.__empty ? '-' : formatPublishDate(row.publishDate) }}
        </template>
      </el-table-column>

      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="!row.__empty" :type="row.isActive ? 'success' : 'info'">
            {{ row.isActive ? '已发布' : '待发布' }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="!row.__empty">
            <el-button type="primary" size="small" @click="handleEdit(row.newsId)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.newsId)">
              删除
            </el-button>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- pagination -->
    <div class="pagination">
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import { getNewsList, deleteNews } from '@/api/editorialAdmin'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  keyword: '',
  dateRange: []
})

const EMPTY_ROW = { __empty: true }

const currentPageData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  const end = start + pageSize.value
  const list = tableData.value.slice(start, end)

  const lack = pageSize.value - list.length
  return lack > 0 ? list.concat(Array.from({ length: lack }, () => EMPTY_ROW)) : list
})

const formatPublishDate = (val) => (val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : '-')

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getNewsList({
      keyword: searchForm.keyword,
      startDate: searchForm.dateRange?.[0]?.toISOString?.() || '',
      endDate: searchForm.dateRange?.[1]?.toISOString?.() || ''
    })
    if (res.success) {
      tableData.value = (res.data || []).sort((a, b) => Number(a.newsId) - Number(b.newsId))
      total.value = tableData.value.length
    }
  } catch (e) {
    ElMessage.error('获取新闻列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchList()
}

const resetForm = () => {
  searchForm.keyword = ''
  searchForm.dateRange = []
  pageNum.value = 1
  fetchList()
}

const handleEdit = (newsId) => {
  router.push({ path: '/editorial-admin/news/edit', query: { newsId } })
}

const handleDelete = async (newsId) => {
  try {
    await ElMessageBox.confirm('确认删除该新闻？', '提示', { type: 'warning' })
    await deleteNews(newsId)
    ElMessage.success('删除成功')

    tableData.value = tableData.value.filter((i) => i.newsId !== newsId)
    total.value = tableData.value.length
    const maxPage = Math.ceil(total.value / pageSize.value)
    if (pageNum.value > maxPage) pageNum.value = Math.max(1, maxPage)
  } catch {}
}

onMounted(fetchList)
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination {
  margin-top: 16px;
  text-align: right;
}
</style>

<style>
.el-table .el-table__row,
.el-table .el-table__empty-row {
  height: 44px;
}
</style>
