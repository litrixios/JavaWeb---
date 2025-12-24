<template>
  <div class="app-container" style="padding: 20px;">
    <el-card class="box-card">
      <template #header>
        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
          <span>审稿人数据库</span>
          <el-button type="primary" @click="handleInvite">
            + 邀请新审稿人
          </el-button>
        </div>
      </template>

      <el-table :data="reviewerList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="fullName" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="researchDirection" label="研究领域" min-width="150" />
        <el-table-column prop="affiliation" label="单位" width="150" />

        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.registerTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button
                v-if="scope.row.status === 0"
                type="success"
                size="small"
                @click="auditReviewer(scope.row.userId, 1)"
            >
              批准
            </el-button>
            <el-button
                v-if="scope.row.status === 0"
                type="warning"
                size="small"
                @click="auditReviewer(scope.row.userId, 2)"
            >
              拒绝
            </el-button>
            <el-button
                v-if="scope.row.status === 1"
                type="danger"
                size="small"
                @click="onRemoveClick(scope.row)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" style="margin-top: 20px; text-align: right;">
        <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="邀请新审稿人" width="500px">
      <el-form :model="form" label-width="100px" :rules="rules" ref="formRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="设置登录用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="fullName">
          <el-input v-model="form.fullName" placeholder="例如：王五" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="例如：wangwu@example.com" />
        </el-form-item>
        <el-form-item label="单位" prop="Affiliation">
          <el-input v-model="form.affiliation" placeholder="例如：北京大学" />
        </el-form-item>
        <el-form-item label="研究领域" prop="researchDirection">
          <el-input v-model="form.researchDirection" placeholder="例如：机器学习、人工智能" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitInvite">发送邀请</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewerList, inviteReviewer, auditReviewer, removeReviewer } from '@/api/eic'

const reviewerList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)

// 分页参数
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单数据
const form = ref({
  username: '',
  fullName: '',
  email: '',
  affiliation: '',
  researchDirection: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  fullName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  researchDirection: [
    { required: true, message: '请输入研究领域', trigger: 'blur' }
  ]
}

// 1. 获取审稿人列表（从后端）
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getReviewerList()
    console.log('从后端获取审稿人列表:', res)

    if (res && res.code === 200) {
      reviewerList.value = res.data || []
      total.value = reviewerList.value.length
    } else {
      ElMessage.error(res.msg || '获取审稿人列表失败')
    }
  } catch (error) {
    console.error("获取审稿人列表失败", error)
    ElMessage.error('获取审稿人列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 2. 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '无'
  return dateStr.substring(0, 19).replace('T', ' ')
}

// 3. 状态标签
const getStatusTagType = (status) => {
  const map = {
    0: 'warning',  // 待审核
    1: 'success',  // 正常
    2: 'danger'    // 已移除
  }
  return map[status] || ''
}

const getStatusText = (status) => {
  const map = {
    0: '待审核',
    1: '正常',
    2: '已移除'
  }
  return map[status] || '未知'
}

// 4. 点击邀请按钮
const handleInvite = () => {
  form.value = {
    username: '',
    fullName: '',
    email: '',
    affiliation: '',
    researchDirection: ''
  }
  dialogVisible.value = true
}

// 5. 提交邀请
const submitInvite = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const res = await inviteReviewer(form.value)
    if (res.code === 200) {
      ElMessage.success('邀请已发送')
      dialogVisible.value = false
      fetchData() // 刷新列表
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error(error)
    if (error.response?.data?.msg) {
      ElMessage.error(error.response.data.msg)
    }
  }
}

// 6. 审核审稿人
const auditReviewerAction = async (userId, status) => {
  try {
    const action = status === 1 ? '批准' : '拒绝'
    const confirmMessage = status === 1
        ? `确定要批准用户ID为${userId}的审稿人吗？`
        : `确定要拒绝用户ID为${userId}的审稿人吗？`

    await ElMessageBox.confirm(confirmMessage, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await auditReviewer(userId, status)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      fetchData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 7. 移除审稿人
const onRemoveClick = (row) => {
  ElMessageBox.prompt('请输入移除该审稿人的理由', '确认移除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S/,
    inputErrorMessage: '理由不能为空'
  }).then(async ({ value }) => {
    try {
      const res = await removeReviewer(row.userId, value)
      if (res.code === 200) {
        ElMessage.success('已移除')
        fetchData()
      }
    } catch (error) {
      ElMessage.error(error.message || '系统错误')
    }
  })
}

// 8. 分页事件
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.card-header {
  font-size: 16px;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
}
</style>