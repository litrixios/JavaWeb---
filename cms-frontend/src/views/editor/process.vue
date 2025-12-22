<template>
  <div class="process-container" style="padding: 20px;">
    <el-page-header @back="$router.back()" content="稿件处理详情" />

    <el-card style="margin-top: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span style="font-weight: bold;">稿件核心信息</span>
          <el-tag type="warning">当前状态: {{ manuscriptDetail.status }}</el-tag>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="稿件标题" :span="2">
          {{ manuscriptDetail.title }}
        </el-descriptions-item>

        <el-descriptions-item label="作者信息">
          <el-tag type="info">匿名作者 ID: {{ manuscriptDetail.authorId }}</el-tag>
        </el-descriptions-item>

        <el-descriptions-item label="提交时间">
          {{ manuscriptDetail.submissionTime }}
        </el-descriptions-item>

        <el-descriptions-item label="关键词" :span="2">
          {{ manuscriptDetail.keywords }}
        </el-descriptions-item>

        <el-descriptions-item label="摘要" :span="2">
          <div style="line-height: 1.6; white-space: pre-wrap;">{{ manuscriptDetail.abstractText }}</div>
        </el-descriptions-item>

        <el-descriptions-item label="源文件" :span="2">
          <el-button
              v-if="manuscriptDetail.filePath"
              type="success"
              size="small"
              @click="handleDownload(manuscriptDetail.filePath)"
          >
            <el-icon style="vertical-align: middle;"><Download /></el-icon>
            <span style="vertical-align: middle; margin-left: 5px;">下载稿件附件</span>
          </el-button>
          <span v-else style="color: #999;">无附件</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card style="margin-top: 20px">
      <h3>1. 指派审稿人</h3>
      <el-select v-model="selectedReviewers" multiple placeholder="请选择审稿人" style="width: 400px">
        <el-option v-for="r in reviewerOptions" :key="r.id" :label="r.name" :value="r.id" />
      </el-select>
      <el-button type="primary" @click="handleInvite" style="margin-left: 10px">发送邀请</el-button>
    </el-card>

    <el-card style="margin-top: 20px">
      <h3>2. 提交建议 (To EIC)</h3>
      <el-form label-position="top">
        <el-form-item label="建议结论">
          <el-select v-model="recommendForm.editorRecommendation" placeholder="请选择">
            <el-option label="建议录用" value="Suggest Acceptance" />
            <el-option label="建议拒稿" value="Suggest Rejection" />
            <el-option label="建议修改" value="Suggest Revision" />
          </el-select>
        </el-form-item>
        <el-form-item label="总结报告">
          <el-input v-model="recommendForm.editorSummaryReport" type="textarea" rows="5" />
        </el-form-item>
        <el-button type="success" @click="handleRecommend">提交总结报告</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const route = useRoute()
const mid = route.query.id

const manuscriptDetail = ref({
  title: '',
  abstractText: '',
  keywords: '',
  authorId: '',
  filePath: '',
  status: '',
  submissionTime: ''
})

// --- 修改点：初始化为空，通过 API 加载 ---
const reviewerOptions = ref([])
const selectedReviewers = ref([])

const recommendForm = ref({
  manuscriptId: mid, // 确保字段名与后端实体一致
  editorRecommendation: '',
  editorSummaryReport: ''
})

// 1. 加载稿件详情
const fetchDetail = async () => {
  const res = await fetch(`http://localhost:8080/api/editor/detail?id=${mid}`, {
    headers: { 'Authorization': localStorage.getItem('token') }
  })
  const result = await res.json()
  if (result.code === 200) manuscriptDetail.value = result.data
}

// 2. 加载审稿人列表 (从数据库 users 表)
// 2. 加载审稿人列表 (从数据库 users 表)
const fetchReviewers = async () => {
  try {
    const res = await fetch(`http://localhost:8080/api/editor/reviewers`, {
      headers: { 'Authorization': localStorage.getItem('token') }
    })
    const result = await res.json()

    if (result.code === 200 && result.data) {
      // 调试：请在控制台确认每个 user 的 id 是否唯一
      console.log("审稿人原始数据:", result.data)

      // 【修正点】: 强制转换 ID 类型并确保字段名匹配
      reviewerOptions.value = result.data.map(user => ({
        // 1. 确保取到的是 UserID (根据你的 SQL 别名是 id)
        // 2. 使用 Number() 强制转为数字，防止字符串匹配导致多选失效
        id: Number(user.id || user.userId),
        // 优先显示 realName (对应 FullName)，如果没有则显示 username
        name: user.realName || user.fullName || user.username
      }))
    }
  } catch (error) {
    console.error("加载失败:", error)
    ElMessage.error("获取审稿人名单失败")
  }
}

onMounted(() => {
  if (!mid) {
    ElMessage.error("未获取到稿件ID")
    return
  }
  fetchDetail()
  fetchReviewers() // 页面加载时执行
})

const handleDownload = (path) => {
  const downloadUrl = `http://localhost:8080/api/file/download?path=${encodeURIComponent(path)}&token=${localStorage.getItem('token')}`
  window.open(downloadUrl, '_blank')
}

const handleInvite = async () => {
  if (selectedReviewers.value.length === 0) {
    ElMessage.warning("请至少选择一位审稿人")
    return
  }

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
    ElMessage.success("邀请已发送")
    fetchDetail() // 刷新状态
  }
}

const handleRecommend = async () => {
  const res = await fetch('http://localhost:8080/api/editor/recommend', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': localStorage.getItem('token') },
    body: JSON.stringify(recommendForm.value)
  })
  const data = await res.json()
  if (data.code === 200) ElMessage.success("建议已提交给主编")
}
</script>