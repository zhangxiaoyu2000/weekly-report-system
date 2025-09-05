/**
 * useRequest composable for handling API requests
 * Provides loading states, error handling, and request management
 */

import { ref, reactive, computed, unref, type Ref, type MaybeRef } from 'vue'
import axios, { type AxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import type { UseRequestReturn, UseRequestOptions, ApiResponse } from '@/types'

export function useRequest<T = any>(
  url: MaybeRef<string>,
  options: UseRequestOptions = {}
): UseRequestReturn<T> {
  const {
    method = 'GET',
    immediate = false,
    loading: initialLoading = false,
    errorMessage = true,
    onSuccess,
    onError,
    transform,
    ...requestOptions
  } = options

  // Reactive state
  const data = ref<T | null>(null)
  const loading = ref(initialLoading)
  const error = ref<string | null>(null)

  // Request configuration
  const requestConfig = reactive<AxiosRequestConfig>({
    method,
    timeout: 10000,
    ...requestOptions
  })

  // Execute request function
  const execute = async (overrideOptions: Partial<UseRequestOptions> = {}): Promise<T> => {
    const finalUrl = unref(url)
    if (!finalUrl) {
      throw new Error('URL is required')
    }

    // Merge options
    const finalOptions = { ...options, ...overrideOptions }
    const finalConfig: AxiosRequestConfig = {
      ...requestConfig,
      url: finalUrl,
      method: finalOptions.method || method,
      params: finalOptions.params,
      data: finalOptions.data,
      headers: finalOptions.headers,
      timeout: finalOptions.timeout || requestConfig.timeout
    }

    // Set loading state
    if (finalOptions.loading !== false) {
      loading.value = true
    }
    error.value = null

    try {
      const response: AxiosResponse = await axios(finalConfig)
      let responseData = response.data

      // Check if response follows ApiResponse format
      if (responseData && typeof responseData === 'object' && 'code' in responseData) {
        const apiResponse = responseData as ApiResponse<T>
        if (apiResponse.code !== 200 && !apiResponse.success) {
          throw new Error(apiResponse.message || 'Request failed')
        }
        responseData = apiResponse.data
      }

      // Transform data if transformer provided
      if (transform && typeof transform === 'function') {
        responseData = transform(responseData)
      }

      data.value = responseData
      
      // Success callback
      if (onSuccess) {
        onSuccess(responseData)
      }
      
      // Success callback from override options
      if (finalOptions.onSuccess && finalOptions.onSuccess !== onSuccess) {
        finalOptions.onSuccess(responseData)
      }

      return responseData
    } catch (err) {
      const errorMsg = getErrorMessage(err)
      error.value = errorMsg

      // Show error message if enabled
      if (finalOptions.errorMessage !== false && errorMessage) {
        ElMessage.error(errorMsg)
      }

      // Error callback
      if (onError) {
        onError(errorMsg)
      }
      
      // Error callback from override options
      if (finalOptions.onError && finalOptions.onError !== onError) {
        finalOptions.onError(errorMsg)
      }

      throw err
    } finally {
      if (finalOptions.loading !== false) {
        loading.value = false
      }
    }
  }

  // Reset function
  const reset = () => {
    data.value = null
    loading.value = false
    error.value = null
  }

  // Execute immediately if specified
  if (immediate) {
    execute().catch(() => {
      // Error already handled in execute function
    })
  }

  return {
    data,
    loading: computed(() => loading.value),
    error: computed(() => error.value),
    execute,
    reset
  }
}

// Specialized hooks for different HTTP methods
export function useGet<T = any>(
  url: MaybeRef<string>, 
  options: Omit<UseRequestOptions, 'method'> = {}
) {
  return useRequest<T>(url, { ...options, method: 'GET' })
}

export function usePost<T = any>(
  url: MaybeRef<string>, 
  options: Omit<UseRequestOptions, 'method'> = {}
) {
  return useRequest<T>(url, { ...options, method: 'POST' })
}

export function usePut<T = any>(
  url: MaybeRef<string>, 
  options: Omit<UseRequestOptions, 'method'> = {}
) {
  return useRequest<T>(url, { ...options, method: 'PUT' })
}

export function useDelete<T = any>(
  url: MaybeRef<string>, 
  options: Omit<UseRequestOptions, 'method'> = {}
) {
  return useRequest<T>(url, { ...options, method: 'DELETE' })
}

// Paginated request hook
export function usePaginatedRequest<T = any>(
  url: MaybeRef<string>,
  options: UseRequestOptions & {
    defaultPageSize?: number
    defaultPage?: number
  } = {}
) {
  const { defaultPageSize = 20, defaultPage = 1, ...requestOptions } = options
  
  const pagination = reactive({
    page: defaultPage,
    pageSize: defaultPageSize,
    total: 0
  })

  const { data, loading, error, execute, reset } = useRequest<{
    list: T[]
    total: number
    page: number
    pageSize: number
  }>(url, {
    ...requestOptions,
    params: {
      ...requestOptions.params,
      page: pagination.page,
      pageSize: pagination.pageSize
    },
    onSuccess: (responseData) => {
      if (responseData) {
        pagination.total = responseData.total || 0
        pagination.page = responseData.page || pagination.page
        pagination.pageSize = responseData.pageSize || pagination.pageSize
      }
      requestOptions.onSuccess?.(responseData)
    }
  })

  const loadPage = async (page: number) => {
    pagination.page = page
    return execute({
      params: {
        ...requestOptions.params,
        page: pagination.page,
        pageSize: pagination.pageSize
      }
    })
  }

  const changePageSize = async (pageSize: number) => {
    pagination.pageSize = pageSize
    pagination.page = 1 // Reset to first page
    return execute({
      params: {
        ...requestOptions.params,
        page: pagination.page,
        pageSize: pagination.pageSize
      }
    })
  }

  const refresh = () => execute()

  const resetPagination = () => {
    pagination.page = defaultPage
    pagination.pageSize = defaultPageSize
    pagination.total = 0
    reset()
  }

  return {
    data,
    loading,
    error,
    pagination: computed(() => pagination),
    execute,
    loadPage,
    changePageSize,
    refresh,
    reset: resetPagination
  }
}

// Parallel requests hook
export function useParallelRequests<T extends Record<string, any>>(
  requests: Record<keyof T, () => Promise<any>>
) {
  const data = reactive<Partial<T>>({})
  const loading = ref(false)
  const errors = reactive<Record<string, string>>({})
  
  const execute = async (): Promise<T> => {
    loading.value = true
    
    // Clear previous errors
    Object.keys(errors).forEach(key => {
      delete errors[key]
    })

    try {
      const promises = Object.entries(requests).map(async ([key, request]) => {
        try {
          const result = await request()
          data[key as keyof T] = result
          return { key, result, error: null }
        } catch (error) {
          const errorMsg = getErrorMessage(error)
          errors[key] = errorMsg
          return { key, result: null, error: errorMsg }
        }
      })

      await Promise.all(promises)
      return data as T
    } finally {
      loading.value = false
    }
  }

  const reset = () => {
    Object.keys(data).forEach(key => {
      delete data[key]
    })
    Object.keys(errors).forEach(key => {
      delete errors[key]
    })
    loading.value = false
  }

  return {
    data: computed(() => data),
    loading: computed(() => loading.value),
    errors: computed(() => errors),
    execute,
    reset
  }
}

// Helper function to extract error message
function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiResponse>
    
    // API error response
    if (axiosError.response?.data?.message) {
      return axiosError.response.data.message
    }
    
    // HTTP status error
    if (axiosError.response?.status) {
      const status = axiosError.response.status
      switch (status) {
        case 400:
          return '请求参数错误'
        case 401:
          return '未授权，请重新登录'
        case 403:
          return '禁止访问'
        case 404:
          return '请求的资源不存在'
        case 408:
          return '请求超时'
        case 422:
          return '请求参数验证失败'
        case 500:
          return '服务器内部错误'
        case 502:
          return '网关错误'
        case 503:
          return '服务不可用'
        case 504:
          return '网关超时'
        default:
          return `请求失败 (${status})`
      }
    }
    
    // Network error
    if (axiosError.code === 'NETWORK_ERROR' || axiosError.message.includes('Network Error')) {
      return '网络连接失败，请检查网络设置'
    }
    
    // Timeout error
    if (axiosError.code === 'ECONNABORTED' || axiosError.message.includes('timeout')) {
      return '请求超时，请稍后重试'
    }
    
    return axiosError.message || '请求失败'
  }
  
  if (error instanceof Error) {
    return error.message
  }
  
  return '未知错误'
}

// Export request utilities
export { getErrorMessage }