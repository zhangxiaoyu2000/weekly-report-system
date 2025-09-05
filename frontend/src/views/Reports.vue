<template>
  <div class="reports-page">
    <div class="page-header">
      <h1>周报管理</h1>
      <el-button type="primary" @click="$router.push('/reports/create')">
        <el-icon><Plus /></el-icon>
        创建周报
      </el-button>
    </div>

    <el-card>
      <el-table :data="reports" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="weekRange" label="周次" width="150" />
        <el-table-column prop="createdAt" label="创建时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewReport(row.id)">
              查看
            </el-button>
            <el-button type="primary" link @click="editReport(row.id)">
              编辑
            </el-button>
            <el-button type="danger" link @click="deleteReport(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)

const reports = ref([
  {
    id: 1,
    title: '2024年第36周工作周报',
    weekRange: '2024.09.02-2024.09.08',
    createdAt: '2024-09-02',
    status: 'submitted'
  },
  {
    id: 2,
    title: '2024年第35周工作周报',
    weekRange: '2024.08.26-2024.09.01',
    createdAt: '2024-08-26',
    status: 'approved'
  }
])

const formatDate = (date) => {
  return new Date(date).toLocaleDateString()
}

const getStatusType = (status) => {
  const statusMap = {
    draft: 'info',
    submitted: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    draft: '草稿',
    submitted: '已提交',
    approved: '已通过',
    rejected: '被拒绝'
  }
  return statusMap[status] || '未知'
}

const viewReport = (id) => {
  router.push(`/reports/${id}`)
}

const editReport = (id) => {
  router.push(`/reports/${id}/edit`)
}

const deleteReport = (id) => {
  // Implementation for delete
  console.log('Delete report:', id)
}

onMounted(() => {
  // Load reports from API
})
</script>

<style scoped>
.reports-page {
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}
</style>