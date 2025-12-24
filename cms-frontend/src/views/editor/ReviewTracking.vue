<template>
  <div class="tracking-container" style="padding: 20px;">
    <h2>评审进度跟踪</h2>
    <el-card>
      <el-table :data="trackingList" v-loading="loading" border style="width: 100%">
        <el-table-column type="expand">
          <template #default="scope">
            <div style="padding: 10px 50px; background-color: #fcfcfc;">
              <h4 style="margin-bottom: 15px; color: #409EFF;">审稿人意见详情</h4>

              <el-table :data="scope.row.opinions" border size="small">
                <index-column label="#" type="index" />
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
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="manuscriptId" label="ID" width="80" />
        <el-table-column prop="title" label="稿件标题" min-width="250" show-overflow-tooltip />
        <el-table-column label="关键词" width="200">
          <template #default="scope">
            <el-tag v-for="kw in scope.row.keywords?.split(/[;；,，]/)" :key="kw" size="small" style="margin-right: 5px;">
              {{ kw }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subStatus" label="当前子状态" width="120">
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.subStatus }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="primary" size="small" @click="goProcess(scope.row.manuscriptId)">
              去判定
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

const router = useRouter();
const trackingList = ref([]);
const loading = ref(false);

// 模拟从 localStorage 获取当前编辑的 ID
const getEditorId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  return userInfo.userId || 1; // 如果没有则默认为1进行测试
};

const fetchTrackingList = async () => {
  loading.value = true;
  const editorId = getEditorId();
  try {
    // 按照你的要求：直接通过 URL 参数传递 editorId
    const response = await fetch(`http://localhost:8080/api/editor/manuscripts/tracking?editorId=${editorId}`, {
      method: 'GET',
      headers: {
        'Authorization': localStorage.getItem('token')
      }
    });
    const result = await response.json();
    if (result.code === 200) {
      trackingList.value = result.data;
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

const getSuggestionTag = (s) => {
  if (!s) return 'info';
  if (s.includes('Accept')) return 'success';
  if (s.includes('Reject')) return 'danger';
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
  font-weight: 400;
  color: #1f2f3d;
}
</style>