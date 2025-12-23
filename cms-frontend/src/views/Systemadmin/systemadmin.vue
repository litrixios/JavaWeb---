<template>
  <div class="user-management-container">
    <div class="header">
      <h2>用户管理</h2>
    </div>

    <div class="search-bar">
      <el-input
          v-model="userQuery.username"
          placeholder="用户名搜索"
          style="width: 200px; margin-right: 10px;"
      />
      <el-select
          v-model="userQuery.role"
          placeholder="角色筛选"
          style="width: 180px; margin-right: 10px;"
      >
        <el-option label="所有角色" value="" />
        <el-option label="SuperAdmin" value="SuperAdmin" />
        <el-option label="EditorInChief" value="EditorInChief" />
        <el-option label="EditorialAdmin" value="EditorialAdmin" />
        <el-option label="Editor" value="Editor" />
        <el-option label="Author" value="Author" />
        <el-option label="Reviewer" value="Reviewer" />
        <el-option label="SystemAdmin" value="SystemAdmin" />
      </el-select>
      <el-select
          v-model="userQuery.status"
          placeholder="状态筛选"
          style="width: 120px; margin-right: 10px;"
      >
        <el-option label="所有状态" value="" />
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="fetchUsers">查询</el-button>
      <el-button type="success" @click="openUserDialog()">新增用户</el-button>
    </div>

    <el-table
        :data="userList"
        border
        style="width: 100%; margin-top: 15px;"
        v-loading="userLoading"
        empty-text="暂无用户数据"
    >
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="fullName" label="姓名" width="120" />
      <el-table-column prop="role" label="角色" width="140" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="affiliation" label="单位" />
      <el-table-column prop="registerTime" label="注册时间" width="180" />
      <el-table-column
          prop="status"
          label="状态"
          width="100"
          align="center"
      >
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button
              size="small"
              type="primary"
              @click="openUserDialog(scope.row)"
          >
            编辑
          </el-button>
          <el-button
              size="small"
              type="warning"
              @click="openPermissionDialog(scope.row)"
          >
            权限
          </el-button>
          <el-button
              size="small"
              type="warning"
              @click="handleChangeStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button
              size="small"
              type="danger"
              @click="handleDeleteUser(scope.row.userId)"
              :disabled="scope.row.role === 'SuperAdmin'"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 用户编辑对话框 -->
    <el-dialog
        v-model="userDialogVisible"
        :title="userDialogTitle"
        width="600px"
        @close="resetUserForm"
    >
      <el-form
          ref="userFormRef"
          :model="userForm"
          :rules="userFormRules"
          label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEditMode" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEditMode">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!isEditMode">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请确认密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="fullName">
          <el-input v-model="userForm.fullName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" style="width: 100%;">
            <el-option
                v-for="role in availableRoles"
                :key="role.value"
                :label="role.label"
                :value="role.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="单位" prop="affiliation">
          <el-input v-model="userForm.affiliation" />
        </el-form-item>
        <el-form-item label="研究方向">
          <el-input v-model="userForm.researchDirection" />
        </el-form-item>
        <el-form-item label="状态" v-if="isEditMode">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saveLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 用户权限对话框 -->
    <el-dialog
        v-model="permissionDialogVisible"
        title="用户权限设置"
        width="700px"
    >
      <el-form :model="userPermissionForm">
        <el-form-item label="用户名">
          <el-input v-model="userPermissionForm.username" disabled />
        </el-form-item>
        <el-form-item label="权限设置">
          <el-checkbox-group v-model="userPermissionForm.permissions">
            <el-checkbox name="userPermissions" label="canSubmitManuscript">投稿权限</el-checkbox>
            <el-checkbox name="userPermissions" label="canViewAllManuscripts">查看所有稿</el-checkbox>
            <el-checkbox name="userPermissions" label="canAssignReviewer">分配审稿人</el-checkbox>
            <el-checkbox name="userPermissions" label="canViewReviewerIdentity">查看审稿人身份</el-checkbox>
            <el-checkbox name="userPermissions" label="canWriteReview">撰写审稿意见</el-checkbox>
            <el-checkbox name="userPermissions" label="canMakeDecision">稿件决议</el-checkbox>
            <el-checkbox name="userPermissions" label="canModifySystemConfig">修改系统配置</el-checkbox>
            <el-checkbox name="userPermissions" label="canTechCheck">技术审查</el-checkbox>
            <el-checkbox name="userPermissions" label="canPublishNews">发布新闻</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUserPermissions">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, onBeforeMount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'

// 添加路由实例
const router = useRouter()

// token验证函数
const checkAuth = () => {
  const token = localStorage.getItem('token')
  const userInfo = localStorage.getItem('userInfo')

  if (!token || !userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }

  try {
    const userData = JSON.parse(userInfo)
    // 可以添加额外的权限验证，比如只有特定角色可以访问用户管理
    const allowedRoles = ['SuperAdmin', 'SystemAdmin', 'EditorialAdmin']
    if (!allowedRoles.includes(userData.role)) {
      ElMessage.error('权限不足，无法访问用户管理')
      router.push('/unauthorized')
      return false
    }
    return true
  } catch (error) {
    console.error('解析用户信息失败:', error)
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
    return false
  }
}

// token过期处理函数
const handleTokenExpired = () => {
  ElMessage.error('登录已过期，请重新登录')
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}

// 用户管理相关
const userList = ref([])
const userLoading = ref(false)
const userQuery = reactive({
  username: '',
  role: '',
  status: '',
})

// 可用的角色选项（新增用户时去掉SuperAdmin）
const availableRoles = computed(() => {
  const allRoles = [
    //{ label: 'SuperAdmin', value: 'SuperAdmin' },
    { label: 'EditorInChief', value: 'EditorInChief' },
    { label: 'EditorialAdmin', value: 'EditorialAdmin' },
    { label: 'Editor', value: 'Editor' },
    { label: 'Author', value: 'Author' },
    { label: 'Reviewer', value: 'Reviewer' },
    // { label: 'SystemAdmin', value: 'SystemAdmin' }
  ]

  // 编辑模式下显示所有角色，新增模式下去掉SuperAdmin
  return isEditMode.value ? allRoles : allRoles.filter(role => role.value !== 'SuperAdmin')
})

// 用户对话框
const userDialogVisible = ref(false)
const userFormRef = ref()
const userForm = reactive({
  userId: null,
  username: '',
  password: '',
  confirmPassword: '',
  fullName: '',
  role: '',
  email: '',
  affiliation: '',
  researchDirection: '',
  status: 1
})

const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== userForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  fullName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const isEditMode = computed(() => !!userForm.userId)
const userDialogTitle = computed(() => isEditMode.value ? '编辑用户' : '新增用户')
const saveLoading = ref(false)

// 用户权限对话框
const permissionDialogVisible = ref(false)
const userPermissionForm = reactive({
  userId: null,
  username: '',
  permissions: []
})

// 在组件加载前检查权限
onBeforeMount(() => {
  //checkAuth()
})

// 初始化
onMounted(() => {
  if (checkAuth()) {
    fetchUsers()
  }
})

// 获取用户列表
const fetchUsers = async () => {
  // 先检查token
  if (!checkAuth()) return

  userLoading.value = true
  try {
    const params = {
      ...userQuery,
      status: userQuery.status === '' ? null : userQuery.status
    }
    const res = await request.get('/api/system-admin/users', { params })
    if (res.code === 200) {
      // 过滤掉SuperAdmin角色的用户
      userList.value = res.data.filter(user => user.role !== 'SuperAdmin'&&user.role!=='SystemAdmin')
    } else {
      // 如果token过期或无效，后端通常会返回401
      if (res.code === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error(res.msg || '获取用户列表失败')
    }
  } catch (err) {
    // 网络错误或token过期
    if (err.response && err.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error('获取用户列表失败')
    console.error(err)
  } finally {
    userLoading.value = false
  }
}

// 打开用户对话框
const openUserDialog = (user = null) => {
  // 检查权限
  if (!checkAuth()) return

  resetUserForm()
  if (user) {
    Object.assign(userForm, {
      userId: user.userId,
      username: user.username,
      fullName: user.fullName,
      role: user.role,
      email: user.email,
      affiliation: user.affiliation,
      researchDirection: user.researchDirection,
      status: user.status
    })
  }
  userDialogVisible.value = true
}

// 重置用户表单
const resetUserForm = () => {
  userFormRef.value?.resetFields()
  Object.keys(userForm).forEach(key => {
    if (key !== 'status') {
      userForm[key] = ''
    }
  })
  userForm.status = 1
}

// 保存用户
const saveUser = async () => {
  // 先检查token
  if (!checkAuth()) return

  if (!userFormRef.value) return

  try {
    await userFormRef.value.validate()
    saveLoading.value = true

    if (isEditMode.value) {
      // 编辑用户
      const { password, confirmPassword, ...editData } = userForm
      const res = await request.put(`/api/system-admin/users/${userForm.userId}`, editData)
      if (res.code === 200) {
        ElMessage.success('用户更新成功')
        userDialogVisible.value = false
        fetchUsers()
      } else {
        if (res.code === 401) {
          handleTokenExpired()
          return
        }
        ElMessage.error(res.msg || '用户更新失败')
      }
    } else {
      // 新增用户
      const res = await request.post('/api/system-admin/users', userForm)
      if (res.code === 200) {
        ElMessage.success('用户创建成功')
        userDialogVisible.value = false
        fetchUsers()
      } else {
        if (res.code === 401) {
          handleTokenExpired()
          return
        }
        ElMessage.error(res.msg || '用户创建失败')
      }
    }
  } catch (err) {
    if (err.errors) {
      return
    }
    if (err.response && err.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error(isEditMode.value ? '用户更新失败' : '用户创建失败')
    console.error(err)
  } finally {
    saveLoading.value = false
  }
}

// 更改用户状态
const handleChangeStatus = async (user) => {
  // 先检查token
  if (!checkAuth()) return

  try {
    const newStatus = user.status === 1 ? 0 : 1
    const action = newStatus === 1 ? '启用' : '禁用'

    await ElMessageBox.confirm(`确定要${action}用户 ${user.username} 吗？`, '提示', {
      type: 'warning'
    })

    const res = await request.put(`/api/system-admin/users/${user.userId}/status`, { status: newStatus })
    if (res.code === 200) {
      ElMessage.success(`用户已${action}`)
      user.status = newStatus
    } else {
      if (res.code === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error(res.msg || `${action}用户失败`)
    }
  } catch (err) {
    if (err === 'cancel') {
      return
    }
    if (err.response && err.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error('状态更新失败')
    console.error(err)
  }
}

// 删除用户
const handleDeleteUser = async (userId) => {
  // 先检查token
  if (!checkAuth()) return

  try {
    await ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复。', '警告', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })

    const res = await request.delete(`/api/system-admin/users/${userId}`)
    if (res.code === 200) {
      ElMessage.success('用户删除成功')
      fetchUsers()
    } else {
      if (res.code === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error(res.msg || '用户删除失败')
    }
  } catch (err) {
    if (err === 'cancel') {
      return
    }
    if (err.response && err.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error('用户删除失败')
    console.error(err)
  }
}

// 打开权限对话框
const openPermissionDialog = (user) => {
  // 检查权限
  if (!checkAuth()) return

  userPermissionForm.userId = user.userId
  userPermissionForm.username = user.username
  userPermissionForm.permissions = []

  // 设置当前权限
  if (user.permissions) {
    Object.keys(user.permissions).forEach(key => {
      if (user.permissions[key] === true && key.startsWith('can')) {
        userPermissionForm.permissions.push(key)
      }
    })
  }

  permissionDialogVisible.value = true
}

// 保存用户权限
const saveUserPermissions = async () => {
  // 先检查token
  if (!checkAuth()) return

  try {
    const permissionData = {
      userId: userPermissionForm.userId,
      canSubmitManuscript: userPermissionForm.permissions.includes('canSubmitManuscript'),
      canViewAllManuscripts: userPermissionForm.permissions.includes('canViewAllManuscripts'),
      canAssignReviewer: userPermissionForm.permissions.includes('canAssignReviewer'),
      canViewReviewerIdentity: userPermissionForm.permissions.includes('canViewReviewerIdentity'),
      canWriteReview: userPermissionForm.permissions.includes('canWriteReview'),
      canMakeDecision: userPermissionForm.permissions.includes('canMakeDecision'),
      canModifySystemConfig: userPermissionForm.permissions.includes('canModifySystemConfig'),
      canTechCheck: userPermissionForm.permissions.includes('canTechCheck'),
      canPublishNews: userPermissionForm.permissions.includes('canPublishNews')
    }

    const res = await request.put('/api/system-admin/users/permissions', permissionData)
    if (res.code === 200) {
      ElMessage.success('用户权限更新成功')
      permissionDialogVisible.value = false
      fetchUsers()
    } else {
      if (res.code === 401) {
        handleTokenExpired()
        return
      }
      ElMessage.error(res.msg || '用户权限更新失败')
    }
  } catch (err) {
    if (err.response && err.response.status === 401) {
      handleTokenExpired()
      return
    }
    ElMessage.error('用户权限更新失败')
    console.error(err)
  }
}
</script>

<style scoped>
.user-management-container {
  padding: 20px;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #303133;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}
</style>