  <template>
    <div class="process-container" style="padding: 20px;">
      <el-page-header @back="$router.back()" content="稿件处理详情" />

      <el-card style="margin-top: 20px">
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: bold;">稿件核心信息</span>
            <el-tag :type="manuscriptDetail.status === 'Decided' ? 'success' : 'warning'">
              当前状态: {{ manuscriptDetail.status }} ({{ manuscriptDetail.subStatus }})
            </el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="稿件标题" :span="2">{{ manuscriptDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="作者 ID">{{ manuscriptDetail.authorId }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ manuscriptDetail.submissionTime }}</el-descriptions-item>
          <el-descriptions-item label="关键词" :span="2">{{ manuscriptDetail.keywords }}</el-descriptions-item>
          <el-descriptions-item label="摘要" :span="2">
            <div style="line-height: 1.6; white-space: pre-wrap;">{{ manuscriptDetail.abstractText }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="源文件" :span="2">
            <el-button
                v-if="manuscriptDetail.manuscriptId"
                type="success"
                size="small"
                @click="handleDownload"
            >
              <el-icon><Download /></el-icon> 下载最新稿件附件
            </el-button>
            <span v-else style="color: #999;">无附件</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card style="margin-top: 20px">
        <h3>指派审稿人</h3>
        <div style="display: flex; align-items: center; gap: 15px;">
          <el-button type="primary" plain @click="reviewerDialogVisible = true">
            <el-icon style="margin-right: 5px"><Search /></el-icon> 选择并添加审稿人
          </el-button>

          <div v-if="selectedReviewers.length > 0" style="flex: 1">
            <span style="font-size: 14px; color: #909399; margin-right: 10px;">已选择:</span>
            <el-tag
                v-for="id in selectedReviewers"
                :key="id"
                closable
                @close="removeReviewer(id)"
                style="margin-right: 8px"
            >
              {{ getReviewerName(id) }}
            </el-tag>
          </div>

          <el-button type="primary" @click="handleInvite" :disabled="selectedReviewers.length === 0">发送邀请</el-button>
        </div>

        <el-dialog v-model="reviewerDialogVisible" title="选择审稿人 (基于专业领域与负载)" width="850px">
          <el-table
              :data="reviewerOptions"
              @selection-change="handleSelectionChange"
              border
              stripe
              max-height="450"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column label="智能推荐" width="150" align="center">
              <template #default="scope">
                <el-tag
                    v-if="scope.$index < 3 && scope.row.recommendScore > 0"
                    type="success"
                    style="
        width: 115px;
        display: inline-flex !important; /* 强制使用 flex */
        flex-direction: row;             /* 强制水平排列 */
        align-items: center;            /* 强制垂直居中 */
        justify-content: center;         /* 强制水平居中 */
        white-space: nowrap !important;  /* 绝不换行 */
        font-weight: bold;
      "
                >
                  <el-icon style="flex-shrink: 0; font-size: 14px; margin-right: 4px; display: inline-flex;">
                    <StarFilled />
                  </el-icon>
                  <span style="flex-shrink: 0; line-height: 1;">强力推荐</span>
                </el-tag>

                <el-tag
                    v-else-if="scope.row.recommendScore > 5"
                    type="warning"
                    style="width: 115px; display: inline-flex; align-items: center; justify-content: center; font-weight: bold;"
                >
                  高度匹配
                </el-tag>

                <el-tag
                    v-else
                    type="primary"
                    plain
                    style="width: 115px; display: inline-flex; align-items: center; justify-content: center;"
                >
                  一般匹配
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="姓名" prop="name" width="120" />
            <el-table-column label="所属单位" prop="affiliation" show-overflow-tooltip />
            <el-table-column label="研究方向" prop="researchDirection" show-overflow-tooltip />
            <el-table-column label="在审任务" prop="activeTasks" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.activeTasks > 3 ? 'danger' : 'success'">
                  {{ scope.row.activeTasks || 0 }} 篇
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <template #footer>
            <el-button @click="reviewerDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmReviewers">确定选择</el-button>
          </template>
        </el-dialog>
      </el-card>
    </div>
  </template>

  <script setup>
  import { ref, onMounted, watch } from 'vue' // 确保引入了 watch
  import { useRoute } from 'vue-router'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { Download, Search, StarFilled } from '@element-plus/icons-vue' // 新增 StarFilled
  import axios from 'axios';



  const route = useRoute()
  const mid = route.query.id // 获取地址栏 ?id=xxx

  // 1. 数据状态定义
  const manuscriptDetail = ref({})
  const reviewerDialogVisible = ref(false)
  const tempSelection = ref([]) // 弹窗临时的勾选记录
  const reviewerOptions = ref([])
  const selectedReviewers = ref([])
  const chatHistory = ref([])
  const msgDialogVisible = ref(false)



  const msgForm = ref({
    title: '',
    content: ''
  })

  // 2. 初始化加载
  onMounted(() => {
    if (!mid) {
      ElMessage.error("未获取到有效的稿件ID")
      return
    }
    fetchDetail()
    fetchReviewers()
    fetchChatHistory()
  })
  watch(reviewerDialogVisible, (val) => {
    if (val) {
      // 当弹窗打开(val为true)时，调用后端推荐接口获取最新排名
      fetchReviewers();
    }
  })
  // 3. 业务逻辑方法
  const fetchDetail = async () => {
    const res = await fetch(`http://localhost:8080/api/editor/detail?id=${mid}`, {
      headers: { 'Authorization': localStorage.getItem('token') }
    })
    const result = await res.json()
    if (result.code === 200) manuscriptDetail.value = result.data
  }

  const fetchReviewers = async () => {
    try {
      // 关键修改：改用 /api/editor/recommend 接口，并带上当前稿件 ID (mid)
      const res = await fetch(`http://localhost:8080/api/editor/recommend?id=${mid}`, {
        headers: { 'Authorization': localStorage.getItem('token') }
      })
      const result = await res.json()
      if (result.code === 200) {
        reviewerOptions.value = result.data.map(user => ({
          id: Number(user.userId || user.id),
          name: user.fullName || user.username,
          affiliation: user.affiliation || '未填写',
          researchDirection: user.researchDirection || '未填写',
          activeTasks: user.activeTasks || 0,
          recommendScore: user.recommendScore || 0 // 映射后端计算的分数
        }))
      }
    } catch (e) {
      ElMessage.error("推荐审稿人加载失败")
    }
  }

  // 监听弹窗内表格的勾选
  const handleSelectionChange = (val) => {
    tempSelection.value = val;
  };

  // 弹窗点击“确定选择”
  const confirmReviewers = () => {
    // 将勾选的人员 ID 合并到已选列表中（去重）
    const newIds = tempSelection.value.map(item => item.id);
    selectedReviewers.value = [...new Set([...selectedReviewers.value, ...newIds])];
    reviewerDialogVisible.value = false;
  };

  // 获取姓名用于 Tag 标签展示
  const getReviewerName = (id) => {
    const reviewer = reviewerOptions.value.find(r => r.id === id);
    return reviewer ? reviewer.name : id;
  };

  // 点击标签上的 x 移除审稿人
  const removeReviewer = (id) => {
    selectedReviewers.value = selectedReviewers.value.filter(item => item !== id);
  };

  const fetchChatHistory = async () => {
    const res = await fetch(`http://localhost:8080/api/message/history/${mid}`, {
      headers: { 'Authorization': localStorage.getItem('token') }
    })
    const result = await res.json()
    if (result.code === 200) chatHistory.value = result.data
  }

  const handleDownload = async () => {
    try {
      const response = await axios({
        url: `http://localhost:8080/api/editor/download/${mid}`,
        method: 'GET',
        headers: { 'Authorization': localStorage.getItem('token') },
        responseType: 'blob' // 必须确保这个属性直接写在请求配置里
      });

      // 1. 健壮性检查：如果后端返回的是 JSON（说明报错了），而不是文件流
      if (response.data.type === 'application/json') {
        const reader = new FileReader();
        reader.onload = () => {
          const result = JSON.parse(reader.result);
          ElMessage.error(result.message || "下载失败");
        };
        reader.readAsText(response.data);
        return;
      }

      // 2. 获取文件名
      let fileName = `manuscript_${mid}.pdf`;
      const disposition = response.headers['content-disposition'];
      if (disposition) {
        const filenameMatch = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (filenameMatch && filenameMatch[1]) {
          fileName = decodeURIComponent(filenameMatch[1].replace(/['"]/g, ''));
          if (fileName.includes("utf-8''")) fileName = fileName.split("utf-8''")[1];
        }
      }

      // 3. 触发下载
      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();

      // 4. 延迟清理
      setTimeout(() => {
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      }, 100);

      ElMessage.success("下载成功");
    } catch (error) {
      console.error("下载出错:", error);
      ElMessage.error("网络请求失败，请稍后重试");
    }
  };
  // 发送邀请逻辑
  const handleInvite = async () => {
    if (selectedReviewers.value.length === 0) return ElMessage.warning("请至少选择一位审稿人")

    const res = await fetch('http://localhost:8080/api/editor/invite', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': localStorage.getItem('token') },
      body: JSON.stringify({
        manuscriptId: parseInt(mid),
        reviewerIds: selectedReviewers.value
      })
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success("邀请已发送")
      fetchDetail() // 刷新状态
      selectedReviewers.value = [] // 清空选择
    }
  }


  </script>

  <style scoped>

  .process-container {
    max-width: 1200px;
    margin: 0 auto;
  }
  .el-card {
    margin-bottom: 20px;
  }
  h3 {
    margin-top: 0;
    margin-bottom: 20px;
    color: #303133;
  }
  /* 1. 增强未勾选时的边框颜色，让它更清晰 */
  :deep(.el-checkbox__inner) {
    border: 1px solid #b0b4bb !important; /* 调深灰色边框 */
    width: 16px;  /* 稍微放大一点点 */
    height: 16px;
  }

  /* 2. 鼠标悬停在勾选框上时的高亮颜色 */
  :deep(.el-checkbox__inner:hover) {
    border-color: #409eff !important;
  }

  /* 3. 勾选后的背景色和边框色（使用更亮的蓝色） */
  :deep(.el-checkbox.is-checked .el-checkbox__inner) {
    background-color: #409eff !important;
    border-color: #409eff !important;
  }

  /* 4. 增强全选/半选状态的视觉效果 */
  :deep(.el-checkbox__input.is-indeterminate .el-checkbox__inner) {
    background-color: #409eff !important;
    border-color: #409eff !important;
  }
  </style>