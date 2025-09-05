/**
 * Types for composables
 */

import type { Ref, ComputedRef } from 'vue'
import type { RequestOptions, ApiResponse, PaginationInfo, FormRule } from './components'

// useRequest composable return type
export interface UseRequestReturn<T = any> {
  data: Ref<T | null>
  loading: Ref<boolean>
  error: Ref<string | null>
  execute: (options?: Partial<RequestOptions>) => Promise<T>
  reset: () => void
}

// useRequest options
export interface UseRequestOptions extends RequestOptions {
  immediate?: boolean
  onSuccess?: (data: any) => void
  onError?: (error: string) => void
  transform?: (data: any) => any
}

// useForm validation result
export interface ValidationResult {
  valid: boolean
  errors: Record<string, string[]>
}

// useForm composable return type
export interface UseFormReturn {
  model: Ref<Record<string, any>>
  rules: Ref<Record<string, FormRule[]>>
  loading: Ref<boolean>
  errors: Ref<Record<string, string[]>>
  validate: (callback?: (valid: boolean, errors?: Record<string, string[]>) => void) => Promise<ValidationResult>
  validateField: (prop: string, callback?: (valid: boolean, error?: string) => void) => Promise<boolean>
  resetFields: () => void
  clearValidate: (props?: string | string[]) => void
  setFieldValue: (prop: string, value: any) => void
  getFieldValue: (prop: string) => any
  setFieldError: (prop: string, error: string) => void
  clearFieldError: (prop: string) => void
  submit: (onValid?: (model: Record<string, any>) => void | Promise<void>) => Promise<boolean>
  reset: () => void
}

// useForm options
export interface UseFormOptions {
  initialValues?: Record<string, any>
  rules?: Record<string, FormRule[]>
  validateOnRuleChange?: boolean
  validateOnChange?: boolean
  onSubmit?: (model: Record<string, any>) => void | Promise<void>
  onReset?: () => void
}

// Table sort info
export interface SortInfo {
  prop: string
  order: 'ascending' | 'descending' | null
}

// Table filter info
export interface FilterInfo {
  [key: string]: any[]
}

// useTable composable return type
export interface UseTableReturn<T = any> {
  data: Ref<T[]>
  loading: Ref<boolean>
  error: Ref<string | null>
  pagination: Ref<PaginationInfo>
  sort: Ref<SortInfo>
  filter: Ref<FilterInfo>
  selection: Ref<T[]>
  expandedRows: Ref<T[]>
  refresh: () => Promise<void>
  load: (params?: Record<string, any>) => Promise<void>
  handleSizeChange: (size: number) => void
  handleCurrentChange: (page: number) => void
  handleSortChange: (sort: SortInfo) => void
  handleFilterChange: (filters: FilterInfo) => void
  handleSelectionChange: (selection: T[]) => void
  handleRowExpand: (row: T, expanded: boolean) => void
  resetSort: () => void
  resetFilter: () => void
  clearSelection: () => void
  toggleRowSelection: (row: T, selected?: boolean) => void
  toggleAllSelection: () => void
  setCurrentRow: (row: T) => void
  reset: () => void
}

// useTable options
export interface UseTableOptions<T = any> {
  api?: (params: any) => Promise<ApiResponse<{ list: T[], total: number }>>
  immediate?: boolean
  defaultSort?: SortInfo
  defaultFilter?: FilterInfo
  defaultPageSize?: number
  onLoad?: (data: T[], total: number) => void
  onError?: (error: string) => void
  transform?: (response: any) => { list: T[], total: number }
}

// Dialog state
export interface DialogState {
  visible: boolean
  loading: boolean
  title: string
  data: any
}

// useDialog composable return type
export interface UseDialogReturn {
  state: Ref<DialogState>
  visible: ComputedRef<boolean>
  loading: ComputedRef<boolean>
  title: ComputedRef<string>
  data: ComputedRef<any>
  open: (options?: Partial<DialogState>) => void
  close: () => void
  setLoading: (loading: boolean) => void
  setTitle: (title: string) => void
  setData: (data: any) => void
  reset: () => void
}

// useDialog options
export interface UseDialogOptions {
  defaultVisible?: boolean
  defaultTitle?: string
  defaultData?: any
  onOpen?: (state: DialogState) => void
  onClose?: (state: DialogState) => void
}

// Loading state for different scopes
export interface LoadingScope {
  global: boolean
  local: Record<string, boolean>
}

// useLoading composable return type
export interface UseLoadingReturn {
  loading: ComputedRef<boolean>
  setLoading: (loading: boolean, scope?: string) => void
  clearLoading: (scope?: string) => void
  withLoading: <T>(fn: () => Promise<T>, scope?: string) => Promise<T>
}

// Debounce options
export interface DebounceOptions {
  delay?: number
  immediate?: boolean
  maxWait?: number
}

// useDebounce return type
export interface UseDebounceReturn<T extends (...args: any[]) => any> {
  debounced: T
  cancel: () => void
  flush: () => void
  isPending: ComputedRef<boolean>
}

// Throttle options
export interface ThrottleOptions {
  delay?: number
  leading?: boolean
  trailing?: boolean
}

// useThrottle return type
export interface UseThrottleReturn<T extends (...args: any[]) => any> {
  throttled: T
  cancel: () => void
  flush: () => void
}

// Event bus event handler
export type EventHandler<T = any> = (payload: T) => void

// useEventBus return type
export interface UseEventBusReturn {
  on: <T = any>(event: string, handler: EventHandler<T>) => void
  off: <T = any>(event: string, handler: EventHandler<T>) => void
  emit: <T = any>(event: string, payload?: T) => void
  clear: (event?: string) => void
}

// Storage options
export interface StorageOptions {
  prefix?: string
  suffix?: string
  serializer?: {
    read: (value: string) => any
    write: (value: any) => string
  }
}

// useStorage return type
export interface UseStorageReturn<T> {
  value: Ref<T>
  remove: () => void
  clear: () => void
}

// Breakpoint definitions
export interface Breakpoints {
  xs: number
  sm: number
  md: number
  lg: number
  xl: number
  xxl: number
}

// useBreakpoint return type
export interface UseBreakpointReturn {
  current: ComputedRef<string>
  xs: ComputedRef<boolean>
  sm: ComputedRef<boolean>
  md: ComputedRef<boolean>
  lg: ComputedRef<boolean>
  xl: ComputedRef<boolean>
  xxl: ComputedRef<boolean>
  greater: (breakpoint: string) => ComputedRef<boolean>
  smaller: (breakpoint: string) => ComputedRef<boolean>
  between: (min: string, max: string) => ComputedRef<boolean>
}