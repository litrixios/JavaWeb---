<template>
  <div class="process-container" v-loading="loading">
    <div class="page-header">
      <el-button icon="ArrowLeft" @click="$router.back()">返回列表</el-button>
      <span class="title">稿件审阅 (Review ID: {{ reviewId }})</span>
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
              <p><strong>分类：</strong> {{ manuscript.category || '-' }}</p>
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

          <el-form :model="form" ref="reviewFormRef" :rules="rules" label-position="top">

            <div class="score-section">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="创新性 (Innovation)" prop="innovationScore">
                    <el-rate v-model="form.innovationScore" show-score allow-half />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="方法论 (Methodology)" prop="methodologyScore">
                    <el-rate v-model="form.methodologyScore" show-score allow-half />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="整体质量 (Quality)" prop="qualityScore">
                    <el-rate v-model="form.qualityScore" show-score allow-half />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>

            <el-divider />

            <el-form-item label="给作者的意见 (Author Comments) - 公开" prop="authorComments">
              <el-input
                  v-model="form.authorComments"
                  type="textarea"
                  :rows="6"
                  placeholder="请详细描述稿件的优缺点，以及修改建议..."
              />
            </el-form-item>

            <el-form-item label="给编辑的意见 (Confidential Comments) - 保密" prop="confidentialComments">
              <el-input
                  v-model="form.confidentialComments"
                  type="textarea"
                  :rows="3"
                  placeholder="仅编辑可见的内容（可选）"
              />
            </el-form-item>

            <el-divider />

            <el-form-item label="总体建议 (Recommendation)" prop="recommendation">
              <el-radio-group v-model="form.recommendation">
                <el-radio-button label="Accept" value="Accept">接受 (Accept)</el-radio-button>
                <el-radio-button label="Minor Revision" value="Minor Revision">小修 (Minor Revision)</el-radio-button>
                <el-radio-button label="Major Revision" value="Major Revision">大修 (Major Revision)</el-radio-button>
                <el-radio-button label="Reject" value="Reject">拒稿 (Reject)</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <div class="form-actions">
              <el-button type="primary" size="large" @click="handleSubmit" :loading="submitting">
                提交审稿意见
              </el-button>
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
  innovationScore: [{ required: true, message: '请评分', trigger: 'change' }],
  methodologyScore: [{ required: true, message: '请评分', trigger: 'change' }],
  qualityScore: [{ required: true, message: '请评分', trigger: 'change' }],
  authorComments: [{ required: true, message: '请填写给作者的意见', trigger: 'blur' }],
  recommendation: [{ required: true, message: '请选择总体建议', trigger: 'change' }]
}

// 获取详情
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

// 下载文件
const handleDownload = async () => {
  try {
    const blob = await downloadAnonymousManuscript(reviewId)
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    // 假设文件名，或者从 response header 获取
    link.setAttribute('download', `Manuscript_${reviewId}_Anonymous.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 提交表单
const handleSubmit = () => {
  reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      ElMessageBox.confirm(
          '提交后将无法修改，确定提交审稿意见吗？',
          '确认提交',
          { type: 'warning' }
      ).then(async () => {
        submitting.value = true
        try {
          const res = await submitReview(form)
          if (res.code === 200) {
            ElMessage.success('审稿意见提交成功！')
            router.push('/reviewer/dashboard')
          }
        } finally {
          submitting.value = false
        }
      })
    } else {
      ElMessage.error('请填写完整的审稿意见')
      return false
    }
  })
}

onMounted(() => {
  if (reviewId) {
    fetchDetails()
  } else {
    ElMessage.error('缺少Review ID')
    router.push('/reviewer/dashboard')
  }
})
</script>

<style scoped>
.process-container {
  padding: 20px;
}
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.title {
  margin-left: 10px;
  font-size: 18px;
  font-weight: bold;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.manuscript-title {
  font-size: 18px;
  color: #303133;
  margin-bottom: 10px;
}
.abstract-content {
  line-height: 1.6;
  color: #606266;
  text-align: justify;
}
.meta-info p {
  margin: 5px 0;
  color: #909399;
  font-size: 14px;
}
.score-section {
  padding: 0 10px;
}
.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>