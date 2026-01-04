<template>
  <div class="app-container">
    <el-card>
      <el-tabs v-model="activeTab">

        <el-tab-pane label="稿件状态 (Track)" name="track">
          <div v-loading="loading">
            <div class="status-header">
              <h3>{{ manuscript.title }}</h3>
              <el-tag size="large" :type="statusType">
                {{ manuscript.subStatus || manuscript.status }}
              </el-tag>
            </div>

            <el-steps :active="activeStep" align-center style="margin: 30px 0;">
              <el-step title="未完成" />
              <el-step title="处理中" />
              <el-step title="修回中" />
              <el-step title="决议中" />
            </el-steps>

            <div v-if="trackInfo.reviewOpinions && trackInfo.reviewOpinions.length > 0" class="review-section">
              <el-divider content-position="left"><h3>审稿人意见 (Review Comments)</h3></el-divider>

              <el-card v-for="(item, index) in trackInfo.reviewOpinions" :key="index" class="review-card" shadow="never">
                <template #header>
                  <div class="review-header">
                    <span class="reviewer-name">{{ item.reviewerAlias }}</span>
                    <el-tag v-if="item.suggestion" size="small" effect="plain">{{ item.suggestion }}</el-tag>
                  </div>
                </template>
                <div class="review-content">
                  <pre>{{ item.comments }}</pre>
                </div>
              </el-card>
              <div style="margin-bottom: 30px;"></div> </div>

            <h4>历史记录 (History)</h4>
            <el-timeline>
              <el-timeline-item
                  v-for="(log, index) in historyLogs"
                  :key="index"
                  :timestamp="formatDate(log.operateTime)"
                  placement="top"
              >
                <el-card shadow="hover">
                  <h4>{{ log.operatorName }} 执行了 {{ log.operationType }}</h4>
                  <p>{{ log.description }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-tab-pane>

        <el-tab-pane label="提交修回 (Revision)" name="revision" :disabled="manuscript.status !== 'Revision'">
          <el-form label-width="140px" style="max-width: 800px; margin-top: 20px;">
            <el-alert title="请根据审稿意见上传修改后的文件" type="warning" show-icon :closable="false" style="margin-bottom: 20px;" />

            <el-form-item label="修改稿 (Clean)">
              <el-upload
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :limit="1"
                  :on-success="(res) => revisionForm.originalFilePath = res.data"
                  :on-error="handleUploadError"
              >
                <el-button>上传 Clean Version PDF</el-button>
              </el-upload>
            </el-form-item>

            <el-form-item label="匿名稿 (Anonymous)" required>
              <el-upload
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :limit="1"
                  :on-success="(res) => revisionForm.anonymousFilePath = res.data"
                  :on-error="handleUploadError"
              >
                <el-button type="primary">上传 Anonymous PDF</el-button>
              </el-upload>
              <div class="tip">必须上传 (用于第二轮盲审)</div>
            </el-form-item>

            <el-form-item label="标记稿 (Marked)" required>
              <el-upload
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :limit="1"
                  :on-success="(res) => revisionForm.markedFilePath = res.data"
                  :on-error="handleUploadError"
              >
                <el-button type="primary">上传 Marked Version PDF</el-button>
              </el-upload>
              <div class="tip">必须上传</div>
            </el-form-item>

            <el-form-item label="回复信 (Response)" required>
              <el-upload
                  action="/api/common/upload"
                  :headers="uploadHeaders"
                  :limit="1"
                  :on-success="(res) => revisionForm.responseLetterPath = res.data"
                  :on-error="handleUploadError"
              >
                <el-button type="primary">上传 Response Letter</el-button>
              </el-upload>
              <div class="tip">必须上传，逐条回复审稿人意见</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleRevisionSubmit">提交修回</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { trackManuscript, submitRevision } from '@/api/manuscript'
import { ElMessage } from 'element-plus'

const route = useRoute()
const manuscriptId = route.params.id
const activeTab = ref(route.query.tab || 'track')
const loading = ref(false)

const token = localStorage.getItem('token')
const uploadHeaders = {
  Authorization: token
}
const handleUploadError = (err) => {
  ElMessage.error('上传失败，请检查登录状态')
  console.error(err)
}

// 详情数据
const manuscript = ref({})
const historyLogs = ref([])
const trackInfo = ref({})

// 修回表单
const revisionForm = reactive({
  manuscriptId: parseInt(manuscriptId),
  originalFilePath: null,
  anonymousFilePath: null,
  markedFilePath: null,
  responseLetterPath: null
})

const statusType = computed(() => {
  const s = manuscript.value.status || ''
  const sub = manuscript.value.subStatus || ''
  if (s === 'Decided') {
    return sub === 'Accepted' ? 'success' : 'danger'
  }
  if (s === 'Revision') return 'warning'
  if (s === 'Processing') return 'primary'
  return 'info'
})

const activeStep = computed(() => {
  const s = manuscript.value.status || ''
  if (!s) return 0
  if (s === 'Incomplete') return 0
  if (s === 'Processing') return 1
  if (s === 'Revision')   return 2
  if (s === 'Decided')    return 4
  return 0
})

const formatDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleString()
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await trackManuscript(manuscriptId)
    if (res.code === 200 && res.data) {
      trackInfo.value = res.data
      if (res.data.manuscript) {
        manuscript.value = res.data.manuscript
      } else {
        manuscript.value = {}
      }
      historyLogs.value = res.data.historyLogs || []
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleRevisionSubmit = async () => {
  if (!revisionForm.markedFilePath || !revisionForm.responseLetterPath || !revisionForm.anonymousFilePath) {
    ElMessage.error('请上传匿名稿、标记稿和回复信')
    return
  }
  const res = await submitRevision(revisionForm)
  if (res.code === 200) {
    ElMessage.success('修回提交成功')
    loadData()
    activeTab.value = 'track'
  } else {
    ElMessage.error(res.msg || '修回提交失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.status-header {
  display: flex;
  align-items: center;
  gap: 15px;
}
.tip {
  font-size: 12px;
  color: #999;
}

/* [新增] 审稿意见样式 */
.review-section {
  margin-top: 20px;
}
.review-card {
  margin-bottom: 15px;
  border-left: 4px solid #409EFF; /* 左侧蓝色提示条 */
}
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
.review-content pre {
  white-space: pre-wrap; /* 保留换行符 */
  font-family: inherit;
  margin: 0;
  color: #333;
  line-height: 1.6;
}
</style>