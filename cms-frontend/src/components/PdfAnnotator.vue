<template>
  <div class="pdf-annotator-container" v-loading="loading">
    <div class="toolbar">
      <el-space>
        <el-button-group>
          <el-button :icon="ZoomOut" @click="scale = Math.max(0.5, scale - 0.2)" />
          <el-button>{{ (scale * 100).toFixed(0) }}%</el-button>
          <el-button :icon="ZoomIn" @click="scale = Math.min(2.5, scale + 0.2)" />
        </el-button-group>
        <el-switch
            v-model="isAnnotationMode"
            active-text="批注模式 (点击页面添加)"
            inactive-text="浏览模式"
        />
        <el-tag type="info">已添加 {{ annotations.length }} 条批注</el-tag>
      </el-space>
    </div>

    <div class="pdf-content">
      <div v-for="page in pageCount" :key="page" class="page-wrapper" :style="{ width: pdfWidth + 'px' }">
        <vue-pdf-embed
            :source="source"
            :page="page"
            :scale="scale"
            @loaded="handlePageLoaded($event, page)"
            class="pdf-page"
        />

        <div
            class="interaction-layer"
            :class="{ 'is-active': isAnnotationMode }"
            @click="handlePageClick($event, page)"
        >
          <div
              v-for="(note, index) in getAnnotationsByPage(page)"
              :key="index"
              class="annotation-marker"
              :style="{ left: note.x + '%', top: note.y + '%' }"
              @click.stop="activeNoteIndex = index"
          >
            <el-popover
                placement="top"
                :width="200"
                trigger="click"
                :content="note.content"
            >
              <template #reference>
                <div class="marker-dot">{{ index + 1 }}</div>
              </template>
              <div class="note-popup">
                <p><strong>批注:</strong> {{ note.content }}</p>
                <el-button type="danger" link size="small" @click="removeAnnotation(note)">删除</el-button>
              </div>
            </el-popover>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="添加批注" width="30%">
      <el-input
          v-model="currentNoteContent"
          type="textarea"
          :rows="3"
          placeholder="请输入对此处的修改意见..."
          autofocus
      />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAnnotation">确认添加</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import VuePdfEmbed from 'vue-pdf-embed'
import { ZoomIn, ZoomOut } from '@element-plus/icons-vue'
// 引入 pdf.js worker，必须配置，否则无法渲染
import * as pdfjsLib from 'pdfjs-dist'

// 设置 Worker 源 (使用 CDN 以避免复杂的构建配置，或者指向本地 public 目录)
pdfjsLib.GlobalWorkerOptions.workerSrc = `https://cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjsLib.version}/pdf.worker.min.js`

const props = defineProps({
  source: {
    type: [String, Object, Uint8Array],
    required: true
  }
})

const emit = defineEmits(['update:annotations'])

const loading = ref(true)
const pageCount = ref(0)
const scale = ref(1.0)
const pdfWidth = ref(800) // 估算宽度，实际由渲染决定
const isAnnotationMode = ref(false)
const dialogVisible = ref(false)
const currentNoteContent = ref('')

// 临时存储当前正在创建的注释坐标
const tempAnnotation = reactive({ page: 0, x: 0, y: 0 })

// 所有注释列表
const annotations = ref([])

// 加载文档信息
watch(() => props.source, async (newSource) => {
  if (newSource) {
    loading.value = true
    try {
      const loadingTask = pdfjsLib.getDocument(newSource)
      const pdf = await loadingTask.promise
      pageCount.value = pdf.numPages
    } catch (e) {
      console.error('PDF Load Error', e)
    } finally {
      loading.value = false
    }
  }
}, { immediate: true })

// 页面加载完成，调整容器宽度
const handlePageLoaded = (data, page) => {
  if (page === 1) {
    // 根据第一页的宽度设置容器宽度，保持居中
    // 实际宽度 = 原始宽度 * 缩放比例
    // 这里简单处理，主要依赖 scale 控制视觉大小
  }
}

// 处理点击页面
const handlePageClick = (event, page) => {
  if (!isAnnotationMode.value) return

  // 获取点击位置相对于当前页面元素的百分比坐标
  const rect = event.target.getBoundingClientRect()
  const x = ((event.clientX - rect.left) / rect.width) * 100
  const y = ((event.clientY - rect.top) / rect.height) * 100

  tempAnnotation.page = page
  tempAnnotation.x = x
  tempAnnotation.y = y
  currentNoteContent.value = ''
  dialogVisible.value = true
}

// 确认添加注释
const confirmAnnotation = () => {
  if (!currentNoteContent.value.trim()) return

  const newNote = {
    id: Date.now(),
    page: tempAnnotation.page,
    x: tempAnnotation.x,
    y: tempAnnotation.y,
    content: currentNoteContent.value
  }

  annotations.value.push(newNote)
  emit('update:annotations', annotations.value)
  dialogVisible.value = false
}

// 删除注释
const removeAnnotation = (note) => {
  const index = annotations.value.indexOf(note)
  if (index > -1) {
    annotations.value.splice(index, 1)
    emit('update:annotations', annotations.value)
  }
}

// 获取某页的注释
const getAnnotationsByPage = (page) => {
  return annotations.value.filter(a => a.page === page)
}
</script>

<style scoped>
.pdf-annotator-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f0f2f5;
  padding: 20px;
  height: 100%;
}

.toolbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: white;
  padding: 10px 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.page-wrapper {
  position: relative;
  margin-bottom: 20px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  background: white;
}

.interaction-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 10;
  cursor: default;
}

.interaction-layer.is-active {
  cursor: crosshair; /* 批注模式下显示十字光标 */
}

.interaction-layer.is-active:hover {
  background-color: rgba(64, 158, 255, 0.05);
}

.annotation-marker {
  position: absolute;
  transform: translate(-50%, -50%); /* 居中定位 */
  z-index: 20;
  cursor: pointer;
}

.marker-dot {
  width: 24px;
  height: 24px;
  background-color: #f56c6c;
  color: white;
  border-radius: 50%;
  text-align: center;
  line-height: 24px;
  font-size: 12px;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
  transition: transform 0.2s;
}

.marker-dot:hover {
  transform: scale(1.2);
}

.note-popup {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>