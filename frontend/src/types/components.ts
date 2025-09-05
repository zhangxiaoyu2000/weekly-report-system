/**
 * Common component types and interfaces
 */

// Common size variants for components
export type ComponentSize = 'large' | 'default' | 'small'

// Common component variants
export type ComponentVariant = 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'

// Common loading state
export interface LoadingState {
  loading: boolean
  error: string | null
  data: any
}

// Base component props that all components should extend
export interface BaseComponentProps {
  size?: ComponentSize
  disabled?: boolean
  loading?: boolean
}

// Button component props
export interface ButtonProps extends BaseComponentProps {
  type?: ComponentVariant
  plain?: boolean
  round?: boolean
  circle?: boolean
  icon?: string
  autofocus?: boolean
  nativeType?: 'button' | 'submit' | 'reset'
}

// Input component props
export interface InputProps extends BaseComponentProps {
  modelValue?: string | number
  type?: string
  maxlength?: number
  minlength?: number
  showWordLimit?: boolean
  placeholder?: string
  clearable?: boolean
  showPassword?: boolean
  readonly?: boolean
  resize?: 'none' | 'both' | 'horizontal' | 'vertical'
  autosize?: boolean | { minRows?: number; maxRows?: number }
  autocomplete?: string
  name?: string
  form?: string
  label?: string
  tabindex?: string | number
  validateEvent?: boolean
  inputStyle?: Record<string, any>
}

// Table column definition
export interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  fixed?: boolean | 'left' | 'right'
  sortable?: boolean | 'custom'
  resizable?: boolean
  showOverflowTooltip?: boolean
  align?: 'left' | 'center' | 'right'
  headerAlign?: 'left' | 'center' | 'right'
  formatter?: (row: any, column: any, cellValue: any, index: number) => any
  slot?: string
}

// Table component props
export interface TableProps extends BaseComponentProps {
  data: any[]
  columns: TableColumn[]
  height?: string | number
  maxHeight?: string | number
  stripe?: boolean
  border?: boolean
  fit?: boolean
  showHeader?: boolean
  highlightCurrentRow?: boolean
  rowClassName?: string | ((row: any, rowIndex: number) => string)
  rowStyle?: Record<string, any> | ((row: any, rowIndex: number) => Record<string, any>)
  cellClassName?: string | ((row: any, column: any, rowIndex: number, columnIndex: number) => string)
  cellStyle?: Record<string, any> | ((row: any, column: any, rowIndex: number, columnIndex: number) => Record<string, any>)
  headerRowClassName?: string | (() => string)
  headerRowStyle?: Record<string, any> | (() => Record<string, any>)
  headerCellClassName?: string | ((column: any, columnIndex: number) => string)
  headerCellStyle?: Record<string, any> | ((column: any, columnIndex: number) => Record<string, any>)
  rowKey?: string | ((row: any) => string)
  emptyText?: string
  defaultExpandAll?: boolean
  expandRowKeys?: any[]
  defaultSort?: { prop: string; order: 'ascending' | 'descending' }
  tooltipEffect?: 'dark' | 'light'
  showSummary?: boolean
  sumText?: string
  summaryMethod?: (columns: any[], data: any[]) => any[]
  spanMethod?: (row: any, column: any, rowIndex: number, columnIndex: number) => number[] | { rowspan: number; colspan: number }
}

// Dialog component props
export interface DialogProps extends BaseComponentProps {
  modelValue: boolean
  title?: string
  width?: string | number
  fullscreen?: boolean
  top?: string
  modal?: boolean
  modalClass?: string
  appendToBody?: boolean
  lockScroll?: boolean
  customClass?: string
  openDelay?: number
  closeDelay?: number
  closeOnClickModal?: boolean
  closeOnPressEscape?: boolean
  showClose?: boolean
  beforeClose?: (done: () => void) => void
  destroyOnClose?: boolean
  center?: boolean
  alignCenter?: boolean
  draggable?: boolean
  overflow?: boolean
}

// Form item rule
export interface FormRule {
  required?: boolean
  message?: string
  trigger?: string | string[]
  type?: string
  validator?: (rule: any, value: any, callback: (error?: string | Error) => void) => void
  min?: number
  max?: number
  len?: number
  enum?: any[]
  pattern?: RegExp
  transform?: (value: any) => any
  asyncValidator?: (rule: any, value: any, callback: (error?: string | Error) => void, source: any, options: any) => void
}

// Form field definition
export interface FormField {
  prop: string
  label: string
  type?: 'input' | 'textarea' | 'select' | 'radio' | 'checkbox' | 'date' | 'datetime' | 'time' | 'switch' | 'slider' | 'rate' | 'upload' | 'custom'
  placeholder?: string
  options?: { label: string; value: any; disabled?: boolean }[]
  rules?: FormRule[]
  span?: number
  offset?: number
  xs?: number | { span?: number; offset?: number }
  sm?: number | { span?: number; offset?: number }
  md?: number | { span?: number; offset?: number }
  lg?: number | { span?: number; offset?: number }
  xl?: number | { span?: number; offset?: number }
  component?: any
  componentProps?: Record<string, any>
  slot?: string
  show?: boolean | ((model: any) => boolean)
  disabled?: boolean | ((model: any) => boolean)
}

// Form component props
export interface FormProps extends BaseComponentProps {
  modelValue: Record<string, any>
  fields: FormField[]
  rules?: Record<string, FormRule[]>
  inline?: boolean
  labelPosition?: 'left' | 'right' | 'top'
  labelWidth?: string | number
  labelSuffix?: string
  hideRequiredAsterisk?: boolean
  showMessage?: boolean
  inlineMessage?: boolean
  statusIcon?: boolean
  validateOnRuleChange?: boolean
  size?: ComponentSize
  disabled?: boolean
  scrollToError?: boolean
  scrollIntoViewOptions?: boolean | ScrollIntoViewOptions
}

// Search component props
export interface SearchProps extends BaseComponentProps {
  modelValue: Record<string, any>
  fields: FormField[]
  showReset?: boolean
  showCollapse?: boolean
  defaultCollapsed?: boolean
  resetButtonText?: string
  searchButtonText?: string
  collapseButtonText?: string
  expandButtonText?: string
  gutter?: number
  span?: number
  responsive?: boolean
}

// Pagination info
export interface PaginationInfo {
  page: number
  pageSize: number
  total: number
}

// API response wrapper
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
}

// Request options
export interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH'
  params?: Record<string, any>
  data?: any
  headers?: Record<string, string>
  timeout?: number
  loading?: boolean
  errorMessage?: boolean
}

// Menu item definition
export interface MenuItem {
  key: string
  label: string
  icon?: string
  path?: string
  children?: MenuItem[]
  disabled?: boolean
  hidden?: boolean
  meta?: Record<string, any>
}