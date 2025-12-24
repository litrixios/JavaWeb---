<template>
  <el-card>
    <template #header>
      <el-button size="small" @click="$router.back()">返回</el-button>
      <span class="title">形式审查 - 稿件 #{{ manuscriptId }}</span>
    </template>

    <!-- 基础信息 -->
    <el-descriptions border :column="1" class="mb-16">
      <el-descriptions-item label="稿件ID">
        {{ manuscriptId }}
      </el-descriptions-item>
    </el-descriptions>

    <el-divider />

    <!-- 获取稿件详情 / 技术分析 -->
    <el-button
        type="primary"
        :loading="analysisLoading"
        @click="loadAnalysis"
    >
      获取稿件详情 / 技术分析
    </el-button>

    <!-- 分析结果 -->
    <el-form label-width="120px" class="mt-16">
      <el-form-item label="字数统计">
        <span v-if="analysisLoaded">
          {{ analysis.wordCount }} 字
        </span>
        <span v-else>-</span>
      </el-form-item>

      <el-form-item label="查重率">
        <span v-if="analysisLoaded">
          {{ (analysis.plagiarismRate * 100).toFixed(2) }}%
        </span>
        <span v-else>-</span>
      </el-form-item>

      <el-form-item label="审查反馈">
        <el-input
            v-model="form.feedback"
            type="textarea"
            :rows="4"
            placeholder="不通过时必填"
        />
      </el-form-item>
    </el-form>

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

const route = useRoute()
const router = useRouter()

const manuscriptId = computed(() => Number(route.query.manuscriptId))

const analysis = ref({})
const analysisLoaded = ref(false)
const analysisLoading = ref(false)
const submitting = ref(false)

const form = ref({
  manuscriptId: manuscriptId.value,
  feedback: '',
  wordCount: 0,
  plagiarismRate: 0
})

/**
 * 手动触发：下载稿件 + 技术分析
 */
const loadAnalysis = async () => {
  analysisLoading.value = true
  try {
    const res = await getTechCheckAnalysis(manuscriptId.value)
    if (res.success) {
      analysis.value = res.data
      form.value.wordCount = res.data.wordCount
      form.value.plagiarismRate = res.data.plagiarismRate
      analysisLoaded.value = true
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

/**
 * 通过审查
 */
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

/**
 * 退回修改
 */
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
</script>

<style scoped>
.title {
  margin-left: 12px;
  font-weight: 500;
}

.mb-16 {
  margin-bottom: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.actions {
  margin-top: 24px;
  text-align: right;
}
</style>
