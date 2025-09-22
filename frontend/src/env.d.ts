/// <reference types="vite/client" />

// 环境变量类型定义
interface ImportMetaEnv {
  // 基础配置
  readonly VITE_APP_ENV: 'development' | 'test' | 'production'
  readonly VITE_APP_TITLE: string
  
  // API配置
  readonly VITE_API_BASE_URL: string
  readonly VITE_API_TIMEOUT: string
  readonly VITE_WS_URL: string
  readonly VITE_UPLOAD_URL: string
  readonly VITE_CDN_URL: string
  
  // 调试配置
  readonly VITE_DEBUG: string
  readonly VITE_LOG_LEVEL: 'debug' | 'info' | 'warn' | 'error'
  readonly VITE_SHOW_DEVTOOLS: string
  readonly VITE_HOT_RELOAD: string
  
  // 测试配置
  readonly VITE_ENABLE_MOCK?: string
  readonly VITE_TEST_DATA?: string
  readonly VITE_SKIP_AUTH?: string
  
  // 监控配置
  readonly VITE_ENABLE_ANALYTICS?: string
  readonly VITE_SENTRY_DSN?: string
  
  // 安全配置
  readonly VITE_SECURE_COOKIES?: string
  readonly VITE_CSRF_PROTECTION?: string
  
  // 代理配置 (本地开发专用)
  readonly VITE_BYPASS_PROXY?: string
  readonly VITE_USE_IP_INSTEAD_LOCALHOST?: string
  readonly VITE_DIRECT_CONNECTION?: string
  readonly VITE_LOCAL_API_HOST?: string
  readonly VITE_LOCAL_API_PORT?: string
  readonly VITE_DISABLE_CACHE?: string
  readonly VITE_FORCE_LOCALHOST_BYPASS?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// 全局类型声明
declare global {
  interface Window {
    __WEEKLY_REPORT_ENV__: ImportMetaEnv
  }
}