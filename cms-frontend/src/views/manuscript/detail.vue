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
              <span class="cycle-tip" v-if="trackInfo.estimatedCycle">预计周期: {{ trackInfo.estimatedCycle }}</span>
            </div>

            <el-steps :active="activeStep" align-center style="margin: 30px 0;">
              <el-step title="未完成" />
              <el-step title="处理中" />
              <el-step title="修回中" />
              <el-step title="决议中" />
            </el-steps>

            <h4>历史记录 (History)</h4>
            <el-timeline>
              <el-timeline-item
                  v-for="(log, index) in historyLogs"
                  :key="index"
                  :timestamp="formatDate(log.operateTime)"
                  placement="top"
              >
                <el-card shadow="hover">
                  <h4>{{ log.operatorName }} 执行了 {{ log.operateType }}</h4>
                  <p>{{ log.content }}</p>
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

        <el-tab-pane label="沟通 (Communication)" name="communication">
          <div class="chat-container">
            <div class="message-list">
              <div v-for="msg in messages" :key="msg.id" class="message-item">
                <div class="message-meta">
                  <span class="sender">{{ msg.senderName }}</span>
                  <span class="time">{{ formatDate(msg.sendTime) }}</span>
                </div>
                <div class="message-content">{{ msg.content }}</div>
              </div>
              <el-empty v-if="messages.length === 0" description="暂无沟通记录" />
            </div>

            <div class="message-input">
              <el-input
                  v-model="newMessage"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入消息内容，发送给编辑..."
              />
              <div style="margin-top: 10px; text-align: right;">
                <el-button type="primary" @click="handleSendMessage">发送消息</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { trackManuscript, submitRevision } from '@/api/manuscript'
import { getManuscriptHistory, sendMessage } from '@/api/message'
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

// 修回表单 - 修改部分：增加 anonymousFilePath
const revisionForm = reactive({
  manuscriptId: parseInt(manuscriptId),
  originalFilePath: null,
  anonymousFilePath: null,
  markedFilePath: null,
  responseLetterPath: null
})

// 消息数据
const messages = ref([])
const newMessage = ref('')

// (以下 computed, formatDate 等辅助函数保持不变)
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
    loadMessages()
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  const res = await getManuscriptHistory(manuscriptId)
  if (res.code === 200) {
    messages.value = res.data
  }
}

// 修改部分：修回提交校验
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

const handleSendMessage = async () => {
  if (!newMessage.value.trim()) return
  const payload = {
    receiverId: manuscript.value.currentEditorId || 2,
    topic: `MS-${manuscriptId}`,
    title: `关于稿件 ${manuscriptId} 的沟通`,
    content: newMessage.value
  }
  const res = await sendMessage(payload)
  if (res.code === 200) {
    ElMessage.success('发送成功')
    newMessage.value = ''
    loadMessages()
  } else {
    ElMessage.error(res.msg || '消息发送失败，请稍后重试')
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
.cycle-tip {
  font-size: 13px;
  color: #666;
}
.tip {
  font-size: 12px;
  color: #999;
}
.chat-container {
  max-width: 800px;
}
.message-list {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  height: 400px;
  overflow-y: auto;
  margin-bottom: 20px;
}
.message-item {
  background: #fff;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}
.message-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
}
.message-content {
  font-size: 14px;
  color: #333;
}
</style>