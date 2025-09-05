<template>
  <div class="base-table">
    <!-- Table toolbar -->
    <div v-if="showToolbar" class="base-table__toolbar">
      <div class="base-table__toolbar-left">
        <slot name="toolbar-left" />
      </div>
      <div class="base-table__toolbar-right">
        <slot name="toolbar-right">
          <!-- Refresh button -->
          <el-button 
            v-if="showRefresh" 
            :icon="RefreshIcon" 
            :loading="loading"
            @click="handleRefresh"
          >
            刷新
          </el-button>
          <!-- Column settings -->
          <el-button 
            v-if="showColumnSettings" 
            :icon="SettingIcon"
            @click="showColumnDialog = true"
          >
            列设置
          </el-button>
        </slot>
      </div>
    </div>

    <!-- Main table -->
    <el-table
      v-bind="tableProps"
      :data="data"
      :loading="loading"
      :class="[
        'base-table__table',
        {
          'base-table__table--striped': stripe,
          'base-table__table--bordered': border
        }
      ]"
      @select="handleSelect"
      @select-all="handleSelectAll"
      @selection-change="handleSelectionChange"
      @row-click="handleRowClick"
      @row-dblclick="handleRowDoubleClick"
      @sort-change="handleSortChange"
      @filter-change="handleFilterChange"
      @current-change="handleCurrentRowChange"
      @header-click="handleHeaderClick"
      @expand-change="handleExpandChange"
    >
      <!-- Selection column -->
      <el-table-column
        v-if="selectable"
        type="selection"
        :width="selectionWidth"
        :fixed="selectionFixed"
        :selectable="selectableFunction"
      />

      <!-- Index column -->
      <el-table-column
        v-if="showIndex"
        type="index"
        :label="indexLabel"
        :width="indexWidth"
        :fixed="indexFixed"
        :index="indexMethod"
      />

      <!-- Expand column -->
      <el-table-column
        v-if="expandable"
        type="expand"
        :width="expandWidth"
        :fixed="expandFixed"
      >
        <template #default="{ row, $index }">
          <slot name="expand" :row="row" :index="$index" />
        </template>
      </el-table-column>

      <!-- Data columns -->
      <template v-for="column in visibleColumns" :key="column.prop">
        <el-table-column
          v-bind="getColumnProps(column)"
        >
          <!-- Column header -->
          <template v-if="column.headerSlot || $slots[`header-${column.prop}`]" #header="scope">
            <slot 
              :name="column.headerSlot || `header-${column.prop}`" 
              v-bind="scope"
            />
          </template>

          <!-- Column content -->
          <template #default="{ row, column: tableColumn, $index }">
            <!-- Custom slot -->
            <slot 
              v-if="column.slot || $slots[column.prop]" 
              :name="column.slot || column.prop"
              :row="row"
              :column="column"
              :value="getCellValue(row, column.prop)"
              :index="$index"
            />
            <!-- Formatted content -->
            <span v-else-if="column.formatter">
              {{ column.formatter(row, tableColumn, getCellValue(row, column.prop), $index) }}
            </span>
            <!-- Default content -->
            <span v-else>
              {{ getCellValue(row, column.prop) }}
            </span>
          </template>
        </el-table-column>
      </template>

      <!-- Action column -->
      <el-table-column
        v-if="$slots.actions || showActions"
        :label="actionLabel"
        :width="actionWidth"
        :fixed="actionFixed"
        :class-name="actionClassName"
      >
        <template #default="{ row, $index }">
          <div class="base-table__actions">
            <slot name="actions" :row="row" :index="$index" />
          </div>
        </template>
      </el-table-column>

      <!-- Empty state -->
      <template #empty>
        <div class="base-table__empty">
          <slot name="empty">
            <div class="base-table__empty-content">
              <el-icon class="base-table__empty-icon"><DocumentIcon /></el-icon>
              <p class="base-table__empty-text">{{ emptyText || '暂无数据' }}</p>
            </div>
          </slot>
        </div>
      </template>
    </el-table>

    <!-- Pagination -->
    <div 
      v-if="showPagination && pagination" 
      class="base-table__pagination"
    >
      <el-pagination
        v-bind="paginationProps"
        :current-page="pagination.page"
        :page-size="pagination.pageSize"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- Column settings dialog -->
    <el-dialog
      v-model="showColumnDialog"
      title="列设置"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="base-table__column-settings">
        <el-checkbox
          v-model="selectAllColumns"
          :indeterminate="isIndeterminate"
          @change="handleSelectAllColumns"
        >
          全选
        </el-checkbox>
        <el-divider />
        <el-checkbox-group v-model="selectedColumns" @change="handleColumnChange">
          <div
            v-for="column in columns"
            :key="column.prop"
            class="base-table__column-item"
          >
            <el-checkbox :label="column.prop">
              {{ column.label }}
            </el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="showColumnDialog = false">取消</el-button>
        <el-button type="primary" @click="saveColumnSettings">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { 
  ElTable, 
  ElTableColumn, 
  ElButton, 
  ElPagination, 
  ElDialog,
  ElCheckbox,
  ElCheckboxGroup,
  ElDivider,
  ElIcon
} from 'element-plus'
import { Refresh as RefreshIcon, Setting as SettingIcon, Document as DocumentIcon } from '@element-plus/icons-vue'
import type { TableProps, TableColumn, PaginationInfo, SortInfo, FilterInfo } from '@/types'

interface Props extends Omit<TableProps, 'data' | 'columns'> {
  data: any[]
  columns: TableColumn[]
  loading?: boolean
  pagination?: PaginationInfo | false
  selectable?: boolean
  selectionWidth?: string | number
  selectionFixed?: boolean | 'left' | 'right'
  selectableFunction?: (row: any, index: number) => boolean
  showIndex?: boolean
  indexLabel?: string
  indexWidth?: string | number
  indexFixed?: boolean | 'left' | 'right'
  indexMethod?: (index: number) => number
  expandable?: boolean
  expandWidth?: string | number
  expandFixed?: boolean | 'left' | 'right'
  showActions?: boolean
  actionLabel?: string
  actionWidth?: string | number
  actionFixed?: boolean | 'left' | 'right'
  actionClassName?: string
  showPagination?: boolean
  showToolbar?: boolean
  showRefresh?: boolean
  showColumnSettings?: boolean
  columnSettingsKey?: string
  paginationLayout?: string
  paginationSizes?: number[]
  paginationPageSizes?: number[]
  paginationPagerCount?: number
  paginationSmall?: boolean
  paginationBackground?: boolean
  paginationHideOnSinglePage?: boolean
}

interface Emits {
  'selection-change': [selection: any[]]
  'sort-change': [sort: SortInfo]
  'filter-change': [filters: FilterInfo]
  'row-click': [row: any, column: any, event: Event]
  'row-double-click': [row: any, column: any, event: Event]
  'current-change': [currentRow: any, oldCurrentRow: any]
  'header-click': [column: any, event: Event]
  'expand-change': [row: any, expanded: boolean]
  'size-change': [size: number]
  'page-change': [page: number]
  'refresh': []
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  stripe: true,
  border: true,
  fit: true,
  showHeader: true,
  highlightCurrentRow: false,
  selectionWidth: 55,
  selectionFixed: 'left',
  showIndex: false,
  indexLabel: '序号',
  indexWidth: 60,
  indexFixed: false,
  expandWidth: 55,
  expandFixed: 'left',
  showActions: true,
  actionLabel: '操作',
  actionWidth: 150,
  actionFixed: 'right',
  showPagination: true,
  showToolbar: true,
  showRefresh: true,
  showColumnSettings: true,
  paginationLayout: 'total, sizes, prev, pager, next, jumper',
  paginationSizes: () => [10, 20, 50, 100],
  paginationPageSizes: () => [10, 20, 50, 100],
  paginationPagerCount: 7,
  paginationSmall: false,
  paginationBackground: true,
  paginationHideOnSinglePage: false
})

const emit = defineEmits<Emits>()

// Refs
const showColumnDialog = ref(false)
const selectedColumns = ref<string[]>([])
const selectAllColumns = ref(true)

// Computed properties
const tableProps = computed(() => {
  const { 
    data, 
    columns, 
    loading,
    pagination,
    selectable,
    selectionWidth,
    selectionFixed,
    selectableFunction,
    showIndex,
    indexLabel,
    indexWidth,
    indexFixed,
    indexMethod,
    expandable,
    expandWidth,
    expandFixed,
    showActions,
    actionLabel,
    actionWidth,
    actionFixed,
    actionClassName,
    showPagination,
    showToolbar,
    showRefresh,
    showColumnSettings,
    columnSettingsKey,
    paginationLayout,
    paginationSizes,
    paginationPageSizes,
    paginationPagerCount,
    paginationSmall,
    paginationBackground,
    paginationHideOnSinglePage,
    ...rest 
  } = props
  return rest
})

const paginationProps = computed(() => ({
  layout: props.paginationLayout,
  pageSizes: props.paginationSizes,
  pagerCount: props.paginationPagerCount,
  small: props.paginationSmall,
  background: props.paginationBackground,
  hideOnSinglePage: props.paginationHideOnSinglePage
}))

const visibleColumns = computed(() => {
  if (!props.showColumnSettings) {
    return props.columns
  }
  return props.columns.filter(column => selectedColumns.value.includes(column.prop))
})

const isIndeterminate = computed(() => {
  const selected = selectedColumns.value.length
  const total = props.columns.length
  return selected > 0 && selected < total
})

// Initialize selected columns
watch(() => props.columns, (newColumns) => {
  if (selectedColumns.value.length === 0) {
    selectedColumns.value = newColumns.map(col => col.prop)
  }
}, { immediate: true })

// Methods
const getCellValue = (row: any, prop: string) => {
  return prop.split('.').reduce((obj, key) => obj?.[key], row)
}

const getColumnProps = (column: TableColumn) => {
  const { slot, headerSlot, formatter, ...props } = column
  return props
}

// Event handlers
const handleSelect = (selection: any[], row: any) => {
  // Handle single row selection
}

const handleSelectAll = (selection: any[]) => {
  // Handle select all
}

const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
}

const handleRowClick = (row: any, column: any, event: Event) => {
  emit('row-click', row, column, event)
}

const handleRowDoubleClick = (row: any, column: any, event: Event) => {
  emit('row-double-click', row, column, event)
}

const handleSortChange = (sort: any) => {
  const sortInfo: SortInfo = {
    prop: sort.prop,
    order: sort.order === 'ascending' ? 'ascending' : sort.order === 'descending' ? 'descending' : null
  }
  emit('sort-change', sortInfo)
}

const handleFilterChange = (filters: any) => {
  emit('filter-change', filters)
}

const handleCurrentRowChange = (currentRow: any, oldCurrentRow: any) => {
  emit('current-change', currentRow, oldCurrentRow)
}

const handleHeaderClick = (column: any, event: Event) => {
  emit('header-click', column, event)
}

const handleExpandChange = (row: any, expanded: boolean) => {
  emit('expand-change', row, expanded)
}

const handleSizeChange = (size: number) => {
  emit('size-change', size)
}

const handleCurrentChange = (page: number) => {
  emit('page-change', page)
}

const handleRefresh = () => {
  emit('refresh')
}

const handleSelectAllColumns = (value: boolean) => {
  if (value) {
    selectedColumns.value = props.columns.map(col => col.prop)
  } else {
    selectedColumns.value = []
  }
}

const handleColumnChange = (value: string[]) => {
  selectAllColumns.value = value.length === props.columns.length
}

const saveColumnSettings = () => {
  // Save column settings to localStorage if key is provided
  if (props.columnSettingsKey) {
    localStorage.setItem(
      `table-columns-${props.columnSettingsKey}`,
      JSON.stringify(selectedColumns.value)
    )
  }
  showColumnDialog.value = false
}

// Load column settings on mount
if (props.columnSettingsKey) {
  const saved = localStorage.getItem(`table-columns-${props.columnSettingsKey}`)
  if (saved) {
    try {
      selectedColumns.value = JSON.parse(saved)
    } catch (error) {
      console.warn('Failed to parse saved column settings:', error)
    }
  }
}
</script>

<style lang="scss" scoped>
.base-table {
  &__toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px 0;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &-left,
    &-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }

  &__table {
    width: 100%;
    
    &--striped {
      :deep(.el-table__row:nth-child(even)) {
        background-color: var(--el-fill-color-lighter);
      }
    }

    &--bordered {
      :deep(.el-table) {
        border: 1px solid var(--el-border-color-base);
      }
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }

  &__pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid var(--el-border-color-lighter);
  }

  &__empty {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 200px;

    &-content {
      text-align: center;
    }

    &-icon {
      font-size: 64px;
      color: var(--el-color-info-light-3);
      margin-bottom: 16px;
    }

    &-text {
      color: var(--el-color-info);
      font-size: 14px;
      margin: 0;
    }
  }

  &__column-settings {
    max-height: 400px;
    overflow-y: auto;
  }

  &__column-item {
    padding: 4px 0;
  }

  // Responsive design
  @media (max-width: 768px) {
    &__toolbar {
      flex-direction: column;
      gap: 12px;
      align-items: stretch;

      &-left,
      &-right {
        justify-content: center;
      }
    }

    &__pagination {
      justify-content: center;
      
      :deep(.el-pagination) {
        flex-wrap: wrap;
        justify-content: center;
      }
    }

    &__actions {
      flex-direction: column;
      align-items: stretch;
      
      :deep(.el-button) {
        margin: 2px 0;
      }
    }
  }

  // Accessibility improvements
  &__table {
    :deep(.el-table__header-wrapper) {
      th {
        &:focus-visible {
          outline: 2px solid var(--el-color-primary);
          outline-offset: -2px;
        }
      }
    }

    :deep(.el-table__row) {
      &:focus-visible {
        outline: 2px solid var(--el-color-primary);
        outline-offset: -2px;
      }
    }
  }
}

// High contrast mode support
@media (prefers-contrast: high) {
  .base-table {
    &__toolbar {
      border-bottom-width: 2px;
    }

    &__pagination {
      border-top-width: 2px;
    }
  }
}

// Reduced motion support
@media (prefers-reduced-motion: reduce) {
  .base-table {
    &__table {
      :deep(.el-table) {
        * {
          transition: none !important;
        }
      }
    }
  }
}
</style>