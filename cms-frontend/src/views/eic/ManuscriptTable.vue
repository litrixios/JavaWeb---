<template>
  <div class="manuscript-table">
    <!-- 表格操作栏 -->
    <div class="table-actions" v-if="showActions">
      <el-button
          v-if="showBatchDeskReview"
          type="primary"
          size="small"
          @click="$emit('batch-desk-review')"
      >
        批量初审
      </el-button>
    </div>

    <!-- 稿件表格 -->
    <el-table
        :data="data"
        border
        style="width: 100%"
        @row-click="handleRowClick"
    >
      <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" />
      <el-table-column prop="title" label="稿件标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="authorList" label="作者列表" width="150" />

      <el-table-column label="投稿时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.submissionTime) }}
        </template>
      </el-table-column>

      <el-table-column label="当前编辑" width="120">
        <template #default="scope">
          <span v-if="scope.row.currentEditorId">
            {{ getEditorName(scope.row.currentEditorId) }}
          </span>
          <span v-else style="color: #909399;">未指派</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="子状态" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.subStatus" :type="getSubStatusType(scope.row.subStatus)" size="small">
            {{ getSubStatusText(scope.row.subStatus) }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="250" fixed="right" align="center">
        <template #default="scope">
          <!-- 初审按钮 -->
          <el-button
              v-if="scope.row.status === 'Incomplete'"
              type="success"
              size="small"
              @click.stop="$emit('desk-review', scope.row)"
          >
            初审
          </el-button>

          <!-- 指派编辑按钮 -->
          <el-button
              v-if="scope.row.status === 'Processing' && scope.row.subStatus === 'PendingAssign'"
              type="primary"
              size="small"
              @click.stop="$emit('assign-editor', scope.row)"
          >
            指派编辑
          </el-button>

          <!-- 终审按钮 -->
          <el-button
              v-if="(scope.row.status === 'Processing' && scope.row.subStatus === 'UnderReview') ||
                   scope.row.editorRecommendation"
              type="warning"
              size="small"
              @click.stop="$emit('final-decision', scope.row)"
          >
            终审
          </el-button>

          <!-- 撤稿按钮 -->
          <el-button
              v-if="scope.row.status === 'Decided'"
              type="danger"
              size="small"
              @click.stop="$emit('retract', scope.row)"
          >
            撤稿
          </el-button>

          <!-- 查看详情按钮 -->
          <el-button
              type="info"
              size="small"
              @click.stop="$emit('view-detail', scope.row)"
          >
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  showActions: {
    type: Boolean,
    default: true
  },
  showBatchDeskReview: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'desk-review',
  'assign-editor',
  'final-decision',
  'retract',
  'view-detail',
  'batch-desk-review'
])

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '无'
  return dateStr.substring(0, 19).replace('T', ' ')
}

// 状态标签颜色
const getStatusType = (status) => {
  const map = {
    'Incomplete': 'info',
    'Processing': 'warning',
    'Revision': 'primary',
    'Decided': 'success',
    'Rejected': 'danger'
  }
  return map[status] || ''
}

// 状态文本
const getStatusText = (status) => {
  const map = {
    'Incomplete': '待初审',
    'Processing': '处理中',
    'Revision': '需修改',
    'Decided': '已有决定',
    'Rejected': '已拒绝'
  }
  return map[status] || status
}

// 子状态标签颜色
const getSubStatusType = (subStatus) => {
  const map = {
    'PendingAssign': 'info',
    'WithEditor': 'warning',
    'UnderReview': 'primary',
    'Accepted': 'success',
    'Rejected': 'danger',
    'TechCheck': 'primary'
  }
  return map[subStatus] || ''
}

// 子状态文本
const getSubStatusText = (subStatus) => {
  const map = {
    'PendingAssign': '待分配',
    'WithEditor': '编辑处理中',
    'UnderReview': '正在审稿',
    'Accepted': '已录用',
    'Rejected': '已拒稿',
    'TechCheck': '技术检查'
  }
  return map[subStatus] || subStatus
}

// 获取编辑姓名（这里需要从父组件传递编辑数据）
const getEditorName = (editorId) => {
  // 在实际应用中，这里应该有编辑的映射数据
  return `编辑#${editorId}`
}

// 点击行
const handleRowClick = (row) => {
  emit('view-detail', row)
}
</script>

<style scoped>
.manuscript-table {
  width: 100%;
}

.table-actions {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.el-button + .el-button {
  margin-left: 8px;
}
</style>