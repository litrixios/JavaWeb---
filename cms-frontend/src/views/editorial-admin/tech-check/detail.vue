<template>
  <el-card>
    <template #header>
      <el-button size="small" @click="$router.back()">返回</el-button>
      <span class="title">形式审查 - 稿件 #{{ manuscriptId }}</span>
    </template>

    <!-- 技术分析按钮 -->
    <div class="analysis-action">
      <el-button
          type="primary"
          :loading="analysisLoading"
          @click="loadAnalysis"
      >
        获取稿件详情 / 技术分析
      </el-button>
    </div>

    <!-- 技术分析结果卡片 -->
    <el-card class="analysis-card" shadow="never">
      <div class="analysis-metrics">
        <!-- 字数 -->
        <div class="metric">
          <div class="metric-label">字数统计</div>
          <div
              class="metric-value"
              :class="{ danger: isWordOverflow }"
          >
            <span v-if="analysisLoaded">{{ analysis.wordCount }}</span>
            <span v-else>-</span>
            <span class="unit">字</span>

            <el-tag
                v-if="analysisLoaded && isWordOverflow"
                type="danger"
                size="small"
                class="metric-tag"
            >
              字数超限
            </el-tag>
          </div>
        </div>

        <!-- 查重率 -->
        <div class="metric">
          <div class="metric-label">查重率</div>
          <div
              class="metric-value"
              :class="{ danger: isHighSimilarity }"
          >
            <span v-if="analysisLoaded">
              {{ (analysis.plagiarismRate * 100).toFixed(2) }}
            </span>
            <span v-else>-</span>
            <span class="unit">%</span>

            <el-tag
                v-if="analysisLoaded && isHighSimilarity"
                type="danger"
                size="small"
                class="metric-tag"
            >
              高相似度
            </el-tag>
          </div>
        </div>
      </div>

      <!-- 审查反馈 -->
      <div class="feedback">
        <div class="feedback-label">审查反馈</div>
        <el-input
            v-model="form.feedback"
            type="textarea"
            :rows="4"
            placeholder="不通过时必填"
        />
      </div>
    </el-card>

    <!-- PDF 标题 + 收起展开 -->
    <div v-if="analysisLoaded" class="pdf-header" @click="togglePdf">
      <span class="pdf-title">稿件 PDF 预览</span>

      <span class="pdf-toggle">
    <el-icon>
      <ArrowUp v-if="pdfVisible" />
      <ArrowDown v-else />
    </el-icon>
    <span class="toggle-text">
      {{ pdfVisible ? '收起' : '展开' }}
    </span>
  </span>
    </div>


    <!-- PDF 在线预览 -->
    <div v-if="pdfVisible" class="pdf-wrapper">
      <iframe
          :src="pdfPreviewUrl"
          width="100%"
          height="800px"
          frameborder="0"
      />
    </div>

    <!-- 审查操作 -->
    <div class="actions">
      <el-button
          type="warning"
          :loading="submitting"
          @click="reject"
      >
        退回修改
      </el-button>

      <el-button
          type="success"
          :loading="submitting"
          :disabled="!analysisLoaded"
          @click="pass"
      >
        通过审查
      </el-button>
    </div>
  </el-card>
</template>




<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getTechCheckAnalysis,
  techCheck,
  unsubmitManuscript
} from '@/api/editorialAdmin'
import { ElMessage } from 'element-plus'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const manuscriptId = computed(() => Number(route.query.manuscriptId))

const analysis = ref({})
const analysisLoaded = ref(false)
const analysisLoading = ref(false)
const submitting = ref(false)

// ✅ 新增
const pdfVisible = ref(false)
const pdfPreviewUrl = computed(() =>
    `/api/editorial-admin/manuscripts/pdf/preview?manuscriptId=${manuscriptId.value}`
)

const form = ref({
  manuscriptId: manuscriptId.value,
  feedback: '',
  wordCount: 0,
  plagiarismRate: 0
})

const loadAnalysis = async () => {
  analysisLoading.value = true
  try {
    const res = await getTechCheckAnalysis(manuscriptId.value)
    if (res.success) {
      analysis.value = res.data
      form.value.wordCount = res.data.wordCount
      form.value.plagiarismRate = res.data.plagiarismRate
      analysisLoaded.value = true

      // ✅ 分析成功后自动展开 PDF
      pdfVisible.value = true

      ElMessage.success('稿件已下载并完成分析')
    } else {
      ElMessage.error(res.message || '稿件分析失败')
    }
  } catch (e) {
    ElMessage.error('稿件下载或分析失败')
  } finally {
    analysisLoading.value = false
  }
}

// 通过 / 退回（原样保留）
const pass = async () => {
  submitting.value = true
  try {
    await techCheck({ ...form.value, passed: true })
    ElMessage.success('形式审查通过')
    router.push('/editorial-admin/tech-check')
  } finally {
    submitting.value = false
  }
}

const reject = async () => {
  if (!form.value.feedback) {
    ElMessage.warning('请填写退回原因')
    return
  }
  submitting.value = true
  try {
    await unsubmitManuscript({ ...form.value, passed: false })
    ElMessage.success('已退回修改')
    router.push('/editorial-admin/tech-check')
  } finally {
    submitting.value = false
  }
}

// 阈值判断
const isWordOverflow = computed(() =>
    analysisLoaded.value && analysis.value.wordCount > 8000
)

const isHighSimilarity = computed(() =>
    analysisLoaded.value && analysis.value.plagiarismRate > 0.2
)

// PDF 展开 / 收起
const togglePdf = () => {
  pdfVisible.value = !pdfVisible.value
}
</script>


<style scoped>
.title {
  margin-left: 12px;
  font-weight: 500;
  font-size: 16px;
}

/* 顶部按钮区 */
.analysis-action {
  margin-bottom: 16px;
}

/* 技术分析卡片 */
.analysis-card {
  margin-bottom: 24px;
  background-color: #fafafa;
}

/* 指标区 */
.analysis-metrics {
  display: flex;
  gap: 40px;
  margin-bottom: 16px;
}

.metric {
  flex: 1;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}

.metric-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.metric-value.danger {
  color: #f56c6c;
}

.metric-value .unit {
  font-size: 14px;
  color: #909399;
}

.metric-tag {
  margin-left: 6px;
}

/* 审查反馈 */
.feedback-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}

/* PDF 头部 */
.pdf-header {
  margin: 32px 0 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.pdf-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* PDF 容器 */
.pdf-wrapper {
  border: 1px solid #ebeef5;
  margin-bottom: 24px;
}

/* 底部操作区 */
.actions {
  margin-top: 32px;
  text-align: right;
}

/* PDF 区块头部（可折叠） */
.pdf-header {
  margin: 32px 0 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  cursor: pointer;
  user-select: none;
}

.pdf-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 收起 / 展开 控制 */
.pdf-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 16px;
  color: #409eff;
}

.toggle-text {
  line-height: 1;
}

/* hover 效果 */
.pdf-header:hover .pdf-toggle {
  color: #66b1ff;
}

</style>

