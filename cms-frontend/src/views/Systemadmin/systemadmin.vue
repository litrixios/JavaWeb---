<template>
  <div class="system-admin-container">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabChange">
      <el-tab-pane label="用户管理" name="userManagement"></el-tab-pane>
<!--      <el-tab-pane label="权限管理" name="permissionManagement"></el-tab-pane>-->
      <el-tab-pane label="系统维护" name="systemMaintenance"></el-tab-pane>
    </el-tabs>

    <!-- 用户管理面板 -->
    <div v-if="activeTab === 'userManagement'" class="tab-content">
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
<!--          <el-option label="SuperAdmin" value="SuperAdmin" />-->
          <el-option label="EditorInChief" value="EditorInChief" />
          <el-option label="EditorialAdmin" value="EditorialAdmin" />
          <el-option label="Editor" value="Editor" />
          <el-option label="Author" value="Author" />
          <el-option label="Reviewer" value="Reviewer" />
<!--          <el-option label="SystemAdmin" value="SystemAdmin" />-->
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


    </div>

    <!-- 权限管理面板 -->
    <div v-if="activeTab === 'permissionManagement'" class="tab-content">
      <el-select
          v-model="selectedRole"
          placeholder="选择角色"
          style="width: 200px; margin-bottom: 20px;"
          @change="fetchRolePermissions"
      >
        <el-option label="EditorInChief" value="EditorInChief" />
        <el-option label="EditorialAdmin" value="EditorialAdmin" />
        <el-option label="Editor" value="Editor" />
        <el-option label="Author" value="Author" />
        <el-option label="Reviewer" value="Reviewer" />
        <el-option label="SystemAdmin" value="SystemAdmin" />
      </el-select>

      <el-card v-if="selectedRole">
        <template #header>
          <div class="card-header">
            <span>{{ selectedRole }} 角色权限设置</span>
            <el-tag type="info">当前权限：{{ currentRolePermissionsCount }} 项</el-tag>
          </div>
        </template>

        <!-- 当前权限概览 -->
        <div class="current-permissions" style="margin-bottom: 20px;">
          <h4>当前已有权限：</h4>
          <div class="permission-tags">
            <el-tag
                v-for="permission in permissionForm.permissions"
                :key="permission"
                type="success"
                style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ getPermissionLabel(permission) }}
            </el-tag>
            <el-tag v-if="permissionForm.permissions.length === 0" type="info">
              暂无权限
            </el-tag>
          </div>
        </div>

        <el-form ref="permissionForm" :model="permissionForm">
          <el-form-item label="权限设置" style="margin-top: 15px;">
            <el-checkbox-group v-model="permissionForm.permissions">
              <el-checkbox
                  name="permissions"
                  label="canSubmitManuscript"
                  border
                  :disabled="!isPermissionEditable('canSubmitManuscript')"
              >投稿权限</el-checkbox>
              <el-checkbox
                  name="permissions"
                  label="canViewAllManuscripts"
                  border
                  :disabled="!isPermissionEditable('canViewAllManuscripts')"
              >查看所有稿</el-checkbox>
              <el-checkbox
                  name="permissions"
                  label="canAssignReviewer"
                  border
                  :disabled="permissionLabels "
              >分配审稿人</el-checkbox>
              <el-checkbox
                  name="permissions"
                  label="canViewReviewerIdentity"
                  border
                  :disabled="!isPermissionEditable('canViewReviewerIdentity')"
              >查看审稿人身份</el-checkbox>
              <el-checkbox
                  name="permissions"
                  label="canWriteReview"
                  border
                  :disabled="!isPermissionEditable('canWriteReview')"
              >撰写审稿意见</el-checkbox>
              <el-checkbox
                  name="permissions"
                  label="canMakeDecision"
                  border
                  :disabled="!isPermissionEditable('canMakeDecision')"
              >稿件决议</el-checkbox>
              <el-checkbox
                  name="permissions"
                  label="canModifySystemConfig"
                  border
                  :disabled="!isPermissionEditable('canModifySystemConfig')"
              >修改系统配置</el-checkbox>
              <el-checkbox
                  name="permissions"
                  label="canTechCheck"
                  border
                  :disabled="!isPermissionEditable('canTechCheck')"
              >技术审查</el-checkbox>
              <el-checkbox
                  name="permissions"
                  label="canPublishNews"
                  border
                  :disabled="!isPermissionEditable('canPublishNews')"
              >发布新闻</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveRolePermissions">保存角色权限</el-button>
            <el-button @click="resetToDefaultPermissions">重置为默认权限</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 系统维护面板 -->
    <div v-if="activeTab === 'systemMaintenance'" class="tab-content">
      <div class="search-bar">
        <el-select
            v-model="logQuery.operationType"
            placeholder="日志类型"
            style="width: 200px; margin-right: 10px;"
        >
          <el-option label="所有日志" value="" />
          <el-option label="登录日志" value="Login" />
          <el-option label="提交日志" value="Submit" />
          <el-option label="系统日志" value="System" />
          <el-option label="用户操作" value="User" />
          <el-option label="审稿操作" value="Review" />
        </el-select>
        <el-date-picker
            v-model="logQuery.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 300px; margin-right: 10px;"
        />
        <el-button type="primary" @click="fetchLogs">查询</el-button>
        <el-button type="warning" @click="clearLogs" :disabled="logList.length === 0">清空日志</el-button>
      </div>

      <el-table
          :data="logList"
          border
          style="width: 100%; margin-top: 15px;"
          v-loading="logLoading"
          empty-text="暂无日志数据"
      >
        <el-table-column prop="logId" label="日志ID" width="100" />
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column prop="operatorName" label="操作人" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="120" />
        <el-table-column prop="manuscriptId" label="稿件ID" width="100" />
        <el-table-column prop="description" label="操作描述" min-width="200" />
      </el-table>


    </div>

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
            <!-- 新增用户时去掉SuperAdmin选项 -->
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
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

// 标签页状态
const activeTab = ref('userManagement')

// 用户管理相关
const userList = ref([])
const userLoading = ref(false)
const totalUsers = ref(0)
const userQuery = reactive({
  username: '',
  role: '',
  status: '',
})

// 可用的角色选项（新增用户时去掉SuperAdmin）
const availableRoles = computed(() => {
  const allRoles = [
    { label: 'SuperAdmin', value: 'SuperAdmin' },
    { label: 'EditorInChief', value: 'EditorInChief' },
    { label: 'EditorialAdmin', value: 'EditorialAdmin' },
    { label: 'Editor', value: 'Editor' },
    { label: 'Author', value: 'Author' },
    { label: 'Reviewer', value: 'Reviewer' },
    { label: 'SystemAdmin', value: 'SystemAdmin' }
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

// 权限管理相关
const selectedRole = ref('')
const permissionForm = reactive({
  permissions: []
})

// 计算当前角色权限数量
const currentRolePermissionsCount = computed(() => permissionForm.permissions.length)

// 权限标签映射
const permissionLabels = {
  canSubmitManuscript: '投稿权限',
  canViewAllManuscripts: '查看所有稿件',
  canAssignReviewer: '分配审稿人',
  canViewReviewerIdentity: '查看审稿人身份',
  canWriteReview: '撰写审稿意见',
  canMakeDecision: '稿件决议',
  canModifySystemConfig: '修改系统配置',
  canTechCheck: '技术审查',
  canPublishNews: '发布新闻'
}

// 获取权限标签
const getPermissionLabel = (permission) => {
  return permissionLabels[permission] || permission
}

// 检查权限是否可编辑（根据角色限制某些权限）
const isPermissionEditable = (permission) => {
  // 可以根据需要添加特定角色的权限限制
  // 例如：某些角色不能拥有某些权限
  const restrictedPermissions = {
    // 'Editor': ['canModifySystemConfig'], // 编辑不能修改系统配置
    // 'Author': ['canViewAllManuscripts', 'canAssignReviewer'] // 作者不能查看所有稿件和分配审稿人
  }

  const roleRestrictions = restrictedPermissions[selectedRole.value]
  return !roleRestrictions || !roleRestrictions.includes(permission)
}

// 用户权限对话框
const permissionDialogVisible = ref(false)
const userPermissionForm = reactive({
  userId: null,
  username: '',
  permissions: []
})

// 系统日志相关
const logList = ref([])
const logLoading = ref(false)
const totalLogs = ref(0)
const logQuery = reactive({
  operationType: '',
  dateRange: [],
})

// 初始化
onMounted(() => {
  fetchUsers()
})

// 切换标签页
const handleTabChange = (tab) => {
  if (tab.name === 'userManagement') {
    fetchUsers()
  } else if (tab.name === 'systemMaintenance') {
    fetchLogs()
  }
}

// 获取用户列表
const fetchUsers = async () => {
  userLoading.value = true
  try {
    const params = {
      ...userQuery,
      status: userQuery.status === '' ? null : userQuery.status
    }
    const res = await request.get('/api/system-admin/users', { params })
    if (res.code === 200) {
      // 在这里添加过滤逻辑
      userList.value = res.data.filter(user => {
        // 示例1：过滤掉特定用户（如SuperAdmin）
        return user.role !== 'SuperAdmin'&&user.role!=='SystemAdmin'

        // 示例2：过滤掉特定用户ID
        // return user.userId !== 1

        // 示例3：多重条件过滤
        // return user.role !== 'SuperAdmin' && user.status === 1

        // 示例4：根据当前登录用户权限过滤
        // return hasPermissionToView(user)
      })
      //userList.value = res.data
      totalUsers.value = res.total || res.data.length
    } else {
      ElMessage.error(res.msg || '获取用户列表失败')
    }
  } catch (err) {
    ElMessage.error('获取用户列表失败')
    console.error(err)
  } finally {
    userLoading.value = false
  }
}

// 分页切换
const handlePageChange = (page) => {
  userQuery.pageNum = page
  fetchUsers()
}

const handleSizeChange = (size) => {
  userQuery.pageSize = size
  userQuery.pageNum = 1
  fetchUsers()
}

// 打开用户对话框
const openUserDialog = (user = null) => {
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
        ElMessage.error(res.msg || '用户创建失败')
      }
    }
  } catch (err) {
    if (err.errors) {
      // 验证错误，不显示错误消息
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
      ElMessage.error(res.msg || `${action}用户失败`)
    }
  } catch (err) {
    if (err === 'cancel') {
      return
    }
    ElMessage.error('状态更新失败')
    console.error(err)
  }
}

// 删除用户
const handleDeleteUser = async (userId) => {
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
      ElMessage.error(res.msg || '用户删除失败')
    }
  } catch (err) {
    if (err === 'cancel') {
      return
    }
    ElMessage.error('用户删除失败')
    console.error(err)
  }
}

// 打开权限对话框
const openPermissionDialog = (user) => {
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
      ElMessage.error(res.msg || '用户权限更新失败')
    }
  } catch (err) {
    ElMessage.error('用户权限更新失败')
    console.error(err)
  }
}

const fetchRolePermissions = async () => {
  if (!selectedRole.value) {
    permissionForm.permissions = []
    return
  }

  try {
    const res = await request.get(`/api/system-admin/role-permissions/${selectedRole.value}`)
    if (res.code === 200 && res.data) {
      // 确保返回的数据是有效的权限数组
      const permissions = Object.keys(res.data).filter(key =>
          res.data[key] === true && key.startsWith('can')
      )
      permissionForm.permissions = permissions.filter(permission =>
          Object.keys(permissionLabels).includes(permission)
      )
    } else {
      // 设置默认权限
      setDefaultRolePermissions()
    }
  } catch (err) {
    console.error('获取角色权限失败:', err)
    // 设置默认权限
    setDefaultRolePermissions()
  }
}

// 设置默认角色权限
const setDefaultRolePermissions = () => {
  if (!selectedRole.value) {
    permissionForm.permissions = []
    return
  }

  const defaultPermissions = {
    EditorInChief: [
      'canViewAllManuscripts', 'canAssignReviewer',
      'canViewReviewerIdentity', 'canMakeDecision'
    ],
    EditorialAdmin: [
      'canViewAllManuscripts', 'canTechCheck', 'canPublishNews'
    ],
    Editor: [
      'canViewAllManuscripts', 'canAssignReviewer', 'canViewReviewerIdentity'
    ],
    Author: ['canSubmitManuscript'],
    Reviewer: ['canWriteReview'],
    SystemAdmin: ['canModifySystemConfig', 'canViewAllManuscripts']
  }

  permissionForm.permissions = defaultPermissions[selectedRole.value] || []
}

// 重置为默认权限
const resetToDefaultPermissions = async () => {
  try {
    await ElMessageBox.confirm('确定要重置为默认权限吗？当前修改将丢失。', '提示', {
      type: 'warning'
    })
    setDefaultRolePermissions()
    ElMessage.success('已重置为默认权限')
  } catch (err) {
    if (err === 'cancel') {
      return
    }
  }
}

// 保存角色权限
const saveRolePermissions = async () => {
  if (!selectedRole.value) {
    ElMessage.warning('请先选择角色')
    return
  }

  try {
    const permissionData = {
      canSubmitManuscript: permissionForm.permissions.includes('canSubmitManuscript'),
      canViewAllManuscripts: permissionForm.permissions.includes('canViewAllManuscripts'),
      canAssignReviewer: permissionForm.permissions.includes('canAssignReviewer'),
      canViewReviewerIdentity: permissionForm.permissions.includes('canViewReviewerIdentity'),
      canWriteReview: permissionForm.permissions.includes('canWriteReview'),
      canMakeDecision: permissionForm.permissions.includes('canMakeDecision'),
      canModifySystemConfig: permissionForm.permissions.includes('canModifySystemConfig'),
      canTechCheck: permissionForm.permissions.includes('canTechCheck'),
      canPublishNews: permissionForm.permissions.includes('canPublishNews')
    }

    const res = await request.put(`/api/system-admin/role-permissions/${selectedRole.value}`, permissionData)
    if (res.code === 200) {
      ElMessage.success('角色权限保存成功')
    } else {
      ElMessage.error(res.msg || '角色权限保存失败')
    }
  } catch (err) {
    ElMessage.error('角色权限保存失败')
    console.error(err)
  }
}

// 获取系统日志
const fetchLogs = async () => {
  logLoading.value = true
  try {
    const params = {
      ...logQuery,
      startDate: logQuery.dateRange?.[0] ? new Date(logQuery.dateRange[0]).toISOString() : null,
      endDate: logQuery.dateRange?.[1] ? new Date(logQuery.dateRange[1]).toISOString() : null,
      operationType: logQuery.operationType || null
    }

    const res = await request.get('/api/system-admin/logs', { params })
    if (res.code === 200) {
      logList.value = res.data
      totalLogs.value = res.total || res.data.length
    } else {
      ElMessage.error(res.msg || '获取日志失败')
    }
  } catch (err) {
    ElMessage.error('获取日志失败')
    console.error(err)
  } finally {
    logLoading.value = false
  }
}

// 清空日志
const clearLogs = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有日志吗？此操作不可恢复。', '警告', {
      type: 'warning'
    })

    const res = await request.delete('/api/system-admin/logs')
    if (res.code === 200) {
      ElMessage.success('日志清空成功')
      fetchLogs()
    } else {
      ElMessage.error(res.msg || '日志清空失败')
    }
  } catch (err) {
    if (err === 'cancel') {
      return
    }
    ElMessage.error('日志清空失败')
    console.error(err)
  }
}

// 日志分页切换
const handleLogPageChange = (page) => {
  logQuery.pageNum = page
  fetchLogs()
}

const handleLogSizeChange = (size) => {
  logQuery.pageSize = size
  logQuery.pageNum = 1
  fetchLogs()
}
</script>

<style scoped>
.system-admin-container {
  padding: 20px;
  min-height: 100vh;
}

.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.tab-content {
  margin-top: 20px;
}

.current-permissions {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}

.current-permissions h4 {
  margin: 0 0 10px 0;
  color: #606266;
}

.permission-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

:deep(.el-checkbox) {
  margin-right: 15px;
  margin-bottom: 10px;
}

:deep(.el-pagination) {
  margin-top: 15px;
  text-align: right;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>