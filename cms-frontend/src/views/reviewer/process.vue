<template>
  <div class="process-container" v-loading="loading">
    <div class="page-header">
      <el-button icon="ArrowLeft" @click="$router.back()">返回列表</el-button>
      <span class="title">
        稿件审阅 (Review ID: {{ reviewId }})
        <el-tag v-if="isReadOnly" type="success" effect="dark" style="margin-left: 10px;">已完成</el-tag>
      </span>
    </div>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="10">
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>稿件详情 (匿名)</span>
              <el-button type="primary" link @click="handleDownload">
                <el-icon><Download /></el-icon> 下载稿件全文
              </el-button>
            </div>
          </template>

          <div v-if="manuscript">
            <h3 class="manuscript-title">{{ manuscript.title }}</h3>
            <el-divider content-position="left">摘要</el-divider>
            <p class="abstract-content">{{ manuscript.abstractText || '无摘要内容' }}</p>
            <el-divider />
            <div class="meta-info">
              <p><strong>关键词：</strong> {{ manuscript.keywords || '-' }}</p>
            </div>
          </div>
          <el-empty v-else description="无法加载稿件信息" />
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card>
          <template #header>
            <span>审稿意见表</span>
          </template>

          <el-form :model="form" ref="reviewFormRef" :rules="rules" label-position="top" :disabled="isReadOnly">
            <div class="score-section">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="创新性" prop="innovationScore">
                    <el-rate v-model="form.innovationScore" show-score allow-half />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="方法论" prop="methodologyScore">
                    <el-rate v-model="form.methodologyScore" show-score allow-half />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="整体质量" prop="qualityScore">
                    <el-rate v-model="form.qualityScore" show-score allow-half />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <el-divider />

            <el-form-item label="给作者的意见 (公开)" prop="authorComments">
              <el-input v-model="form.authorComments" type="textarea" :rows="6" placeholder="请详细描述..." />
            </el-form-item>

            <el-form-item label="给编辑的意见 (保密)" prop="confidentialComments">
              <el-input v-model="form.confidentialComments" type="textarea" :rows="3" placeholder="仅编辑可见..." />
            </el-form-item>

            <el-divider />

            <el-form-item label="总体建议" prop="recommendation">
              <el-radio-group v-model="form.recommendation">
                <el-radio-button label="Accept">接受 (Accept)</el-radio-button>
                <el-radio-button label="Minor Revision">小修 (Minor Revision)</el-radio-button>
                <el-radio-button label="Major Revision">大修 (Major Revision)</el-radio-button>
                <el-radio-button label="Reject">拒稿 (Reject)</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <div class="form-actions" v-if="!isReadOnly">
              <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
                提交审稿意见
              </el-button>
            </div>
            <div v-else class="form-actions">
              <el-alert title="该审稿任务已完成，仅供查看。" type="info" :closable="false" center />
            </div>

          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getManuscriptForReview, downloadAnonymousManuscript, submitReview } from '@/api/reviewer'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const reviewId = route.params.reviewId

const loading = ref(false)
const submitting = ref(false)
const manuscript = ref(null)
const reviewFormRef = ref(null)
const isReadOnly = ref(false)

const form = reactive({
  reviewId: parseInt(reviewId),
  innovationScore: 0,
  methodologyScore: 0,
  qualityScore: 0,
  authorComments: '',
  confidentialComments: '',
  recommendation: ''
})

const rules = {
  qualityScore: [{ required: true, message: '请评分', trigger: 'change' }],
  authorComments: [{ required: true, message: '请填写给作者的意见', trigger: 'blur' }],
  recommendation: [{ required: true, message: '请选择总体建议', trigger: 'change' }]
}

const fetchDetails = async () => {
  loading.value = true
  try {
    const res = await getManuscriptForReview(reviewId)
    if (res.code === 200) {
      manuscript.value = res.data
    }
  } catch(e) {
    ElMessage.error("获取稿件信息失败")
  } finally {
    loading.value = false
  }
}

const handleDownload = async () => {
  try {
    const blob = await downloadAnonymousManuscript(reviewId)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `Manuscript_${reviewId}_Anonymous.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

const handleSubmit = () => {
  reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      ElMessageBox.confirm('提交后将无法修改，确定提交审稿意见吗？', '确认提交', { type: 'warning' })
          .then(async () => {
            submitting.value = true
            try {
              const res = await submitReview(form)
              if (res.code === 200) {
                ElMessage.success('审稿意见提交成功！')
                router.push('/reviewer/dashboard')
              }
            } finally { submitting.value = false }
          })
    }
  })
}

// 初始化
onMounted(() => {
  if (!reviewId) {
    ElMessage.error('缺少Review ID')
    router.push('/reviewer/dashboard')
    return
  }

  // 获取稿件详情（左侧）
  fetchDetails()

  // 检查是否有传递过来的历史数据（用于回显已完成的审稿）
  // 注意：这依赖于 dashboard.vue 中 router.push 的 state 传参
  const historyData = history.state?.reviewData
  if (historyData) {
    if (historyData.status === 'Completed') {
      isReadOnly.value = true
      // 回显数据 (注意：后端可能只存了一个 score，这里假设 score 对应 qualityScore)
      form.qualityScore = historyData.score || 0
      // 这里的 mapping 需要根据后端实际返回的字段名调整
      form.authorComments = historyData.commentsToAuthor || historyData.authorComments || ''
      form.confidentialComments = historyData.confidentialComments || ''
      form.recommendation = historyData.suggestion || historyData.recommendation || ''
    }
  }
})
</script>

<style scoped>
.process-container { padding: 20px; }
.page-header { display: flex; align-items: center; margin-bottom: 20px; }
.title { margin-left: 10px; font-size: 18px; font-weight: bold; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.manuscript-title { font-size: 18px; color: #303133; margin-bottom: 10px; }
.abstract-content { line-height: 1.6; color: #606266; text-align: justify; }
.meta-info p { margin: 5px 0; color: #909399; font-size: 14px; }
.form-actions { display: flex; justify-content: center; margin-top: 30px; }
</style>