<template>
  <div class="app-container">
    <el-card>
      <el-tabs v-model="activeTab">

        <el-tab-pane label="稿件状态 (Track)" name="track">
          <div v-loading="loading">
            <div class="status-header">
              <h3>{{ manuscript.title }}</h3>
              <el-tag size="large" :type="statusType">{{ manuscript.status }}</el-tag>
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

        <el-tab-pane label="提交修回 (Revision)" name="revision" :disabled="manuscript.status !== 'Need Revision'">
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

// 状态样式
const statusType = computed(() => {
  const s = manuscript.value.status
  if (s === 'Accepted') return 'success'
  if (s === 'Rejected') return 'danger'
  if (s === 'Need Revision') return 'warning'
  return 'primary'
})

// 简易进度条映射
const activeStep = computed(() => {
  const s = manuscript.value.status
  console.log("s:",s)
  if (!s) return 0
  if (s.includes('Submit')) return 1
  if (s.includes('Check') || s.includes('Form')) return 2
  if (s.includes('Editor') || s.includes('Assign')) return 3
  if (s.includes('Review')) return 4
  if (s.includes('Decision') || s.includes('Accept') || s.includes('Reject') || s.includes('Revision')) return 5
  return 1
})

const formatDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleString()
}

// 初始化加载
const loadData = async () => {
  loading.value = true
  try {
    // 1. 获取详情
    const res = await trackManuscript(manuscriptId)
    if (res.code === 200) {
      trackInfo.value = res.data
      manuscript.value = res.data.manuscript || {}
      historyLogs.value = res.data.historyLogs || []
    }
    // 2. 获取消息历史
    loadMessages()
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

// 提交修回
const handleRevisionSubmit = async () => {
  if (!revisionForm.markedFilePath || !revisionForm.responseLetterPath) {
    ElMessage.error('请上传标记稿和回复信')
    return
  }
  const res = await submitRevision(revisionForm)
  if (res.code === 200) {
    ElMessage.success('修回提交成功')
    loadData() // 刷新状态
    activeTab.value = 'track'
  } else {
    ElMessage.error(res.msg || '修回提交失败')
  }
}

// 发送消息
const handleSendMessage = async () => {
  if (!newMessage.value.trim()) return

  // 假设发给当前处理该稿件的编辑，或者系统默认接收人
  // 这里简化处理，Topic 为 MS-{id}
  const payload = {
    // 修改点 1：如果没有分配编辑，默认发给总编(ID=2)而不是管理员(ID=1)，以匹配后端默认策略
    receiverId: manuscript.value.currentEditorId || 2,
    topic: `MS-${manuscriptId}`,
    title: `关于稿件 ${manuscriptId} 的沟通`,
    content: newMessage.value
  }

  const res = await sendMessage(payload)

  // 修改点 2：添加 else 分支，处理后端报错
  if (res.code === 200) {
    ElMessage.success('发送成功')
    newMessage.value = ''
    loadMessages()
  } else {
    // 显示具体错误信息，例如 "您只能回复编辑部工作人员..."
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
/* 聊天样式 */
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