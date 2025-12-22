<template>
  <div class="app-container">
    <el-card>
      <el-tabs v-model="activeTab">

        <el-tab-pane label="稿件状态 (Track)" name="track">
          <div v-loading="loading">
            <div class="status-header">
              <h3>{{ manuscript.title }}</h3>
              <small style="color: #999; font-weight: normal;">(Status: {{ manuscript.status }})</small>

              <el-tag size="large" :type="statusType">
                {{ formatFullStatus(manuscript) }}
              </el-tag>
              <span class="cycle-tip" v-if="trackInfo.estimatedCycle">预计周期: {{ trackInfo.estimatedCycle }}</span>
            </div>

            <el-steps :active="activeStepIndex" align-center finish-status="success" style="margin: 30px 0;">
              <el-step title="未完成 (Incomplete)" description="Draft / Saved" />

              <el-step
                  title="处理中 (Processing)"
                  :description="processingDesc"
              >
                <template #icon v-if="manuscript.status === 'Processing'">
                  <el-icon class="is-loading"><Loading /></el-icon>
                </template>
              </el-step>

              <el-step title="需修回 (Revision)" description="Waiting for Author" />

              <el-step
                  title="已决议 (Decided)"
                  :description="decidedDesc"
                  :status="decidedStepStatus"
              />
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

        <el-tab-pane label="提交修回 (Revision)" name="revision" :disabled="!isRevisionStatus">
          <el-form label-width="140px" style="max-width: 800px; margin-top: 20px;">
            <el-alert title="请根据审稿意见上传修改后的文件" type="warning" show-icon :closable="false" style="margin-bottom: 20px;" />

            <el-form-item label="修改稿 (Clean)">
              <el-upload action="/api/common/upload" :limit="1" :on-success="(res) => revisionForm.originalFilePath = res.data">
                <el-button>上传 Clean Version PDF</el-button>
              </el-upload>
            </el-form-item>

            <el-form-item label="标记稿 (Marked)" required>
              <el-upload action="/api/common/upload" :limit="1" :on-success="(res) => revisionForm.markedFilePath = res.data">
                <el-button type="primary">上传 Marked Version PDF</el-button>
              </el-upload>
              <div class="tip">必须上传</div>
            </el-form-item>

            <el-form-item label="回复信 (Response)" required>
              <el-upload action="/api/common/upload" :limit="1" :on-success="(res) => revisionForm.responseLetterPath = res.data">
                <el-button type="primary">上传 Response Letter</el-button>
              </el-upload>
              <div class="tip">必须上传，逐条回复审稿人意见</div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleRevisionSubmit">提交修回</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane
            label="沟通 (Communication)"
            name="communication"
            v-if="shouldShowCommunication"
        >
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
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { trackManuscript, submitRevision } from '@/api/manuscript'
import { getManuscriptHistory, sendMessage } from '@/api/message'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const route = useRoute()
const manuscriptId = route.params.id
const activeTab = ref(route.query.tab || 'track')
const loading = ref(false)

// 详情数据
const manuscript = ref({})
const historyLogs = ref([])
const trackInfo = ref({})

// 修回表单
const revisionForm = reactive({
  manuscriptId: parseInt(manuscriptId),
  originalFilePath: null,
  markedFilePath: null,
  responseLetterPath: null
})

// 消息数据
const messages = ref([])
const newMessage = ref('')

// --- 判定是否为修回状态 ---
const isRevisionStatus = computed(() => {
  const s = manuscript.value.status
  // 兼容 'Need Revision' 和 'Revision'
  return s && (s === 'Revision' || s === 'Need Revision')
})

// --- 核心修改：沟通 Tab 显示逻辑 (白名单模式) ---
const shouldShowCommunication = computed(() => {
  const s = manuscript.value.status
  if (!s) return false // 数据还没加载回来，先不显示

  // 1. 如果是 Revision (包括 Need Revision), 肯定显示
  if (isRevisionStatus.value) return true

  // 2. 如果是已决议 (Decided, Accepted, Rejected), 显示
  if (['Decided', 'Accepted', 'Rejected'].includes(s)) return true

  // 3. 剩下的通常是 Processing 或 Incomplete -> 不显示
  // 如果您发现某些 Processing 状态也想显示，可以在这里把 return false 改为特定判断
  return false
})

// --- 状态标签颜色 ---
const statusType = computed(() => {
  const s = manuscript.value.status
  const sub = manuscript.value.subStatus
  if (s === 'Decided' || s === 'Accepted') {
    return (sub === 'Accepted' || s === 'Accepted') ? 'success' : 'danger'
  }
  if (isRevisionStatus.value) return 'warning'
  if (s === 'Incomplete') return 'info'
  return 'primary'
})

// --- 进度条映射 ---
const activeStepIndex = computed(() => {
  const s = manuscript.value.status
  if (!s || s === 'Incomplete') return 0
  if (s === 'Processing') return 1
  if (isRevisionStatus.value) return 2
  if (['Decided', 'Accepted', 'Rejected'].includes(s)) return 4
  return 0
})

// --- 进度条描述文字 ---
const processingDesc = computed(() => {
  if (manuscript.value.status === 'Processing') {
    return `当前阶段: ${manuscript.value.subStatus || 'Unknown'}`
  }
  if (isRevisionStatus.value || ['Decided', 'Accepted', 'Rejected'].includes(manuscript.value.status)) {
    return 'Completed'
  }
  return ''
})

const decidedDesc = computed(() => {
  if (['Decided', 'Accepted', 'Rejected'].includes(manuscript.value.status)) {
    return `结果: ${manuscript.value.subStatus || manuscript.value.status}`
  }
  return ''
})

const decidedStepStatus = computed(() => {
  const s = manuscript.value.status
  const sub = manuscript.value.subStatus
  if (['Decided', 'Accepted', 'Rejected'].includes(s)) {
    return (sub === 'Rejected' || s === 'Rejected') ? 'error' : 'success'
  }
  return 'wait'
})

const formatFullStatus = (m) => {
  if (!m.status) return ''
  if (m.subStatus) return `${m.status} - ${m.subStatus}`
  return m.status
}

const formatDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleString()
}

// 初始化加载
const loadData = async () => {
  loading.value = true
  try {
    const res = await trackManuscript(manuscriptId)
    if (res.code === 200) {
      trackInfo.value = res.data
      manuscript.value = res.data.manuscript || {}
      historyLogs.value = res.data.historyLogs || []

      // 自动修正 Tab (如果当前选了沟通但不可见，切回Track)
      if (activeTab.value === 'communication' && !shouldShowCommunication.value) {
        activeTab.value = 'track'
      }
    }
  } finally {
    loading.value = false
  }
}

const loadMessages = async () => {
  try {
    const res = await getManuscriptHistory(manuscriptId)
    if (res.code === 200) {
      messages.value = res.data
    }
  } catch (e) {
    console.warn('获取消息失败', e)
  }
}

// --- 新增：监听器 ---
// 只要沟通面板应该显示了，就立即去加载消息数据
watch(shouldShowCommunication, (newVal) => {
  if (newVal) {
    loadMessages()
  }
})

// 提交修回
const handleRevisionSubmit = async () => {
  if (!revisionForm.markedFilePath || !revisionForm.responseLetterPath) {
    ElMessage.error('请上传标记稿和回复信')
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

// 发送消息
const handleSendMessage = async () => {
  if (!newMessage.value.trim()) return

  const receiver = manuscript.value.currentEditorId || 2
  const payload = {
    receiverId: receiver,
    topic: `MS-${manuscriptId}`,
    title: `关于稿件 ${manuscriptId} 的沟通`,
    content: newMessage.value
  }

  try {
    const res = await sendMessage(payload)
    if (res.code === 200) {
      ElMessage.success('发送成功')
      newMessage.value = ''
      loadMessages()
    } else {
      ElMessage.error(res.msg || '消息发送失败')
    }
  } catch (e) {
    ElMessage.error('发送失败，请检查网络')
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