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

          <div v-else-if="currentSessionType === 'chat'" class="chat-view">
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
import { getChatSessions, getManuscriptHistory, getSystemNotifications, sendMessage } from '@/api/message'
import { Bell, Document } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// === 状态定义 ===
const chatSessions = ref([])       // 左侧稿件会话列表
const systemMessages = ref([])     // 系统通知列表
const currentChatMessages = ref([])// 当前选中的会话聊天记录

const currentSessionType = ref('system') // 当前视图类型: 'system' | 'chat'
const currentSessionData = ref({})       // 当前选中的 Session 对象 (ChatSessionDTO)
const inputContent = ref('')             // 输入框内容
const sending = ref(false)               // 发送加载状态

const messageBox = ref(null) // 聊天滚动容器 ref

// 获取当前用户信息
const currentUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserId = currentUserInfo.userId

// === 计算属性 ===

// 系统通知未读数
const systemUnreadCount = computed(() => {
  return systemMessages.value.filter(m => m.isRead === 0 || m.isRead === false).length
})

// 系统通知预览文案
const systemPreview = computed(() => {
  return systemMessages.value.length > 0 ? systemMessages.value[0].content : '暂无新通知'
})

// 是否允许发送消息 (必须有确定的接收人ID)
const canSendMessage = computed(() => {
  return currentSessionData.value &&
      currentSessionData.value.targetUserId !== null &&
      currentSessionData.value.targetUserId !== undefined
})

// === 方法 ===

// 初始化数据
const initData = async () => {
  try {
    // 1. 获取会话列表 (后端根据稿件关系自动生成)
    const resSession = await getChatSessions()
    if (resSession.code === 200) {
      chatSessions.value = resSession.data
    }
    // 2. 获取系统通知
    const resSys = await getSystemNotifications()
    if (resSys.code === 200) {
      systemMessages.value = resSys.data
    }
  } catch (error) {
    console.error('初始化消息中心失败', error)
  }
}

// 切换会话
const switchSession = async (type, sessionData) => {
  currentSessionType.value = type

  if (type === 'chat') {
    currentSessionData.value = sessionData
    inputContent.value = '' // 切换时清空输入框

    // 加载该稿件的历史记录
    const res = await getManuscriptHistory(sessionData.manuscriptId)
    if (res.code === 200) {
      currentChatMessages.value = res.data
      scrollToBottom()

      // (可选)在此处调用后端接口将该会话的消息标记为已读
      // await markAsRead(sessionData.topic)
      // 然后本地把 sessionData.unreadCount 置为 0
    }
  }
}

// 发送消息
const handleSend = async () => {
  if (!inputContent.value.trim()) return
  if (!canSendMessage.value) return

  sending.value = true
  try {
    const payload = {
      receiverId: currentSessionData.value.targetUserId,
      topic: currentSessionData.value.topic,
      title: `关于稿件 ${currentSessionData.value.manuscriptTitle} 的沟通`, // 标题可作为邮件主题
      content: inputContent.value,
      msgType: 1 // 1=聊天消息
    }

    const res = await sendMessage(payload)
    if (res.code === 200) {
      ElMessage.success('发送成功')
      inputContent.value = ''

      // 刷新当前聊天记录
      const historyRes = await getManuscriptHistory(currentSessionData.value.manuscriptId)
      if (historyRes.code === 200) {
        currentChatMessages.value = historyRes.data
        scrollToBottom()
      }

      // 刷新左侧会话列表以更新预览
      // 为了体验更好，这里可以选择局部更新 chatSessions 的 lastMessageContent
      initData()
    } else {
      ElMessage.error(res.msg || '发送失败')
    }
  } catch (e) {
    ElMessage.error('发送出错')
    console.error(e)
  } finally {
    sending.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageBox.value) {
      messageBox.value.scrollTop = messageBox.value.scrollHeight
    }
  })
}

// 辅助工具函数
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

// 生命周期
onMounted(() => {
  initData()
})
</script>

<style scoped>
.message-center-container {
  height: calc(100vh - 84px); /* 适配你的 Layout 高度 */
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
  overflow: hidden; /* 防止溢出 */
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
}

.scroll-wrapper {
  flex: 1;
  overflow-y: auto;
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
  height: 100%;
}

.message-history {
  flex: 1;
  overflow-y: auto;
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