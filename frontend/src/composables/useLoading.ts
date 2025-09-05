/**
 * useLoading composable for managing loading states
 */

import { ref, computed, reactive } from 'vue'
import type { UseLoadingReturn } from '@/types'

export function useLoading(): UseLoadingReturn {
  const globalLoading = ref(false)
  const localLoading = reactive<Record<string, boolean>>({})

  // Computed loading state
  const loading = computed(() => {
    return globalLoading.value || Object.values(localLoading).some(Boolean)
  })

  // Set loading state
  const setLoading = (loading: boolean, scope?: string) => {
    if (scope) {
      if (loading) {
        localLoading[scope] = true
      } else {
        delete localLoading[scope]
      }
    } else {
      globalLoading.value = loading
    }
  }

  // Clear loading state
  const clearLoading = (scope?: string) => {
    if (scope) {
      delete localLoading[scope]
    } else {
      globalLoading.value = false
      Object.keys(localLoading).forEach(key => {
        delete localLoading[key]
      })
    }
  }

  // Execute function with loading state
  const withLoading = async <T>(
    fn: () => Promise<T>,
    scope?: string
  ): Promise<T> => {
    setLoading(true, scope)
    try {
      return await fn()
    } finally {
      setLoading(false, scope)
    }
  }

  return {
    loading,
    setLoading,
    clearLoading,
    withLoading
  }
}

export default useLoading