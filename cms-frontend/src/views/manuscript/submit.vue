<template>
  <div class="app-container">
    <el-steps :active="activeStep" finish-status="success" simple style="margin-bottom: 20px">
      <el-step title="1. 基本信息" />
      <el-step title="2. 作者信息" />
      <el-step title="3. 文件上传" />
    </el-steps>

    <el-form :model="form" ref="formRef" label-width="120px" :rules="rules">

      <div v-show="activeStep === 0">
        <el-form-item label="稿件标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入论文标题" />
        </el-form-item>
        <el-form-item label="摘要" prop="abstract">
          <el-input type="textarea" v-model="form.abstract" :rows="6" placeholder="请输入摘要" />
        </el-form-item>
        <el-form-item label="关键词" prop="keywords">
          <el-select
              v-model="form.keywords"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="输入关键词并回车">
          </el-select>
        </el-form-item>
      </div>

      <div v-show="activeStep === 1">
        <el-button type="primary" size="small" @click="addAuthor" style="margin-bottom:10px;">添加作者</el-button>
        <el-table :data="form.authors" border>
          <el-table-column label="姓名">
            <template #default="{ row }"><el-input v-model="row.name" /></template>
          </el-table-column>
          <el-table-column label="邮箱">
            <template #default="{ row }"><el-input v-model="row.email" /></template>
          </el-table-column>
          <el-table-column label="单位">
            <template #default="{ row }"><el-input v-model="row.affiliation" /></template>
          </el-table-column>
          <el-table-column label="通讯作者" width="100" align="center">
            <template #default="{ row }">
              <el-switch v-model="row.isCorresponding" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button link type="danger" @click="form.authors.splice($index, 1)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-show="activeStep === 2">
        <el-form-item label="Manuscript (PDF)" required>
          <el-upload
              class="upload-demo"
              action="/api/file/upload"
              :limit="1"
              :on-success="(res) => handleFileSuccess(res, 'manuscriptPath')">
            <el-button type="primary">点击上传原稿</el-button>
          </el-upload>
        </el-form-item>

        <el-form-item label="Cover Letter">
          <el-upload
              class="upload-demo"
              action="/api/file/upload"
              :limit="1"
              :on-success="(res) => handleFileSuccess(res, 'coverLetterPath')">
            <el-button>点击上传 Cover Letter</el-button>
          </el-upload>
        </el-form-item>
      </div>

      <div style="margin-top: 30px; text-align: center;">
        <el-button v-if="activeStep > 0" @click="activeStep--">上一步</el-button>
        <el-button v-if="activeStep < 2" type="primary" @click="nextStep">下一步</el-button>

        <template v-if="activeStep === 2">
          <el-button type="warning" @click="handleSubmit('SAVE')">保存草稿</el-button>
          <el-button type="success" @click="handleSubmit('SUBMIT')">正式提交</el-button>
        </template>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { submitManuscript } from '@/api/author'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeStep = ref(0)
const formRef = ref(null)

// 这里的数据结构要对应后端的 ManuscriptDTO
const form = reactive({
  title: '',
  abstract: '',
  keywords: [],
  authors: [
    { name: '', email: '', affiliation: '', isCorresponding: true } // 默认有一个作者
  ],
  manuscriptPath: '', // 文件路径
  coverLetterPath: '',
  actionType: '' // SUBMIT 或 SAVE
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  abstract: [{ required: true, message: '请输入摘要', trigger: 'blur' }]
}

const addAuthor = () => {
  form.authors.push({ name: '', email: '', affiliation: '', isCorresponding: false })
}

const handleFileSuccess = (response, field) => {
  // 假设后端返回 Result<String>，data 是文件路径或ID
  if(response.code === 200) {
    form[field] = response.data
    ElMessage.success('上传成功')
  }
}

const nextStep = () => {
  // 简单校验
  if (activeStep.value === 0 && !form.title) {
    ElMessage.warning('请先填写标题')
    return
  }
  activeStep.value++
}

const handleSubmit = async (type) => {
  form.actionType = type

  // 数据清洗：keywords 数组转字符串（如果后端需要字符串）
  // 这里的 data 结构必须严格符合你的 ManuscriptDTO
  const dto = {
    ...form,
    keywords: form.keywords.join(',') // 假设后端存的是逗号分割字符串
  }

  try {
    await submitManuscript(dto)
    ElMessage.success(type === 'SUBMIT' ? '投稿成功！' : '草稿已保存')
    router.push('/manuscript/list')
  } catch (error) {
    console.error(error)
  }
}
</script>