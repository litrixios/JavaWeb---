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
          <div v-for="(item, index) in fundingList" :key="index" style="display: flex; margin-bottom: 10px; width: 100%">
            <el-input v-model="item.value" placeholder="如：国家自然科学基金 (No. 123456)" style="flex: 1; margin-right: 10px;" />
            <el-button type="danger" icon="Delete" circle @click="removeFunding(index)" v-if="fundingList.length > 1"></el-button>
            <el-button type="primary" icon="Plus" circle @click="addFunding" v-if="index === fundingList.length - 1"></el-button>
          </div>
          <div class="tip">请填写项目资助信息，多项资助请点击“+”号添加</div>
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
            <el-button type="primary">点击上传 PDF/Word (含作者信息)</el-button>
          </el-upload>
          <div class="tip">请上传包含完整作者信息的原稿</div>
        </el-form-item>

        <el-form-item label="匿名手稿" prop="anonymousFilePath">
          <el-upload
              class="upload-demo"
              action="/api/common/upload"
              :headers="uploadHeaders"
              :limit="1"
              :on-success="handleAnonymousSuccess"
              :on-error="handleUploadError"
              :file-list="anonymousFileList"
          >
            <el-button type="primary">点击上传 PDF (匿名)</el-button>
          </el-upload>
          <div class="tip">请上传已隐去作者姓名和单位的匿名稿件，用于盲审</div>
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
import { ref, reactive } from 'vue'
import { submitManuscript } from '@/api/manuscript'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref(null)
const isEdit = ref(false)

const token = localStorage.getItem('token')
const uploadHeaders = {
  Authorization: token
}
const handleUploadError = (err) => {
  ElMessage.error('上传失败，请检查登录状态或文件大小')
  console.error(err)
}

// 资助信息列表
const fundingList = ref([{ value: '' }])

const form = reactive({
  manuscriptId: null,
  title: '',
  abstractText: '',
  keywords: '',
  topic: '',
  fundingInfo: '', // 最终提交时拼接
  authors: [
    { name: '', email: '', unit: '', isCorresponding: true }
  ],
  recommendedReviewers: [],
  originalFilePath: '',
  anonymousFilePath: '', // 新增字段
  coverLetterPath: ''
})

const manuscriptFileList = ref([])
const anonymousFileList = ref([]) // 新增文件列表
const coverLetterFileList = ref([])

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  abstractText: [{ required: true, message: '请输入摘要', trigger: 'blur' }],
  topic: [{ required: true, message: '请选择研究主题', trigger: 'change' }],
  originalFilePath: [{ required: true, message: '请上传手稿文件', trigger: 'change' }],
  anonymousFilePath: [{ required: true, message: '请上传匿名手稿', trigger: 'change' }] // 新增校验
}

// 资助信息操作
const addFunding = () => {
  fundingList.value.push({ value: '' })
}
const removeFunding = (index) => {
  fundingList.value.splice(index, 1)
}

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

// 文件上传回调
const handleManuscriptSuccess = (res) => {
  if(res.code === 200) {
    form.originalFilePath = res.data
    ElMessage.success('手稿上传成功')
  } else {
    ElMessage.error(res.msg || '手稿上传失败')
  }
}
const handleAnonymousSuccess = (res) => {
  if(res.code === 200) {
    form.anonymousFilePath = res.data
    ElMessage.success('匿名手稿上传成功')
  } else {
    ElMessage.error(res.msg || '匿名手稿上传失败')
  }
}
const handleCoverLetterSuccess = (res) => {
  if(res.code === 200) {
    form.coverLetterPath = res.data
    ElMessage.success('Cover Letter 上传成功')
  } else {
    ElMessage.error(res.msg || 'Cover Letter 上传失败')
  }
}

// 提交或保存
const handleSubmit = async (actionType) => {
  if (!formRef.value) return

  if (actionType === 'SUBMIT') {
    await formRef.value.validate()
  }

  // 拼接资助信息
  form.fundingInfo = fundingList.value
      .map(item => item.value)
      .filter(val => val && val.trim() !== '')
      .join('; ')

  const payload = {
    ...form,
    actionType // "SUBMIT" or "SAVE"
  }

  const res = await submitManuscript(payload)
  if (res.code === 200) {
    ElMessage.success(res.data)
    router.push('/manuscript/list')
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