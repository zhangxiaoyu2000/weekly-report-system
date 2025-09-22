// 环境配置工具函数

/**
 * 构建API基础URL，支持代理绕过配置
 */
export function buildApiUrl(endpoint: string = ''): string {
  const envBaseUrl = import.meta.env.VITE_API_BASE_URL
  
  // 如果设置了绕过代理配置，使用IP地址替代localhost
  if (import.meta.env.VITE_BYPASS_PROXY === 'true' && 
      import.meta.env.VITE_USE_IP_INSTEAD_LOCALHOST === 'true') {
    const host = import.meta.env.VITE_LOCAL_API_HOST || '127.0.0.1'
    const port = import.meta.env.VITE_LOCAL_API_PORT || '8080'
    const baseUrl = `http://${host}:${port}/api`
    return endpoint ? `${baseUrl}${endpoint}` : baseUrl
  }
  
  const baseUrl = envBaseUrl || 'http://127.0.0.1:8080/api'
  return endpoint ? `${baseUrl}${endpoint}` : baseUrl
}

/**
 * 获取代理绕过相关的请求头
 */
export function getProxyBypassHeaders(): Record<string, string> {
  if (import.meta.env.VITE_BYPASS_PROXY === 'true') {
    return {
      'Cache-Control': 'no-cache, no-store, must-revalidate',
      'Pragma': 'no-cache',
      'Expires': '0'
    }
  }
  return {}
}

/**
 * 获取请求配置，包含代理绕过优化
 */
export function getRequestOptions(options: RequestInit = {}): RequestInit {
  const defaultOptions: RequestInit = {
    headers: {
      'Content-Type': 'application/json',
      ...getProxyBypassHeaders(),
      ...options.headers,
    },
    // 如果启用了缓存禁用，添加cache控制
    ...(import.meta.env.VITE_DISABLE_CACHE === 'true' ? {
      cache: 'no-store'
    } : {}),
    ...options,
  }
  
  return defaultOptions
}

/**
 * 获取环境配置信息
 */
export function getEnvConfig() {
  return {
    env: import.meta.env.VITE_APP_ENV || 'development',
    debug: import.meta.env.VITE_DEBUG === 'true',
    logLevel: import.meta.env.VITE_LOG_LEVEL || 'info',
    apiTimeout: parseInt(import.meta.env.VITE_API_TIMEOUT || '8000'),
    bypassProxy: import.meta.env.VITE_BYPASS_PROXY === 'true',
    useIpInsteadLocalhost: import.meta.env.VITE_USE_IP_INSTEAD_LOCALHOST === 'true',
    disableCache: import.meta.env.VITE_DISABLE_CACHE === 'true',
  }
}

/**
 * 控制台日志输出（基于环境变量）
 */
export function envLog(level: 'debug' | 'info' | 'warn' | 'error', message: string, ...args: any[]) {
  const currentLogLevel = import.meta.env.VITE_LOG_LEVEL || 'info'
  const levels = ['debug', 'info', 'warn', 'error']
  const currentLevelIndex = levels.indexOf(currentLogLevel)
  const messageLevelIndex = levels.indexOf(level)
  
  if (messageLevelIndex >= currentLevelIndex) {
    console[level](`[${import.meta.env.VITE_APP_ENV || 'dev'}] ${message}`, ...args)
  }
}