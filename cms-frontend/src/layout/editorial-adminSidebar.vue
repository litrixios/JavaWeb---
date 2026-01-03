<!-- src/views/editorial-admin/editorial-adminSidebar.vue -->
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
        <el-menu-item index="/editorial-admin/news">
          <el-icon><Document /></el-icon>
          <span>新闻管理</span>
        </el-menu-item>

        <el-menu-item index="/editorial-admin/tech-check">
          <el-icon><Edit /></el-icon>
          <span>形式审查</span>
        </el-menu-item>

        <!-- 个人信息 -->
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
                @error="handleAvatarError"
            >
              {{ userName?.charAt?.(0) || 'U' }}
            </el-avatar>
            <span class="user-info">{{ userName }} ({{ userRole }})</span>
          </div>
          <el-button link type="danger" size="small" @click="handleLogout">
            退出
          </el-button>
        </div>
      </div>

      <div class="app-main">
        <router-view />
      </div>
    </div>

    <!-- 个人信息弹窗 -->
    <el-dialog
        v-model="showProfileDialog"
        title="修改个人信息"
        width="600px"
        :before-close="handleCloseDialog"
        @open="handleDialogOpen"
    >
      <div class="profile-dialog-content">
        <!-- 头像上传区域 -->
        <div class="avatar-upload-section">
          <div class="avatar-preview">
            <el-avatar
                :size="100"
                :src="avatarPreviewUrl"
                :alt="profileForm.fullName"
                @error="handlePreviewAvatarError"
            >
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
                :http-request="handleAvatarUpload"
                accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <el-button type="primary" size="small">
                <el-icon><Upload /></el-icon>
                选择头像
              </el-button>
            </el-upload>

            <div class="upload-tips">
              支持 JPG、PNG、GIF、WEBP 格式，大小不超过 2MB
            </div>

            <el-button
                v-if="avatarPreviewUrl && avatarPreviewUrl !== defaultAvatar"
                type="danger"
                size="small"
                @click="handleRemoveAvatar"
            >
              移除头像
            </el-button>
          </div>
        </div>

        <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            class="profile-form"
        >
          <el-form-item label="用户ID" prop="userId">
            <el-input v-model="profileForm.userId" disabled />
          </el-form-item>

          <el-form-item label="用户名" prop="username">
            <el-input v-model="profileForm.username" disabled />
          </el-form-item>

          <el-form-item label="角色" prop="role">
            <el-input v-model="profileForm.role" disabled />
          </el-form-item>

          <el-form-item label="姓名" prop="fullName">
            <el-input v-model="profileForm.fullName" placeholder="请输入您的姓名" />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱地址" />
          </el-form-item>

          <el-form-item label="所属机构" prop="affiliation">
            <el-input v-model="profileForm.affiliation" placeholder="请输入所属机构" />
          </el-form-item>

          <el-form-item label="研究方向" prop="researchDirection">
            <el-input
                v-model="profileForm.researchDirection"
                type="textarea"
                :rows="2"
                placeholder="请输入研究方向"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showProfileDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSaveProfile" :loading="saveLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Edit, User, Upload } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

/**
 * 如果你的编辑部管理员接口是 /api/editorial-admin/...
 * 直接把下面两个常量改成对应路径即可：
 */
const AVATAR_API_PREFIX = '/api/system-admin/avatar'
const UPDATE_PROFILE_API = '/api/system-admin/update-profile'

const userName = ref('User')
const userRole = ref('编辑部管理员')
const showProfileDialog = ref(false)
const saveLoading = ref(false)
const profileFormRef = ref()
const uploadRef = ref()
const avatarFile = ref(null)

const defaultAvatar = '/default-avatar.png'
const userAvatarUrl = ref(defaultAvatar)
const avatarPreviewUrl = ref(defaultAvatar)

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
  fullName: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  affiliation: [{ required: true, message: '请输入所属机构', trigger: 'blur' }]
})

const activeMenu = computed(() => {
  // 让详情页也高亮到其所属菜单
  const p = route.path || ''
  if (p.startsWith('/editorial-admin/news')) return '/editorial-admin/news'
  if (p.startsWith('/editorial-admin/tech-check')) return '/editorial-admin/tech-check'
  return '/editorial-admin/news'
})

const getAvatarUrl = async (userId) => {
  if (!userId) return defaultAvatar
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`${AVATAR_API_PREFIX}/${userId}`, {
      method: 'GET',
      headers: { Authorization: `Bearer ${token}` }
    })

    if (!response.ok) return defaultAvatar
    const blob = await response.blob()
    return URL.createObjectURL(blob)
  } catch {
    return defaultAvatar
  }
}

const loadUserInfo = async () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return

  try {
    const userInfo = JSON.parse(userInfoStr)
    userName.value = userInfo.fullName || userInfo.username || 'User'
    userRole.value = userInfo.role || '编辑部管理员'

    Object.assign(profileForm, {
      userId: userInfo.userId || null,
      username: userInfo.username || '',
      role: userInfo.role || '',
      fullName: userInfo.fullName || '',
      email: userInfo.email || '',
      affiliation: userInfo.affiliation || '',
      researchDirection: userInfo.researchDirection || '',
      remark: userInfo.remark || '',
      avatarUrl: userInfo.avatarUrl || ''
    })

    if (userInfo.userId) {
      const avatarUrl = await getAvatarUrl(userInfo.userId)
      userAvatarUrl.value = avatarUrl
      avatarPreviewUrl.value = avatarUrl
    }
  } catch (e) {
    console.error('用户信息解析失败', e)
  }
}

const handleAvatarError = () => {
  userAvatarUrl.value = defaultAvatar
}
const handlePreviewAvatarError = () => {
  avatarPreviewUrl.value = defaultAvatar
}

onMounted(() => {
  loadUserInfo()
})

watch(
    () => profileForm.userId,
    async (newUserId) => {
      if (!newUserId) return
      userAvatarUrl.value = await getAvatarUrl(newUserId)
    }
)

const beforeAvatarUpload = (file) => {
  const isImage = /\.(jpg|jpeg|png|gif|webp)$/i.test(file.name)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('头像只能是图片格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }

  const reader = new FileReader()
  reader.onload = (e) => (avatarPreviewUrl.value = e.target.result)
  reader.readAsDataURL(file)

  avatarFile.value = file
  return false
}

const handleAvatarUpload = () => {
  // 保存时统一上传
}

const handleRemoveAvatar = () => {
  avatarPreviewUrl.value = defaultAvatar
  avatarFile.value = null
}

const handleDialogOpen = () => {
  avatarPreviewUrl.value = userAvatarUrl.value
  avatarFile.value = null
}

const isFormModified = () => {
  const userInfoStr = localStorage.getItem('userInfo')
  if (!userInfoStr) return false

  try {
    const userInfo = JSON.parse(userInfoStr)
    return (
        profileForm.fullName !== (userInfo.fullName || '') ||
        profileForm.email !== (userInfo.email || '') ||
        profileForm.affiliation !== (userInfo.affiliation || '') ||
        profileForm.researchDirection !== (userInfo.researchDirection || '') ||
        profileForm.remark !== (userInfo.remark || '')
    )
  } catch {
    return false
  }
}

const handleCloseDialog = (done) => {
  if (isFormModified() || avatarFile.value) {
    ElMessageBox.confirm('确定要放弃修改吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
        .then(() => done())
        .catch(() => {})
  } else {
    done()
  }
}

const updateUserInfo = async (userData, file) => {
  const token = localStorage.getItem('token')
  if (!token) throw new Error('未找到认证令牌')

  const formData = new FormData()
  if (file) formData.append('file', file)

  formData.append('userId', userData.userId)
  formData.append('fullName', userData.fullName)
  formData.append('email', userData.email)
  if (userData.affiliation) formData.append('affiliation', userData.affiliation)
  if (userData.researchDirection) formData.append('researchDirection', userData.researchDirection)

  const response = await fetch(UPDATE_PROFILE_API, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: formData
  })

  if (!response.ok) {
    const err = await response.json().catch(() => ({}))
    throw new Error(err.message || `HTTP错误! 状态: ${response.status}`)
  }
  return await response.json()
}

const handleSaveProfile = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
    if (!profileForm.userId) {
      ElMessage.error('用户ID不能为空')
      return
    }

    const currentUserInfoStr = localStorage.getItem('userInfo')
    if (currentUserInfoStr) {
      const currentUserInfo = JSON.parse(currentUserInfoStr)
      if (currentUserInfo.userId !== profileForm.userId) {
        ElMessage.error('只能修改自己的个人信息')
        return
      }
    }

    saveLoading.value = true
    const result = await updateUserInfo(profileForm, avatarFile.value)

    if (result && result.success) {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        const userInfo = JSON.parse(userInfoStr)
        const updatedUserInfo = {
          ...userInfo,
          fullName: profileForm.fullName,
          email: profileForm.email,
          affiliation: profileForm.affiliation,
          researchDirection: profileForm.researchDirection,
          remark: profileForm.remark
        }
        localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
        userName.value = profileForm.fullName || userInfo.username || 'User'
        userRole.value = userInfo.role || '编辑部管理员'

        if (avatarFile.value) {
          userAvatarUrl.value = await getAvatarUrl(profileForm.userId)
        }
      }

      ElMessage.success('个人信息更新成功')
      showProfileDialog.value = false
      avatarFile.value = null
    } else {
      ElMessage.error(result?.message || '更新失败')
    }
  } catch (e) {
    ElMessage.error('保存失败：' + (e?.message || '未知错误'))
  } finally {
    saveLoading.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
  display: flex;
  height: 100vh;
  width: 100%;
}

.sidebar-container {
  width: 210px;
  background-color: #304156;
  height: 100%;
  overflow-y: auto;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f0f2f5;
}

.navbar {
  height: 50px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 20px;
}

.right-menu {
  font-size: 14px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar-container {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.avatar-container:hover {
  background-color: #f5f5f5;
}

.user-info {
  font-size: 14px;
}

.app-main {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 弹窗内容样式 */
.profile-dialog-content {
  max-height: 70vh;
  overflow-y: auto;
}

.avatar-upload-section {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.avatar-preview {
  flex-shrink: 0;
}

.avatar-upload-controls {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-tips {
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

.profile-form {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .avatar-upload-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .right-menu {
    flex-direction: column;
    gap: 8px;
  }

  .avatar-container {
    flex-direction: column;
    text-align: center;
  }
}
</style>
