<template>
  <div class="profile-page">
    <div class="page-header">
      <h1>个人资料</h1>
    </div>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item label="部门" prop="department">
          <el-select v-model="form.department" placeholder="请选择部门">
            <el-option label="技术部" value="tech" />
            <el-option label="产品部" value="product" />
            <el-option label="运营部" value="operations" />
          </el-select>
        </el-form-item>

        <el-form-item label="职位" prop="position">
          <el-input v-model="form.position" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="updateProfile" :loading="loading">
            更新资料
          </el-button>
          <el-button @click="resetForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = ref({
  username: '',
  name: '',
  email: '',
  department: '',
  position: ''
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const updateProfile = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('资料更新成功')
    
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

onMounted(() => {
  // Load user profile
  form.value = {
    username: userStore.userInfo?.username || 'admin',
    name: userStore.userName || '管理员',
    email: 'admin@example.com',
    department: 'tech',
    position: '前端开发工程师'
  }
})
</script>

<style scoped>
.profile-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}
</style>