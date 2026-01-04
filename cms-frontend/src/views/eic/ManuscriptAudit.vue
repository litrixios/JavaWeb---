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
            <!-- 新增：报表导出功能 -->
            <el-date-picker
                v-model="exportDateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                size="small"
                style="margin-right: 10px; width: 240px;"
            />
            <el-button type="success" size="small" @click="handleExportReport" :loading="exportLoading">
              <el-icon><Download /></el-icon> 导出报表
            </el-button>

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
            <el-table-column label="作者列表" width="200">
              <template #default="{ row }">
                {{ formatAuthorList(row.authorList) }}
              </template>
            </el-table-column>
            <el-table-column prop="keywords" label="关键词" width="120" />

            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="240" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="success"
                    size="small"
                    @click="openDeskReviewDialog(scope.row)"
                >
                  初审
                </el-button>
                <!-- 新增：历史按钮 -->
                <el-button
                    type="primary"
                    size="small"
                    @click="handleViewDetail(scope.row)"
                >
                  历史
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
            <el-table-column label="作者列表" width="200">
              <template #default="{ row }">
                {{ formatAuthorList(row.authorList) }}
              </template>
            </el-table-column>
            <el-table-column prop="keywords" label="关键词" width="120" />
            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="240" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="primary"
                    size="small"
                    @click="openAssignDialog(scope.row)"
                >
                  指派编辑
                </el-button>
                <!-- 新增：历史按钮 -->
                <el-button
                    type="primary"
                    size="small"
                    @click="handleViewDetail(scope.row)"
                >
                  历史
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
            <el-table-column label="作者列表" width="200">
              <template #default="{ row }">
                {{ formatAuthorList(row.authorList) }}
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

            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <!-- 新增：历史按钮 -->
                <el-button
                    type="primary"
                    size="small"
                    @click="handleViewDetail(scope.row)"
                >
                  历史
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
            <el-table-column label="作者列表" width="200">
              <template #default="{ row }">
                {{ formatAuthorList(row.authorList) }}
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

            <el-table-column label="投稿时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.submissionTime) }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="240" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="warning"
                    size="small"
                    @click="openFinalDecisionDialog(scope.row)"
                >
                  终审
                </el-button>
                <!-- 新增：历史按钮 -->
                <el-button
                    type="primary"
                    size="small"
                    @click="handleViewDetail(scope.row)"
                >
                  历史
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
            <el-table-column label="作者列表" width="200">
              <template #default="{ row }">
                {{ formatAuthorList(row.authorList) }}
              </template>
            </el-table-column>
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

            <el-table-column label="操作" width="180" fixed="right" align="center">
              <template #default="scope">
                <!-- 新增：历史按钮 -->
                <el-button
                    type="primary"
                    size="small"
                    @click="handleViewDetail(scope.row)"
                >
                  历史
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
            <el-table-column label="作者列表" width="200">
              <template #default="{ row }">
                {{ formatAuthorList(row.authorList) }}
              </template>
            </el-table-column>
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

            <el-table-column label="操作" width="320" fixed="right" align="center">
              <template #default="scope">
                <el-button
                    type="danger"
                    size="small"
                    @click="openRetractDialog(scope.row)"
                >
                  撤稿
                </el-button>
                <el-button
                    type="warning"
                    size="small"
                    @click="openRescindDialog(scope.row)"
                >
                  撤销决定
                </el-button>
                <!-- 新增：历史按钮 -->
                <el-button
                    type="primary"
                    size="small"
                    @click="handleViewDetail(scope.row)"
                >
                  历史
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

    <!-- 新增：详细历史与审稿意见弹窗 -->
    <el-dialog
        v-model="historyDialogVisible"
        title="稿件详细历史与审稿意见"
        width="800px"
        destroy-on-close
    >
      <div v-if="historyLoading" v-loading="true" style="height: 200px;"></div>
      <div v-else-if="manuscriptHistory">
        <el-descriptions title="基本信息" :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="稿件ID">{{ manuscriptHistory.manuscript?.manuscriptId || currentManuscriptId }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ manuscriptHistory.manuscript?.title || tempTitle }}</el-descriptions-item>
          <el-descriptions-item label="作者">
            {{ formatAuthorList(manuscriptHistory.manuscript?.authorList) }}
          </el-descriptions-item>
          <el-descriptions-item label="投稿时间">{{ formatDate(manuscriptHistory.manuscript?.submissionTime) }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag size="small">{{ getStatusLabel(manuscriptHistory.manuscript?.subStatus || manuscriptHistory.manuscript?.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <h3 v-if="editorOpinions.length > 0">编辑意见摘要</h3>
        <el-table
            v-if="editorOpinions.length > 0"
            :data="editorOpinions"
            border
            stripe
            style="margin-bottom: 20px;"
        >
          <el-table-column label="编辑" min-width="140">
            <template #default="scope">
              {{ getEditorName(scope.row.editorId) }}
            </template>
          </el-table-column>

          <el-table-column prop="recommendation" label="推荐意见" width="150" show-overflow-tooltip>
            <template #default="scope">
              <el-tag v-if="scope.row.recommendation" type="info" effect="plain">
                {{ scope.row.recommendation }}
              </el-tag>
              <span v-else style="color: #909399;">-</span>
            </template>
          </el-table-column>

          <el-table-column prop="summary" label="处理总结" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              {{ scope.row.summary || '-' }}
            </template>
          </el-table-column>

          <el-table-column label="决策" width="100" align="center">
            <template #default="scope">
              <el-tag
                  v-if="scope.row.displayStatus"
                  :type="scope.row.displayStatus === '录用' ? 'success' : (scope.row.displayStatus === '拒稿' ? 'danger' : 'warning')"
              >
                {{ scope.row.displayStatus }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="决策时间" width="170">
            <template #default="scope">
              {{ formatDate(scope.row.decisionTime) }}
            </template>
          </el-table-column>
        </el-table>
        <h3>审稿意见摘要</h3>
        <el-table :data="manuscriptHistory.reviewSummaries || []" border stripe style="margin-bottom: 20px;">
          <el-table-column prop="ReviewerName" label="审稿人" width="120" />
          <el-table-column prop="Score" label="评分" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.Score >= 4 ? 'success' : 'warning'">{{ scope.row.Score }}分</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="Advice" label="建议" width="120" />
          <el-table-column prop="Comments" label="具体意见" show-overflow-tooltip />
        </el-table>

        <h3>全生命周期日志</h3>
        <el-timeline>
          <el-timeline-item
              v-for="(log, index) in manuscriptHistory.historyLogs || []"
              :key="index"
              :timestamp="formatDate(log.operationTime)"
              :type="log.operationType === 'Decision' ? 'primary' : 'info'"
          >
            <h4>{{ log.operationType }}</h4>
            <p><strong>操作人：</strong>{{ log.operatorName }}</p>
            <p><strong>详情：</strong>{{ log.description }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>

    <!-- 原有弹窗保持不变 -->
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

    <!-- 撤销决定对话框 -->
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
            <el-table-column label="作者" width="200">
              <template #default="{ row }">
                {{ formatAuthorList(row.authorList) }}
              </template>
            </el-table-column>

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
import { Refresh, Download } from '@element-plus/icons-vue'
import axios from 'axios'
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
  rescindDecision
} from '@/api/eic'

// 响应式数据
const manuscriptList = ref([])
const loading = ref(false)
const activeTab = ref('incomplete')
const statistics = ref({})

// 新增：历史功能相关数据
const historyDialogVisible = ref(false)
const historyLoading = ref(false)
const manuscriptHistory = ref(null)
const currentManuscriptId = ref(null)

// 新增：报表导出相关数据
const exportDateRange = ref([])
const exportLoading = ref(false)

// 对话框控制
const assignVisible = ref(false)
const deskReviewVisible = ref(false)
const finalDecisionVisible = ref(false)
const retractVisible = ref(false)
const rescindVisible = ref(false)

// 数据
const tempTitle = ref('')
const tempDecision = ref('')
const expertiseFilter = ref('')
const allEditors = ref([])
const filteredEditors = ref([])
const editorMap = ref({})

// 可用的新状态选项（用于撤销决定）
const availableStatuses = ref([
  { value: 'PendingDeskReview', label: '待初审' },
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

// 新增：批量初审相关数据
const batchDeskReviewVisible = ref(false)
const batchDeskReviewData = ref([])

// 新增：批量初审计算属性
const pendingAcceptCount = computed(() => {
  return batchDeskReviewData.value.filter(item => item.decision === 'DeskAccept').length
})

const pendingRejectCount = computed(() => {
  return batchDeskReviewData.value.filter(item => item.decision === 'DeskReject').length
})
// 新增：构建编辑意见数据，用于在弹窗中以表格形式展示
const editorOpinions = computed(() => {
  const history = manuscriptHistory.value
  if (!history || !history.manuscript) return []

  const m = history.manuscript

  // 如果没有核心数据，返回空数组（或者你可以选择始终显示一行空数据）
  // 这里逻辑是：只有当编辑给出了推荐意见、总结报告或做出了决定时才显示
  const hasContent = m.editorRecommendation || m.editorSummaryReport || m.decision

  if (!hasContent) return []

  return [{
    editorId: m.currentEditorId,
    recommendation: m.editorRecommendation,
    summary: m.editorSummaryReport,
    decision: m.subStatus === 'Accepted' ? '录用' : (m.subStatus === 'Rejected' ? '拒稿' : m.decision),
    decisionTime: m.decisionTime,
    // 如果有 subStatus，优先显示 subStatus 的中文含义
    displayStatus: m.subStatus === 'Accepted' ? '录用' :
        (m.subStatus === 'Rejected' ? '拒稿' :
            (m.decision || '处理中'))
  }]
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
    'Decided': '已有决定',
    'Accepted': '录用',
    'Rejected': '拒稿',
    'TechCheck': '技术审查',
    'DeskAccept': '通过初审',
    'DeskReject': '直接拒稿',
    'Accept': '录用',
    'Reject': '拒稿'
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
  currentManuscript.value = row
  detailVisible.value = true
}

// 新增：查看历史功能
const handleViewDetail = async (row) => {
  historyDialogVisible.value = true
  historyLoading.value = true
  currentManuscriptId.value = row.manuscriptId
  tempTitle.value = row.title

  try {
    const response = await axios.get(`/api/eic/manuscript/${row.manuscriptId}/details`)
    if (response.data.success) {
      manuscriptHistory.value = response.data.data
    } else {
      ElMessage.error('获取历史失败：' + response.data.msg)
    }
  } catch (error) {
    ElMessage.error('网络请求错误')
  } finally {
    historyLoading.value = false
  }
}

// 新增：导出报表功能
const handleExportReport = async () => {
  if (!exportDateRange.value || exportDateRange.value.length !== 2) {
    return ElMessage.warning('请选择导出时间范围')
  }
  exportLoading.value = true
  try {
    const [startDate, endDate] = exportDateRange.value
    const response = await axios.get('/api/eic/report/export', {
      params: { startDate, endDate },
      responseType: 'blob'
    })
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `报表_${startDate}_${endDate}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 原有业务逻辑保持不变
const openDeskReviewDialog = (row) => {
  tempTitle.value = row.title
  deskReviewForm.value.manuscriptId = row.manuscriptId
  deskReviewForm.value.decision = ''
  deskReviewForm.value.comments = ''
  deskReviewVisible.value = true
}

const submitDeskReview = async () => {
  if (!deskReviewForm.value.decision) {
    return ElMessage.warning('请选择初审决策')
  }

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

const handleEditorRowClick = (row) => {
  assignForm.value.editorId = row.userId
}

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

const openFinalDecisionDialog = (row) => {
  tempTitle.value = row.title
  finalDecisionForm.value.manuscriptId = row.manuscriptId
  finalDecisionForm.value.decision = ''
  finalDecisionForm.value.comments = ''
  finalDecisionForm.value.revisionDeadline = null
  finalDecisionVisible.value = true
}

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

const openRetractDialog = (row) => {
  tempTitle.value = row.title
  retractForm.value.manuscriptId = row.manuscriptId
  retractForm.value.comments = ''
  retractVisible.value = true
}

const submitRetract = async () => {
  if (!retractForm.value.comments) {
    return ElMessage.warning('请输入撤稿原因')
  }

  const currentUser = JSON.parse(localStorage.getItem('userInfo') || '{}');
  console.log('当前用户信息:', currentUser);

  const data = {
    manuscriptId: retractForm.value.manuscriptId,
    comments: retractForm.value.comments,
    operatorId: currentUser.userId,
    operatorName: currentUser.fullName
  }

  console.log('提交撤稿数据:', data);

  loading.value = true
  try {
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

const openRescindDialog = (row) => {
  tempTitle.value = row.title
  tempDecision.value = row.subStatus === 'Accepted' ? '录用' : '拒稿'
  rescindForm.value.manuscriptId = row.manuscriptId
  rescindForm.value.newStatus = ''
  rescindForm.value.reason = ''
  rescindVisible.value = true
}

const submitRescind = async () => {
  if (!rescindForm.value.newStatus) {
    return ElMessage.warning('请选择新状态')
  }

  if (!rescindForm.value.reason) {
    return ElMessage.warning('请输入撤销理由')
  }

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
    return
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

// 批量初审相关方法
const getPlaceholder = (decision) => {
  if (!decision) return '请先选择决策'
  if (decision === 'DeskReject') return '请输入拒稿理由（必填）'
  return '可填写备注（可选）'
}

const handleDecisionChange = (row) => {
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

const openBatchDeskReviewDialog = () => {
  const pendingManuscripts = incompleteManuscripts.value

  if (pendingManuscripts.length === 0) {
    ElMessage.warning('当前没有待初审的稿件')
    return
  }

  batchDeskReviewData.value = pendingManuscripts.map(manuscript => ({
    manuscriptId: manuscript.manuscriptId,
    title: manuscript.title,
    authorList: manuscript.authorList,
    decision: '',
    comments: '',
    originalStatus: manuscript.status,
    originalSubStatus: manuscript.subStatus
  }))

  batchDeskReviewVisible.value = true
}

const submitBatchDeskReview = async () => {
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
    return
  }

  const dtos = batchDeskReviewData.value
      .filter(item => item.decision)
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
    const res = await batchDeskReview(dtos)
    if (res && res.code === 200) {
      ElMessage.success(`批量初审完成，成功处理${dtos.length}篇稿件`)
      batchDeskReviewVisible.value = false

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
// 通用的作者格式化函数
const formatAuthorList = (authorData) => {
  if (!authorData) return '未知'

  let authors = authorData

  // 1. 如果是 JSON 字符串格式 (例如: '["张三", "李四"]')，尝试解析
  if (typeof authors === 'string' && authors.startsWith('[')) {
    try {
      authors = JSON.parse(authors)
    } catch (e) {
      return authors // 解析失败则返回原始字符串
    }
  }

  // 2. 如果解析后是数组，进行 map 拼接
  if (Array.isArray(authors)) {
    return authors.map(item => item.name || item).join('，')
  }

  // 3. 如果是普通字符串，直接返回
  return authors
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
  align-items: center;
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