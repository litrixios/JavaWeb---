<template>
  <div class="app-container" style="padding: 20px;">
    <!-- 统计信息卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :span="6" v-for="(stat, key) in statistics" :key="key">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stat }}</div>
              <div class="stat-label">{{ getStatusLabel(key) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 标签页 -->
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>稿件全览与决策中心</span>
          <div class="header-actions">
            <el-button type="primary" @click="refreshData">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <!-- 待初审稿件 -->
        <el-tab-pane label="待初审" name="incomplete">
          <div class="table-actions">
            <el-button type="primary" size="small" @click="openBatchDeskReviewDialog">
              批量初审
            </el-button>
          </div>
          <el-table
              :data="incompleteManuscripts"
              border
              style="width: 100%"
              v-loading="loading"
          >
            <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" />
            <el-table-column prop="title" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="authorList" label="作者列表" width="150" />
            <el-table-column prop="keywords" label="关键词" width="120" />

            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="success"
                    size="small"
                    @click="openDeskReviewDialog(scope.row)"
                >
                  初审
                </el-button>
                <el-button
                    type="info"
                    size="small"
                    @click="viewDetail(scope.row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 待分配编辑 -->
        <el-tab-pane label="待分配" name="pendingAssign">
          <el-table
              :data="pendingAssignManuscripts"
              border
              style="width: 100%"
              v-loading="loading"
          >
            <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" />
            <el-table-column prop="title" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="authorList" label="作者列表" width="150" />
            <el-table-column prop="keywords" label="关键词" width="120" />
            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="primary"
                    size="small"
                    @click="openAssignDialog(scope.row)"
                >
                  指派编辑
                </el-button>
                <el-button
                    type="info"
                    size="small"
                    @click="viewDetail(scope.row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 编辑处理中 -->
        <el-tab-pane label="编辑处理中" name="withEditor">
          <el-table
              :data="withEditorManuscripts"
              border
              style="width: 100%"
              v-loading="loading"
          >
            <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" />
            <el-table-column prop="title" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="authorList" label="作者列表" width="150" />
            <el-table-column label="当前编辑" width="120">
              <template #default="scope">
                <span v-if="scope.row.currentEditorId">
                  {{ getEditorName(scope.row.currentEditorId) }}
                </span>
                <span v-else style="color: #909399;">未指派</span>
              </template>
            </el-table-column>

            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="info"
                    size="small"
                    @click="viewDetail(scope.row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 正在审稿 -->
        <el-tab-pane label="正在审稿" name="underReview">
          <el-table
              :data="underReviewManuscripts"
              border
              style="width: 100%"
              v-loading="loading"
          >
            <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" />
            <el-table-column prop="title" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="authorList" label="作者列表" width="150" />
            <el-table-column label="当前编辑" width="120">
              <template #default="scope">
                <span v-if="scope.row.currentEditorId">
                  {{ getEditorName(scope.row.currentEditorId) }}
                </span>
                <span v-else style="color: #909399;">未指派</span>
              </template>
            </el-table-column>

            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="warning"
                    size="small"
                    @click="openFinalDecisionDialog(scope.row)"
                >
                  终审
                </el-button>
                <el-button
                    type="info"
                    size="small"
                    @click="viewDetail(scope.row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 需要修改 -->
        <el-tab-pane label="需要修改" name="revision">
          <el-table
              :data="revisionManuscripts"
              border
              style="width: 100%"
              v-loading="loading"
          >
            <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" />
            <el-table-column prop="title" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="authorList" label="作者列表" width="150" />
            <el-table-column prop="revisionDeadline" label="修改截止日" width="120">
              <template #default="scope">
                {{ formatDate(scope.row.revisionDeadline) }}
              </template>
            </el-table-column>

            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="info"
                    size="small"
                    @click="viewDetail(scope.row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
        <el-dialog v-model="detailVisible" title="稿件详情" width="650px">
          <div v-if="currentManuscript" class="manuscript-detail">
            <div class="detail-item">
              <label>标题：</label>
              <div class="content title">{{ currentManuscript.title }}</div>
            </div>

            <div class="detail-item">
              <label>作者列表：</label>
              <div class="content">{{ currentManuscript.authorList }}</div>
            </div>

            <div class="detail-item">
              <label>关键词：</label>
              <div class="content">{{ currentManuscript.keywords }}</div>
            </div>

            <div class="detail-item">
              <label>摘要：</label>
              <div class="content abstract">{{ currentManuscript.abstractText || '暂无摘要内容' }}</div>
            </div>
          </div>
        </el-dialog>
        <!-- 已有决定 -->
        <el-tab-pane label="已有决定" name="decided">
          <el-table
              :data="decidedManuscripts"
              border
              style="width: 100%"
              v-loading="loading"
          >
            <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" />
            <el-table-column prop="title" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="authorList" label="作者列表" width="150" />
            <el-table-column label="决定" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.subStatus === 'Accepted' ? 'success' : 'danger'">
                  {{ scope.row.subStatus === 'Accepted' ? '录用' : '拒稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="决定时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.decisionTime) }}
              </template>
            </el-table-column>

            <!-- 新增：决策详情列 -->
            <el-table-column label="决策详情" width="200">
              <template #default="scope">
                <div v-if="scope.row.editorSummaryReport" class="decision-details">
                  <span class="decision-text" :title="scope.row.editorSummaryReport">
                    {{ truncateText(scope.row.editorSummaryReport, 30) }}
                  </span>
                </div>
                <span v-else style="color: #909399;">无详情</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="260" fixed="right" align="center">
              <template #default="scope">
                <!-- 撤稿按钮 -->
                <el-button
                    type="danger"
                    size="small"
                    @click="openRetractDialog(scope.row)"
                >
                  撤稿
                </el-button>
                <!-- 新增：撤销决定按钮 -->
                <el-button
                    type="warning"
                    size="small"
                    @click="openRescindDialog(scope.row)"
                >
                  撤销决定
                </el-button>
                <el-button
                    type="info"
                    size="small"
                    @click="viewDetail(scope.row)"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 初审对话框 -->
    <el-dialog v-model="deskReviewVisible" title="稿件初审" width="500px">
      <el-form :model="deskReviewForm" label-width="80px">
        <el-form-item label="稿件标题">
          <el-input :value="tempTitle" disabled />
        </el-form-item>
        <el-form-item label="初审决策" required>
          <el-radio-group v-model="deskReviewForm.decision">
            <el-radio label="DeskAccept">通过初审（直接送审）</el-radio>
            <el-radio label="DeskReject">直接拒稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
            label="拒稿理由"
            v-if="deskReviewForm.decision === 'DeskReject'"
            required
        >
          <el-input
              v-model="deskReviewForm.comments"
              type="textarea"
              :rows="3"
              placeholder="请输入拒稿理由，如：主题超出期刊范围"
          />
        </el-form-item>
        <el-form-item
            label="备注"
            v-if="deskReviewForm.decision === 'DeskAccept'"
        >
          <el-input
              v-model="deskReviewForm.comments"
              type="textarea"
              :rows="2"
              placeholder="可选备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deskReviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDeskReview">提交</el-button>
      </template>
    </el-dialog>

    <!-- 指派编辑对话框 -->
    <el-dialog v-model="assignVisible" title="指派编辑" width="700px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="稿件标题">
          <el-input :value="tempTitle" disabled />
        </el-form-item>

        <!-- 编辑筛选 -->
        <el-form-item label="专长筛选">
          <el-input
              v-model="expertiseFilter"
              placeholder="输入研究方向筛选编辑（如：机器学习、人工智能）"
              clearable
              @input="filterEditors"
          />
        </el-form-item>

        <!-- 编辑列表表格 -->
        <el-form-item label="选择编辑">
          <div style="font-size: 12px; color: #666; margin-bottom: 10px;">
            点击表格行选择编辑，已选编辑会高亮显示
          </div>
          <el-table
              :data="filteredEditors"
              border
              style="width: 100%"
              height="300"
              @row-click="handleEditorRowClick"
              :row-class-name="getEditorRowClassName"
          >
            <el-table-column prop="fullName" label="姓名" width="120" />
            <el-table-column prop="email" label="邮箱" width="180" />
            <el-table-column prop="researchDirection" label="研究方向" />
            <el-table-column prop="affiliation" label="单位" width="150" />
          </el-table>
        </el-form-item>

        <el-form-item label="分配理由">
          <el-input
              v-model="assignForm.comments"
              type="textarea"
              :rows="2"
              placeholder="填写分配理由，如：该编辑擅长相关研究领域"
          />
        </el-form-item>

        <el-form-item label="已选编辑" v-if="assignForm.editorId">
          <el-tag type="success" size="large">
            {{ selectedEditorName }}
          </el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="submitAssign"
            :disabled="!assignForm.editorId"
        >
          确认指派
        </el-button>
      </template>
    </el-dialog>

    <!-- 终审对话框 -->
    <el-dialog v-model="finalDecisionVisible" title="终审决策" width="500px">
      <el-form :model="finalDecisionForm" label-width="80px">
        <el-form-item label="稿件标题">
          <el-input :value="tempTitle" disabled />
        </el-form-item>
        <el-form-item label="终审决策" required>
          <el-radio-group v-model="finalDecisionForm.decision">
            <el-radio label="Accept">录用</el-radio>
            <el-radio label="Reject">拒稿</el-radio>
            <el-radio label="Revise">需要修改</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="决策理由" required>
          <el-input
              v-model="finalDecisionForm.comments"
              type="textarea"
              :rows="4"
              placeholder="请输入决策理由，基于审稿意见和编辑建议"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finalDecisionVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFinalDecision">提交</el-button>
      </template>
    </el-dialog>

    <!-- 撤稿对话框 -->
    <el-dialog v-model="retractVisible" title="撤稿操作" width="500px">
      <el-form :model="retractForm" label-width="80px">
        <el-form-item label="稿件标题">
          <el-input :value="tempTitle" disabled />
        </el-form-item>
        <el-form-item label="撤稿原因" required>
          <el-input
              v-model="retractForm.comments"
              type="textarea"
              :rows="4"
              placeholder="请输入撤稿原因，如：发现学术不端行为、数据造假等"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="retractVisible = false">取消</el-button>
        <el-button type="danger" @click="submitRetract">确认撤稿</el-button>
      </template>
    </el-dialog>

    <!-- 新增：撤销决定对话框 -->
    <el-dialog v-model="rescindVisible" title="撤销决定" width="500px">
      <el-form :model="rescindForm" label-width="100px">
        <el-form-item label="稿件标题">
          <el-input :value="tempTitle" disabled />
        </el-form-item>
        <el-form-item label="原决定">
          <el-input :value="tempDecision" disabled />
        </el-form-item>
        <el-form-item label="新状态" required>
          <el-select
              v-model="rescindForm.newStatus"
              placeholder="请选择新状态"
              style="width: 100%"
          >
            <el-option
                v-for="status in availableStatuses"
                :key="status.value"
                :label="status.label"
                :value="status.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="撤销理由" required>
          <el-input
              v-model="rescindForm.reason"
              type="textarea"
              :rows="4"
              placeholder="请输入撤销决定的详细理由，如：发现审稿过程中存在错误、需要重新评估等"
          />
        </el-form-item>
        <el-form-item label="影响说明">
          <el-alert
              type="warning"
              :closable="false"
              show-icon
          >
            <p>撤销决定后：</p>
            <p>1. 稿件状态将从"已有决定"变为"处理中"</p>
            <p>2. 新的处理状态为：{{ getStatusLabel(rescindForm.newStatus) }}</p>
            <p>3. 系统会记录此次撤销操作日志</p>
          </el-alert>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rescindVisible = false">取消</el-button>
        <el-button
            type="warning"
            @click="submitRescind"
            :disabled="!rescindForm.newStatus || !rescindForm.reason"
        >
          确认撤销
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量初审对话框 -->
    <el-dialog v-model="batchDeskReviewVisible" title="批量初审" width="800px">
      <div class="batch-review-dialog">
        <!-- 说明文字 -->
        <el-alert
            title="批量初审说明"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
        >
          <p>1. 请在表格中为每个稿件选择初审决策</p>
          <p>2. 如果选择"直接拒稿"，必须填写拒稿理由</p>
          <p>3. 选择"通过初审"时，备注为可选</p>
        </el-alert>

        <!-- 批量初审表格 -->
        <div class="batch-table-container">
          <el-table
              :data="batchDeskReviewData"
              border
              style="width: 100%"
              height="400"
              :row-class-name="getBatchRowClassName"
          >
            <el-table-column prop="manuscriptId" label="稿件ID" width="80" align="center" fixed="left" />
            <el-table-column prop="title" label="稿件标题" min-width="200" show-overflow-tooltip />
            <el-table-column prop="authorList" label="作者" width="120" />

            <el-table-column label="初审决策" width="150" align="center">
              <template #default="scope">
                <el-select
                    v-model="scope.row.decision"
                    placeholder="选择决策"
                    size="small"
                    style="width: 100%"
                    @change="handleDecisionChange(scope.row)"
                >
                  <el-option label="通过初审" value="DeskAccept" />
                  <el-option label="直接拒稿" value="DeskReject" />
                </el-select>
              </template>
            </el-table-column>

            <el-table-column label="理由/备注" min-width="200">
              <template #default="scope">
                <el-input
                    v-model="scope.row.comments"
                    type="textarea"
                    :rows="2"
                    :placeholder="getPlaceholder(scope.row.decision)"
                    :disabled="!scope.row.decision"
                    @input="handleCommentsChange(scope.row)"
                />
              </template>
            </el-table-column>

            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag
                    v-if="scope.row.decision"
                    :type="scope.row.decision === 'DeskAccept' ? 'success' : 'danger'"
                    size="small"
                >
                  {{ scope.row.decision === 'DeskAccept' ? '通过' : '拒稿' }}
                </el-tag>
                <el-tag v-else type="info" size="small">待选择</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 统计信息 -->
        <div class="batch-stats" style="margin-top: 15px;">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <span class="stat-label">待处理稿件：</span>
                <span class="stat-value">{{ batchDeskReviewData.length }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <span class="stat-label">待通过：</span>
                <span class="stat-value">{{ pendingAcceptCount }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <span class="stat-label">待拒绝：</span>
                <span class="stat-value">{{ pendingRejectCount }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchDeskReviewVisible = false">取消</el-button>
        <el-button
            type="primary"
            @click="submitBatchDeskReview"
            :disabled="!canSubmitBatchReview"
        >
          提交批量初审 ({{ validBatchCount }}/{{ batchDeskReviewData.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  getAllManuscripts,
  getManuscriptStatistics,
  assignEditor,
  withdrawManuscript,
  deskReview,
  getEditorList,
  getEditorsByExpertise,
  makeFinalDecision,
  batchDeskReview,
  rescindDecision  // 新增导入撤销决定API
} from '@/api/eic'

// 响应式数据
const manuscriptList = ref([])
const loading = ref(false)
const activeTab = ref('incomplete')
const statistics = ref({})

// 对话框控制
const assignVisible = ref(false)
const deskReviewVisible = ref(false)
const finalDecisionVisible = ref(false)
const retractVisible = ref(false)
const rescindVisible = ref(false) // 新增：撤销决定对话框控制

// 数据
const tempTitle = ref('')
const tempDecision = ref('') // 新增：存储原决定
const expertiseFilter = ref('')
const allEditors = ref([])
const filteredEditors = ref([])
const editorMap = ref({})

// 可用的新状态选项（用于撤销决定）
const availableStatuses = ref([
  { value: 'PendingDeskReview', label: '待初审' },  // 新增：待初审
  { value: 'PendingAssign', label: '待分配' },
  { value: 'WithEditor', label: '编辑处理中' },
  { value: 'UnderReview', label: '正在审稿' }
])

// 表单数据
const assignForm = ref({
  manuscriptId: null,
  editorId: null,
  comments: ''
})

const deskReviewForm = ref({
  manuscriptId: null,
  decision: '',
  comments: ''
})

const finalDecisionForm = ref({
  manuscriptId: null,
  decision: '',
  comments: '',
  revisionDeadline: null
})

const retractForm = ref({
  manuscriptId: null,
  comments: ''
})

// 新增：撤销决定表单数据
const rescindForm = ref({
  manuscriptId: null,
  newStatus: '',
  reason: ''
})

// 计算属性：从后端数据计算各种状态的稿件
const incompleteManuscripts = computed(() => {
  return manuscriptList.value.filter(m =>
      m.status === 'Processing' && m.subStatus === 'PendingDeskReview'
  )
})

const pendingAssignManuscripts = computed(() => {
  return manuscriptList.value.filter(m =>
      m.status === 'Processing' && m.subStatus === 'PendingAssign'
  )
})

const withEditorManuscripts = computed(() => {
  return manuscriptList.value.filter(m =>
      m.status === 'Processing' && m.subStatus === 'WithEditor'
  )
})

const underReviewManuscripts = computed(() => {
  return manuscriptList.value.filter(m =>
      m.status === 'Processing' && m.subStatus === 'UnderReview'
  )
})

const revisionManuscripts = computed(() => {
  return manuscriptList.value.filter(m => m.status === 'Revision')
})

const decidedManuscripts = computed(() => {
  return manuscriptList.value.filter(m => m.status === 'Decided')
})

const selectedEditorName = computed(() => {
  const editor = allEditors.value.find(e => e.userId === assignForm.value.editorId)
  return editor ? `${editor.fullName} (${editor.researchDirection}) - ${editor.affiliation}` : ''
})

// 方法
const formatDate = (dateStr) => {
  if (!dateStr) return '无'
  return dateStr.substring(0, 19).replace('T', ' ')
}

// 新增：截断文本显示
const truncateText = (text, length) => {
  if (!text) return ''
  if (text.length <= length) return text
  return text.substring(0, length) + '...'
}

const getStatusLabel = (key) => {
  const labels = {
    'avgReviewDays': '平均审稿天数',
    'PendingDeskReview': '待初审',
    'PendingAssign': '待分配',
    'WithEditor': '编辑处理中',
    'UnderReview': '正在审稿',
    'Revision': '需要修改',
    'Decided': '已有决定'
  }
  return labels[key] || key
}

// 获取编辑姓名
const getEditorName = (editorId) => {
  const editor = editorMap.value[editorId]
  return editor ? `${editor.fullName} (${editor.researchDirection})` : `编辑#${editorId}`
}

// 表格行样式
const getEditorRowClassName = ({row}) => {
  return row.userId === assignForm.value.editorId ? 'selected-row' : ''
}

// 加载稿件数据（从后端）
const loadManuscripts = async () => {
  loading.value = true
  try {
    const res = await getAllManuscripts()
    if (res && res.code === 200) {
      manuscriptList.value = res.data || []
      console.log('从后端获取稿件数据:', manuscriptList.value.length, '条')
    } else {
      ElMessage.error(res.msg || '获取稿件数据失败')
    }
  } catch (error) {
    console.error('加载稿件数据失败:', error)
    ElMessage.error('加载稿件数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载统计信息（从后端）
const loadStatistics = async () => {
  try {
    const res = await getManuscriptStatistics()
    if (res && res.code === 200) {
      statistics.value = res.data || {}
      console.log('从后端获取统计信息:', statistics.value)
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 加载编辑列表（从后端）
const loadEditors = async () => {
  try {
    const res = await getEditorList()
    if (res && res.code === 200) {
      allEditors.value = res.data || []
      filteredEditors.value = [...allEditors.value]

      // 构建编辑映射表
      editorMap.value = {}
      allEditors.value.forEach(editor => {
        editorMap.value[editor.userId] = editor
      })

      console.log('从后端获取编辑列表:', allEditors.value.length, '位编辑')
    }
  } catch (error) {
    console.error('加载编辑列表失败:', error)
    ElMessage.error('加载编辑列表失败')
  }
}

// 筛选编辑
const filterEditors = async () => {
  if (!expertiseFilter.value) {
    filteredEditors.value = [...allEditors.value]
    return
  }

  try {
    const res = await getEditorsByExpertise(expertiseFilter.value)
    if (res && res.code === 200) {
      filteredEditors.value = res.data || []
    }
  } catch (error) {
    console.error('筛选编辑失败:', error)
    filteredEditors.value = [...allEditors.value]
  }
}

// 处理标签切换
const handleTabClick = (tab) => {
  console.log('切换到标签:', tab.props.name)
}

// 刷新数据
const refreshData = () => {
  loadManuscripts()
  loadStatistics()
  ElMessage.success('数据已刷新')
}

const detailVisible = ref(false)
const currentManuscript = ref(null)

// 查看详情
const viewDetail = (row) => {
  // 此时 row 已经包含了从后端获取的 title, authorList 和 abstractText [cite: 157, 158]
  currentManuscript.value = row
  detailVisible.value = true
}

// 新增：打开撤销决定对话框
const openRescindDialog = (row) => {
  tempTitle.value = row.title
  // 显示原决定
  tempDecision.value = row.subStatus === 'Accepted' ? '录用' : '拒稿'
  rescindForm.value.manuscriptId = row.manuscriptId
  rescindForm.value.newStatus = ''
  rescindForm.value.reason = ''
  rescindVisible.value = true
}

// 新增：提交撤销决定
const submitRescind = async () => {
  if (!rescindForm.value.newStatus) {
    return ElMessage.warning('请选择新状态')
  }

  if (!rescindForm.value.reason) {
    return ElMessage.warning('请输入撤销理由')
  }

  // 确认提示
  try {
    await ElMessageBox.confirm(
        `确定要撤销稿件【${tempTitle.value}】的决定吗？\n撤销后稿件将重新进入${getStatusLabel(rescindForm.value.newStatus)}状态。`,
        '确认撤销决定',
        {
          confirmButtonText: '确定撤销',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
    )
  } catch {
    return // 用户取消
  }

  loading.value = true
  try {
    const res = await rescindDecision(
        rescindForm.value.manuscriptId,
        rescindForm.value.newStatus,
        rescindForm.value.reason
    )

    if (res && res.code === 200) {
      ElMessage.success('撤销决定成功')
      rescindVisible.value = false

      // 刷新数据
      await loadManuscripts()
      await loadStatistics()
    } else {
      ElMessage.error(res.msg || '撤销决定失败')
    }
  } catch (error) {
    console.error('撤销决定失败:', error)
    ElMessage.error('撤销决定失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 新增：批量初审相关数据
const batchDeskReviewVisible = ref(false)
const batchDeskReviewData = ref([])

// 新增：计算属性
const pendingAcceptCount = computed(() => {
  return batchDeskReviewData.value.filter(item => item.decision === 'DeskAccept').length
})

const pendingRejectCount = computed(() => {
  return batchDeskReviewData.value.filter(item => item.decision === 'DeskReject').length
})

const validBatchCount = computed(() => {
  return batchDeskReviewData.value.filter(item => {
    if (!item.decision) return false
    if (item.decision === 'DeskReject') {
      return item.comments && item.comments.trim().length > 0
    }
    return true
  }).length
})

const canSubmitBatchReview = computed(() => {
  return validBatchCount.value > 0
})

// 新增：方法
const getPlaceholder = (decision) => {
  if (!decision) return '请先选择决策'
  if (decision === 'DeskReject') return '请输入拒稿理由（必填）'
  return '可填写备注（可选）'
}

const handleDecisionChange = (row) => {
  // 如果切换到通过初审，清空理由（可选）
  if (row.decision === 'DeskAccept') {
    row.comments = ''
  }
}

const handleCommentsChange = (row) => {
  // 可选：添加实时验证
}

const getBatchRowClassName = ({ row }) => {
  if (!row.decision) return 'row-pending'
  if (row.decision === 'DeskReject' && (!row.comments || row.comments.trim() === '')) {
    return 'row-error'
  }
  if (row.decision === 'DeskAccept') return 'row-success'
  if (row.decision === 'DeskReject') return 'row-danger'
  return ''
}

// 打开批量初审对话框
const openBatchDeskReviewDialog = () => {
  // 获取待初审的稿件
  const pendingManuscripts = incompleteManuscripts.value

  if (pendingManuscripts.length === 0) {
    ElMessage.warning('当前没有待初审的稿件')
    return
  }

  // 初始化批量初审数据
  batchDeskReviewData.value = pendingManuscripts.map(manuscript => ({
    manuscriptId: manuscript.manuscriptId,
    title: manuscript.title,
    authorList: manuscript.authorList,
    decision: '', // 初始为空
    comments: '', // 初始为空
    originalStatus: manuscript.status,
    originalSubStatus: manuscript.subStatus
  }))

  batchDeskReviewVisible.value = true
}

// 提交批量初审
const submitBatchDeskReview = async () => {
  // 验证数据
  const invalidItems = []
  batchDeskReviewData.value.forEach((item, index) => {
    if (item.decision === 'DeskReject' && (!item.comments || item.comments.trim() === '')) {
      invalidItems.push(`第${index + 1}行：拒稿理由不能为空`)
    }
  })

  if (invalidItems.length > 0) {
    ElMessage.error(`以下稿件信息不完整：\n${invalidItems.join('\n')}`)
    return
  }

  // 确认提示
  try {
    await ElMessageBox.confirm(
        `确定要批量处理${batchDeskReviewData.value.length}篇稿件吗？\n其中：${pendingAcceptCount.value}篇通过，${pendingRejectCount.value}篇拒绝。`,
        '确认批量初审',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )
  } catch {
    return // 用户取消
  }

  // 构建提交数据
  const dtos = batchDeskReviewData.value
      .filter(item => item.decision) // 只处理已选择决策的
      .map(item => ({
        manuscriptId: item.manuscriptId,
        decision: item.decision,
        comments: item.comments || ''
      }))

  if (dtos.length === 0) {
    ElMessage.warning('请至少为一条稿件选择决策')
    return
  }

  loading.value = true
  try {
    // 调用批量初审API
    const res = await batchDeskReview(dtos)
    if (res && res.code === 200) {
      ElMessage.success(`批量初审完成，成功处理${dtos.length}篇稿件`)
      batchDeskReviewVisible.value = false

      // 刷新数据
      await loadManuscripts()
      await loadStatistics()
    } else {
      ElMessage.error(res.msg || '批量初审失败')
    }
  } catch (error) {
    console.error('批量初审失败:', error)
    ElMessage.error('批量初审失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 打开初审对话框
const openDeskReviewDialog = (row) => {
  tempTitle.value = row.title
  deskReviewForm.value.manuscriptId = row.manuscriptId
  deskReviewForm.value.decision = ''
  deskReviewForm.value.comments = ''
  deskReviewVisible.value = true
}

// 提交初审
const submitDeskReview = async () => {
  if (!deskReviewForm.value.decision) {
    return ElMessage.warning('请选择初审决策')
  }

  // 如果是拒稿，需要理由
  if (deskReviewForm.value.decision === 'DeskReject' && !deskReviewForm.value.comments) {
    return ElMessage.warning('请输入拒稿理由')
  }

  loading.value = true
  try {
    const res = await deskReview(deskReviewForm.value)
    if (res && res.code === 200) {
      ElMessage.success('初审完成')
      deskReviewVisible.value = false
      await loadManuscripts()
      await loadStatistics()
    } else {
      ElMessage.error(res.msg || '初审失败')
    }
  } catch (error) {
    console.error('初审失败:', error)
    ElMessage.error('初审失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 打开指派编辑对话框
const openAssignDialog = (row) => {
  tempTitle.value = row.title
  assignForm.value.manuscriptId = row.manuscriptId
  assignForm.value.editorId = null
  assignForm.value.comments = ''
  expertiseFilter.value = ''

  // 加载编辑列表
  loadEditors()
  assignVisible.value = true
}

// 点击编辑行
const handleEditorRowClick = (row) => {
  assignForm.value.editorId = row.userId
}

// 提交指派
const submitAssign = async () => {
  if (!assignForm.value.editorId) {
    return ElMessage.warning('请选择编辑')
  }

  loading.value = true
  try {
    const res = await assignEditor(assignForm.value)
    if (res && res.code === 200) {
      ElMessage.success('指派成功')
      assignVisible.value = false
      await loadManuscripts()
      await loadStatistics()
    } else {
      ElMessage.error(res.msg || '指派失败')
    }
  } catch (error) {
    console.error('指派失败:', error)
    ElMessage.error('指派失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 打开终审对话框
const openFinalDecisionDialog = (row) => {
  tempTitle.value = row.title
  finalDecisionForm.value.manuscriptId = row.manuscriptId
  finalDecisionForm.value.decision = ''
  finalDecisionForm.value.comments = ''
  finalDecisionForm.value.revisionDeadline = null
  finalDecisionVisible.value = true
}

// 提交终审
const submitFinalDecision = async () => {
  if (!finalDecisionForm.value.decision) {
    return ElMessage.warning('请选择终审决策')
  }

  if (!finalDecisionForm.value.comments) {
    return ElMessage.warning('请输入决策理由')
  }

  loading.value = true
  try {
    const res = await makeFinalDecision(finalDecisionForm.value)
    if (res && res.code === 200) {
      ElMessage.success('终审完成')
      finalDecisionVisible.value = false
      await loadManuscripts()
      await loadStatistics()
    } else {
      ElMessage.error(res.msg || '终审失败')
    }
  } catch (error) {
    console.error('终审失败:', error)
    ElMessage.error('终审失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 打开撤稿对话框
const openRetractDialog = (row) => {
  tempTitle.value = row.title
  retractForm.value.manuscriptId = row.manuscriptId
  retractForm.value.comments = ''
  retractVisible.value = true
}

// 提交撤稿
const submitRetract = async () => {
  if (!retractForm.value.comments) {
    return ElMessage.warning('请输入撤稿原因')
  }

  // 添加当前用户ID（假设可以从登录信息获取）
  const currentUser = JSON.parse(localStorage.getItem('userInfo') || '{}');
  console.log('当前用户信息:', currentUser); // 添加这行

  // 构建完整的数据对象
  const data = {
    manuscriptId: retractForm.value.manuscriptId,
    comments: retractForm.value.comments,
    operatorId: currentUser.userId || 1,  // 确保有默认值
    operatorName: currentUser.fullName || '系统管理员'
  }

  console.log('提交撤稿数据:', data); // 添加这行便于调试

  loading.value = true
  try {
    // 重要：传递 data 而不是 retractForm.value
    const res = await withdrawManuscript(data)
    if (res && res.code === 200) {
      ElMessage.success('撤稿成功')
      retractVisible.value = false
      await loadManuscripts()
      await loadStatistics()
    } else {
      ElMessage.error(res.msg || '撤稿失败')
    }
  } catch (error) {
    console.error('撤稿失败:', error)
    ElMessage.error('撤稿失败: ' + error.message)
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  loadManuscripts()
  loadStatistics()
  loadEditors()
})
</script>

<style scoped>
/* 新增批量初审相关样式 */
.batch-table-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
}

:deep(.row-pending) {
  background-color: #fafafa;
}

:deep(.row-success) {
  background-color: #f0f9eb;
}

:deep(.row-danger) {
  background-color: #fef0f0;
}

:deep(.row-error) {
  background-color: #fff2f2;
}

:deep(.row-error:hover > td) {
  background-color: #ffe6e6 !important;
}

.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-5px);
  transition: transform 0.3s;
}

.stat-content {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.main-card {
  margin-top: 20px;
}

.table-actions {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.el-button + .el-button {
  margin-left: 8px;
}

:deep(.selected-row) {
  background-color: #ecf5ff;
}

/* 新增：决策详情样式 */
.decision-details {
  max-width: 200px;
  overflow: hidden;
}

.decision-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
  font-size: 13px;
}
.detail-item {
  margin-bottom: 20px;
}
.detail-item label {
  font-weight: bold;
  display: block;
  margin-bottom: 8px;
  color: #606266;
}
.detail-item .content {
  line-height: 1.6;
  color: #333;
}
.detail-item .title {
  font-size: 18px;
  color: #409EFF;
}
.detail-item .abstract {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}
</style>