<template>
  <div class="app-wrapper">
    <div class="sidebar-container">
      <h3 style="color:white; text-align:center; padding: 20px 0;">CMS 系统</h3>
      <el-menu
          active-text-color="#409EFF"
          background-color="#304156"
          text-color="#bfcbd9"
          :default-active="activeMenu"
          router
      >
        <el-menu-item index="/reviewer/dashboard">
          <el-icon><List /></el-icon>
          <span>我的审稿任务</span>
        </el-menu-item>

        <el-menu-item index="/reviewer/message">
          <el-icon><Bell /></el-icon>
          <span>消息中心</span>
        </el-menu-item>

        <el-menu-item @click="showProfileDialog = true">
          <el-icon><User /></el-icon>
          <span>修改个人信息</span>
        </el-menu-item>
      </el-menu>
    </div>

    <div class="main-container">
      <div class="navbar">
        <div class="right-menu">
          <div class="avatar-container" @click="showProfileDialog = true">
            <el-avatar
                :size="32"
                :src="userAvatarUrl"
                :alt="userName"
            >
              {{ userName.charAt(0) }}
            </el-avatar>
            <span class="user-info">{{ userName }} ({{ userRole }})</span>
          </div>
          <el-button link type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>

      <div class="app-main">
        <router-view />
      </div>
    </div>

    <el-dialog
        v-model="showProfileDialog"
        title="修改个人信息"
        width="600px"
        @open="handleDialogOpen"
    >
      <div class="profile-dialog-content">
        <div class="avatar-upload-section">
          <div class="avatar-preview">
            <el-avatar :size="100" :src="avatarPreviewUrl" :alt="profileForm.fullName">
              {{ profileForm.fullName ? profileForm.fullName.charAt(0) : 'U' }}
            </el-avatar>
          </div>
          <div class="avatar-upload-controls">
            <el-upload
                ref="uploadRef"
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <el-button type="primary" size="small"><el-icon><Upload /></el-icon>更换头像</el-button>
            </el-upload>
            <div style="font-size:12px;color:#666;">支持 JPG/PNG/WEBP，不超过 2MB</div>
          </div>
        </div>

        <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px">
          <el-form-item label="用户ID"><el-input v-model="profileForm.userId" disabled /></el-form-item>
          <el-form-item label="用户名"><el-input v-model="profileForm.username" disabled /></el-form-item>
          <el-form-item label="角色"><el-input v-model="profileForm.role" disabled /></el-form-item>
          <el-form-item label="姓名" prop="fullName"><el-input v-model="profileForm.fullName" /></el-form-item>
          <el-form-item label="邮箱" prop="email"><el-input v-model="profileForm.email" /></el-form-item>
          <el-form-item label="所属机构"><el-input v-model="profileForm.affiliation" /></el-form-item>
          <el-form-item label="研究方向"><el-input v-model="profileForm.researchDirection" type="textarea" :rows="2" /></el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveProfile" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, User, Upload, Bell } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

// 用户信息状态
const userName = ref('Reviewer')
const userRole = ref('审稿人')
const showProfileDialog = ref(false)
const saveLoading = ref(false)
const defaultAvatar = '/default-avatar.png'
const userAvatarUrl = ref(defaultAvatar)
const avatarPreviewUrl = ref(defaultAvatar)

// 表单
const profileFormRef = ref()
const profileForm = reactive({
  userId: null,
  username: '',
  role: '',
  fullName: '',
  email: '',
  affiliation: '',
  researchDirection: '',
  remark: '',
  avatarUrl: ''
})

const profileRules = reactive({
  fullName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '格式不正确', trigger: 'blur' }]
})

// === 方法 ===
// 1. 获取头像
const getAvatarUrl = async (userId) => {
  if (!userId) return defaultAvatar
  try {
    const token = localStorage.getItem('token')
    const res = await fetch(`/api/system-admin/avatar/${userId}`, {
      method: 'GET',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    if (res.ok) {
      const blob = await res.blob()
      return URL.createObjectURL(blob)
    }
  } catch (e) {
    console.error(e)
  }
  return defaultAvatar
}

// 2. 加载用户
const loadUserInfo = async () => {
  const str = localStorage.getItem('userInfo')
  if (str) {
    try {
      const info = JSON.parse(str)
      userName.value = info.fullName || info.username
      userRole.value = info.role || '审稿人'
      Object.assign(profileForm, {
        userId: info.userId,
        username: info.username,
        role: info.role,
        fullName: info.fullName,
        email: info.email,
        affiliation: info.affiliation,
        researchDirection: info.researchDirection
      })
      if (info.userId) {
        userAvatarUrl.value = await getAvatarUrl(info.userId)
      }
    } catch (e) {}
  }
}

// 3. 上传前校验
const avatarFile = ref(null)
const beforeAvatarUpload = (file) => {
  if (!/\.(jpg|jpeg|png|gif|webp)$/i.test(file.name)) {
    ElMessage.error('格式不支持')
    return false
  }
  if (file.size / 1024 / 1024 > 2) {
    ElMessage.error('不能超过 2MB')
    return false
  }
  const reader = new FileReader()
  reader.onload = (e) => (avatarPreviewUrl.value = e.target.result)
  reader.readAsDataURL(file)
  avatarFile.value = file
  return false
}

// 4. 打开/关闭弹窗
const handleDialogOpen = () => {
  avatarPreviewUrl.value = userAvatarUrl.value
  avatarFile.value = null
}

// 5. 更新逻辑 (复用后端 API)
const updateUserInfo = async (data, file) => {
  const token = localStorage.getItem('token')
  const formData = new FormData()
  if (file) formData.append('file', file)
  formData.append('userId', data.userId)
  formData.append('fullName', data.fullName)
  formData.append('email', data.email)
  if (data.affiliation) formData.append('affiliation', data.affiliation)
  if (data.researchDirection) formData.append('researchDirection', data.researchDirection)

  const res = await fetch('/api/system-admin/update-profile', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
    body: formData
  })
  if (!res.ok) throw new Error('更新失败')
  return await res.json()
}

const handleSaveProfile = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate()

  saveLoading.value = true
  try {
    const res = await updateUserInfo(profileForm, avatarFile.value)
    if (res && res.success) {
      // 更新本地缓存
      const oldInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
      const newInfo = { ...oldInfo, ...profileForm }
      localStorage.setItem('userInfo', JSON.stringify(newInfo))

      userName.value = profileForm.fullName
      if (avatarFile.value) {
        userAvatarUrl.value = await getAvatarUrl(profileForm.userId)
      }
      ElMessage.success('保存成功')
      showProfileDialog.value = false
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存出错')
  } finally {
    saveLoading.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
/* 统一样式 */
.app-wrapper { display: flex; height: 100vh; width: 100%; }
.sidebar-container { width: 210px; background-color: #304156; height: 100%; overflow-y: auto; }
.main-container { flex: 1; display: flex; flex-direction: column; overflow: hidden; background-color: #f0f2f5; }
.navbar { height: 50px; background: #fff; box-shadow: 0 1px 4px rgba(0,21,41,.08); display: flex; align-items: center; justify-content: flex-end; padding-right: 20px; }
.right-menu { font-size: 14px; color: #333; display: flex; align-items: center; gap: 15px; }
.avatar-container { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.app-main { flex: 1; padding: 20px; overflow-y: auto; }
.profile-dialog-content { max-height: 70vh; overflow-y: auto; }
.avatar-upload-section { display: flex; gap: 20px; margin-bottom: 20px; padding: 20px; background-color: #f8f9fa; border-radius: 8px; }
.avatar-upload-controls { display: flex; flex-direction: column; gap: 10px; }
</style>