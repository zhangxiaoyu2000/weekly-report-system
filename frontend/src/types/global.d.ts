// Global type definitions for the application

// API Response Types
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
}

// User Types
export interface User {
  id: number
  username: string
  email: string
  name: string
  avatar?: string
  role: 'admin' | 'user' | 'manager'
  createdAt: string
  updatedAt: string
}

// Report Types
export interface WeeklyReport {
  id: number
  title: string
  content: string
  weekStartDate: string
  weekEndDate: string
  status: 'draft' | 'submitted' | 'approved' | 'rejected'
  userId: number
  user?: User
  createdAt: string
  updatedAt: string
  attachments?: ReportAttachment[]
}

export interface ReportAttachment {
  id: number
  filename: string
  url: string
  size: number
  type: string
  reportId: number
}

// Form Types
export interface CreateReportForm {
  title: string
  content: string
  weekStartDate: string
  weekEndDate: string
}

export interface UpdateReportForm extends Partial<CreateReportForm> {
  id: number
}

// API Request/Response Types
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  user: User
  token: string
  refreshToken: string
  expiresIn: number
}

// Pagination Types
export interface PaginationParams {
  page: number
  limit: number
  sort?: string
  order?: 'asc' | 'desc'
}

export interface PaginatedResponse<T> {
  items: T[]
  total: number
  page: number
  limit: number
  totalPages: number
}

// Component Props Types
export interface BaseComponentProps {
  class?: string
  style?: string | Record<string, any>
}

// Theme Types
export type ThemeMode = 'light' | 'dark' | 'auto'

export interface ThemeConfig {
  mode: ThemeMode
  primaryColor: string
  borderRadius: string
  fontSize: string
}

// Locale Types
export type SupportedLocale = 'en' | 'zh'

// Store Types
export interface UserState {
  currentUser: User | null
  token: string | null
  isLoggedIn: boolean
}

export interface ReportState {
  reports: WeeklyReport[]
  currentReport: WeeklyReport | null
  loading: boolean
  total: number
}

// Router Meta Types
declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    roles?: string[]
    icon?: string
    hideInMenu?: boolean
    keepAlive?: boolean
  }
}

// Vite Environment Variables
interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_APP_TITLE: string
  readonly VITE_APP_VERSION: string
  readonly VITE_DEBUG: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}