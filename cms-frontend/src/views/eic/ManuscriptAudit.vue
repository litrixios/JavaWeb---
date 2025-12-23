<template>
  <div class="app-container" style="padding: 20px;">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>稿件全览与决策中心</span>
        </div>
      </template>

      <el-table :data="manuscriptList" border v-loading="loading" style="width: 100%">
        <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" />
        <el-table-column prop="title" label="稿件标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="authorList" label="作者列表" width="150" />

        <el-table-column label="投稿时间" width="180">
          <template #default="scope">
            {{ scope.row.submissionTime ? scope.row.submissionTime.substring(0, 19).replace('T', ' ') : '无' }}
          </template>
        </el-table-column>

        <el-table-column prop="status" label="主状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="subStatus" label="子状态" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getSubStatusType(scope.row.subStatus)" size="small">
              {{ scope.row.subStatus || '无' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="scope">
            <!-- 只对状态为 Processing 且子状态为 PendingAssign 的稿件显示指派按钮 -->
            <el-button
                v-if="scope.row.status === 'Processing' && scope.row.subStatus === 'PendingAssign'"
                type="primary"
                size="small"
                @click="openAssignDialog(scope.row)"
            >指派编辑</el-button>

            <!-- 初审操作 -->
            <el-button
                v-if="scope.row.status === 'Incomplete'"
                type="success"
                size="small"
                @click="openDeskReviewDialog(scope.row)"
            >初审</el-button>

            <el-button
                type="danger"
                size="small"
                @click="handleWithdraw(scope.row)"
            >撤稿</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 指派编辑对话框 -->
    <el-dialog v-model="assignVisible" title="指派副主编" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="稿件标题">
          <el-input :value="tempTitle" disabled />
        </el-form-item>
        <el-form-item label="当前状态">
          <el-tag type="info">Processing / PendingAssign</el-tag>
        </el-form-item>
        <el-form-item label="选择编辑">
          <el-select v-model="assignForm.editorId" placeholder="请选择副主编" style="width: 100%">
            <el-option
                v-for="item in editorOptions"
                :key="item.userId"
                :label="item.fullName"
                :value="item.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确认指派</el-button>
      </template>
    </el-dialog>

    <!-- 初审对话框 -->
    <el-dialog v-model="deskReviewVisible" title="稿件初审" width="400px">
      <el-form :model="deskReviewForm" label-width="80px">
        <el-form-item label="稿件标题">
          <el-input :value="tempTitle" disabled />
        </el-form-item>
        <el-form-item label="初审决策">
          <el-radio-group v-model="deskReviewForm.decision">
            <el-radio label="DeskAccept">通过初审</el-radio>
            <el-radio label="DeskReject">直接拒稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评审意见">
          <el-input
              v-model="deskReviewForm.comments"
              type="textarea"
              :rows="3"
              placeholder="请输入评审意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deskReviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDeskReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllManuscripts, assignEditor, withdrawManuscript, deskReview } from '@/api/eic'

const manuscriptList = ref([])
const loading = ref(false)
const assignVisible = ref(false)
const deskReviewVisible = ref(false)
const tempTitle = ref('')
const assignForm = ref({manuscriptId: null, editorId: null, comments: ''})
const deskReviewForm = ref({manuscriptId: null, decision: '', comments: ''})

// 模拟编辑数据
const editorOptions = ref([
  {userId: 4, fullName: '周涛 (副主编)'},
  {userId: 5, fullName: '李娜 (副主编)'}
])

// 1. 加载数据
const fetchManuscripts = async () => {
  loading.value = true
  try {
    const res = await getAllManuscripts()
    if (res && res.code === 200) {
      manuscriptList.value = res.data || []
    } else {
      ElMessage.error(res.msg || '获取数据失败')
    }
  } catch (error) {
    console.error('接口请求异常:', error)
    ElMessage.error('无法连接服务器，请检查后端服务')
  } finally {
    loading.value = false
  }
}

// 2. 状态标签颜色过滤
const getStatusType = (status) => {
  const map = {
    'Incomplete': 'info',
    'Processing': 'warning',
    'Revision': 'primary',
    'Decided': 'success',
    'Rejected': 'danger'
  }
  return map[status] || ''
}

const getSubStatusType = (subStatus) => {
  const map = {
    'PendingAssign': 'info',
    'WithEditor': 'warning',
    'UnderReview': 'primary',
    'Accepted': 'success',
    'Rejected': 'danger'
  }
  return map[subStatus] || ''
}

// 3. 处理撤稿
const handleWithdraw = (row) => {
  ElMessageBox.prompt('请输入撤稿理由', '警告', {
    confirmButtonText: '确定撤稿',
    cancelButtonText: '取消',
    inputPattern: /\S/,
    inputErrorMessage: '理由不能为空'
  }).then(async ({ value }) => {
    try {
      const res = await withdrawManuscript({
        manuscriptId: row.manuscriptId,
        comments: value
      })
      if (res.code === 200) {
        ElMessage.success('已成功强制撤稿')
        fetchManuscripts()
      }
    } catch (e) {
      ElMessage.error('操作失败')
    }
  })
}

// 4. 指派编辑弹窗逻辑
const openAssignDialog = (row) => {
  tempTitle.value = row.title
  assignForm.value.manuscriptId = row.manuscriptId
  assignForm.value.editorId = null
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!assignForm.value.editorId) return ElMessage.warning('请选择编辑')

  loading.value = true
  try {
    const payload = {
      manuscriptId: assignForm.value.manuscriptId,
      editorId: assignForm.value.editorId,
      comments: '指派副主编处理稿件'
    }

    const res = await assignEditor(payload)
    if (res && res.code === 200) {
      ElMessage.success('指派成功')
      assignVisible.value = false
      await fetchManuscripts()
    } else {
      ElMessage.error(res.msg || '指派失败')
    }
  } catch (e) {
    console.error('请求发生异常:', e)
    ElMessage.error(e.message || '指派编辑失败')
  } finally {
    loading.value = false
  }
}

// 5. 初审操作
const openDeskReviewDialog = (row) => {
  tempTitle.value = row.title
  deskReviewForm.value.manuscriptId = row.manuscriptId
  deskReviewForm.value.decision = ''
  deskReviewForm.value.comments = ''
  deskReviewVisible.value = true
}

const submitDeskReview = async () => {
  if (!deskReviewForm.value.decision) return ElMessage.warning('请选择初审决策')
  if (!deskReviewForm.value.comments) return ElMessage.warning('请输入评审意见')

  loading.value = true
  try {
    const payload = {
      manuscriptId: deskReviewForm.value.manuscriptId,
      decision: deskReviewForm.value.decision,
      comments: deskReviewForm.value.comments
    }

    const res = await deskReview(payload)
    if (res && res.code === 200) {
      ElMessage.success('初审完成')
      deskReviewVisible.value = false
      await fetchManuscripts()
    } else {
      ElMessage.error(res.msg || '初审失败')
    }
  } catch (e) {
    console.error('请求发生异常:', e)
    ElMessage.error(e.message || '初审操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchManuscripts()
})
</script>