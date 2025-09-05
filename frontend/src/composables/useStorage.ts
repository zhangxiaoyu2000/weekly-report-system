/**
 * useStorage composable for localStorage and sessionStorage
 */

import { ref, watch, type Ref } from 'vue'
import type { UseStorageReturn, StorageOptions } from '@/types'

export function useStorage<T>(
  key: string,
  defaultValue: T,
  storage: Storage = localStorage,
  options: StorageOptions = {}
): UseStorageReturn<T> {
  const {
    prefix = '',
    suffix = '',
    serializer = {
      read: (value: string) => {
        try {
          return JSON.parse(value)
        } catch {
          return value
        }
      },
      write: (value: any) => JSON.stringify(value)
    }
  } = options

  const fullKey = `${prefix}${key}${suffix}`

  // Read initial value
  const read = (): T => {
    try {
      const item = storage.getItem(fullKey)
      if (item === null) {
        return defaultValue
      }
      return serializer.read(item)
    } catch (error) {
      console.error(`Failed to read storage key "${fullKey}":`, error)
      return defaultValue
    }
  }

  // Write value to storage
  const write = (value: T): void => {
    try {
      if (value === null || value === undefined) {
        storage.removeItem(fullKey)
      } else {
        storage.setItem(fullKey, serializer.write(value))
      }
    } catch (error) {
      console.error(`Failed to write storage key "${fullKey}":`, error)
    }
  }

  // Remove from storage
  const remove = (): void => {
    try {
      storage.removeItem(fullKey)
      value.value = defaultValue
    } catch (error) {
      console.error(`Failed to remove storage key "${fullKey}":`, error)
    }
  }

  // Clear all storage
  const clear = (): void => {
    try {
      storage.clear()
    } catch (error) {
      console.error('Failed to clear storage:', error)
    }
  }

  // Reactive value
  const value = ref(read()) as Ref<T>

  // Watch for changes and persist
  watch(
    value,
    (newValue) => {
      write(newValue)
    },
    { deep: true }
  )

  // Listen for storage changes from other tabs/windows
  if (typeof window !== 'undefined') {
    window.addEventListener('storage', (e) => {
      if (e.key === fullKey && e.newValue !== serializer.write(value.value)) {
        value.value = e.newValue ? serializer.read(e.newValue) : defaultValue
      }
    })
  }

  return {
    value,
    remove,
    clear
  }
}

// Specialized storage hooks
export function useLocalStorage<T>(
  key: string,
  defaultValue: T,
  options?: StorageOptions
): UseStorageReturn<T> {
  return useStorage(key, defaultValue, localStorage, options)
}

export function useSessionStorage<T>(
  key: string,
  defaultValue: T,
  options?: StorageOptions
): UseStorageReturn<T> {
  return useStorage(key, defaultValue, sessionStorage, options)
}

// Storage utility functions
export const storageUtils = {
  // Check if storage is available
  isStorageAvailable: (type: 'localStorage' | 'sessionStorage' = 'localStorage'): boolean => {
    try {
      const storage = window[type]
      const test = '__storage_test__'
      storage.setItem(test, test)
      storage.removeItem(test)
      return true
    } catch {
      return false
    }
  },

  // Get all keys with optional prefix filter
  getKeys: (storage: Storage = localStorage, prefix?: string): string[] => {
    const keys: string[] = []
    for (let i = 0; i < storage.length; i++) {
      const key = storage.key(i)
      if (key && (!prefix || key.startsWith(prefix))) {
        keys.push(key)
      }
    }
    return keys
  },

  // Get storage size estimate
  getStorageSize: (storage: Storage = localStorage): number => {
    let size = 0
    for (let i = 0; i < storage.length; i++) {
      const key = storage.key(i)
      if (key) {
        const value = storage.getItem(key)
        if (value) {
          size += key.length + value.length
        }
      }
    }
    return size
  },

  // Clear keys with prefix
  clearWithPrefix: (storage: Storage = localStorage, prefix: string): void => {
    const keysToRemove = storageUtils.getKeys(storage, prefix)
    keysToRemove.forEach(key => storage.removeItem(key))
  },

  // Export storage data
  exportData: (storage: Storage = localStorage, prefix?: string): Record<string, any> => {
    const data: Record<string, any> = {}
    const keys = storageUtils.getKeys(storage, prefix)
    
    keys.forEach(key => {
      const value = storage.getItem(key)
      if (value !== null) {
        try {
          data[key] = JSON.parse(value)
        } catch {
          data[key] = value
        }
      }
    })
    
    return data
  },

  // Import storage data
  importData: (
    data: Record<string, any>,
    storage: Storage = localStorage,
    overwrite: boolean = false
  ): void => {
    Object.entries(data).forEach(([key, value]) => {
      if (overwrite || storage.getItem(key) === null) {
        try {
          storage.setItem(key, typeof value === 'string' ? value : JSON.stringify(value))
        } catch (error) {
          console.error(`Failed to import key "${key}":`, error)
        }
      }
    })
  }
}

export default useStorage