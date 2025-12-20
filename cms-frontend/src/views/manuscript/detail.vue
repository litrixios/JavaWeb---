<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ manuscript.title }} ({{ manuscript.status }})</span>
          <el-button type="primary" @click="dialogVisible = true">联系编辑</el-button>
        </div>
      </template>

      <el-steps :active="activeStepStatus" align-center style="margin: 30px 0;">
        <el-step title="Submitted" description="已投稿" />
        <el-step title="With Editor" description="编辑处理中" />
        <el-step title="Under Review" description="专家审稿中" />
        <el-step title="Decision" description="决议完成" />
      </el-steps>

      <el-descriptions border :column="2">
        <el-descriptions-item label="稿件ID">{{ manuscript.id }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ manuscript.submitTime }}</el-descriptions-item>
        <el-descriptions-item label="摘要" :span="2">{{ manuscript.abstract }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发送消息给编辑">
      <el-form :model="msgForm">
        <el-form-item label="主题" label-width="60px">
          <el-input v-model="msgForm.topic" placeholder="例如：延期申请" />
        </el-form-item>
        <el-form-item label="内容" label-width="60px">
          <el-input type="textarea" v-model="msgForm.content" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSendMsg">发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { sendMessage } from '@/api/author'
import { ElMessage } from 'element-plus'

// 模拟数据 (实际应从 getDetail 接口获取)
const manuscript = ref({
  id: 'MS-2025-001',
  title: '基于深度学习的图像识别研究',
  status: 'Under Review',
  abstract: '...',
  submitTime: '2025-12-01',
  editorId: 101 // ★ 关键：后端必须返回当前处理该稿件的编辑ID，否则消息发给谁？
})

const activeStepStatus = 2 // 根据 status 计算步骤条位置
const dialogVisible = ref(false)

const msgForm = reactive({
  topic: '',
  title: '来自作者的消息', // 默认标题
  content: ''
})

const handleSendMsg = async () => {
  if (!manuscript.value.editorId) {
    ElMessage.error('当前稿件暂无分配编辑，无法发送消息')
    return
  }

  // 构造 MessageSendDTO
  const dto = {
    receiverId: manuscript.value.editorId, // 接收者
    topic: msgForm.topic,
    title: `关于稿件[${manuscript.value.id}]的咨询`,
    content: msgForm.content
  }

  try {
    await sendMessage(dto)
    ElMessage.success('发送成功')
    dialogVisible.value = false
  } catch (err) {
    console.error(err)
  }
}
</script>