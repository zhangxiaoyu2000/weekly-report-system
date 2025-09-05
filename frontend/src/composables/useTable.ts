/**
 * useTable composable for table state management
 * Provides pagination, sorting, filtering, and selection management
 */

import { ref, reactive, computed, watch, nextTick } from 'vue'
import { usePaginatedRequest } from './useRequest'
import type { 
  UseTableReturn, 
  UseTableOptions, 
  PaginationInfo, 
  SortInfo, 
  FilterInfo,
  ApiResponse
} from '@/types'

export function useTable<T = any>(options: UseTableOptions<T> = {}): UseTableReturn<T> {
  const {
    api,
    immediate = true,
    defaultSort,
    defaultFilter = {},
    defaultPageSize = 20,
    onLoad,
    onError,
    transform
  } = options

  // Table state
  const data = ref<T[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  
  // Pagination state
  const pagination = reactive<PaginationInfo>({
    page: 1,
    pageSize: defaultPageSize,
    total: 0
  })

  // Sort state
  const sort = ref<SortInfo>({
    prop: defaultSort?.prop || '',
    order: defaultSort?.order || null
  })

  // Filter state
  const filter = ref<FilterInfo>({ ...defaultFilter })

  // Selection state
  const selection = ref<T[]>([])
  const expandedRows = ref<T[]>([])

  // Current request parameters
  const requestParams = computed(() => ({
    page: pagination.page,
    pageSize: pagination.pageSize,
    ...(sort.value.prop && sort.value.order && {
      sortBy: sort.value.prop,
      sortOrder: sort.value.order
    }),
    ...filter.value
  }))

  // Load data function
  const load = async (params: Record<string, any> = {}) => {
    if (!api) return

    loading.value = true
    error.value = null

    try {
      const response = await api({
        ...requestParams.value,
        ...params
      })

      let responseData = response.data || response
      
      // Transform response if transformer provided
      if (transform) {
        responseData = transform(responseData)
      }

      // Handle different response formats
      if (responseData && typeof responseData === 'object') {
        if ('list' in responseData && 'total' in responseData) {
          // Standard paginated response
          data.value = responseData.list || []
          pagination.total = responseData.total || 0
        } else if (Array.isArray(responseData)) {
          // Simple array response
          data.value = responseData
          pagination.total = responseData.length
        } else {
          // Other response formats
          data.value = []
          pagination.total = 0
        }
      } else {
        data.value = []
        pagination.total = 0
      }

      // Success callback
      if (onLoad) {
        onLoad(data.value, pagination.total)
      }
    } catch (err) {
      const errorMsg = err instanceof Error ? err.message : 'Failed to load data'
      error.value = errorMsg
      data.value = []
      pagination.total = 0

      if (onError) {
        onError(errorMsg)
      }
    } finally {
      loading.value = false
    }
  }

  // Refresh function (reload with current parameters)
  const refresh = () => load()

  // Pagination handlers
  const handleSizeChange = async (size: number) => {
    pagination.pageSize = size
    pagination.page = 1 // Reset to first page
    await nextTick()
    await load()
  }

  const handleCurrentChange = async (page: number) => {
    pagination.page = page
    await nextTick()
    await load()
  }

  // Sort handlers
  const handleSortChange = async (sortInfo: SortInfo) => {
    sort.value = { ...sortInfo }
    pagination.page = 1 // Reset to first page when sorting changes
    await nextTick()
    await load()
  }

  const resetSort = async () => {
    sort.value = {
      prop: defaultSort?.prop || '',
      order: defaultSort?.order || null
    }
    pagination.page = 1
    await nextTick()
    await load()
  }

  // Filter handlers
  const handleFilterChange = async (filters: FilterInfo) => {
    filter.value = { ...filters }
    pagination.page = 1 // Reset to first page when filters change
    await nextTick()
    await load()
  }

  const resetFilter = async () => {
    filter.value = { ...defaultFilter }
    pagination.page = 1
    await nextTick()
    await load()
  }

  const setFilter = async (key: string, value: any) => {
    filter.value[key] = value
    pagination.page = 1
    await nextTick()
    await load()
  }

  const removeFilter = async (key: string) => {
    if (key in filter.value) {
      delete filter.value[key]
      pagination.page = 1
      await nextTick()
      await load()
    }
  }

  // Selection handlers
  const handleSelectionChange = (selectedRows: T[]) => {
    selection.value = selectedRows
  }

  const clearSelection = () => {
    selection.value = []
  }

  const toggleRowSelection = (row: T, selected?: boolean) => {
    const index = selection.value.findIndex(item => item === row)
    
    if (selected === undefined) {
      // Toggle selection
      if (index > -1) {
        selection.value.splice(index, 1)
      } else {
        selection.value.push(row)
      }
    } else if (selected) {
      // Add to selection
      if (index === -1) {
        selection.value.push(row)
      }
    } else {
      // Remove from selection
      if (index > -1) {
        selection.value.splice(index, 1)
      }
    }
  }

  const toggleAllSelection = () => {
    if (selection.value.length === data.value.length) {
      // All selected, clear selection
      clearSelection()
    } else {
      // Select all
      selection.value = [...data.value]
    }
  }

  const setCurrentRow = (row: T) => {
    // This would typically highlight the current row
    // Implementation depends on the specific table component
  }

  // Expand handlers
  const handleRowExpand = (row: T, expanded: boolean) => {
    const index = expandedRows.value.findIndex(item => item === row)
    
    if (expanded && index === -1) {
      expandedRows.value.push(row)
    } else if (!expanded && index > -1) {
      expandedRows.value.splice(index, 1)
    }
  }

  const expandRow = (row: T) => {
    const index = expandedRows.value.findIndex(item => item === row)
    if (index === -1) {
      expandedRows.value.push(row)
    }
  }

  const collapseRow = (row: T) => {
    const index = expandedRows.value.findIndex(item => item === row)
    if (index > -1) {
      expandedRows.value.splice(index, 1)
    }
  }

  const toggleRowExpansion = (row: T) => {
    const index = expandedRows.value.findIndex(item => item === row)
    if (index > -1) {
      expandedRows.value.splice(index, 1)
    } else {
      expandedRows.value.push(row)
    }
  }

  // Reset all state
  const reset = () => {
    data.value = []
    loading.value = false
    error.value = null
    pagination.page = 1
    pagination.pageSize = defaultPageSize
    pagination.total = 0
    sort.value = {
      prop: defaultSort?.prop || '',
      order: defaultSort?.order || null
    }
    filter.value = { ...defaultFilter }
    selection.value = []
    expandedRows.value = []
  }

  // Computed properties for convenience
  const hasData = computed(() => data.value.length > 0)
  const isEmpty = computed(() => !loading.value && data.value.length === 0)
  const hasError = computed(() => !!error.value)
  const hasSelection = computed(() => selection.value.length > 0)
  const isAllSelected = computed(() => 
    data.value.length > 0 && selection.value.length === data.value.length
  )
  const isIndeterminate = computed(() => 
    selection.value.length > 0 && selection.value.length < data.value.length
  )

  // Helper methods
  const getRowIndex = (row: T) => data.value.findIndex(item => item === row)
  const getRowByIndex = (index: number) => data.value[index]
  const isRowSelected = (row: T) => selection.value.includes(row)
  const isRowExpanded = (row: T) => expandedRows.value.includes(row)

  // Search functionality
  const search = async (searchParams: Record<string, any>) => {
    // Clear current filters and apply search params
    filter.value = { ...defaultFilter, ...searchParams }
    pagination.page = 1
    await nextTick()
    await load()
  }

  // Load initial data
  if (immediate && api) {
    load().catch(() => {
      // Error handling is done in load function
    })
  }

  return {
    // State
    data,
    loading: computed(() => loading.value),
    error: computed(() => error.value),
    pagination: computed(() => pagination),
    sort,
    filter,
    selection,
    expandedRows,

    // Data operations
    refresh,
    load,
    search,
    reset,

    // Pagination
    handleSizeChange,
    handleCurrentChange,

    // Sorting
    handleSortChange,
    resetSort,

    // Filtering
    handleFilterChange,
    resetFilter,
    setFilter,
    removeFilter,

    // Selection
    handleSelectionChange,
    clearSelection,
    toggleRowSelection,
    toggleAllSelection,
    setCurrentRow,

    // Expansion
    handleRowExpand,
    expandRow,
    collapseRow,
    toggleRowExpansion,

    // Computed helpers
    hasData,
    isEmpty,
    hasError,
    hasSelection,
    isAllSelected,
    isIndeterminate,

    // Utility methods
    getRowIndex,
    getRowByIndex,
    isRowSelected,
    isRowExpanded
  }
}

// Specialized table hooks
export function useLocalTable<T = any>(
  initialData: T[] = [],
  options: Omit<UseTableOptions<T>, 'api' | 'immediate'> = {}
) {
  const { defaultPageSize = 20, onLoad } = options
  
  const originalData = ref<T[]>(initialData)
  const table = useTable<T>({ 
    ...options, 
    immediate: false,
    api: undefined
  })

  // Override load function to work with local data
  const load = async (params: Record<string, any> = {}) => {
    table.loading.value = true
    
    try {
      let filteredData = [...originalData.value]
      
      // Apply filters
      Object.entries(table.filter.value).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          filteredData = filteredData.filter(item => {
            const itemValue = getNestedValue(item, key)
            if (Array.isArray(value)) {
              return value.includes(itemValue)
            }
            return String(itemValue).toLowerCase().includes(String(value).toLowerCase())
          })
        }
      })
      
      // Apply sorting
      if (table.sort.value.prop && table.sort.value.order) {
        const { prop, order } = table.sort.value
        filteredData.sort((a, b) => {
          const aValue = getNestedValue(a, prop)
          const bValue = getNestedValue(b, prop)
          
          let result = 0
          if (aValue < bValue) result = -1
          else if (aValue > bValue) result = 1
          
          return order === 'ascending' ? result : -result
        })
      }
      
      // Apply pagination
      const total = filteredData.length
      const start = (table.pagination.value.page - 1) * table.pagination.value.pageSize
      const end = start + table.pagination.value.pageSize
      const pageData = filteredData.slice(start, end)
      
      table.data.value = pageData
      table.pagination.value.total = total
      
      if (onLoad) {
        onLoad(pageData, total)
      }
    } catch (err) {
      table.error.value = err instanceof Error ? err.message : 'Failed to process data'
    } finally {
      table.loading.value = false
    }
  }

  // Update original data
  const setData = (newData: T[]) => {
    originalData.value = newData
    table.pagination.value.page = 1
    load()
  }

  // Add item
  const addItem = (item: T) => {
    originalData.value.push(item)
    load()
  }

  // Update item
  const updateItem = (index: number, item: T) => {
    if (index >= 0 && index < originalData.value.length) {
      originalData.value[index] = item
      load()
    }
  }

  // Remove item
  const removeItem = (index: number) => {
    if (index >= 0 && index < originalData.value.length) {
      originalData.value.splice(index, 1)
      load()
    }
  }

  // Override table methods
  const enhancedTable = {
    ...table,
    load,
    setData,
    addItem,
    updateItem,
    removeItem,
    originalData: computed(() => originalData.value)
  }

  // Initial load
  if (initialData.length > 0) {
    load()
  }

  return enhancedTable
}

// Helper function to get nested object value
function getNestedValue(obj: any, path: string): any {
  return path.split('.').reduce((current, key) => current?.[key], obj)
}

export default useTable