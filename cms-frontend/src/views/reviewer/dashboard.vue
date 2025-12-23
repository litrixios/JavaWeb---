<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>审稿工作台</span>
          <el-tag type="info">Reviewer Workspace</el-tag>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待处理邀请 (Invitations)" name="INVITATIONS">
          <el-table v-loading="loading" :data="invitationList" style="width: 100%">
            <el-table-column prop="reviewId" label="任务ID" width="80" />
            <el-table-column prop="manuscriptTitle" label="稿件标题" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.manuscriptTitle || '（暂无标题信息/盲审）' }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="邀请时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleAccept(row)">
                  接受
                </el-button>
                <el-button type="danger" size="small" @click="openRejectDialog(row)">
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="正在审稿 (Processing)" name="PROCESSING">
          <el-empty description="后端暂未提供获取'已接受任务'的接口，请先实现 backend: getMyActiveReviews" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="拒绝审稿邀请" width="30%">
      <el-form :model="rejectForm">
        <el-form-item label="拒绝理由" required>
          <el-input
              v-model="rejectForm.reason"
              type="textarea"
              rows="3"
              placeholder="请说明拒绝理由，例如：专业不符、时间冲突等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确认拒绝</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingInvitations, respondToInvitation } from '@/api/reviewer'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('INVITATIONS')
const loading = ref(false)
const invitationList = ref([])

// 拒绝弹窗相关
const rejectDialogVisible = ref(false)
const currentReviewId = ref(null)
const rejectForm = reactive({
  reason: ''
})

// 格式化时间
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

// 获取邀请列表
const getInvitations = async () => {
  loading.value = true
  try {
    const res = await getPendingInvitations()
    if (res.code === 200) {
      invitationList.value = res.data
    }
  } finally {
    loading.value = false
  }
}

// 接受邀请
const handleAccept = (row) => {
  ElMessageBox.confirm(
      `确认接受该稿件的审稿邀请吗？`,
      '接受邀请',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
  ).then(async () => {
    try {
      const res = await respondToInvitation({
        reviewId: row.reviewId,
        isAccepted: true
      })
      if (res.code === 200) {
        ElMessage.success(res.data || '已接受邀请')
        getInvitations() // 刷新列表
        // 如果有"进行中"列表，可以在这里切换 Tab
        // activeTab.value = 'PROCESSING'

        // 临时逻辑：直接跳转去审稿（因为列表里它会消失）
        router.push(`/reviewer/process/${row.reviewId}`)
      }
    } catch (error) {
      // error handled in request.js
    }
  })
}

// 打开拒绝弹窗
const openRejectDialog = (row) => {
  currentReviewId.value = row.reviewId
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  if (!rejectForm.reason.trim()) {
    ElMessage.warning('请填写拒绝理由')
    return
  }
  try {
    const res = await respondToInvitation({
      reviewId: currentReviewId.value,
      isAccepted: false,
      reason: rejectForm.reason
    })
    if (res.code === 200) {
      ElMessage.success('已拒绝该邀请')
      rejectDialogVisible.value = false
      getInvitations()
    }
  } catch (error) {
    console.error(error)
  }
}

// 切换 Tab
const handleTabChange = (name) => {
  if (name === 'INVITATIONS') {
    getInvitations()
  }
  // else if (name === 'PROCESSING') { getMyActiveReviews() }
}

// 模拟跳转审稿（给开发测试用，实际应从 Processing 列表跳转）
const goReview = (id) => {
  router.push(`/reviewer/process/${id}`)
}

onMounted(() => {
  getInvitations()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>