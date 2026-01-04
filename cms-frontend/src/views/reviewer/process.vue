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
              <el-space>
                <el-button type="primary" link @click="handleDownload">
                  <el-icon><Download /></el-icon> 下载PDF
                </el-button>
                <el-button type="success" link @click="openPdfViewer">
                  <el-icon><View /></el-icon> 在线批注
                </el-button>
              </el-space>
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

          <el-form :model="form" ref="reviewFormRef" :rules="rules" label-position="top" :disabled="isReadOnly">

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
              <template #label>
                <span>给作者的意见 (Author Comments) - 公开 </span>
                <el-tooltip content="您可以使用左侧的'在线批注'功能自动生成意见" placement="top">
                  <el-icon><QuestionFilled /></el-icon>
                </el-tooltip>
              </template>
              <el-input
                  v-model="form.authorComments"
                  type="textarea"
                  :rows="8"
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

    <el-dialog
        v-model="pdfViewerVisible"
        title="在线审阅与批注"
        fullscreen
        destroy-on-close
        class="pdf-viewer-dialog"
    >
      <div style="height: calc(100vh - 120px); overflow-y: auto; background-color: #f0f2f5;">
        <PdfAnnotator
            v-if="pdfBlobUrl"
            :source="pdfBlobUrl"
            @update:annotations="handleAnnotationsUpdate"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="pdfViewerVisible = false">关闭</el-button>
          <el-button type="primary" @click="saveAnnotationsToComment" :disabled="isReadOnly">
            将批注汇总到意见框
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getManuscriptForReview, downloadAnonymousManuscript, submitReview } from '@/api/reviewer'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, ArrowLeft, View, QuestionFilled } from '@element-plus/icons-vue'
import PdfAnnotator from '@/components/PdfAnnotator.vue' // 引入我们新建的组件

const route = useRoute()
const router = useRouter()
const reviewId = route.params.reviewId

const loading = ref(false)
const submitting = ref(false)
const manuscript = ref(null)
const reviewFormRef = ref(null)
const isReadOnly = ref(false)

// PDF Viewer 相关状态
const pdfViewerVisible = ref(false)
const pdfBlobUrl = ref('')
const currentAnnotations = ref([])

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
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `Manuscript_${reviewId}_Anonymous.pdf`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 打开在线查看器
const openPdfViewer = async () => {
  loading.value = true
  try {
    // 复用下载接口获取 Blob
    const blob = await downloadAnonymousManuscript(reviewId)
    // 创建本地 Blob URL 供 PDF.js 读取
    if (pdfBlobUrl.value) window.URL.revokeObjectURL(pdfBlobUrl.value) // 清理旧的
    pdfBlobUrl.value = window.URL.createObjectURL(blob)
    pdfViewerVisible.value = true
  } catch (error) {
    ElMessage.error('无法加载PDF文件，请尝试直接下载')
  } finally {
    loading.value = false
  }
}

// 监听批注更新
const handleAnnotationsUpdate = (list) => {
  currentAnnotations.value = list
}

// 将批注汇总到意见框
const saveAnnotationsToComment = () => {
  if (currentAnnotations.value.length === 0) {
    ElMessage.info('当前没有添加任何批注')
    return
  }

  // 生成格式化的文本
  const summary = currentAnnotations.value
      .sort((a, b) => {
        if (a.page !== b.page) return a.page - b.page
        return a.y - b.y // 同一页按垂直位置排序
      })
      .map((note, index) => `${index + 1}. [第 ${note.page} 页] ${note.content}`)
      .join('\n')

  const separator = '\n\n=== 在线批注汇总 ===\n'

  // 智能追加：如果已经有内容，先换行
  if (form.authorComments) {
    form.authorComments += separator + summary
  } else {
    form.authorComments = summary
  }

  ElMessage.success('批注已成功添加到意见输入框中')
  pdfViewerVisible.value = false
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

// 生命周期钩子
onMounted(() => {
  if (!reviewId) {
    ElMessage.error('缺少Review ID')
    router.push('/reviewer/dashboard')
    return
  }

  fetchDetails()

  // 历史数据回显逻辑 (只读模式)
  const historyData = history.state?.reviewData
  if (historyData && historyData.status === 'Completed') {
    isReadOnly.value = true
    // 回显数据
    form.qualityScore = historyData.score || 0
    // 假设后端返回的数据结构中有这些字段
    form.innovationScore = historyData.innovationScore || 0
    form.methodologyScore = historyData.methodologyScore || 0

    // 兼容不同的字段命名
    form.authorComments = historyData.commentsToAuthor || historyData.authorComments || ''
    form.confidentialComments = historyData.confidentialComments || ''
    form.recommendation = historyData.suggestion || historyData.recommendation || ''
  }
})

// 组件销毁时释放内存
onUnmounted(() => {
  if (pdfBlobUrl.value) {
    window.URL.revokeObjectURL(pdfBlobUrl.value)
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
/* 覆盖 Dialog 样式 */
:deep(.pdf-viewer-dialog .el-dialog__body) {
  padding: 0;
  margin: 0;
}
</style>