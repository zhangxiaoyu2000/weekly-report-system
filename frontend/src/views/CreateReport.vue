<template>
  <div class="create-report-page">
    <div class="page-header">
      <h1>创建周报</h1>
    </div>

    <el-card>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="周报标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入周报标题" />
        </el-form-item>

        <el-form-item label="周次" prop="weekRange">
          <el-date-picker
            v-model="form.weekRange"
            type="week"
            format="YYYY 第 ww 周"
            value-format="YYYY-MM-DD"
            placeholder="选择周次"
          />
        </el-form-item>

        <el-form-item label="本周工作完成情况" prop="workCompleted">
          <el-input
            v-model="form.workCompleted"
            type="textarea"
            :rows="6"
            placeholder="请详细描述本周完成的工作内容..."
          />
        </el-form-item>

        <el-form-item label="下周工作计划" prop="workPlan">
          <el-input
            v-model="form.workPlan"
            type="textarea"
            :rows="6"
            placeholder="请详细描述下周的工作计划..."
          />
        </el-form-item>

        <el-form-item label="遇到的问题" prop="problems">
          <el-input
            v-model="form.problems"
            type="textarea"
            :rows="4"
            placeholder="描述工作中遇到的问题和困难..."
          />
        </el-form-item>

        <el-form-item label="需要协助的事项" prop="assistance">
          <el-input
            v-model="form.assistance"
            type="textarea"
            :rows="4"
            placeholder="描述需要其他同事或部门协助的事项..."
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveAsDraft" :loading="saving">
            保存为草稿
          </el-button>
          <el-button type="success" @click="submit" :loading="submitting">
            提交周报
          </el-button>
          <el-button @click="$router.back()">
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const formRef = ref()
const saving = ref(false)
const submitting = ref(false)

const form = ref({
  title: '',
  weekRange: '',
  workCompleted: '',
  workPlan: '',
  problems: '',
  assistance: ''
})

const rules = {
  title: [
    { required: true, message: '请输入周报标题', trigger: 'blur' }
  ],
  weekRange: [
    { required: true, message: '请选择周次', trigger: 'change' }
  ],
  workCompleted: [
    { required: true, message: '请填写本周工作完成情况', trigger: 'blur' }
  ],
  workPlan: [
    { required: true, message: '请填写下周工作计划', trigger: 'blur' }
  ]
}

const saveAsDraft = async () => {
  saving.value = true
  try {
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('草稿保存成功')
    router.push('/reports')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const submit = async () => {
  if (!formRef.value) return
  
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    submitting.value = true
    
    // Mock API call
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('周报提交成功')
    router.push('/reports')
    
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-report-page {
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