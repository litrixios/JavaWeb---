<template>
  <div class="message-center-container">
    <el-card class="box-card" :body-style="{ padding: '0px', height: '100%' }">
      <div class="layout-container">

        <div class="session-list">
          <div class="list-header">消息中心</div>

          <div
              class="session-item"
              :class="{ active: currentSessionType === 'system' }"
              @click="switchSession('system', null)"
          >
            <el-icon class="icon"><Bell /></el-icon>
            <div class="info">
              <div class="name">系统通知</div>
              <div class="preview">{{ systemPreview }}</div>
            </div>
            <el-badge :value="systemUnreadCount" class="badge" v-if="systemUnreadCount > 0" />
          </div>

          <template v-if="canChat">
            <div class="divider">稿件沟通</div>

            <div
                v-for="session in chatSessions"
                :key="session.topic"
                class="session-item"
                :class="{ active: currentSessionType === 'chat' && currentSessionData.topic === session.topic }"
                @click="switchSession('chat', session)"
            >
              <el-icon class="icon"><Document /></el-icon>
              <div class="info">
                <div class="name" :title="session.manuscriptTitle">
                  {{ truncate(session.manuscriptTitle, 12) }}
                </div>
                <div class="preview">
                  {{ session.targetUserName }}: {{ truncate(session.lastMessageContent, 15) || '点击发起沟通' }}
                </div>
              </div>
              <el-badge :value="session.unreadCount" class="badge" v-if="session.unreadCount > 0" />
            </div>

            <el-empty v-if="chatSessions.length === 0" description="暂无相关稿件" image-size="60" />
          </template>
        </div>

        <div class="content-area">

          <div v-if="currentSessionType === 'system'" class="system-view">
            <div class="view-header">系统通知</div>
            <div class="scroll-wrapper">
              <el-timeline style="padding: 20px;">
                <el-timeline-item
                    v-for="msg in systemMessages"
                    :key="msg.messageId"
                    :timestamp="formatDate(msg.sendTime)"
                    placement="top"
                    type="primary"
                >
                  <el-card shadow="hover">
                    <h4>{{ msg.title }}</h4>
                    <p>{{ msg.content }}</p>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-if="systemMessages.length === 0" description="暂无系统通知" />
            </div>
          </div>

          <div v-else-if="currentSessionType === 'chat' && canChat" class="chat-view">
            <div class="view-header">
              <span class="title">稿件：{{ currentSessionData.manuscriptTitle }}</span>
              <el-tag size="small" style="margin-left: 10px;">
                对话对象: {{ currentSessionData.targetUserName }} ({{ currentSessionData.targetRole }})
              </el-tag>
            </div>

            <div class="message-history" ref="messageBox">
              <div v-for="msg in currentChatMessages" :key="msg.messageId"
                   class="chat-bubble-wrapper"
                   :class="{ 'mine': isMyMessage(msg) }"
              >
                <div class="chat-meta">
                  <span class="name">{{ msg.senderName }}</span>
                  <span class="time">{{ formatDate(msg.sendTime) }}</span>
                </div>
                <div class="chat-bubble">
                  {{ msg.content }}
                </div>
              </div>
              <el-empty v-if="currentChatMessages.length === 0" description="暂无沟通记录，打个招呼吧~" />
            </div>

            <div class="input-area">
              <el-input
                  v-model="inputContent"
                  type="textarea"
                  :rows="3"
                  resize="none"
                  :placeholder="canSendMessage ? '请输入消息内容 (Ctrl + Enter 发送)...' : '当前稿件未分配编辑，暂无法发送消息'"
                  :disabled="!canSendMessage"
                  @keyup.enter.ctrl="handleSend"
              />
              <div class="action-bar">
                <span class="tip" v-if="canSendMessage">Ctrl + Enter 发送</span>
                <el-button
                    type="primary"
                    size="small"
                    @click="handleSend"
                    :disabled="!canSendMessage"
                    :loading="sending"
                >
                  发送
                </el-button>
              </div>
            </div>
          </div>

          <div v-else class="empty-view">
            <el-empty description="请选择左侧会话进行查看" />
          </div>

        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import {
  getChatSessions,
  getManuscriptHistory,
  getSystemNotifications,
  sendMessage,
  markAllSystemAsRead,
  markTopicAsRead // 确保 api/message.js 中已导出此方法
} from '@/api/message'
import { Bell, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// === 状态定义 ===
const chatSessions = ref([])
const systemMessages = ref([])
const currentChatMessages = ref([])

const currentSessionType = ref('system')
const currentSessionData = ref({})
const inputContent = ref('')
const sending = ref(false)

const messageBox = ref(null)

const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = currentUserInfo.userId
const currentUserRole = currentUserInfo.role || ''

// === 核心逻辑 ===
const canChat = computed(() => {
  const allowedRoles = ['Author', 'AUTHOR', 'Editor', 'EDITOR']
  return allowedRoles.includes(currentUserRole)
})

const systemUnreadCount = computed(() => {
  return systemMessages.value.filter(m => !m.isRead).length
})

const systemPreview = computed(() => {
  return systemMessages.value.length > 0 ? systemMessages.value[0].content : '暂无新通知'
})

const canSendMessage = computed(() => {
  return currentSessionData.value &&
      currentSessionData.value.targetUserId !== null &&
      currentSessionData.value.targetUserId !== undefined
})

// === 方法 ===
const initData = async () => {
  try {
    const resSys = await getSystemNotifications()
    if (resSys.code === 200) systemMessages.value = resSys.data

    if (canChat.value) {
      const resSession = await getChatSessions()
      if (resSession.code === 200) chatSessions.value = resSession.data
    }
  } catch (error) {
    console.error('初始化消息中心失败', error)
  }
}

const switchSession = async (type, sessionData) => {
  currentSessionType.value = type

  if (type === 'system') {
    if (systemUnreadCount.value > 0) {
      try {
        await markAllSystemAsRead()
        systemMessages.value.forEach(msg => msg.isRead = true)
      } catch (e) {
        console.error('标记系统通知已读失败', e)
      }
    }
  }

  if (type === 'chat') {
    if (!canChat.value) return
    currentSessionData.value = sessionData
    inputContent.value = ''

    if (sessionData.unreadCount > 0) {
      sessionData.unreadCount = 0
      try {
        await markTopicAsRead(sessionData.topic)
      } catch (e) {
        console.error('标记会话已读失败', e)
      }
    }

    const res = await getManuscriptHistory(sessionData.manuscriptId)
    if (res.code === 200) {
      currentChatMessages.value = res.data
      scrollToBottom()
    }
  }
}

const handleSend = async () => {
  if (!inputContent.value.trim()) return
  if (!canSendMessage.value) return

  sending.value = true
  try {
    const payload = {
      receiverId: currentSessionData.value.targetUserId,
      topic: currentSessionData.value.topic,
      title: `关于稿件 ${currentSessionData.value.manuscriptTitle} 的沟通`,
      content: inputContent.value,
      msgType: 1
    }

    const res = await sendMessage(payload)
    if (res.code === 200) {
      ElMessage.success('发送成功')
      inputContent.value = ''
      const historyRes = await getManuscriptHistory(currentSessionData.value.manuscriptId)
      if (historyRes.code === 200) {
        currentChatMessages.value = historyRes.data
        scrollToBottom()
      }
      initData()
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (e) {
    ElMessage.error('发送出错')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageBox.value) {
      messageBox.value.scrollTop = messageBox.value.scrollHeight
    }
  })
}

const truncate = (str, len) => {
  if (!str) return ''
  return str.length > len ? str.substring(0, len) + '...' : str
}

const formatDate = (str) => {
  if (!str) return ''
  return new Date(str).toLocaleString()
}

const isMyMessage = (msg) => {
  return msg.senderId === currentUserId
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
/* 容器高度适配 */
.message-center-container {
  height: calc(100vh - 84px);
  padding: 20px;
  box-sizing: border-box;
}

.box-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.layout-container {
  display: flex;
  height: 100%;
}

/* --- 左侧列表样式 --- */
.session-list {
  width: 280px;
  border-right: 1px solid #e6e6e6;
  display: flex;
  flex-direction: column;
  background-color: #f7f7f7;
  flex-shrink: 0;
}

.list-header {
  height: 50px;
  line-height: 50px;
  padding-left: 20px;
  font-weight: bold;
  border-bottom: 1px solid #eee;
  background: #fff;
  color: #333;
}

.session-item {
  padding: 15px 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  position: relative;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.session-item:hover {
  background-color: #ebedf0;
}

.session-item.active {
  background-color: #fff;
  border-left: 4px solid #409EFF;
}

.session-item .icon {
  font-size: 24px;
  margin-right: 12px;
  color: #606266;
  background: #fff;
  padding: 8px;
  border-radius: 4px;
}

.session-item .info {
  flex: 1;
  overflow: hidden;
}

.session-item .name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item .preview {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.badge {
  position: absolute;
  right: 15px;
  top: 15px;
}

.divider {
  padding: 8px 15px;
  font-size: 12px;
  color: #909399;
  background: #eee;
}

/* --- 右侧内容区域样式 --- */
.content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden; /* 防止父级溢出，确保子元素 flex 生效 */
}

/* 关键修复：添加 .system-view 样式
   将其设置为 flex 容器，并占满 content-area
*/
.system-view {
  display: flex;
  flex-direction: column;
  flex: 1;       /* 占满剩余空间 */
  min-height: 0; /* 允许 flex 压缩，配合 overflow 实现内部滚动 */
}

.view-header {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #eee;
  font-size: 16px;
  font-weight: 500;
  background: #fff;
  flex-shrink: 0; /* 头部不被压缩 */
}

.scroll-wrapper {
  flex: 1;           /* 占据剩余空间 */
  overflow-y: auto;  /* 开启垂直滚动 */
}

.empty-view {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fcfcfc;
}

/* --- 聊天窗口样式 --- */
.chat-view {
  display: flex;
  flex-direction: column;
  /* 关键修复：确保聊天窗口也支持滚动 */
  flex: 1;
  min-height: 0;
}

.message-history {
  flex: 1;
  overflow-y: auto; /* 开启聊天记录滚动 */
  padding: 20px;
  background: #f5f7fa;
}

.chat-bubble-wrapper {
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.chat-bubble-wrapper.mine {
  align-items: flex-end;
}

.chat-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
  padding: 0 4px;
}

.chat-bubble {
  background: #fff;
  padding: 10px 15px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  max-width: 70%;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.chat-bubble-wrapper.mine .chat-bubble {
  background: #95ec69; /* 微信绿 */
  border-color: #95ec69;
  color: #000;
}

.input-area {
  height: 160px;
  border-top: 1px solid #eee;
  padding: 15px;
  background: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0; /* 输入框不被压缩 */
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 10px;
}

.action-bar .tip {
  font-size: 12px;
  color: #999;
  margin-right: 15px;
}
</style>