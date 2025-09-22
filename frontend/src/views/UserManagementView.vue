<template>
  <div class="user-management bg-white dark:bg-gray-800 text-gray-900 dark:text-white">
    <div class="header">
      <h1>用户管理</h1>
      <p class="subtitle">管理系统中的所有用户和权限</p>
      <button @click="showCreateModal = true" class="create-btn">
        新增用户
      </button>
    </div>

    <div class="stats">
      <div class="stat-card bg-white dark:bg-gray-700 border-gray-200 dark:border-gray-600">
        <h3>超级管理员</h3>
        <p class="count">{{ userStats.superAdmin }}</p>
      </div>
      <div class="stat-card bg-white dark:bg-gray-700 border-gray-200 dark:border-gray-600">
        <h3>管理员</h3>
        <p class="count">{{ userStats.admin }}</p>
      </div>
      <div class="stat-card bg-white dark:bg-gray-700 border-gray-200 dark:border-gray-600">
        <h3>主管</h3>
        <p class="count">{{ userStats.manager }}</p>
      </div>
      <div class="stat-card bg-white dark:bg-gray-700 border-gray-200 dark:border-gray-600">
        <h3>总用户数</h3>
        <p class="count">{{ userStats.total }}</p>
      </div>
    </div>

    <div class="filters bg-gray-50 dark:bg-gray-700">
      <div class="filter-group">
        <label>角色</label>
        <select v-model="filterRole" class="dark:bg-gray-600 dark:border-gray-500 dark:text-white">
          <option value="all">全部角色</option>
          <option value="SUPER_ADMIN">超级管理员</option>
          <option value="ADMIN">管理员</option>
          <option value="MANAGER">主管</option>
        </select>
      </div>
      <div class="filter-group">
        <label>状态</label>
        <select v-model="filterStatus" class="dark:bg-gray-600 dark:border-gray-500 dark:text-white">
          <option value="all">全部状态</option>
          <option value="ACTIVE">正常</option>
          <option value="INACTIVE">停用</option>
        </select>
      </div>
      <div class="filter-group">
        <label>搜索</label>
        <input 
          v-model="searchTerm" 
          type="text" 
          placeholder="搜索用户名、邮箱"
          class="search-input dark:bg-gray-600 dark:border-gray-500 dark:text-white dark:placeholder-gray-400"
        >
      </div>
    </div>

    <div class="user-table bg-white dark:bg-gray-700 border-gray-200 dark:border-gray-600">
      <table>
        <thead>
          <tr>
            <th class="bg-gray-50 dark:bg-gray-600 dark:text-white dark:border-gray-600">用户</th>
            <th class="bg-gray-50 dark:bg-gray-600 dark:text-white dark:border-gray-600">角色</th>
            <th class="bg-gray-50 dark:bg-gray-600 dark:text-white dark:border-gray-600">状态</th>
            <th class="bg-gray-50 dark:bg-gray-600 dark:text-white dark:border-gray-600">部门</th>
            <th class="bg-gray-50 dark:bg-gray-600 dark:text-white dark:border-gray-600">最后登录</th>
            <th class="bg-gray-50 dark:bg-gray-600 dark:text-white dark:border-gray-600">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in filteredUsers" :key="user.id" class="dark:border-gray-600">
            <td class="dark:border-gray-600">
              <div class="user-info">
                <div class="avatar">{{ user.username.charAt(0).toUpperCase() }}</div>
                <div class="details">
                  <p class="name dark:text-white">{{ user.username }}</p>
                  <p class="email dark:text-gray-300">{{ user.email }}</p>
                </div>
              </div>
            </td>
            <td class="dark:border-gray-600">
              <span :class="['role-badge', user.role.toLowerCase()]">
                {{ getRoleText(user.role) }}
              </span>
            </td>
            <td class="dark:border-gray-600">
              <span :class="['status-badge', user.status.toLowerCase()]">
                {{ getStatusText(user.status) }}
              </span>
            </td>
            <td class="dark:border-gray-600 dark:text-gray-300">-</td>
            <td class="dark:border-gray-600 dark:text-gray-300">{{ formatDate(user.lastLoginTime) }}</td>
            <td class="dark:border-gray-600">
              <div class="actions">
                <button @click="editUser(user)" class="edit-btn">编辑</button>
                <button 
                  v-if="user.role !== 'SUPER_ADMIN'"
                  @click="resetPassword(user)" 
                  class="reset-btn"
                >
                  重置密码
                </button>
                <button 
                  v-if="user.role !== 'SUPER_ADMIN' && user.status === 'ACTIVE'"
                  @click="toggleUserStatus(user, 'INACTIVE')" 
                  class="disable-btn"
                >
                  停用
                </button>
                <button 
                  v-if="user.role !== 'SUPER_ADMIN' && user.status !== 'ACTIVE'"
                  @click="toggleUserStatus(user, 'ACTIVE')" 
                  class="enable-btn"
                >
                  启用
                </button>
                <button 
                  v-if="user.role !== 'SUPER_ADMIN' && user.id !== authStore.user?.id"
                  @click="deleteUser(user)" 
                  class="delete-btn"
                >
                  删除
                </button>
                <span 
                  v-if="user.role === 'SUPER_ADMIN'" 
                  class="protected-text dark:text-gray-400"
                >
                  受保护账户
                </span>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 创建用户模态框 -->
    <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
      <div class="modal bg-white dark:bg-gray-800" @click.stop>
        <div class="modal-header dark:border-gray-600">
          <h2 class="dark:text-white">新增用户</h2>
          <button @click="showCreateModal = false" class="close-btn dark:text-gray-400">×</button>
        </div>
        <form @submit.prevent="createUser" class="modal-form">
          <div class="form-group">
            <label class="dark:text-gray-300">用户名</label>
            <input 
              v-model="newUser.username" 
              type="text" 
              required 
              pattern="^[a-zA-Z0-9_]+$"
              minlength="3"
              maxlength="50"
              class="dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              placeholder="只能包含字母、数字和下划线，3-50字符"
            >
            <p class="form-hint dark:text-gray-400">只能包含字母、数字和下划线，长度3-50字符</p>
          </div>
          <div class="form-group">
            <label class="dark:text-gray-300">邮箱</label>
            <input v-model="newUser.email" type="email" required class="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
          </div>
          <div class="form-group">
            <label class="dark:text-gray-300">角色</label>
            <select v-model="newUser.role" required class="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="MANAGER">主管</option>
              <option value="ADMIN">管理员</option>
              <option value="SUPER_ADMIN">超级管理员</option>
            </select>
          </div>
          <div class="form-group">
            <label class="dark:text-gray-300">密码</label>
            <input 
              v-model="newUser.password" 
              type="password" 
              required 
              minlength="8"
              maxlength="100"
              class="dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              placeholder="至少8个字符"
            >
            <p class="form-hint dark:text-gray-400">密码长度8-100字符</p>
          </div>
          <div class="form-group">
            <label class="dark:text-gray-300">确认密码</label>
            <input 
              v-model="newUser.confirmPassword" 
              type="password" 
              required 
              class="dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              placeholder="请再次输入密码"
            >
            <p v-if="newUser.password && newUser.confirmPassword && newUser.password !== newUser.confirmPassword" class="form-error">密码不匹配</p>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showCreateModal = false" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn">创建</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 编辑用户模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeEditModal">
      <div class="modal bg-white dark:bg-gray-800" @click.stop>
        <div class="modal-header dark:border-gray-600">
          <h2 class="dark:text-white">编辑用户</h2>
          <button @click="closeEditModal" class="close-btn dark:text-gray-400">×</button>
        </div>
        <form @submit.prevent="updateUser" class="modal-form" v-if="editingUser">
          <div class="form-group">
            <label class="dark:text-gray-300">用户名</label>
            <input v-model="editingUser.username" type="text" required class="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
          </div>
          <div class="form-group">
            <label class="dark:text-gray-300">邮箱</label>
            <input v-model="editingUser.email" type="email" required class="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
          </div>
          <div class="form-group">
            <label class="dark:text-gray-300">角色</label>
            <select v-model="editingUser.role" required class="dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="MANAGER">主管</option>
              <option value="ADMIN">管理员</option>
              <option value="SUPER_ADMIN" :disabled="authStore.user?.role !== 'SUPER_ADMIN'">超级管理员</option>
            </select>
          </div>
          <div class="form-group">
            <label class="dark:text-gray-300">状态</label>
            <select 
              v-model="editingUser.status" 
              required 
              :disabled="editingUser.role === 'SUPER_ADMIN'"
              class="dark:bg-gray-700 dark:border-gray-600 dark:text-white"
              :class="{ 'disabled-select': editingUser.role === 'SUPER_ADMIN' }"
            >
              <option value="ACTIVE">正常</option>
              <option value="INACTIVE">停用</option>
            </select>
            <p v-if="editingUser.role === 'SUPER_ADMIN'" class="form-hint dark:text-gray-400">
              超级管理员状态不可修改
            </p>
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeEditModal" class="cancel-btn">取消</button>
            <button type="submit" class="submit-btn">更新</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { userAPI } from '@/services/api'

const authStore = useAuthStore()

const users = ref([])
const filterRole = ref('all')
const filterStatus = ref('all')
const searchTerm = ref('')
const showCreateModal = ref(false)
const showEditModal = ref(false)
const editingUser = ref(null)

const newUser = ref({
  username: '',
  email: '',
  role: 'MANAGER',
  password: '',
  confirmPassword: ''
})

const userStats = computed(() => {
  const stats = {
    superAdmin: 0,
    admin: 0,
    manager: 0,
    total: users.value.length
  }
  
  users.value.forEach((user: any) => {
    switch (user.role) {
      case 'SUPER_ADMIN':
        stats.superAdmin++
        break
      case 'ADMIN':
        stats.admin++
        break
      case 'MANAGER':
        stats.manager++
        break
    }
  })
  
  return stats
})

const filteredUsers = computed(() => {
  let filtered = [...users.value]
  
  if (filterRole.value !== 'all') {
    filtered = filtered.filter((user: any) => user.role === filterRole.value)
  }
  
  if (filterStatus.value !== 'all') {
    filtered = filtered.filter((user: any) => user.status === filterStatus.value)
  }
  
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase()
    filtered = filtered.filter((user: any) => 
      user.username.toLowerCase().includes(term) ||
      user.email.toLowerCase().includes(term)
    )
  }
  
  return filtered
})

const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    'SUPER_ADMIN': '超级管理员',
    'ADMIN': '管理员',
    'MANAGER': '主管'
  }
  return roleMap[role] || role
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'ACTIVE': '正常',
    'INACTIVE': '停用'
  }
  return statusMap[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '从未登录'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadUsers = async () => {
  try {
    const response = await userAPI.list()
    
    if (response.success) {
      // 后端返回分页数据，需要取content字段
      users.value = response.data.content || response.data
      console.log('✅ 用户列表加载成功:', users.value.length, '个用户')
    } else {
      console.error('❌ 加载用户失败:', response.message)
      alert('加载用户失败：' + response.message)
    }
  } catch (error) {
    console.error('❌ 加载用户列表失败:', error)
    alert('加载用户失败，请检查网络连接')
  }
}

const createUser = async () => {
  try {
    // 验证用户名格式
    const usernamePattern = /^[a-zA-Z0-9_]+$/
    if (!usernamePattern.test(newUser.value.username)) {
      alert('用户名只能包含字母、数字和下划线')
      return
    }
    
    // 验证用户名长度
    if (newUser.value.username.length < 3 || newUser.value.username.length > 50) {
      alert('用户名长度必须在3-50字符之间')
      return
    }
    
    // 验证密码长度
    if (newUser.value.password.length < 8 || newUser.value.password.length > 100) {
      alert('密码长度必须在8-100字符之间')
      return
    }
    
    // 验证密码匹配
    if (newUser.value.password !== newUser.value.confirmPassword) {
      alert('密码和确认密码不匹配')
      return
    }

    const response = await userAPI.create({
      username: newUser.value.username,
      email: newUser.value.email,
      role: newUser.value.role,
      password: newUser.value.password,
      confirmPassword: newUser.value.confirmPassword
    })
    
    if (response.success) {
      alert('用户创建成功')
      showCreateModal.value = false
      newUser.value = {
        username: '',
        email: '',
        role: 'MANAGER',
        password: '',
        confirmPassword: ''
      }
      loadUsers()
    } else {
      // 解析后端验证错误信息
      let errorMessage = response.message
      if (errorMessage.includes('Username can only contain letters, numbers and underscores')) {
        errorMessage = '用户名只能包含字母、数字和下划线'
      } else if (errorMessage.includes('Username must be between 3 and 50 characters')) {
        errorMessage = '用户名长度必须在3-50字符之间'
      } else if (errorMessage.includes('Password must be between 8 and 100 characters')) {
        errorMessage = '密码长度必须在8-100字符之间'
      } else if (errorMessage.includes('Username already exists')) {
        errorMessage = '用户名已存在'
      } else if (errorMessage.includes('Email already exists')) {
        errorMessage = '邮箱已存在'
      } else if (errorMessage.includes('Email should be valid')) {
        errorMessage = '邮箱格式不正确'
      } else {
        errorMessage = '创建失败: ' + errorMessage
      }
      alert(errorMessage)
    }
  } catch (error) {
    console.error('创建用户失败:', error)
    alert('创建用户失败，请检查网络连接')
  }
}

const editUser = (user: any) => {
  editingUser.value = { ...user }
  showEditModal.value = true
}

const updateUser = async () => {
  try {
    if (!editingUser.value) return
    
    // 先更新基本信息
    const response = await userAPI.update(editingUser.value.id, {
      username: editingUser.value.username,
      email: editingUser.value.email,
      role: editingUser.value.role
    })
    
    if (!response.success) {
      alert('更新失败: ' + response.message)
      return
    }

    // 如果不是超级管理员，还需要更新状态
    if (editingUser.value.role !== 'SUPER_ADMIN') {
      // 使用新的启用/禁用接口
      const statusResponse = editingUser.value.status === 'ACTIVE' 
        ? await userAPI.enable(editingUser.value.id)
        : await userAPI.disable(editingUser.value.id)
      
      if (!statusResponse.success) {
        alert('状态更新失败: ' + statusResponse.message)
        return
      }
    }
    
    alert('用户信息更新成功')
    showEditModal.value = false
    editingUser.value = null
    loadUsers()
    
  } catch (error) {
    console.error('更新用户失败:', error)
    alert('更新用户失败')
  }
}

const closeEditModal = () => {
  showEditModal.value = false
  editingUser.value = null
}

const resetPassword = async (user: any) => {
  const defaultPassword = 'Password123!'
  if (!confirm(`确定要重置用户 ${user.username} 的密码吗？重置后密码将变为：${defaultPassword}`)) {
    return
  }
  
  try {
    const response = await userAPI.resetPassword(user.id, defaultPassword)
    
    if (response.success) {
      alert(`密码重置成功！新密码为：${defaultPassword}`)
      loadUsers()
    } else {
      alert('重置失败: ' + response.message)
    }
  } catch (error) {
    console.error('重置密码失败:', error)
    alert('重置密码失败')
  }
}

const toggleUserStatus = async (user: any, newStatus: string) => {
  // 前端额外保护：超级管理员不允许状态变更
  if (user.role === 'SUPER_ADMIN') {
    alert('超级管理员账户受保护，无法修改状态')
    return
  }

  // 友好的确认对话框
  const actionText = newStatus === 'ACTIVE' ? '启用' : '禁用'
  const statusDescription = newStatus === 'ACTIVE' ? '用户将可以正常登录和使用系统' : '用户将无法登录系统'
  
  if (!confirm(`确定要${actionText}用户 ${user.username} 吗？\n\n${statusDescription}`)) {
    return
  }

  try {
    // 使用新的启用/禁用接口
    const response = newStatus === 'ACTIVE' 
      ? await userAPI.enable(user.id)
      : await userAPI.disable(user.id)
    
    if (response.success) {
      const actionText = newStatus === 'ACTIVE' ? '启用' : '禁用'
      alert(`用户已成功${actionText}`)
      loadUsers()
    } else {
      alert('操作失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('更新用户状态失败:', error)
    alert('操作失败，请稍后重试')
  }
}

const deleteUser = async (user: any) => {
  // 前端额外保护：超级管理员不允许删除
  if (user.role === 'SUPER_ADMIN') {
    alert('超级管理员账户受保护，无法删除')
    return
  }

  if (!confirm(`⚠️ 危险操作确认\n\n确定要永久删除用户 ${user.username} 吗？\n\n此操作不可撤销，用户的所有数据将被永久删除！`)) {
    return
  }
  
  try {
    const response = await userAPI.delete(user.id)
    
    if (response.success) {
      alert('✅ 用户已永久删除')
      loadUsers()
    } else {
      alert('❌ 删除失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('删除用户失败:', error)
    alert('删除失败')
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header h1 {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.subtitle {
  color: #6b7280;
  font-size: 1rem;
  margin: 4px 0 0 0;
}

.create-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
}

.stat-card h3 {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.stat-card .count {
  font-size: 2rem;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.filter-group select,
.search-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
}

.user-table {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th {
  background: #f9fafb;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}

td {
  padding: 12px;
  border-bottom: 1px solid #f3f4f6;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3b82f6;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.details {
  flex: 1;
}

.details .name {
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.details .email,
.details .username {
  font-size: 0.875rem;
  color: #6b7280;
  margin: 2px 0 0 0;
}

.role-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.role-badge.super_admin {
  background: #fdf4ff;
  color: #7c3aed;
}

.role-badge.admin {
  background: #fef3c7;
  color: #d97706;
}

.role-badge.manager {
  background: #dbeafe;
  color: #1e40af;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 500;
}

.status-badge.active {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #991b1b;
}


.actions {
  display: flex;
  gap: 8px;
}

.actions button {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 500;
}

.edit-btn {
  background: #3b82f6;
  color: white;
}

.enable-btn {
  background: #10b981;
  color: white;
}

.disable-btn {
  background: #f59e0b;
  color: white;
}

.delete-btn {
  background: #ef4444;
  color: white;
}

.reset-btn {
  background: #8b5cf6;
  color: white;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  width: 500px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
}

.modal-form {
  padding: 20px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.875rem;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.cancel-btn {
  background: #6b7280;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.submit-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}

.protected-text {
  font-size: 0.75rem;
  color: #6b7280;
  font-style: italic;
  padding: 4px 8px;
}

.disabled-select {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-hint {
  font-size: 0.75rem;
  color: #6b7280;
  margin-top: 4px;
  font-style: italic;
}

.form-error {
  font-size: 0.75rem;
  color: #ef4444;
  margin-top: 4px;
  font-weight: 500;
}

.form-group input:invalid {
  border-color: #ef4444;
}

.form-group input:valid {
  border-color: #10b981;
}
</style>