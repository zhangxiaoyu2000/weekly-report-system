<template>
  <div class="dashboard">
    <el-row :gutter="24" class="stats-cards">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon color="#1890ff" size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ reportStats.total }}</div>
              <div class="stat-label">总周报数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon color="#52c41a" size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ reportStats.thisWeek }}</div>
              <div class="stat-label">本周已提交</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon color="#faad14" size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ reportStats.pending }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon color="#f5222d" size="32"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ reportStats.overdue }}</div>
              <div class="stat-label">逾期未交</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="24" class="content-row">
      <el-col :xs="24" :lg="16">
        <el-card title="最近周报">
          <template #header>
            <div class="card-header">
              <span>最近周报</span>
              <el-button type="primary" @click="$router.push('/reports/create')">
                创建周报
              </el-button>
            </div>
          </template>
          <el-table :data="recentReports" v-loading="loading">
            <el-table-column prop="title" label="标题" />
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
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="primary" link @click="viewReport(row.id)">
                  查看
                </el-button>
                <el-button type="primary" link @click="editReport(row.id)">
                  编辑
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="8">
        <el-card title="待办事项">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
            </div>
          </template>
          <div class="todo-list">
            <div v-for="todo in todoList" :key="todo.id" class="todo-item">
              <el-icon class="todo-icon" :color="todo.priority === 'high' ? '#f5222d' : '#1890ff'">
                <Bell />
              </el-icon>
              <div class="todo-content">
                <div class="todo-title">{{ todo.title }}</div>
                <div class="todo-time">{{ todo.dueTime }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Document,
  CircleCheck,
  Clock,
  Warning,
  Bell
} from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)

// Mock data - 在实际项目中会从API获取
const reportStats = ref({
  total: 24,
  thisWeek: 1,
  pending: 3,
  overdue: 1
})

const recentReports = ref([
  {
    id: 1,
    title: '2024年第36周工作周报',
    createdAt: '2024-09-02',
    status: 'submitted'
  },
  {
    id: 2,
    title: '2024年第35周工作周报',
    createdAt: '2024-08-26',
    status: 'approved'
  },
  {
    id: 3,
    title: '2024年第34周工作周报',
    createdAt: '2024-08-19',
    status: 'draft'
  }
])

const todoList = ref([
  {
    id: 1,
    title: '提交本周工作周报',
    dueTime: '今天 18:00',
    priority: 'high'
  },
  {
    id: 2,
    title: '审核团队成员周报',
    dueTime: '明天 12:00',
    priority: 'normal'
  },
  {
    id: 3,
    title: '准备月度总结',
    dueTime: '本周五',
    priority: 'normal'
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

onMounted(() => {
  // 在实际项目中，这里会调用API获取数据
  console.log('Dashboard mounted')
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  margin-right: 16px;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.content-row {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-icon {
  margin-right: 12px;
}

.todo-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.todo-time {
  font-size: 12px;
  color: #999;
}
</style>