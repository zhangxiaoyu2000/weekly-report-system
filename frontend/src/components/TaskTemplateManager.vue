<template>
  <div class="task-template-manager">
    <div class="header">
      <h3>📝 任务模板管理</h3>
      <div class="header-actions">
        <input 
          v-model="searchKeyword" 
          @input="searchTemplates"
          type="text" 
          placeholder="搜索模板名称..."
          class="search-input"
        >
        <button @click="showCreateForm = true" class="create-btn">
          ➕ 新建模板
        </button>
      </div>
    </div>

    <!-- 创建/编辑模板表单 -->
    <div v-if="showCreateForm" class="form-section">
      <h4>{{ editingTemplate ? '编辑模板' : '创建新模板' }}</h4>
      <form @submit.prevent="saveTemplate" class="template-form">
        <div class="form-row">
          <div class="form-group">
            <label>模板名称 *</label>
            <input 
              v-model="form.templateName" 
              type="text" 
              required 
              placeholder="请输入模板名称"
            >
          </div>
        </div>

        <div class="form-group">
          <label>模板描述</label>
          <textarea 
            v-model="form.templateDescription" 
            placeholder="请描述此模板的用途和适用场景"
            rows="3"
          ></textarea>
        </div>

        <div class="form-group">
          <label>分配成员（可选）</label>
          <input 
            v-model="form.assignedMembers" 
            type="text" 
            placeholder="默认负责人，填写任务时可以修改"
          >
        </div>

        <div class="form-group">
          <label>时间线（可选）</label>
          <textarea 
            v-model="form.timeline" 
            placeholder="默认时间安排，填写任务时可以修改"
            rows="2"
          ></textarea>
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>关键指标 *</label>
            <textarea 
              v-model="form.keyIndicators" 
              required
              placeholder="此类任务的关键性指标（必填）"
              rows="3"
            ></textarea>
          </div>
          <div class="form-group">
            <label>预期结果 *</label>
            <textarea 
              v-model="form.estimatedResults" 
              required
              placeholder="此类任务的预期结果（必填）"
              rows="3"
            ></textarea>
          </div>
        </div>

        <div class="form-actions">
          <button type="submit" :disabled="isLoading">
            {{ isLoading ? '保存中...' : (editingTemplate ? '更新模板' : '创建模板') }}
          </button>
          <button type="button" @click="cancelForm" :disabled="isLoading">
            取消
          </button>
        </div>
      </form>
    </div>

    <!-- 模板列表 -->
    <div v-if="!showCreateForm" class="templates-section">
      <div class="templates-header">
        <div class="tabs">
          <button 
            @click="currentTab = 'all'; loadTemplates()"
            :class="['tab-btn', { active: currentTab === 'all' }]"
          >
            所有模板 ({{ allTemplates.length }})
          </button>
          <button 
            @click="currentTab = 'my'; loadMyTemplates()"
            :class="['tab-btn', { active: currentTab === 'my' }]"
          >
            我的模板 ({{ myTemplates.length }})
          </button>
        </div>
        <button @click="loadTemplates()" class="refresh-btn">
          🔄 刷新
        </button>
      </div>

      <div v-if="currentTemplates.length > 0" class="templates-grid">
        <div 
          v-for="template in currentTemplates" 
          :key="template.id"
          :class="['template-card', { inactive: !template.isActive }]"
        >
          <div class="card-header">
            <div class="template-title">
              <h5>{{ template.templateName }}</h5>
              <span :class="['status-badge', { active: template.isActive, inactive: !template.isActive }]">
                {{ template.isActive ? '启用' : '停用' }}
              </span>
            </div>
            <div class="template-actions">
              <button 
                @click="editTemplate(template)" 
                class="action-btn edit"
                title="编辑模板"
              >
                ✏️
              </button>
              <button 
                v-if="template.isActive"
                @click="deactivateTemplate(template.id)" 
                class="action-btn deactivate"
                title="停用模板"
              >
                ⏸️
              </button>
              <button 
                v-else
                @click="activateTemplate(template.id)" 
                class="action-btn activate"
                title="启用模板"
              >
                ▶️
              </button>
            </div>
          </div>

          <div class="card-content">
            <div v-if="template.templateDescription" class="description">
              <strong>描述：</strong>{{ template.templateDescription }}
            </div>
            
            <div class="template-fields">
              <div v-if="template.assignedMembers" class="field-item">
                <strong>默认成员：</strong>{{ template.assignedMembers }}
              </div>
              <div v-if="template.timeline" class="field-item">
                <strong>默认时间线：</strong>{{ truncateText(template.timeline, 50) }}
              </div>
              <div class="field-item">
                <strong>关键指标：</strong>{{ truncateText(template.keyIndicators, 80) }}
              </div>
              <div class="field-item">
                <strong>预期结果：</strong>{{ truncateText(template.estimatedResults, 80) }}
              </div>
            </div>

            <div class="template-meta">
              <small class="creator" v-if="template.createdByUsername">
                创建者：{{ template.createdByUsername }}
              </small>
              <small class="create-time">
                创建时间：{{ formatDate(template.createdAt) }}
              </small>
              <small v-if="template.updatedAt !== template.createdAt" class="update-time">
                更新时间：{{ formatDate(template.updatedAt) }}
              </small>
            </div>
          </div>

          <div class="card-footer">
            <button 
              @click="useTemplate(template)" 
              :disabled="!template.isActive"
              class="use-btn"
            >
              {{ template.isActive ? '🚀 使用此模板' : '模板已停用' }}
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <p v-if="currentTab === 'all'">暂无模板数据</p>
        <p v-else>您还没有创建任何模板</p>
        <small>创建您的第一个任务模板，提高工作效率</small>
      </div>
    </div>

    <!-- 使用模板弹窗 -->
    <div v-if="showUseModal" class="modal-overlay" @click="closeUseModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h4>使用模板：{{ selectedTemplate?.templateName }}</h4>
          <button @click="closeUseModal" class="close-btn">✕</button>
        </div>
        <div class="modal-body">
          <p>此功能将在任务填写界面中实现。</p>
          <p>选择此模板后，相关字段将自动预填充：</p>
          <ul>
            <li v-if="selectedTemplate?.assignedMembers">人员分配：{{ selectedTemplate.assignedMembers }}</li>
            <li v-if="selectedTemplate?.timeline">时间线：{{ selectedTemplate.timeline }}</li>
            <li>关键指标：{{ selectedTemplate?.keyIndicators }}</li>
            <li>预期结果：{{ selectedTemplate?.estimatedResults }}</li>
          </ul>
        </div>
        <div class="modal-actions">
          <button @click="closeUseModal" class="cancel-btn">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { taskTemplateAPI, type TaskTemplate } from '../services/api'

// Props and Emits
const emit = defineEmits(['template-selected'])

// 响应式数据
const isLoading = ref(false)
const showCreateForm = ref(false)
const showUseModal = ref(false)
const editingTemplate = ref<TaskTemplate | null>(null)
const selectedTemplate = ref<TaskTemplate | null>(null)
const currentTab = ref<'all' | 'my'>('all')
const searchKeyword = ref('')
const allTemplates = ref<TaskTemplate[]>([])
const myTemplates = ref<TaskTemplate[]>([])

// 表单数据
const form = reactive({
  templateName: '',
  templateDescription: '',
  assignedMembers: '',
  timeline: '',
  keyIndicators: '',
  estimatedResults: ''
})

// 计算属性
const currentTemplates = computed(() => {
  return currentTab.value === 'all' ? allTemplates.value : myTemplates.value
})

// 加载所有活跃模板
const loadTemplates = async () => {
  try {
    const response = await taskTemplateAPI.getActiveTemplates(searchKeyword.value || undefined)
    if (response.success) {
      allTemplates.value = response.data
    }
  } catch (error) {
    console.error('Load templates error:', error)
  }
}

// 加载我的模板
const loadMyTemplates = async () => {
  try {
    const response = await taskTemplateAPI.getMyTemplates()
    if (response.success) {
      myTemplates.value = response.data
    }
  } catch (error) {
    console.error('Load my templates error:', error)
  }
}

// 搜索模板
const searchTemplates = async () => {
  if (currentTab.value === 'all') {
    await loadTemplates()
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(form, {
    templateName: '',
    templateDescription: '',
    assignedMembers: '',
    timeline: '',
    keyIndicators: '',
    estimatedResults: ''
  })
}

// 取消表单
const cancelForm = () => {
  showCreateForm.value = false
  editingTemplate.value = null
  resetForm()
}

// 编辑模板
const editTemplate = (template: TaskTemplate) => {
  editingTemplate.value = template
  Object.assign(form, {
    templateName: template.templateName,
    templateDescription: template.templateDescription || '',
    assignedMembers: template.assignedMembers || '',
    timeline: template.timeline || '',
    keyIndicators: template.keyIndicators,
    estimatedResults: template.estimatedResults
  })
  showCreateForm.value = true
}

// 保存模板
const saveTemplate = async () => {
  isLoading.value = true
  
  try {
    let response
    if (editingTemplate.value) {
      response = await taskTemplateAPI.update(editingTemplate.value.id, form)
    } else {
      response = await taskTemplateAPI.create(form)
    }
    
    if (response.success) {
      alert(editingTemplate.value ? '✅ 模板更新成功！' : '✅ 模板创建成功！')
      cancelForm()
      await loadTemplates()
      await loadMyTemplates()
    } else {
      alert('❌ 操作失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  } finally {
    isLoading.value = false
  }
}

// 停用模板
const deactivateTemplate = async (templateId: number) => {
  if (!confirm('确定要停用此模板吗？停用后将无法在任务创建时使用。')) {
    return
  }

  try {
    const response = await taskTemplateAPI.deactivate(templateId)
    if (response.success) {
      alert('✅ 模板已停用！')
      await loadTemplates()
      await loadMyTemplates()
    } else {
      alert('❌ 停用失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  }
}

// 激活模板
const activateTemplate = async (templateId: number) => {
  try {
    const response = await taskTemplateAPI.activate(templateId)
    if (response.success) {
      alert('✅ 模板已激活！')
      await loadTemplates()
      await loadMyTemplates()
    } else {
      alert('❌ 激活失败：' + response.message)
    }
  } catch (error) {
    alert('❌ 网络错误：' + error.message)
  }
}

// 使用模板
const useTemplate = (template: TaskTemplate) => {
  selectedTemplate.value = template
  showUseModal.value = true
  // 发射事件给父组件，可以在其他地方使用
  emit('template-selected', template)
}

// 关闭使用模板弹窗
const closeUseModal = () => {
  showUseModal.value = false
  selectedTemplate.value = null
}

// 截断文本
const truncateText = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString()
}

// 组件挂载
onMounted(() => {
  loadTemplates()
  loadMyTemplates()
})
</script>

<style scoped>
.task-template-manager {
  max-width: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 200px;
}

.create-btn {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}

/* 表单区域 */
.form-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.form-section h4 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 16px;
}

.template-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  margin-bottom: 5px;
  font-weight: 600;
  color: #555;
  font-size: 14px;
}

.form-group input,
.form-group textarea {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
}

.form-group textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
}

.form-actions button {
  padding: 12px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.form-actions button[type="submit"] {
  background: #007bff;
  color: white;
  flex: 1;
}

.form-actions button[type="submit"]:hover:not(:disabled) {
  background: #0056b3;
}

.form-actions button[type="button"] {
  background: #6c757d;
  color: white;
  flex: 0 0 auto;
}

.form-actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 模板区域 */
.templates-section {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.templates-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.tabs {
  display: flex;
  gap: 5px;
}

.tab-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: white;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-btn:hover {
  border-color: #007bff;
  color: #007bff;
}

.tab-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.refresh-btn {
  padding: 8px 12px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

/* 模板网格 */
.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
  padding: 20px;
}

.template-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  background: white;
}

.template-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.template-card.inactive {
  opacity: 0.6;
  border-color: #ccc;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
}

.template-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.template-title h5 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.inactive {
  background: #f8d7da;
  color: #721c24;
}

.template-actions {
  display: flex;
  gap: 5px;
}

.action-btn {
  padding: 6px 8px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.edit {
  background: #007bff;
  color: white;
}

.action-btn.deactivate {
  background: #ffc107;
  color: #212529;
}

.action-btn.activate {
  background: #28a745;
  color: white;
}

.action-btn:hover {
  opacity: 0.8;
}

.card-content {
  padding: 15px;
}

.description {
  margin-bottom: 15px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
  font-size: 14px;
}

.template-fields {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}

.field-item {
  font-size: 14px;
  line-height: 1.4;
}

.field-item strong {
  color: #555;
  display: inline-block;
  min-width: 80px;
}

.template-meta {
  border-top: 1px solid #eee;
  padding-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.template-meta small {
  color: #666;
  font-size: 12px;
}

.card-footer {
  padding: 15px;
  border-top: 1px solid #eee;
  background: #f8f9fa;
}

.use-btn {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.use-btn:not(:disabled) {
  background: #28a745;
  color: white;
}

.use-btn:not(:disabled):hover {
  background: #218838;
}

.use-btn:disabled {
  background: #6c757d;
  color: white;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 弹窗样式 */
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

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h4 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #666;
  padding: 5px;
}

.modal-body {
  padding: 20px;
}

.modal-body ul {
  margin: 10px 0;
  padding-left: 20px;
}

.modal-body li {
  margin-bottom: 5px;
}

.modal-actions {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  text-align: right;
}

.cancel-btn {
  padding: 8px 16px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 5px;
}

.empty-state small {
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .search-input {
    width: auto;
    flex: 1;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
  
  .templates-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .tabs {
    justify-content: center;
    width: 100%;
  }
  
  .templates-grid {
    grid-template-columns: 1fr;
    padding: 15px;
  }
  
  .template-actions {
    flex-direction: column;
    gap: 3px;
  }
}
</style>