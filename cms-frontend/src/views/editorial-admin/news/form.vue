<template>
  <el-card>
    <template #header>
      <h2 style="display:inline-block;">
        {{ isEdit ? '编辑新闻' : '新增新闻' }}
      </h2>
    </template>

    <el-form ref="formRef" :model="newsForm" label-width="120px" class="mt-4">
      <el-form-item
          label="新闻标题"
          prop="title"
          :rules="[{ required: true, message: '请输入标题', trigger: 'blur' }]"
      >
        <el-input v-model="newsForm.title" placeholder="请输入新闻标题" style="width: 600px;" />
      </el-form-item>

      <el-form-item label="发布时间" prop="scheduledPublishDate" :rules="getPublishDateRules()">
        <!-- 新增 -->
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

        <!-- 编辑 -->
        <template v-else>
          <div class="edit-publish-time-container">
            <div class="original-time">
              <span class="label">原发布时间:</span>
              <span class="value">{{ formatDate(originalPublishDate) }}</span>
            </div>

            <div class="modify-controls">
              <el-radio-group
                  v-model="isModifyPublishDate"
                  @change="handleModifyPublishDateChange"
                  class="radio-group"
              >
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

      <el-form-item
          label="新闻内容"
          prop="content"
          :rules="[{ required: true, message: '请输入内容', trigger: 'blur' }]"
      >
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
          <el-upload
              action="#"
              :http-request="handleFileUpload"
              :show-file-list="false"
              :auto-upload="true"
              :limit="5"
          >
            <el-button type="primary" icon="Upload">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持上传 PDF、Word 等格式文件</div>
            </template>
          </el-upload>

          <el-table
              v-if="displayFileList.length > 0"
              :data="displayFileList"
              border
              size="small"
              class="attachment-table"
          >
            <el-table-column prop="name" label="文件名" />
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button type="text" text-color="#ff4d4f" @click="handleFileRemove(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form-item>
    </el-form>

    <div style="margin-top: 20px; text-align: right;">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="submitForm" :loading="submitting" style="margin-left: 10px;">
        {{ isEdit ? '保存修改' : '保存并发布' }}
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import {
  addNews,
  updateNews,
  getNewsDetail,
  uploadNewsFile,
  getNewsFiles,
  deleteNewsFile
} from '@/api/editorialAdmin'

const route = useRoute()
const router = useRouter()

const newsId = computed(() => route.query.newsId)
const isEdit = computed(() => !!newsId.value)

const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const publishType = ref('immediate')

// 新增模式：本地附件暂存
const fileList = ref([])

// 编辑模式：后端原始附件快照 + 暂存新增 + 暂存删除
const originalFileList = ref([])        // 原始附件列表（含 fileId）
const addedFiles = ref([])              // 新增暂存：{ name, raw, isNew: true }
const removedFileIds = ref(new Set())   // 暂存删除：Set<fileId>

// 新闻表单原始快照（用于取消还原）
const originalNewsSnapshot = ref(null)

const originalPublishDate = ref(null)
const isModifyPublishDate = ref(false)

const newsForm = reactive({
  newsId: newsId.value || null,
  title: '',
  content: '',
  scheduledPublishDate: null
})

const formatDate = (date) => {
  if (!date) return '无'
  const d = dayjs(date)
  return d.isValid() ? d.format('YYYY-MM-DD HH:mm:ss') : '无'
}

const convertToStandardDate = (date) => {
  if (!date) return null
  const d = dayjs(date)
  return d.isValid() ? d.format('YYYY-MM-DD HH:mm:ss') : null
}

// 编辑模式：展示“原始-已标记删除 + 暂存新增”；新增模式：展示 fileList
const displayFileList = computed(() => {
  if (!isEdit.value) return fileList.value

  const removed = removedFileIds.value
  const keptOriginal = originalFileList.value.filter((f) => !removed.has(f.fileId))

  const stagedNew = addedFiles.value.map((f) => ({
    name: f.name,
    isNew: true
  }))

  return [
    ...keptOriginal.map((f) => ({
      ...f,
      name: f.fileName || f.name
    })),
    ...stagedNew
  ]
})

const getPublishDateRules = () => {
  if (!isEdit.value) {
    return [{ required: publishType.value === 'scheduled', message: '请选择定时发布时间', trigger: 'blur' }]
  }
  return [{ required: isModifyPublishDate.value, message: '请选择新的发布时间', trigger: 'blur' }]
}

const handleModifyPublishDateChange = (val) => {
  // 你的原逻辑这里其实两边都赋 originalPublishDate；保留但更直观一点
  if (val) newsForm.scheduledPublishDate = originalPublishDate.value || null
  else newsForm.scheduledPublishDate = originalPublishDate.value || null
}

const handlePublishTypeChange = (val) => {
  if (val === 'immediate') newsForm.scheduledPublishDate = null
}

// 获取编辑详情
const getNewsDetailInfo = async () => {
  if (!isEdit.value) return
  loading.value = true
  try {
    const res = await getNewsDetail(newsId.value)
    if (res.success) {
      const data = res.data
      newsForm.title = data.title
      newsForm.content = data.content

      originalPublishDate.value =
          convertToStandardDate(data.publishDate) ||
          convertToStandardDate(data.scheduledPublishDate) ||
          null

      if (data.scheduledPublishDate) {
        newsForm.scheduledPublishDate = convertToStandardDate(data.scheduledPublishDate)
      }

      // ✅ 表单原始快照（取消还原）
      originalNewsSnapshot.value = {
        title: newsForm.title,
        content: newsForm.content,
        scheduledPublishDate: newsForm.scheduledPublishDate,
        publishType: 'immediate', // 编辑界面不使用 publishType，但留个字段不影响
        isModifyPublishDate: false
      }

      await getFileList()
    }
  } catch (e) {
    ElMessage.error('获取新闻详情失败')
  } finally {
    loading.value = false
  }
}

// 获取附件列表（编辑：保存为原始快照并清空暂存）
const getFileList = async () => {
  try {
    const res = await getNewsFiles(newsId.value)
    if (res.success) {
      originalFileList.value = res.data.map((file) => ({
        ...file,
        name: file.fileName,
        url: ''
      }))

      // ✅ 清空暂存改动
      addedFiles.value = []
      removedFileIds.value = new Set()
    }
  } catch {
    ElMessage.error('获取附件列表失败')
  }
}

// 上传附件：新增/编辑都先暂存；编辑不立刻上传
const handleFileUpload = async (params) => {
  const file = params.file
  const staged = { name: file.name, raw: file, isNew: true }

  if (isEdit.value) {
    addedFiles.value.push(staged)
    ElMessage.success('文件已加入（待保存后上传）')
    params.onSuccess()
    return
  }

  fileList.value.push(staged)
  params.onSuccess()
}

// 删除附件：新增直接移除本地；编辑仅标记删除或移除暂存
const handleFileRemove = async (file) => {
  if (isEdit.value) {
    // 删除暂存新增文件
    if (file.isNew) {
      addedFiles.value = addedFiles.value.filter((f) => f.name !== file.name)
      ElMessage.success('已移除（未保存，不会影响后台）')
      return
    }

    // 标记删除原始文件
    if (file.fileId) {
      const newSet = new Set(removedFileIds.value)
      newSet.add(file.fileId)
      removedFileIds.value = newSet
      ElMessage.success('已标记删除（待保存后生效）')
    }
    return
  }

  fileList.value = fileList.value.filter((f) => f !== file)
}

// 取消：新增清空本地；编辑丢弃暂存并还原表单
const handleCancel = () => {
  if (!isEdit.value) {
    // 新增：清空所有本地编辑痕迹
    newsForm.title = ''
    newsForm.content = ''
    newsForm.scheduledPublishDate = null
    publishType.value = 'immediate'
    fileList.value = []
    router.go(-1)
    return
  }

  // 编辑：还原表单
  if (originalNewsSnapshot.value) {
    newsForm.title = originalNewsSnapshot.value.title
    newsForm.content = originalNewsSnapshot.value.content
    newsForm.scheduledPublishDate = originalNewsSnapshot.value.scheduledPublishDate
  }

  // 编辑：丢弃附件暂存
  addedFiles.value = []
  removedFileIds.value = new Set()
  isModifyPublishDate.value = false

  router.go(-1)
}

const toBackendIso = (val) => {
  if (!val) return null
  const d = dayjs(val, ['YYYY-MM-DD HH:mm:ss', dayjs.ISO_8601], true)
  if (!d.isValid()) return null
  return d.format('YYYY-MM-DD[T]HH:mm:ss.SSSZ').replace(/([+-]\d{2})(\d{2})$/, '$1:$2')
}

const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    // ✅ 不管是否修改，都保证发给后端的是 ISO
    const finalDate = (isEdit.value && !isModifyPublishDate.value)
        ? originalPublishDate.value
        : newsForm.scheduledPublishDate

    newsForm.scheduledPublishDate = toBackendIso(finalDate)

    let res
    if (isEdit.value) res = await updateNews(newsForm)
    else res = await addNews(newsForm)

    if (res.success) {
      const currentNewsId = isEdit.value ? newsId.value : res.data

      // ✅ 附件统一提交
      if (isEdit.value) {
        // 1) 删除（仅删除用户标记删除的原始附件）
        for (const id of removedFileIds.value) {
          await deleteNewsFile(id)
        }

        // 2) 上传（仅上传用户暂存新增的附件）
        for (const f of addedFiles.value) {
          if (f.raw) {
            const fd = new FormData()
            fd.append('file', f.raw)
            await uploadNewsFile(currentNewsId, fd)
          }
        }
      } else {
        // 新增：上传本地暂存附件
        for (const f of fileList.value) {
          if (f.isNew && f.raw) {
            const fd = new FormData()
            fd.append('file', f.raw)
            await uploadNewsFile(currentNewsId, fd)
          }
        }
      }

      ElMessage.success(isEdit.value ? '新闻更新成功' : '新闻发布成功')
      router.push('/editorial-admin/news')
    }
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败，请重试' : '发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  getNewsDetailInfo()
})
</script>

<style scoped>
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
