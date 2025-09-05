/**
 * Test utility functions and helpers
 */

import { nextTick } from 'vue'
import { mount, VueWrapper, type ComponentMountingOptions } from '@vue/test-utils'
import type { DefineComponent } from 'vue'

// Wait for DOM updates and Vue reactivity
export const waitFor = async (condition: () => boolean, timeout = 1000) => {
  const startTime = Date.now()
  
  while (!condition() && Date.now() - startTime < timeout) {
    await nextTick()
    await new Promise(resolve => setTimeout(resolve, 10))
  }
  
  if (!condition()) {
    throw new Error(`Condition not met within ${timeout}ms`)
  }
}

// Wait for element to be visible
export const waitForElement = async (
  wrapper: VueWrapper<any>, 
  selector: string, 
  timeout = 1000
) => {
  await waitFor(() => wrapper.find(selector).exists(), timeout)
}

// Wait for async component loading
export const waitForAsyncComponent = async (wrapper: VueWrapper<any>) => {
  await nextTick()
  await new Promise(resolve => setTimeout(resolve, 0))
}

// Simulate user input
export const simulateInput = async (
  wrapper: VueWrapper<any>, 
  selector: string, 
  value: string
) => {
  const input = wrapper.find(selector)
  await input.setValue(value)
  await input.trigger('input')
  await input.trigger('change')
  await nextTick()
}

// Simulate form submission
export const simulateFormSubmit = async (wrapper: VueWrapper<any>, formSelector = 'form') => {
  const form = wrapper.find(formSelector)
  await form.trigger('submit.prevent')
  await nextTick()
}

// Create a test wrapper with common Element Plus setup
export const createTestWrapper = (
  component: DefineComponent,
  options: ComponentMountingOptions<any> = {}
) => {
  const defaultOptions: ComponentMountingOptions<any> = {
    global: {
      stubs: {
        // Stub heavy components for faster tests
        'el-table': true,
        'el-dialog': true,
        'el-date-picker': true,
        'el-upload': true,
        ...options.global?.stubs
      }
    },
    ...options
  }
  
  return mount(component, defaultOptions)
}

// Mock API response
export const mockApiResponse = <T = any>(data: T, delay = 0) => {
  return new Promise<{ data: T }>((resolve) => {
    setTimeout(() => {
      resolve({ data })
    }, delay)
  })
}

// Mock API error
export const mockApiError = (message = 'API Error', status = 500, delay = 0) => {
  return new Promise((_, reject) => {
    setTimeout(() => {
      const error = new Error(message) as any
      error.response = {
        status,
        data: { message }
      }
      reject(error)
    }, delay)
  })
}

// Mock file for upload testing
export const createMockFile = (
  name = 'test.txt',
  content = 'test content',
  type = 'text/plain'
) => {
  return new File([content], name, { type })
}

// Assert element text content
export const expectText = (wrapper: VueWrapper<any>, selector: string, expected: string) => {
  const element = wrapper.find(selector)
  expect(element.exists()).toBe(true)
  expect(element.text()).toBe(expected)
}

// Assert element has class
export const expectClass = (wrapper: VueWrapper<any>, selector: string, className: string) => {
  const element = wrapper.find(selector)
  expect(element.exists()).toBe(true)
  expect(element.classes()).toContain(className)
}

// Assert element visibility
export const expectVisible = (wrapper: VueWrapper<any>, selector: string) => {
  const element = wrapper.find(selector)
  expect(element.exists()).toBe(true)
  expect(element.isVisible()).toBe(true)
}

export const expectHidden = (wrapper: VueWrapper<any>, selector: string) => {
  const element = wrapper.find(selector)
  if (element.exists()) {
    expect(element.isVisible()).toBe(false)
  }
}

// Assert form field value
export const expectFieldValue = (
  wrapper: VueWrapper<any>, 
  selector: string, 
  expected: string | number
) => {
  const field = wrapper.find(selector)
  expect(field.exists()).toBe(true)
  expect((field.element as HTMLInputElement).value).toBe(String(expected))
}

// Mock console methods
export const mockConsole = () => {
  const originalConsole = { ...console }
  const consoleMock = {
    log: vi.fn(),
    warn: vi.fn(),
    error: vi.fn(),
    info: vi.fn()
  }
  
  Object.assign(console, consoleMock)
  
  return {
    consoleMock,
    restore: () => {
      Object.assign(console, originalConsole)
    }
  }
}

// Create reactive ref for testing
export const createTestRef = <T>(initialValue: T) => {
  return ref(initialValue)
}

// Create test store state
export const createTestStore = (initialState: Record<string, any> = {}) => {
  return reactive(initialState)
}

// Assert emission
export const expectEmitted = (
  wrapper: VueWrapper<any>, 
  eventName: string, 
  expectedPayload?: any
) => {
  const emitted = wrapper.emitted(eventName)
  expect(emitted).toBeTruthy()
  
  if (expectedPayload !== undefined) {
    expect(emitted![emitted!.length - 1]).toEqual([expectedPayload])
  }
}

// Assert not emitted
export const expectNotEmitted = (wrapper: VueWrapper<any>, eventName: string) => {
  const emitted = wrapper.emitted(eventName)
  expect(emitted).toBeFalsy()
}

// Test data generators
export const generateTestData = {
  user: (overrides: Record<string, any> = {}) => ({
    id: 1,
    name: 'Test User',
    email: 'test@example.com',
    role: 'user',
    createdAt: '2024-01-01T00:00:00Z',
    ...overrides
  }),
  
  tableData: (count = 5) => 
    Array.from({ length: count }, (_, index) => ({
      id: index + 1,
      name: `Item ${index + 1}`,
      status: index % 2 === 0 ? 'active' : 'inactive',
      value: (index + 1) * 10
    })),
  
  formData: (overrides: Record<string, any> = {}) => ({
    name: '',
    email: '',
    phone: '',
    description: '',
    ...overrides
  }),
  
  pagination: (overrides: Record<string, any> = {}) => ({
    page: 1,
    pageSize: 20,
    total: 100,
    ...overrides
  })
}

export default {
  waitFor,
  waitForElement,
  waitForAsyncComponent,
  simulateInput,
  simulateFormSubmit,
  createTestWrapper,
  mockApiResponse,
  mockApiError,
  createMockFile,
  expectText,
  expectClass,
  expectVisible,
  expectHidden,
  expectFieldValue,
  mockConsole,
  createTestRef,
  createTestStore,
  expectEmitted,
  expectNotEmitted,
  generateTestData
}