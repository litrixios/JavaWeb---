<!-- src/views/editorial-admin/news/form.vue -->
<template>
  <el-card>
    <div slot="header">
      <el-button @click="$router.go(-1)" icon="ArrowLeft" size="small">返回</el-button>
      <h2 style="display: inline-block; margin-left: 16px;">{{ isEdit ? '编辑新闻' : '新增新闻' }}</h2>
    </div>

    <el-form ref="formRef" :model="newsForm" label-width="120px" class="mt-4">
      <el-form-item label="新闻标题" prop="title" :rules="[{ required: true, message: '请输入标题', trigger: 'blur' }]">
        <el-input
            v-model="newsForm.title"
            placeholder="请输入新闻标题"
            style="width: 600px;"
        />
      </el-form-item>

      <!-- 发布时间：区分新增/编辑状态的不同逻辑 -->
      <el-form-item
          label="发布时间"
          prop="scheduledPublishDate"
          :rules="getPublishDateRules()"
      >
        <!-- 新增新闻：保留原有立即/定时发布逻辑 -->
        <template v-if="!isEdit">
          <div class="publish-time-container">
            <el-radio-group v-model="publishType" @change="handlePublishTypeChange" class="radio-group">
              <el-radio label="immediate">立即发布</el-radio>
              <el-radio label="scheduled">定时发布</el-radio>
            </el-radio-group>

            <el-date-picker
                v-if="publishType === 'scheduled'"
                v-model="newsForm.scheduledPublishDate"
                type="datetime"
                placeholder="选择发布时间"
                class="date-picker"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </div>
        </template>

        <!-- 编辑新闻：新的交互逻辑 -->
        <template v-else>
          <div class="edit-publish-time-container">
            <div class="original-time">
              <span class="label">原发布时间:</span>
              <span class="value">{{ formatDate(originalPublishDate) }}</span>
            </div>

            <div class="modify-controls">
              <el-radio-group v-model="isModifyPublishDate" @change="handleModifyPublishDateChange" class="radio-group">
                <el-radio :label="false">不修改</el-radio>
                <el-radio :label="true">修改</el-radio>
              </el-radio-group>

              <el-date-picker
                  v-if="isModifyPublishDate"
                  v-model="newsForm.scheduledPublishDate"
                  type="datetime"
                  placeholder="选择新的发布时间"
                  class="date-picker"
                  :default-value="originalPublishDate ? dayjs(originalPublishDate, 'YYYY-MM-DD HH:mm:ss').toDate() : undefined"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
              />
            </div>
          </div>
        </template>
      </el-form-item>

      <el-form-item label="新闻内容" prop="content" :rules="[{ required: true, message: '请输入内容', trigger: 'blur' }]">
        <el-input
            type="textarea"
            v-model="newsForm.content"
            rows="15"
            placeholder="请输入新闻内容"
            style="width: 800px;"
        />
      </el-form-item>

      <el-form-item label="附件上传">
        <div class="attachment-wrapper">
          <!-- 上传按钮 -->
          <el-upload
              action="#"
              :http-request="handleFileUpload"
              :show-file-list="false"
              :auto-upload="true"
              :limit="5"
          >
            <el-button type="primary" icon="Upload">
              上传附件
            </el-button>

            <template #tip>
              <div class="el-upload__tip">
                支持上传 PDF、Word 等格式文件
              </div>
            </template>
          </el-upload>

          <!-- 附件列表（始终在下面） -->
          <el-table
              v-if="fileList.length > 0"
              :data="fileList"
              border
              size="small"
              class="attachment-table"
          >
            <el-table-column prop="name" label="文件名" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button
                    type="text"
                    text-color="#ff4d4f"
                    @click="handleFileRemove(scope.row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form-item>

    </el-form>

    <div style="margin-top: 20px; text-align: right;">
      <el-button @click="$router.go(-1)">取消</el-button>
      <el-button
          type="primary"
          @click="submitForm"
          :loading="submitting"
          style="margin-left: 10px;"
      >
        {{ isEdit ? '保存修改' : '保存并发布' }}
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 可选：推荐使用dayjs格式化时间，需先安装：npm install dayjs
import dayjs from 'dayjs'
import {
  addNews,
  updateNews,
  getNewsDetail,
  uploadNewsFile,
  getNewsFiles,
  deleteNewsFile
} from '@/api/editorialAdmin'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const newsId = computed(() => route.query.newsId)
const isEdit = computed(() => !!newsId.value)

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const publishType = ref('immediate') // 新增新闻用：立即/定时发布
const fileList = ref([])
// 新增：编辑新闻用的变量
const originalPublishDate = ref(null) // 存储原发布时间（统一为YYYY-MM-DD HH:mm:ss格式的字符串）
const isModifyPublishDate = ref(false) // 是否修改发布时间（默认不修改）

const newsForm = reactive({
  newsId: newsId.value || null,
  title: '',
  content: '',
  scheduledPublishDate: null
})

// 修复：优化格式化时间函数，增强对不同时间类型的兼容处理
const formatDate = (date) => {
  if (!date) return '无'
  // 统一用dayjs处理，兼容字符串、时间戳、Date对象
  const dayjsDate = dayjs(date)
  // 校验时间是否有效，无效则返回'无'
  return dayjsDate.isValid() ? dayjsDate.format('YYYY-MM-DD HH:mm:ss') : '无'
}

// 新增：封装时间转换函数，将任意时间类型转为标准的YYYY-MM-DD HH:mm:ss字符串
const convertToStandardDate = (date) => {
  if (!date) return null
  const dayjsDate = dayjs(date)
  return dayjsDate.isValid() ? dayjsDate.format('YYYY-MM-DD HH:mm:ss') : null
}

// 新增：动态获取发布时间的验证规则
const getPublishDateRules = () => {
  if (!isEdit.value) {
    // 新增状态：定时发布时必填
    return [{
      required: publishType.value === 'scheduled',
      message: '请选择定时发布时间',
      trigger: 'blur'
    }]
  } else {
    // 编辑状态：选择修改时必填
    return [{
      required: isModifyPublishDate.value,
      message: '请选择新的发布时间',
      trigger: 'blur'
    }]
  }
}

// 修复：处理是否修改发布时间的变化，使用统一的时间转换函数
const handleModifyPublishDateChange = (val) => {
  if (val) {
    // 让 date-picker 直接显示原时间（字符串也可以，因为 value-format 是字符串）
    newsForm.scheduledPublishDate = originalPublishDate.value || null
  } else {
    // 不修改：直接还原为原时间（而不是置空）
    newsForm.scheduledPublishDate = originalPublishDate.value || null
  }
}

// 获取新闻详情
const getNewsDetailInfo = async () => {
  if (!isEdit.value) return

  loading.value = true
  try {
    const res = await getNewsDetail(newsId.value)
    if (res.success) {
      const data = res.data
      newsForm.title = data.title
      newsForm.content = data.content

      // 修复：1. 使用封装的函数统一转换为标准时间格式 2. 去掉new Date()默认值，改为null（避免显示当前时间）
      originalPublishDate.value = convertToStandardDate(data.publishDate) || convertToStandardDate(data.scheduledPublishDate) || null
      // 初始化：如果有定时发布时间，设置为表单值（编辑时备用）
      if (data.scheduledPublishDate) {
        newsForm.scheduledPublishDate = convertToStandardDate(data.scheduledPublishDate)
      }

      // 获取附件列表
      getFileList()
    }
  } catch (error) {
    console.error('获取新闻详情失败', error)
    ElMessage.error('获取新闻详情失败')
  } finally {
    loading.value = false
  }
}

// 获取附件列表
const getFileList = async () => {
  try {
    const res = await getNewsFiles(newsId.value)
    if (res.success) {
      fileList.value = res.data.map(file => ({
        ...file,
        name: file.fileName,
        url: ''
      }))
    }
  } catch (error) {
    console.error('获取附件列表失败', error)
    ElMessage.error('获取附件列表失败')
  }
}

// 处理发布类型变更（新增新闻用）
const handlePublishTypeChange = (val) => {
  if (val === 'immediate') {
    newsForm.scheduledPublishDate = null
  }
}

// 处理文件上传
const handleFileUpload = async (params) => {
  const file = params.file
  const formData = new FormData()
  formData.append('file', file)

  if (isEdit.value) {
    try {
      const res = await uploadNewsFile(newsId.value, formData)
      if (res.success) {
        ElMessage.success('文件上传成功')
        getFileList()
      }
    } catch (error) {
      console.error('文件上传失败', error)
      ElMessage.error('文件上传失败')
      params.onError()
    }
  } else {
    fileList.value.push({
      name: file.name,
      raw: file,
      isNew: true
    })
    params.onSuccess()
  }
}

// 处理文件删除
const handleFileRemove = async (file) => {
  // ===== 编辑新闻：后端已存在的附件 =====
  if (isEdit.value && file.fileId) {
    try {
      const res = await deleteNewsFile(file.fileId)
      if (res.success) {
        ElMessage.success('文件删除成功')
        fileList.value = fileList.value.filter(
            f => f.fileId !== file.fileId
        )
      }
    } catch (error) {
      console.error('文件删除失败', error)
      ElMessage.error('文件删除失败')
    }
    return
  }

  // ===== 新增新闻：本地未上传的附件 =====
  fileList.value = fileList.value.filter(f => f !== file)
}


// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  newsForm.scheduledPublishDate = toBackendIso(newsForm.scheduledPublishDate)

  try {
    await formRef.value.validate()
    submitting.value = true

    // 新增：编辑状态下处理发布时间逻辑
    if (isEdit.value) {
      if (!isModifyPublishDate.value) {
        // 不修改发布时间：使用原时间（标准格式）
        newsForm.scheduledPublishDate = originalPublishDate.value
      }
      // 修改的话：直接使用表单中的选择值
    }

    // 处理新闻保存
    let res
    if (isEdit.value) {
      res = await updateNews(newsForm)
    } else {
      res = await addNews(newsForm)
    }

    if (res.success) {
      const currentNewsId = isEdit.value ? newsId.value : res.data

      // 处理新增的文件上传
      if (!isEdit.value && fileList.value.length > 0) {
        for (const file of fileList.value) {
          if (file.isNew && file.raw) {
            const formData = new FormData()
            formData.append('file', file.raw)
            await uploadNewsFile(currentNewsId, formData)
          }
        }
      }

      ElMessage.success(isEdit.value ? '新闻更新成功' : '新闻发布成功')
      router.push('/editorial-admin/news')
    }
  } catch (error) {
    console.error(isEdit.value ? '更新新闻失败' : '发布新闻失败', error)
    ElMessage.error(isEdit.value ? '更新失败，请重试' : '发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

const toBackendIso = (val) => {
  if (!val) return null
  // val 可能是字符串（因为 value-format），也可能是 Date
  const d = dayjs(val, ['YYYY-MM-DD HH:mm:ss', dayjs.ISO_8601], true)
  if (!d.isValid()) return null
  // 输出：2025-12-03T00:00:00.000+08:00 （带冒号）
  return d.format('YYYY-MM-DD[T]HH:mm:ss.SSSZ').replace(/([+-]\d{2})(\d{2})$/, '$1:$2')
}


onMounted(() => {
  getNewsDetailInfo()
})
</script>

<style scoped>
.upload-demo {
  margin-bottom: 20px;
}

/* 发布时间容器样式 */
.publish-time-container,
.modify-controls {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.edit-publish-time-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.original-time {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 14px;
}

.original-time .label {
  margin-right: 8px;
  color: #333;
  font-weight: 500;
}

.original-time .value {
  color: #409EFF;
  font-weight: 500;
}

.radio-group {
  flex-shrink: 0;
}

.date-picker {
  width: 240px;
  flex-shrink: 0;
}

.attachment-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-table {
  width: 800px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .publish-time-container,
  .modify-controls {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .date-picker {
    width: 100%;
  }
}
</style>


