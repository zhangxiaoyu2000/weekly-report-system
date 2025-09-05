<template>
  <div class="base-search">
    <el-form
      ref="searchFormRef"
      :model="searchModel"
      :class="[
        'base-search__form',
        {
          'base-search__form--collapsed': collapsed,
          'base-search__form--loading': loading
        }
      ]"
      @submit.prevent="handleSearch"
    >
      <!-- Search fields grid -->
      <el-row :gutter="gutter">
        <template v-for="(field, index) in visibleFields" :key="field.prop">
          <el-col v-bind="getColProps(field, index)">
            <el-form-item
              :prop="field.prop"
              :label="field.label"
              :class="[
                'base-search__item',
                `base-search__item--${field.type || 'input'}`
              ]"
            >
              <!-- Field label slot -->
              <template v-if="field.labelSlot || $slots[`label-${field.prop}`]" #label>
                <slot 
                  :name="field.labelSlot || `label-${field.prop}`" 
                  :field="field"
                  :model="searchModel"
                />
              </template>

              <!-- Custom field slot -->
              <slot 
                v-if="field.slot || $slots[field.prop]"
                :name="field.slot || field.prop"
                :field="field"
                :model="searchModel"
                :value="getFieldValue(field.prop)"
                :setValue="(value: any) => setFieldValue(field.prop, value)"
              />
              
              <!-- Built-in field types -->
              <component 
                v-else
                :is="getFieldComponent(field)"
                v-bind="getFieldProps(field)"
                :model-value="getFieldValue(field.prop)"
                :disabled="loading"
                @update:model-value="(value: any) => handleFieldChange(field.prop, value)"
                @keydown.enter="handleSearch"
              >
                <!-- Select options -->
                <template v-if="field.type === 'select' && field.options">
                  <el-option
                    v-for="option in field.options"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                    :disabled="option.disabled"
                  />
                </template>

                <!-- Radio options -->
                <template v-if="field.type === 'radio' && field.options">
                  <el-radio
                    v-for="option in field.options"
                    :key="option.value"
                    :label="option.value"
                    :disabled="option.disabled"
                  >
                    {{ option.label }}
                  </el-radio>
                </template>

                <!-- Checkbox options -->
                <template v-if="field.type === 'checkbox' && field.options">
                  <el-checkbox
                    v-for="option in field.options"
                    :key="option.value"
                    :label="option.value"
                    :disabled="option.disabled"
                  >
                    {{ option.label }}
                  </el-checkbox>
                </template>
              </component>
            </el-form-item>
          </el-col>
        </template>

        <!-- Action buttons -->
        <el-col :span="actionSpan" class="base-search__actions">
          <el-form-item>
            <div class="base-search__buttons">
              <el-button
                type="primary"
                :icon="SearchIcon"
                :loading="loading"
                :disabled="loading"
                @click="handleSearch"
              >
                {{ searchButtonText }}
              </el-button>
              
              <el-button
                v-if="showReset"
                :icon="RefreshIcon"
                :disabled="loading"
                @click="handleReset"
              >
                {{ resetButtonText }}
              </el-button>
              
              <el-button
                v-if="showCollapse && collapsibleFields.length > 0"
                :icon="collapsed ? ExpandIcon : CollapseIcon"
                text
                @click="toggleCollapse"
              >
                {{ collapsed ? expandButtonText : collapseButtonText }}
              </el-button>

              <!-- Custom action buttons -->
              <slot name="actions" :model="searchModel" :search="handleSearch" :reset="handleReset" />
            </div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- Advanced search panel -->
    <div v-if="$slots.advanced" v-show="showAdvanced" class="base-search__advanced">
      <el-divider>{{ advancedTitle }}</el-divider>
      <slot name="advanced" :model="searchModel" :search="handleSearch" :reset="handleReset" />
    </div>

    <!-- Search result summary -->
    <div v-if="showSummary && searchCount !== null" class="base-search__summary">
      <el-text type="info" size="small">
        <el-icon><InfoIcon /></el-icon>
        {{ getSummaryText() }}
      </el-text>
      
      <!-- Quick clear button -->
      <el-button
        v-if="hasActiveFilters"
        text
        type="primary"
        size="small"
        @click="clearAllFilters"
      >
        清空筛选
      </el-button>
    </div>

    <!-- Active filters -->
    <div v-if="showActiveFilters && activeFilters.length > 0" class="base-search__filters">
      <el-text size="small" class="base-search__filters-label">筛选条件：</el-text>
      <div class="base-search__filter-tags">
        <el-tag
          v-for="filter in activeFilters"
          :key="filter.key"
          closable
          size="small"
          @close="removeFilter(filter.key)"
        >
          <strong>{{ filter.label }}：</strong>{{ filter.text }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, watch, nextTick } from 'vue'
import { 
  ElForm, 
  ElFormItem, 
  ElRow, 
  ElCol, 
  ElButton,
  ElInput,
  ElSelect,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElCheckbox,
  ElCheckboxGroup,
  ElDatePicker,
  ElTimeSelect,
  ElTimePicker,
  ElSwitch,
  ElSlider,
  ElCascader,
  ElDivider,
  ElText,
  ElTag,
  ElIcon
} from 'element-plus'
import { 
  Search as SearchIcon, 
  Refresh as RefreshIcon,
  ArrowDown as ExpandIcon,
  ArrowUp as CollapseIcon,
  InfoFilled as InfoIcon
} from '@element-plus/icons-vue'
import type { SearchProps, FormField } from '@/types'

interface FilterItem {
  key: string
  label: string
  text: string
  value: any
}

interface Props extends SearchProps {
  loading?: boolean
  searchCount?: number | null
  showSummary?: boolean
  showActiveFilters?: boolean
  showAdvanced?: boolean
  advancedTitle?: string
  defaultCollapsedRows?: number
  onSearch?: (model: Record<string, any>) => void | Promise<void>
  onReset?: () => void
  onFieldChange?: (prop: string, value: any, model: Record<string, any>) => void
}

interface Emits {
  'update:modelValue': [value: Record<string, any>]
  search: [model: Record<string, any>]
  reset: []
  'field-change': [prop: string, value: any, model: Record<string, any>]
  'filter-change': [filters: FilterItem[]]
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  showReset: true,
  showCollapse: true,
  defaultCollapsed: true,
  resetButtonText: '重置',
  searchButtonText: '搜索',
  collapseButtonText: '收起',
  expandButtonText: '展开',
  gutter: 20,
  span: 8,
  responsive: true,
  searchCount: null,
  showSummary: false,
  showActiveFilters: true,
  showAdvanced: false,
  advancedTitle: '高级搜索',
  defaultCollapsedRows: 1
})

const emit = defineEmits<Emits>()

// Refs
const searchFormRef = ref<InstanceType<typeof ElForm>>()
const collapsed = ref(props.defaultCollapsed)
const showAdvanced = ref(false)

// Computed properties
const searchModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const collapsibleFields = computed(() => {
  return props.fields.filter((_, index) => index >= getFieldsPerRow() * props.defaultCollapsedRows)
})

const visibleFields = computed(() => {
  if (!collapsed.value || !props.showCollapse) {
    return props.fields
  }
  
  const fieldsPerRow = getFieldsPerRow()
  const maxVisible = fieldsPerRow * props.defaultCollapsedRows
  return props.fields.slice(0, maxVisible)
})

const actionSpan = computed(() => {
  if (props.responsive) {
    return { xs: 24, sm: 24, md: 8, lg: 6, xl: 4 }
  }
  return props.span || 8
})

const hasActiveFilters = computed(() => {
  return Object.values(searchModel.value).some(value => {
    if (Array.isArray(value)) return value.length > 0
    return value !== undefined && value !== null && value !== ''
  })
})

const activeFilters = computed(() => {
  const filters: FilterItem[] = []
  
  props.fields.forEach(field => {
    const value = getFieldValue(field.prop)
    if (value !== undefined && value !== null && value !== '') {
      let text = ''
      
      if (Array.isArray(value)) {
        if (value.length === 0) return
        text = value.join(', ')
      } else if (field.options) {
        const option = field.options.find(opt => opt.value === value)
        text = option ? option.label : String(value)
      } else if (typeof value === 'boolean') {
        text = value ? '是' : '否'
      } else {
        text = String(value)
      }
      
      filters.push({
        key: field.prop,
        label: field.label,
        text,
        value
      })
    }
  })
  
  return filters
})

// Methods
const getFieldsPerRow = () => {
  if (!props.responsive) return Math.floor(24 / (props.span || 8))
  // Default responsive: 3 fields per row on desktop, 2 on tablet, 1 on mobile
  return 3
}

const getFieldValue = (prop: string) => {
  return prop.split('.').reduce((obj, key) => obj?.[key], searchModel.value)
}

const setFieldValue = (prop: string, value: any) => {
  const keys = prop.split('.')
  const lastKey = keys.pop()!
  const target = keys.reduce((obj, key) => {
    if (!obj[key]) obj[key] = {}
    return obj[key]
  }, searchModel.value)
  target[lastKey] = value
  
  emit('update:modelValue', { ...searchModel.value })
}

const getColProps = (field: FormField, index: number) => {
  if (props.responsive) {
    return {
      xs: field.xs || 24,
      sm: field.sm || 12,
      md: field.md || 8,
      lg: field.lg || 6,
      xl: field.xl || 6
    }
  }
  
  return {
    span: field.span || props.span || 8,
    offset: field.offset || 0
  }
}

const getFieldComponent = (field: FormField) => {
  const componentMap = {
    input: ElInput,
    select: ElSelect,
    radio: ElRadioGroup,
    checkbox: ElCheckboxGroup,
    switch: ElSwitch,
    date: ElDatePicker,
    datetime: ElDatePicker,
    daterange: ElDatePicker,
    datetimerange: ElDatePicker,
    time: ElTimePicker,
    timeSelect: ElTimeSelect,
    slider: ElSlider,
    cascader: ElCascader,
    custom: field.component
  }
  
  return componentMap[field.type || 'input'] || ElInput
}

const getFieldProps = (field: FormField) => {
  const baseProps = {
    placeholder: field.placeholder || `请${getPlaceholderPrefix(field)}${field.label}`,
    clearable: true,
    size: 'default',
    ...field.componentProps
  }
  
  // Type-specific props
  switch (field.type) {
    case 'select':
      return { ...baseProps, filterable: true }
    case 'date':
      return { ...baseProps, type: 'date', valueFormat: 'YYYY-MM-DD' }
    case 'datetime':
      return { ...baseProps, type: 'datetime', valueFormat: 'YYYY-MM-DD HH:mm:ss' }
    case 'daterange':
      return { 
        ...baseProps, 
        type: 'daterange', 
        rangeSeparator: '至',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        valueFormat: 'YYYY-MM-DD'
      }
    case 'datetimerange':
      return { 
        ...baseProps, 
        type: 'datetimerange', 
        rangeSeparator: '至',
        startPlaceholder: '开始时间',
        endPlaceholder: '结束时间',
        valueFormat: 'YYYY-MM-DD HH:mm:ss'
      }
    default:
      return baseProps
  }
}

const getPlaceholderPrefix = (field: FormField) => {
  const selectTypes = ['select', 'radio', 'checkbox', 'cascader']
  return selectTypes.includes(field.type || 'input') ? '选择' : '输入'
}

const getSummaryText = () => {
  if (props.searchCount === null) return ''
  if (props.searchCount === 0) return '未找到匹配的结果'
  return `找到 ${props.searchCount} 条匹配的结果`
}

// Event handlers
const handleFieldChange = (prop: string, value: any) => {
  setFieldValue(prop, value)
  emit('field-change', prop, value, searchModel.value)
  
  if (props.onFieldChange) {
    props.onFieldChange(prop, value, searchModel.value)
  }
}

const handleSearch = async () => {
  if (props.loading) return
  
  emit('search', searchModel.value)
  emit('filter-change', activeFilters.value)
  
  if (props.onSearch) {
    try {
      await props.onSearch(searchModel.value)
    } catch (error) {
      console.error('Search error:', error)
    }
  }
}

const handleReset = () => {
  // Reset form fields
  const resetModel: Record<string, any> = {}
  props.fields.forEach(field => {
    if (field.type === 'checkbox') {
      resetModel[field.prop] = []
    } else {
      resetModel[field.prop] = undefined
    }
  })
  
  emit('update:modelValue', resetModel)
  emit('reset')
  emit('filter-change', [])
  
  if (props.onReset) {
    props.onReset()
  }
  
  // Auto search after reset if needed
  nextTick(() => {
    if (props.onSearch) {
      props.onSearch(resetModel)
    }
  })
}

const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

const removeFilter = (key: string) => {
  setFieldValue(key, undefined)
  nextTick(() => {
    handleSearch()
  })
}

const clearAllFilters = () => {
  handleReset()
}

// Expose methods
defineExpose({
  search: handleSearch,
  reset: handleReset,
  toggle: toggleCollapse,
  formRef: searchFormRef
})
</script>

<style lang="scss" scoped>
.base-search {
  background: var(--el-bg-color);
  border-radius: 4px;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  margin-bottom: 16px;

  &__form {
    &--collapsed {
      .base-search__item {
        &:not(:first-child) {
          transition: all 0.3s ease;
        }
      }
    }

    &--loading {
      opacity: 0.7;
      pointer-events: none;
    }
  }

  &__item {
    margin-bottom: 0;

    :deep(.el-form-item__label) {
      color: var(--el-text-color-regular);
      font-weight: 500;
      line-height: 32px;
    }

    :deep(.el-form-item__content) {
      line-height: 32px;
    }

    // Field type specific styles
    &--select {
      :deep(.el-select) {
        width: 100%;
      }
    }

    &--date,
    &--datetime,
    &--daterange,
    &--datetimerange {
      :deep(.el-date-editor) {
        width: 100%;
      }
    }

    &--radio,
    &--checkbox {
      :deep(.el-radio-group),
      :deep(.el-checkbox-group) {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }
    }
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    align-items: flex-end;

    :deep(.el-form-item) {
      margin-bottom: 0;
    }
  }

  &__buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    @media (max-width: 768px) {
      width: 100%;
      justify-content: stretch;

      .el-button {
        flex: 1;
      }
    }
  }

  &__advanced {
    margin-top: 16px;
    padding-top: 16px;
  }

  &__summary {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding: 8px 12px;
    background: var(--el-fill-color-lighter);
    border-radius: 4px;
    font-size: 13px;

    .el-icon {
      margin-right: 4px;
    }
  }

  &__filters {
    margin-top: 12px;
    display: flex;
    align-items: flex-start;
    gap: 8px;
    flex-wrap: wrap;

    &-label {
      white-space: nowrap;
      margin-top: 4px;
      color: var(--el-text-color-regular);
    }
  }

  &__filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    flex: 1;
  }

  // Responsive design
  @media (max-width: 768px) {
    padding: 12px;

    &__actions {
      justify-content: center;
    }

    &__summary {
      flex-direction: column;
      gap: 8px;
      text-align: center;
    }

    &__filters {
      flex-direction: column;
      align-items: stretch;

      &-label {
        margin-top: 0;
      }
    }

    &__filter-tags {
      justify-content: flex-start;
    }
  }

  // Accessibility improvements
  :deep(.el-button) {
    &:focus-visible {
      outline: 2px solid var(--el-color-primary);
      outline-offset: 2px;
    }
  }
}

// Animation for collapsible fields
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

// High contrast mode support
@media (prefers-contrast: high) {
  .base-search {
    border-width: 2px;

    &__summary {
      border: 1px solid var(--el-border-color-base);
    }
  }
}

// Reduced motion support
@media (prefers-reduced-motion: reduce) {
  .base-search {
    &__form--collapsed .base-search__item {
      transition: none;
    }
  }
  
  .slide-fade-enter-active,
  .slide-fade-leave-active {
    transition: none;
  }
}
</style>