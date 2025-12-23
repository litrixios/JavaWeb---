<template>
  <div class="process-container" style="padding: 20px;">
    <el-page-header @back="$router.back()" content="稿件处理详情" />

    <el-card style="margin-top: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: bold;">稿件核心信息</span>
          <el-tag :type="manuscriptDetail.status === 'Decided' ? 'success' : 'warning'">
            当前状态: {{ manuscriptDetail.status }} ({{ manuscriptDetail.subStatus }})
          </el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="稿件标题" :span="2">{{ manuscriptDetail.title }}</el-descriptions-item>
        <el-descriptions-item label="作者 ID">{{ manuscriptDetail.authorId }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ manuscriptDetail.submissionTime }}</el-descriptions-item>
        <el-descriptions-item label="关键词" :span="2">{{ manuscriptDetail.keywords }}</el-descriptions-item>
        <el-descriptions-item label="摘要" :span="2">
          <div style="line-height: 1.6; white-space: pre-wrap;">{{ manuscriptDetail.abstractText }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="源文件" :span="2">
          <el-button
              v-if="manuscriptDetail.manuscriptId"
              type="success"
              size="small"
              @click="handleDownload"
          >
            <el-icon><Download /></el-icon> 下载最新稿件附件
          </el-button>
          <span v-else style="color: #999;">无附件</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px">
      <h3>1. 指派审稿人</h3>
      <div style="display: flex; align-items: center; gap: 15px;">
        <el-select v-model="selectedReviewers" multiple placeholder="请选择审稿人" style="width: 400px">
          <el-option
              v-for="r in reviewerOptions"
              :key="r.id"
              :label="r.name"
              :value="r.id"
          />
        </el-select>
        <el-button type="primary" @click="handleInvite">发送邀请</el-button>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <h3>2. 提交建议 (给主编)</h3>
      <el-form label-position="top">
        <el-form-item label="建议结论">
          <el-select v-model="recommendForm.editorRecommendation" placeholder="请选择建议方向" style="width: 300px">
            <el-option label="建议录用 (Acceptance)" value="Suggest Acceptance" />
            <el-option label="建议拒稿 (Rejection)" value="Suggest Rejection" />
            <el-option label="建议修改 (Revision)" value="Suggest Revision" />
          </el-select>
        </el-form-item>
        <el-form-item label="总结报告 (编辑评审意见汇总)">
          <el-input v-model="recommendForm.editorSummaryReport" type="textarea" :rows="5" placeholder="请填写给主编的总结报告..." />
        </el-form-item>
        <el-button type="success" @click="handleRecommend">提交总结报告并通知主编</el-button>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: bold;">3. 与作者沟通历史</span>
          <el-button type="primary" plain size="small" @click="msgDialogVisible = true">给作者发消息</el-button>
        </div>
      </template>

      <el-timeline v-if="chatHistory.length > 0" style="margin-top: 10px">
        <el-timeline-item
            v-for="msg in chatHistory"
            :key="msg.messageId"
            :timestamp="new Date(msg.sendTime).toLocaleString()"
            :type="msg.senderId === manuscriptDetail.authorId ? 'primary' : 'success'"
            placement="top"
        >
          <el-card shadow="hover">
            <h4 style="margin: 0 0 10px 0;">{{ msg.title }}</h4>
            <p style="font-size: 14px; color: #666;">{{ msg.content }}</p>
            <div style="margin-top: 10px; text-align: right;">
              <el-tag size="small">{{ msg.senderId === manuscriptDetail.authorId ? '作者回复' : '我的消息' }}</el-tag>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无与作者的沟通记录" />
    </el-card>

    <el-dialog v-model="msgDialogVisible" title="发送站内信" width="550px">
      <el-form :model="msgForm" label-position="top">
        <el-form-item label="消息标题" required>
          <el-input v-model="msgForm.title" placeholder="如：关于稿件实验数据的疑问" />
        </el-form-item>
        <el-form-item label="沟通内容" required>
          <el-input v-model="msgForm.content" type="textarea" :rows="6" placeholder="请详细描述您需要作者澄清或修改的内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="msgDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSendMessage">确认发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import axios from 'axios';

const route = useRoute()
const mid = route.query.id // 获取地址栏 ?id=xxx

// 1. 数据状态定义
const manuscriptDetail = ref({})
const reviewerOptions = ref([])
const selectedReviewers = ref([])
const chatHistory = ref([])
const msgDialogVisible = ref(false)

const recommendForm = ref({
  manuscriptId: mid,
  editorRecommendation: '',
  editorSummaryReport: ''
})

const msgForm = ref({
  title: '',
  content: ''
})

// 2. 初始化加载
onMounted(() => {
  if (!mid) {
    ElMessage.error("未获取到有效的稿件ID")
    return
  }
  fetchDetail()
  fetchReviewers()
  fetchChatHistory()
})

// 3. 业务逻辑方法
const fetchDetail = async () => {
  const res = await fetch(`http://localhost:8080/api/editor/detail?id=${mid}`, {
    headers: { 'Authorization': localStorage.getItem('token') }
  })
  const result = await res.json()
  if (result.code === 200) manuscriptDetail.value = result.data
}

const fetchReviewers = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/editor/reviewers`, {
      headers: { 'Authorization': localStorage.getItem('token') }
    })
    const result = await res.json()
    if (result.code === 200) {
      // 核心修复：确保 ID 唯一且为数字，Name 使用全名
      reviewerOptions.value = result.data.map(user => ({
        id: Number(user.userId || user.id),
        name: user.fullName || user.realName || user.username
      }))
    }
  } catch (e) { ElMessage.error("审稿人加载失败") }
}

const fetchChatHistory = async () => {
  const res = await fetch(`http://localhost:8080/api/message/history/${mid}`, {
    headers: { 'Authorization': localStorage.getItem('token') }
  })
  const result = await res.json()
  if (result.code === 200) chatHistory.value = result.data
}

const handleDownload = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/editor/download/${mid}`, {
      headers: { 'Authorization': localStorage.getItem('token') },
      responseType: 'blob'
    });

    // 调试：请务必检查这里打印的内容
    console.log("Headers from Backend:", response.headers);

    const disposition = response.headers['content-disposition'];
    let fileName = `manuscript_${mid}.pdf`; // 默认备选名

    if (disposition) {
      // 提取 filename= 后的内容
      const match = disposition.match(/filename=(.*)/);
      if (match && match[1]) {
        // 解码并去掉引号
        fileName = decodeURIComponent(match[1].replace(/['"]/g, ''));
        // 处理可能存在的 filename* 情况
        if (fileName.includes("utf-8''")) {
          fileName = decodeURIComponent(fileName.split("utf-8''")[1]);
        }
      }
    }

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();

    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
    ElMessage.success("下载成功");
  } catch (error) {
    console.error("下载出错:", error);
    ElMessage.error("下载失败，请检查文件是否存在");
  }
};
// 发送邀请逻辑
const handleInvite = async () => {
  if (selectedReviewers.value.length === 0) return ElMessage.warning("请至少选择一位审稿人")

  const res = await fetch('http://localhost:8080/api/editor/invite', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': localStorage.getItem('token') },
    body: JSON.stringify({
      manuscriptId: parseInt(mid),
      reviewerIds: selectedReviewers.value
    })
  })
  const data = await res.json()
  if (data.code === 200) {
    ElMessage.success("邀请已发送，状态已更新")
    fetchDetail() // 刷新状态
    selectedReviewers.value = [] // 清空选择
  }
}

// 提交建议给主编逻辑
const handleRecommend = async () => {
  if (!recommendForm.value.editorRecommendation || !recommendForm.value.editorSummaryReport) {
    return ElMessage.warning("请完整填写建议结论和报告")
  }

  const res = await fetch('http://localhost:8080/api/editor/recommend', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': localStorage.getItem('token') },
    body: JSON.stringify(recommendForm.value)
  })
  const data = await res.json()
  if (data.code === 200) {
    ElMessage.success("建议已提交，系统已通知主编")
    fetchDetail()
  }
}

// 发送消息给作者逻辑
const handleSendMessage = async () => {
  if (!msgForm.value.title || !msgForm.value.content) return ElMessage.warning("请填写完整标题和内容")

  const res = await fetch('http://localhost:8080/api/message/send', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': localStorage.getItem('token') },
    body: JSON.stringify({
      receiverId: manuscriptDetail.value.authorId,
      topic: "MS-" + mid,
      title: msgForm.value.title,
      content: msgForm.value.content
    })
  })
  const data = await res.json()
  if (data.code === 200) {
    ElMessage.success("消息发送成功")
    msgDialogVisible.value = false
    msgForm.value = { title: '', content: '' } // 重置表单
    fetchChatHistory() // 刷新时间线
  } else {
    ElMessage.error(data.message)
  }
}
</script>

<style scoped>
.process-container {
  max-width: 1200px;
  margin: 0 auto;
}
.el-card {
  margin-bottom: 20px;
}
h3 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #303133;
}
</style>