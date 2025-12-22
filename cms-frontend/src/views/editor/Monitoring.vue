<template>
  <div class="monitoring-container" style="padding: 20px;">
    <el-card>
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 18px;">审稿监控中心 (逾期管理)</span>
          <el-button type="primary" :icon="Refresh" @click="fetchOverdue" circle />
        </div>
      </template>

      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-statistic title="逾期审稿任务" :value="overdueList.length" />
        </el-col>
      </el-row>

      <el-table :data="overdueList" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="稿件标题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="reviewerName" label="审稿人" width="120" />
        <el-table-column prop="reviewerEmail" label="联系邮箱" width="200" />
        <el-table-column prop="deadline" label="截止日期" width="160">
          <template #default="scope">
            {{ new Date(scope.row.deadline).toLocaleDateString() }}
          </template>
        </el-table-column>
        <el-table-column label="逾期时长" width="120">
          <template #default="scope">
            <el-tag type="danger" effect="dark">逾期 {{ scope.row.overdueDays }} 天</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="warning" size="small" @click="handleOpenDialog(scope.row)">
              手动催审
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="发送催审通知" width="650px">
      <el-form label-position="top">
        <el-form-item label="快速选择模板">
          <el-radio-group v-model="selectedTemplate" @change="applyTemplate">
            <el-radio-button label="gentle">温和版 (初次提醒)</el-radio-button>
            <el-radio-button label="urgent">严肃版 (多次逾期)</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="邮件预览 (支持 HTML)">
          <el-input
              v-model="mailContent"
              type="textarea"
              :rows="10"
              placeholder="请输入催审内容..."
          />
        </el-form-item>

        <div style="background: #f8f9fa; padding: 10px; border-radius: 4px; font-size: 12px; color: #909399;">
          提示：点击“确认发送”后，系统将通过 EmailService 异步发送邮件。
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRemind" :loading="sending">确认发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'

// 状态变量
const overdueList = ref([])
const loading = ref(false)
const sending = ref(false)
const dialogVisible = ref(false)
const mailContent = ref('')
const selectedTemplate = ref('gentle')
const currentRow = ref(null)

// 1. 获取逾期数据
const fetchOverdue = async () => {
  loading.ref = true
  try {
    const res = await fetch('http://localhost:8080/api/editor/monitoring/overdue', {
      headers: { 'Authorization': localStorage.getItem('token') }
    })
    const result = await res.json()
    if (result.code === 200) {
      overdueList.value = result.data
    }
  } catch (error) {
    ElMessage.error("获取逾期数据失败")
  } finally {
    loading.value = false
  }
}

// 2. 弹窗与模板逻辑
const handleOpenDialog = (row) => {
  currentRow.value = row
  dialogVisible.value = true
  applyTemplate('gentle') // 默认温和模板
}

const applyTemplate = (val) => {
  const reviewer = currentRow.value.reviewerName
  const title = currentRow.value.title
  const days = currentRow.value.overdueDays

  if (val === 'gentle') {
    mailContent.value =
        `<p>尊敬的 ${reviewer} 老师：</p>
<p>您好！感谢您百忙之中担任我刊的审稿专家。</p>
<p>关于稿件<b>《${title}》</b>，系统显示已超过预定评审截止日期。如果您已完成评审，请尽快登录系统提交意见；如果您需要更多时间，请回复告知。谢谢！</p>
<p>祝好！<br/>编辑部</p>`
  } else {
    mailContent.value =
        `<p><b>【紧急催审】</b></p>
<p>${reviewer} 老师：</p>
<p>您好。您负责评审的稿件《${title}》已逾期 <b>${days}</b> 天，目前严重影响了该稿件的处理进度。</p>
<p>请您务必于近期反馈审稿意见，如有特殊困难请及时与编辑部取得联系。感谢您的支持与配合。</p>`
  }
}

// 3. 提交催审请求
const submitRemind = async () => {
  if (!mailContent.value) return ElMessage.warning("邮件内容不能为空")

  sending.value = true
  try {
    const res = await fetch('http://localhost:8080/api/editor/monitoring/remind', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token')
      },
      body: JSON.stringify({
        reviewId: currentRow.value.reviewId, // 对应后端 Map 中的 reviewId
        content: mailContent.value
      })
    })
    const result = await res.json()
    if (result.code === 200) {
      ElMessage.success("催审邮件已加入发送队列")
      dialogVisible.value = false
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error("服务器响应失败")
  } finally {
    sending.value = false
  }
}

onMounted(fetchOverdue)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.monitoring-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>