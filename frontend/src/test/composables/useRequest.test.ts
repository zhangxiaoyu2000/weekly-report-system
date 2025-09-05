/**
 * Tests for useRequest composable
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref, nextTick } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useRequest, useGet, usePost, usePaginatedRequest } from '@/composables/useRequest'
import { mockApiResponse, mockApiError } from '@/test/utils/test-helpers'

// Mock axios
vi.mock('axios')
const mockedAxios = vi.mocked(axios)

// Mock Element Plus message
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    }
  }
})

describe('useRequest', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.clearAllTimers()
  })

  describe('Basic Functionality', () => {
    it('initializes with correct default state', () => {
      const { data, loading, error } = useRequest('/api/test')
      
      expect(data.value).toBeNull()
      expect(loading.value).toBe(false)
      expect(error.value).toBeNull()
    })

    it('sets loading to true during request', async () => {
      const mockResponse = { data: { message: 'success' } }
      mockedAxios.mockResolvedValueOnce(mockResponse)
      
      const { loading, execute } = useRequest('/api/test')
      
      const promise = execute()
      expect(loading.value).toBe(true)
      
      await promise
      expect(loading.value).toBe(false)
    })

    it('updates data on successful request', async () => {
      const mockData = { message: 'success', items: [1, 2, 3] }
      mockedAxios.mockResolvedValueOnce({ data: mockData })
      
      const { data, execute } = useRequest('/api/test')
      
      await execute()
      
      expect(data.value).toEqual(mockData)
    })

    it('handles error state', async () => {
      const errorMessage = 'Request failed'
      mockedAxios.mockRejectedValueOnce(new Error(errorMessage))
      
      const { error, execute } = useRequest('/api/test')
      
      await expect(execute()).rejects.toThrow(errorMessage)
      expect(error.value).toBe(errorMessage)
    })
  })

  describe('Options', () => {
    it('executes immediately when immediate is true', async () => {
      const mockData = { message: 'immediate' }
      mockedAxios.mockResolvedValueOnce({ data: mockData })
      
      const { data } = useRequest('/api/test', { immediate: true })
      
      await nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))
      
      expect(data.value).toEqual(mockData)
    })

    it('calls onSuccess callback', async () => {
      const mockData = { message: 'success' }
      const onSuccess = vi.fn()
      mockedAxios.mockResolvedValueOnce({ data: mockData })
      
      const { execute } = useRequest('/api/test', { onSuccess })
      
      await execute()
      
      expect(onSuccess).toHaveBeenCalledWith(mockData)
    })

    it('calls onError callback', async () => {
      const errorMessage = 'Request failed'
      const onError = vi.fn()
      mockedAxios.mockRejectedValueOnce(new Error(errorMessage))
      
      const { execute } = useRequest('/api/test', { onError })
      
      await expect(execute()).rejects.toThrow()
      expect(onError).toHaveBeenCalledWith(errorMessage)
    })

    it('transforms data when transformer is provided', async () => {
      const mockData = { items: [1, 2, 3] }
      const transformedData = { count: 3 }
      const transform = vi.fn().mockReturnValue(transformedData)
      mockedAxios.mockResolvedValueOnce({ data: mockData })
      
      const { data, execute } = useRequest('/api/test', { transform })
      
      await execute()
      
      expect(transform).toHaveBeenCalledWith(mockData)
      expect(data.value).toEqual(transformedData)
    })

    it('shows error message when errorMessage is true', async () => {
      const errorMessage = 'Request failed'
      mockedAxios.mockRejectedValueOnce(new Error(errorMessage))
      
      const { execute } = useRequest('/api/test', { errorMessage: true })
      
      await expect(execute()).rejects.toThrow()
      expect(ElMessage.error).toHaveBeenCalledWith(errorMessage)
    })

    it('does not show error message when errorMessage is false', async () => {
      const errorMessage = 'Request failed'
      mockedAxios.mockRejectedValueOnce(new Error(errorMessage))
      
      const { execute } = useRequest('/api/test', { errorMessage: false })
      
      await expect(execute()).rejects.toThrow()
      expect(ElMessage.error).not.toHaveBeenCalled()
    })
  })

  describe('API Response Format', () => {
    it('handles standard API response format', async () => {
      const responseData = { message: 'success' }
      const apiResponse = {
        code: 200,
        success: true,
        data: responseData,
        message: 'OK'
      }
      mockedAxios.mockResolvedValueOnce({ data: apiResponse })
      
      const { data, execute } = useRequest('/api/test')
      
      await execute()
      
      expect(data.value).toEqual(responseData)
    })

    it('handles API error response', async () => {
      const apiResponse = {
        code: 400,
        success: false,
        data: null,
        message: 'Bad Request'
      }
      mockedAxios.mockResolvedValueOnce({ data: apiResponse })
      
      const { execute } = useRequest('/api/test')
      
      await expect(execute()).rejects.toThrow('Bad Request')
    })

    it('handles plain data response', async () => {
      const plainData = [1, 2, 3]
      mockedAxios.mockResolvedValueOnce({ data: plainData })
      
      const { data, execute } = useRequest('/api/test')
      
      await execute()
      
      expect(data.value).toEqual(plainData)
    })
  })

  describe('Reset Functionality', () => {
    it('resets state to initial values', async () => {
      const mockData = { message: 'test' }
      mockedAxios.mockResolvedValueOnce({ data: mockData })
      
      const { data, loading, error, execute, reset } = useRequest('/api/test')
      
      await execute()
      expect(data.value).toEqual(mockData)
      
      reset()
      
      expect(data.value).toBeNull()
      expect(loading.value).toBe(false)
      expect(error.value).toBeNull()
    })
  })

  describe('Dynamic URL', () => {
    it('uses reactive URL', async () => {
      const url = ref('/api/initial')
      const mockData = { message: 'updated' }
      mockedAxios.mockResolvedValueOnce({ data: mockData })
      
      const { execute } = useRequest(url)
      
      url.value = '/api/updated'
      await execute()
      
      expect(mockedAxios).toHaveBeenCalledWith(
        expect.objectContaining({
          url: '/api/updated'
        })
      )
    })
  })

  describe('Error Handling', () => {
    it('handles network errors', async () => {
      const networkError = new Error('Network Error')
      ;(networkError as any).code = 'NETWORK_ERROR'
      mockedAxios.mockRejectedValueOnce(networkError)
      
      const { error, execute } = useRequest('/api/test')
      
      await expect(execute()).rejects.toThrow()
      expect(error.value).toBe('网络连接失败，请检查网络设置')
    })

    it('handles timeout errors', async () => {
      const timeoutError = new Error('timeout of 1000ms exceeded')
      ;(timeoutError as any).code = 'ECONNABORTED'
      mockedAxios.mockRejectedValueOnce(timeoutError)
      
      const { error, execute } = useRequest('/api/test')
      
      await expect(execute()).rejects.toThrow()
      expect(error.value).toBe('请求超时，请稍后重试')
    })

    it('handles HTTP error status codes', async () => {
      const httpError = new Error('Request failed')
      ;(httpError as any).response = {
        status: 404,
        data: { message: 'Not Found' }
      }
      mockedAxios.mockRejectedValueOnce(httpError)
      
      const { error, execute } = useRequest('/api/test')
      
      await expect(execute()).rejects.toThrow()
      expect(error.value).toBe('Not Found')
    })
  })
})

describe('HTTP Method Shortcuts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('useGet makes GET request', async () => {
    const mockData = { message: 'get success' }
    mockedAxios.mockResolvedValueOnce({ data: mockData })
    
    const { execute } = useGet('/api/test')
    await execute()
    
    expect(mockedAxios).toHaveBeenCalledWith(
      expect.objectContaining({
        method: 'GET'
      })
    )
  })

  it('usePost makes POST request', async () => {
    const mockData = { message: 'post success' }
    const postData = { name: 'test' }
    mockedAxios.mockResolvedValueOnce({ data: mockData })
    
    const { execute } = usePost('/api/test')
    await execute({ data: postData })
    
    expect(mockedAxios).toHaveBeenCalledWith(
      expect.objectContaining({
        method: 'POST',
        data: postData
      })
    )
  })
})

describe('usePaginatedRequest', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('initializes with default pagination', () => {
    const { pagination } = usePaginatedRequest('/api/list')
    
    expect(pagination.value.page).toBe(1)
    expect(pagination.value.pageSize).toBe(20)
    expect(pagination.value.total).toBe(0)
  })

  it('updates pagination from response', async () => {
    const mockResponse = {
      list: [1, 2, 3],
      total: 100,
      page: 2,
      pageSize: 10
    }
    mockedAxios.mockResolvedValueOnce({ data: mockResponse })
    
    const { pagination, execute } = usePaginatedRequest('/api/list')
    
    await execute()
    
    expect(pagination.value.total).toBe(100)
    expect(pagination.value.page).toBe(2)
    expect(pagination.value.pageSize).toBe(10)
  })

  it('loads specific page', async () => {
    const mockResponse = { list: [], total: 0 }
    mockedAxios.mockResolvedValueOnce({ data: mockResponse })
    
    const { loadPage } = usePaginatedRequest('/api/list')
    
    await loadPage(3)
    
    expect(mockedAxios).toHaveBeenCalledWith(
      expect.objectContaining({
        params: expect.objectContaining({
          page: 3
        })
      })
    )
  })

  it('changes page size and resets to first page', async () => {
    const mockResponse = { list: [], total: 0 }
    mockedAxios.mockResolvedValueOnce({ data: mockResponse })
    
    const { changePageSize, pagination } = usePaginatedRequest('/api/list')
    
    // Set to page 3 first
    pagination.value.page = 3
    
    await changePageSize(50)
    
    expect(pagination.value.page).toBe(1)
    expect(pagination.value.pageSize).toBe(50)
  })
})