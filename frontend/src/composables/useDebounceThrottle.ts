/**
 * useDebounce and useThrottle composables
 */

import { ref, computed, onUnmounted } from 'vue'
import type { UseDebounceReturn, UseThrottleReturn, DebounceOptions, ThrottleOptions } from '@/types'

export function useDebounce<T extends (...args: any[]) => any>(
  fn: T,
  options: DebounceOptions = {}
): UseDebounceReturn<T> {
  const { delay = 300, immediate = false, maxWait } = options
  
  let timeoutId: NodeJS.Timeout | null = null
  let maxTimeoutId: NodeJS.Timeout | null = null
  let lastCallTime = 0
  let lastInvokeTime = 0
  let pending = ref(false)

  const debounced = ((...args: Parameters<T>) => {
    const currentTime = Date.now()
    lastCallTime = currentTime

    const invokeFunction = () => {
      pending.value = false
      lastInvokeTime = Date.now()
      return fn(...args)
    }

    const shouldCallNow = immediate && !timeoutId

    // Clear existing timeout
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }

    // Set pending state
    pending.value = true

    if (shouldCallNow) {
      return invokeFunction()
    }

    // Handle maxWait
    if (maxWait && !maxTimeoutId) {
      maxTimeoutId = setTimeout(() => {
        if (pending.value) {
          clearTimeout(timeoutId!)
          timeoutId = null
          maxTimeoutId = null
          invokeFunction()
        }
      }, maxWait)
    }

    // Set debounce timeout
    timeoutId = setTimeout(() => {
      timeoutId = null
      if (maxTimeoutId) {
        clearTimeout(maxTimeoutId)
        maxTimeoutId = null
      }
      if (pending.value) {
        invokeFunction()
      }
    }, delay)
  }) as T

  const cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
    if (maxTimeoutId) {
      clearTimeout(maxTimeoutId)
      maxTimeoutId = null
    }
    pending.value = false
  }

  const flush = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
      if (maxTimeoutId) {
        clearTimeout(maxTimeoutId)
        maxTimeoutId = null
      }
      if (pending.value) {
        pending.value = false
        return fn(...([] as any))
      }
    }
  }

  const isPending = computed(() => pending.value)

  // Clean up on unmount
  onUnmounted(cancel)

  return {
    debounced,
    cancel,
    flush,
    isPending
  }
}

export function useThrottle<T extends (...args: any[]) => any>(
  fn: T,
  options: ThrottleOptions = {}
): UseThrottleReturn<T> {
  const { delay = 300, leading = true, trailing = true } = options
  
  let timeoutId: NodeJS.Timeout | null = null
  let lastCallTime = 0
  let lastInvokeTime = 0
  let lastArgs: Parameters<T>

  const throttled = ((...args: Parameters<T>) => {
    const currentTime = Date.now()
    lastCallTime = currentTime
    lastArgs = args

    const timeSinceLastInvoke = currentTime - lastInvokeTime
    const shouldCallLeading = leading && timeSinceLastInvoke >= delay
    const shouldCallTrailing = trailing && !timeoutId

    const invokeFunction = () => {
      lastInvokeTime = Date.now()
      return fn(...args)
    }

    if (shouldCallLeading) {
      return invokeFunction()
    }

    if (shouldCallTrailing) {
      timeoutId = setTimeout(() => {
        timeoutId = null
        const timeSinceLastCall = Date.now() - lastCallTime
        
        if (timeSinceLastCall < delay && trailing) {
          // Call trailing if there were calls during throttle period
          invokeFunction()
        }
      }, delay - timeSinceLastInvoke)
    }
  }) as T

  const cancel = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }

  const flush = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
      return fn(...lastArgs)
    }
  }

  // Clean up on unmount
  onUnmounted(cancel)

  return {
    throttled,
    cancel,
    flush
  }
}

export default { useDebounce, useThrottle }