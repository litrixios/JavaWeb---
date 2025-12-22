<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑稿件' : '新建投稿' }}</span>
      </template>

      <el-form :model="form" ref="formRef" :rules="rules" label-width="120px">

        <el-divider content-position="left">基本信息 (Metadata)</el-divider>
        <el-form-item label="稿件标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入稿件标题" />
        </el-form-item>

        <el-form-item label="摘要" prop="abstractText">
          <el-input type="textarea" :rows="5" v-model="form.abstractText" placeholder="请输入摘要 (Abstract)" />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="关键词" prop="keywords">
              <el-input v-model="form.keywords" placeholder="逗号分隔, 如: AI, Deep Learning" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="研究主题" prop="topic">
              <el-select v-model="form.topic" placeholder="请选择主题" style="width: 100%">
                <el-option label="人工智能" value="AI" />
                <el-option label="大数据" value="BigData" />
                <el-option label="网络安全" value="Security" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="项目资助" prop="fundingInfo">
          <el-input v-model="form.fundingInfo" placeholder="如：国家自然科学基金 (No. 123456)" />
        </el-form-item>

        <el-divider content-position="left">作者列表 (Authors)</el-divider>
        <el-table :data="form.authors" border style="margin-bottom: 10px">
          <el-table-column prop="name" label="姓名" width="120">
            <template #default="{ row }"><el-input v-model="row.name" /></template>
          </el-table-column>
          <el-table-column prop="email" label="邮箱">
            <template #default="{ row }"><el-input v-model="row.email" /></template>
          </el-table-column>
          <el-table-column prop="unit" label="单位">
            <template #default="{ row }"><el-input v-model="row.unit" /></template>
          </el-table-column>
          <el-table-column label="通讯作者" width="100" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.isCorresponding" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" link @click="removeAuthor($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" plain size="small" @click="addAuthor">添加作者</el-button>

        <el-divider content-position="left">文件上传 (Files)</el-divider>
        <el-form-item label="手稿文件" prop="originalFilePath">
          <el-upload
              class="upload-demo"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :limit="1"
              :on-success="handleManuscriptSuccess"
              :on-error="handleUploadError"
              :file-list="manuscriptFileList"
          >
            <el-button type="primary">点击上传 PDF/Word</el-button>
          </el-upload>
          <div class="tip">请上传包含完整作者信息的原稿</div>
        </el-form-item>

        <el-form-item label="Cover Letter" prop="coverLetterPath">
          <el-upload
              class="upload-demo"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :limit="1"
              :on-success="handleCoverLetterSuccess"
              :on-error="handleUploadError"
              :file-list="coverLetterFileList"
          >
            <el-button type="primary">点击上传 Cover Letter</el-button>
          </el-upload>
        </el-form-item>

        <el-divider content-position="left">推荐审稿人 (Recommended Reviewers)</el-divider>
        <div v-for="(reviewer, index) in form.recommendedReviewers" :key="index" class="reviewer-card">
          <el-row :gutter="20">
            <el-col :span="6"><el-input v-model="reviewer.name" placeholder="姓名" /></el-col>
            <el-col :span="8"><el-input v-model="reviewer.email" placeholder="邮箱" /></el-col>
            <el-col :span="8"><el-input v-model="reviewer.reason" placeholder="推荐理由" /></el-col>
            <el-col :span="2"><el-button type="danger" icon="Delete" circle @click="removeReviewer(index)" /></el-col>
          </el-row>
        </div>
        <el-button type="primary" plain size="small" @click="addReviewer" style="margin-top: 10px">添加推荐审稿人</el-button>

        <div class="form-footer">
          <el-button @click="handleSubmit('SAVE')">保存草稿</el-button>
          <el-button type="primary" @click="handleSubmit('SUBMIT')">正式提交</el-button>
        </div>

      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue' // 修改点：引入 computed
import { submitManuscript } from '@/api/manuscript'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const isEdit = ref(false)

const form = reactive({
  manuscriptId: null,
  title: '',
  abstractText: '',
  keywords: '',
  topic: '',
  fundingInfo: '',
  authors: [
    { name: '', email: '', unit: '', isCorresponding: true }
  ],
  recommendedReviewers: [],
  originalFilePath: '',
  coverLetterPath: ''
})

const manuscriptFileList = ref([])
const coverLetterFileList = ref([])

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  abstractText: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  topic: [{ required: true, message: '请选择研究主题', trigger: 'change' }],
  originalFilePath: [{ required: true, message: '请上传手稿文件', trigger: 'change' }]
}

// 修改点：计算上传所需的 Headers (主要是 Authorization Token)
const uploadHeaders = computed(() => {
  return {
    Authorization: localStorage.getItem('token') || ''
  }
})

// 作者操作
const addAuthor = () => {
  form.authors.push({ name: '', email: '', unit: '', isCorresponding: false })
}
const removeAuthor = (index) => {
  form.authors.splice(index, 1)
}

// 审稿人操作
const addReviewer = () => {
  form.recommendedReviewers.push({ name: '', email: '', reason: '' })
}
const removeReviewer = (index) => {
  form.recommendedReviewers.splice(index, 1)
}

// 修改点：处理网络或权限等上传错误
const handleUploadError = (error) => {
  console.error(error)
  ElMessage.error('文件上传失败，请检查登录状态或网络连接')
}

// 文件上传回调
const handleManuscriptSuccess = (res) => {
  if(res.code === 200) {
    form.originalFilePath = res.data
    ElMessage.success('手稿上传成功')
  } else {
    // 修改点：增加错误提示
    ElMessage.error(res.msg || '手稿上传失败')
  }
}
const handleCoverLetterSuccess = (res) => {
  if(res.code === 200) {
    form.coverLetterPath = res.data
    ElMessage.success('Cover Letter 上传成功')
  } else {
    // 修改点：增加错误提示
    ElMessage.error(res.msg || 'Cover Letter 上传失败')
  }
}

// 提交或保存
const handleSubmit = async (actionType) => {
  if (!formRef.value) return

  if (actionType === 'SUBMIT') {
    await formRef.value.validate()
  }

  const payload = {
    ...form,
    actionType // "SUBMIT" or "SAVE"
  }

  try {
    const res = await submitManuscript(payload)
    if (res.code === 200) {
      ElMessage.success(res.data)
      router.push('/manuscript/list')
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    // request.js 拦截器通常已经处理了错误，但这里加 try/catch 更稳妥
    console.error(error)
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.reviewer-card {
  margin-bottom: 10px;
  background-color: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
}
.form-footer {
  margin-top: 40px;
  text-align: center;
}
.tip {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}
</style>