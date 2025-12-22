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
import { Download } from '@element-plus/icons-vue' // 引入下载图标

const route = useRoute()
const mid = route.query.id // 获取 URL 中的 manuscriptID

// 新增：稿件详情响应式变量
const manuscriptDetail = ref({
  title: '',
  abstractText: '',
  keywords: '',
  authorId: '',
  filePath: '',
  status: '',
  submissionTime: ''
})

// 模拟审稿人数据
const reviewerOptions = ref([{ id: 101, name: '张教授' }, { id: 102, name: '李专家' }])
const selectedReviewers = ref([])

const recommendForm = ref({
  manuscriptID: mid,
  editorRecommendation: '',
  editorSummaryReport: ''
})

// 新增：页面挂载时请求稿件详情
onMounted(async () => {
  if (!mid) {
    ElMessage.error("未获取到稿件ID")
    return
  }
  try {
    const res = await fetch(`http://localhost:8080/api/editor/detail?id=${mid}`, {
      method: 'GET',
      headers: { 'Authorization': localStorage.getItem('token') }
    })
    const result = await res.json()
    if (result.code === 200) {
      manuscriptDetail.value = result.data
    } else {
      ElMessage.error("详情获取失败: " + result.message)
    }
  } catch (error) {
    console.error("请求异常:", error)
  }
})

// 新增：处理下载逻辑
const handleDownload = (path) => {
  // 注意：此处 path 建议由后端下载接口处理，直接打开后端流地址
  const downloadUrl = `http://localhost:8080/api/file/download?path=${encodeURIComponent(path)}&token=${localStorage.getItem('token')}`
  window.open(downloadUrl, '_blank')
}

// 原有功能：指派接口
const handleInvite = async () => {
  const res = await fetch('http://localhost:8080/api/editor/invite', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': localStorage.getItem('token') },
    body: JSON.stringify({
      manuscriptId: parseInt(mid),
      reviewerIds: selectedReviewers.value
    })
  })
  const data = await res.json()
  if (data.code === 200) ElMessage.success("邀请已发送")
}

// 原有功能：提交建议接口
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