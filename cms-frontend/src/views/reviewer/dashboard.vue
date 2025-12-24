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
            <el-table-column prop="inviteDate" label="邀请时间" width="180">
              <template #default="{ row }">
                {{ formatDate(row.inviteDate || row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="success" size="small" @click="handleAccept(row)">接受</el-button>
                <el-button type="danger" size="small" @click="openRejectDialog(row)">拒绝</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的审稿任务 (My Reviews)" name="MY_REVIEWS">
          <el-table v-loading="loading" :data="myReviewList" style="width: 100%">
            <el-table-column prop="reviewId" label="任务ID" width="80" />
            <el-table-column prop="manuscriptTitle" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusTag(row.status)">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deadline" label="截止日期" width="160">
              <template #default="{ row }">
                <span :class="{ 'text-danger': isOverdue(row.deadline) }">
                  {{ formatDate(row.deadline) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="完成时间" width="160">
              <template #default="{ row }">
                {{ row.submitTime ? formatDate(row.submitTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                    v-if="row.status === 'Accepted'"
                    type="primary"
                    size="small"
                    @click="goReview(row)"
                >
                  <el-icon><EditPen /></el-icon> 去审稿
                </el-button>
                <el-button
                    v-else-if="row.status === 'Completed'"
                    type="info"
                    size="small"
                    plain
                    @click="goReview(row)"
                >
                  <el-icon><View /></el-icon> 查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
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
import { getPendingInvitations, respondToInvitation, getMyReviews } from '@/api/reviewer'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, View } from '@element-plus/icons-vue'

const router = useRouter()
const activeTab = ref('INVITATIONS')
const loading = ref(false)
const invitationList = ref([])
const myReviewList = ref([])

// 拒绝弹窗相关
const rejectDialogVisible = ref(false)
const currentReviewId = ref(null)
const rejectForm = reactive({ reason: '' })

// 工具函数
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString()
}

const getStatusTag = (status) => {
  const map = { 'Accepted': 'warning', 'Completed': 'success', 'Overdue': 'danger' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { 'Accepted': '正在审稿', 'Completed': '已完成', 'Overdue': '已逾期' }
  return map[status] || status
}

const isOverdue = (deadline) => {
  if (!deadline) return false
  return new Date() > new Date(deadline)
}

// 获取待处理邀请
const fetchInvitations = async () => {
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

// [新增] 获取我的审稿任务
const fetchMyReviews = async () => {
  loading.value = true
  try {
    const res = await getMyReviews()
    if (res.code === 200) {
      myReviewList.value = res.data
    }
  } finally {
    loading.value = false
  }
}

// Tab 切换逻辑
const handleTabChange = (name) => {
  if (name === 'INVITATIONS') {
    fetchInvitations()
  } else if (name === 'MY_REVIEWS') {
    fetchMyReviews()
  }
}

// 接受邀请
const handleAccept = (row) => {
  ElMessageBox.confirm('确认接受该稿件的审稿邀请吗？', '接受邀请', {
    confirmButtonText: '确定', cancelButtonText: '取消', type: 'info'
  }).then(async () => {
    try {
      const res = await respondToInvitation({ reviewId: row.reviewId, isAccepted: true })
      console.log(res)
      if (res.code === 200) {
        ElMessage.success('已接受邀请')
        fetchInvitations() // 刷新当前列表
        // 自动跳转到"我的任务"并刷新
        activeTab.value = 'MY_REVIEWS'
        fetchMyReviews()
      }
    } catch (e) {}
  })
}

// 拒绝逻辑
const openRejectDialog = (row) => {
  currentReviewId.value = row.reviewId
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason.trim()) return ElMessage.warning('请填写拒绝理由')
  try {
    const res = await respondToInvitation({
      reviewId: currentReviewId.value, isAccepted: false, reason: rejectForm.reason
    })
    if (res.code === 200) {
      ElMessage.success('已拒绝邀请')
      rejectDialogVisible.value = false
      fetchInvitations()
    }
  } catch (e) {}
}

// 跳转去审稿页面（如果是已完成，传递 reviewData 供回显）
const goReview = (row) => {
  router.push({
    name: 'ReviewProcess', // 确保你的路由配置里 name 是 ReviewProcess，或者用 path: `/reviewer/process/${row.reviewId}`
    params: { reviewId: row.reviewId },
    state: { reviewData: row } // 将当前行数据传递过去，用于详情回显
  })
}

onMounted(() => {
  fetchInvitations()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.text-danger { color: #f56c6c; font-weight: bold; }
</style>