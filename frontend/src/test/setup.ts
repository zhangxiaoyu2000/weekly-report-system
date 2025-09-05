/**
 * Test setup file for Vitest
 * Configures Element Plus and global test utilities
 */

import { vi } from 'vitest'
import { config } from '@vue/test-utils'
import ElementPlus from 'element-plus'

// Mock Element Plus components globally
config.global.plugins = [ElementPlus]

// Mock Element Plus global properties
config.global.mocks = {
  $message: {
    success: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
    error: vi.fn()
  },
  $msgbox: vi.fn(),
  $alert: vi.fn(),
  $confirm: vi.fn(),
  $prompt: vi.fn(),
  $notify: vi.fn(),
  $loading: vi.fn().mockReturnValue({
    close: vi.fn()
  })
}

// Mock window.matchMedia (used by responsive components)
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(), // deprecated
    removeListener: vi.fn(), // deprecated
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  }))
})

// Mock ResizeObserver (used by some components)
global.ResizeObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}))

// Mock IntersectionObserver (used by some components)
global.IntersectionObserver = vi.fn().mockImplementation(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}))

// Mock localStorage and sessionStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn(),
}

const sessionStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
  length: 0,
  key: vi.fn(),
}

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
})

Object.defineProperty(window, 'sessionStorage', {
  value: sessionStorageMock,
})

// Mock CSS.supports (used by some styling)
global.CSS = {
  supports: vi.fn().mockReturnValue(false),
}

// Mock getComputedStyle
global.getComputedStyle = vi.fn().mockReturnValue({
  getPropertyValue: vi.fn().mockReturnValue(''),
})

// Suppress console warnings during tests
const originalConsoleWarn = console.warn
console.warn = (...args: any[]) => {
  // Filter out Element Plus warnings that are not relevant to tests
  const message = args[0]
  if (typeof message === 'string' && (
    message.includes('[Element Plus]') ||
    message.includes('[Vue warn]')
  )) {
    return
  }
  originalConsoleWarn(...args)
}

// Add global test utilities
declare global {
  namespace Vi {
    interface Assertion<T> {
      toHaveBeenCalledOnceWith(...args: any[]): T
      toHaveBeenCalledExactlyOnceWith(...args: any[]): T
    }
  }
}

// Custom matchers
expect.extend({
  toHaveBeenCalledOnceWith(received, ...expected) {
    const pass = received.mock.calls.length === 1 && 
                 received.mock.calls[0].every((arg, index) => 
                   this.equals(arg, expected[index]))
    
    return {
      pass,
      message: () => pass 
        ? `expected function not to have been called once with ${this.utils.printExpected(expected)}`
        : `expected function to have been called once with ${this.utils.printExpected(expected)}, but it was called ${received.mock.calls.length} times`
    }
  },
  
  toHaveBeenCalledExactlyOnceWith(received, ...expected) {
    const pass = received.mock.calls.length === 1 && 
                 received.mock.calls[0].length === expected.length &&
                 received.mock.calls[0].every((arg, index) => 
                   this.equals(arg, expected[index]))
    
    return {
      pass,
      message: () => pass 
        ? `expected function not to have been called exactly once with ${this.utils.printExpected(expected)}`
        : `expected function to have been called exactly once with ${this.utils.printExpected(expected)}`
    }
  }
})