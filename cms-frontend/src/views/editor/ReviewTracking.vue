<template>
  <div class="tracking-container" style="padding: 20px;">
    <h2>评审进度跟踪与判定</h2>
    <el-card>
      <el-table :data="trackingList" v-loading="loading" border row-key="manuscriptId" style="width: 100%">
        <el-table-column type="expand">
          <template #default="scope">
            <div style="padding: 10px 50px; background-color: #fcfcfc;">
              <h4 style="margin-bottom: 15px; color: #409EFF;">审稿人意见详情</h4>

              <el-table :data="scope.row.opinions" border size="small">
                <el-table-column label="#" type="index" />
                <el-table-column prop="reviewerName" label="审稿人" width="120" />
                <el-table-column label="评分" width="150">
                  <template #default="sub">
                    <el-rate v-model="sub.row.score" disabled show-score text-color="#ff9900" />
                  </template>
                </el-table-column>
                <el-table-column prop="suggestion" label="建议" width="120">
                  <template #default="sub">
                    <el-tag :type="getSuggestionTag(sub.row.suggestion)">{{ sub.row.suggestion }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="confidentialComments" label="给编辑的保密意见">
                  <template #default="sub">
                    <span style="color: #e6a23c; font-weight: bold;">{{ sub.row.confidentialComments || '（暂无保密意见）' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="任务状态" width="100" />
              </el-table>

              <div style="margin-top: 25px; padding: 20px; border: 1px solid #e1f3d8; background-color: #fafff5; border-radius: 8px;">
                <h4 style="margin-bottom: 15px; color: #67C23A; display: flex; align-items: center;">
                  <el-icon style="margin-right: 8px;"><Promotion /></el-icon>
                  提交编辑建议 (发送至主编)
                </h4>
                <el-form label-position="top" size="small">
                  <el-row :gutter="20">
                    <el-col :span="8">
                      <el-form-item label="建议结论" required>
                        <el-select v-model="scope.row.tempRecommendation" placeholder="请选择判定方向" style="width: 100%">
                          <el-option label="建议录用 (Suggest Acceptance)" value="Suggest Acceptance" />
                          <el-option label="建议拒稿 (Suggest Rejection)" value="Suggest Rejection" />
                          <el-option label="建议修改 (Suggest Revision)" value="Suggest Revision" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="16">
                      <el-form-item label="总结报告 (给主编的评审汇总)" required>
                        <el-input
                            v-model="scope.row.tempSummaryReport"
                            type="textarea"
                            :rows="3"
                            placeholder="请汇总上述审稿意见，并说明您的判断理由..."
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <div style="text-align: right; margin-top: 10px;">
                    <el-button
                        type="success"
                        @click="handleRecommend(scope.row)"
                        :loading="scope.row.submitting"
                    >
                      提交结论并通知主编
                    </el-button>
                  </div>
                </el-form>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="manuscriptId" label="ID" width="80" />
        <el-table-column prop="title" label="稿件标题" min-width="250" show-overflow-tooltip />
        <el-table-column label="关键词" width="300">
          <template #default="scope">
            <el-tag v-for="kw in scope.row.keywords?.split(/[;；,，]/)" :key="kw" size="small" style="margin-right: 5px; margin-bottom: 2px;">
              {{ kw }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subStatus" label="当前子状态" width="150">
          <template #default="scope">
            <el-tag :type="scope.row.subStatus === 'UnderReview' ? 'warning' : 'info'">
              {{ scope.row.subStatus }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="goProcess(scope.row.manuscriptId)">
              指派/沟通
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Promotion } from '@element-plus/icons-vue';

const router = useRouter();
const trackingList = ref([]);
const loading = ref(false);

const getEditorId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  return userInfo.userId || 1;
};

// 获取列表数据
const fetchTrackingList = async () => {
  loading.value = true;
  const editorId = getEditorId();
  try {
    const response = await fetch(`http://localhost:8080/api/editor/manuscripts/tracking?editorId=${editorId}`, {
      method: 'GET',
      headers: { 'Authorization': localStorage.getItem('token') }
    });
    const result = await response.json();
    if (result.code === 200) {
      // 初始化每一行，确保 temp 变量响应式
      trackingList.value = result.data.map(item => ({
        ...item,
        tempRecommendation: '',
        tempSummaryReport: '',
        submitting: false
      }));
    } else {
      ElMessage.error(result.msg || '获取数据失败');
    }
  } catch (error) {
    console.error('Fetch error:', error);
    ElMessage.error('服务器连接失败');
  } finally {
    loading.value = false;
  }
};

// 提交判定逻辑 (从 process.vue 迁移并适配)
const handleRecommend = async (row) => {
  if (!row.tempRecommendation || !row.tempSummaryReport) {
    return ElMessage.warning("请完整填写判定方向和总结报告");
  }

  row.submitting = true;
  try {
    const res = await fetch('http://localhost:8080/api/editor/recommend', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': localStorage.getItem('token')
      },
      body: JSON.stringify({
        manuscriptId: row.manuscriptId,
        editorRecommendation: row.tempRecommendation,
        editorSummaryReport: row.tempSummaryReport
      })
    });
    const result = await res.json();
    if (result.code === 200) {
      ElMessage.success("建议已提交，系统已通知主编并记录操作历史");
      fetchTrackingList(); // 刷新列表，日志同步生成
    } else {
      ElMessage.error(result.msg || "提交失败");
    }
  } catch (e) {
    ElMessage.error("网络异常，请稍后重试");
  } finally {
    row.submitting = false;
  }
};

const getSuggestionTag = (s) => {
  if (!s) return 'info';
  if (s.toLowerCase().includes('accept')) return 'success';
  if (s.toLowerCase().includes('reject')) return 'danger';
  return 'warning';
};

const goProcess = (id) => {
  router.push(`/editor/process?id=${id}`);
};

onMounted(() => {
  fetchTrackingList();
});
</script>

<style scoped>
.tracking-container {
  background-color: #f5f7fa;
  min-height: 100vh;
}
h2 {
  margin-bottom: 20px;
  font-weight: 500;
  color: #1f2f3d;
}
:deep(.el-table__expanded-cell) {
  padding: 0 !important;
}
</style>