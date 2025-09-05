/**
 * useEventBus composable for component communication
 */

import { ref, onUnmounted } from 'vue'
import type { UseEventBusReturn, EventHandler } from '@/types'

// Global event bus
const eventBus = new Map<string, Set<EventHandler>>()

export function useEventBus(): UseEventBusReturn {
  const registeredHandlers = ref<Array<{ event: string; handler: EventHandler }>>([])

  const on = <T = any>(event: string, handler: EventHandler<T>) => {
    if (!eventBus.has(event)) {
      eventBus.set(event, new Set())
    }
    
    eventBus.get(event)!.add(handler as EventHandler)
    registeredHandlers.value.push({ event, handler: handler as EventHandler })
  }

  const off = <T = any>(event: string, handler: EventHandler<T>) => {
    const handlers = eventBus.get(event)
    if (handlers) {
      handlers.delete(handler as EventHandler)
      
      // Remove from registered handlers
      const index = registeredHandlers.value.findIndex(
        item => item.event === event && item.handler === handler
      )
      if (index > -1) {
        registeredHandlers.value.splice(index, 1)
      }
      
      // Clean up empty event
      if (handlers.size === 0) {
        eventBus.delete(event)
      }
    }
  }

  const emit = <T = any>(event: string, payload?: T) => {
    const handlers = eventBus.get(event)
    if (handlers) {
      handlers.forEach(handler => {
        try {
          handler(payload)
        } catch (error) {
          console.error(`Event handler error for event '${event}':`, error)
        }
      })
    }
  }

  const clear = (event?: string) => {
    if (event) {
      // Clear specific event
      eventBus.delete(event)
      registeredHandlers.value = registeredHandlers.value.filter(item => item.event !== event)
    } else {
      // Clear all events registered by this composable
      registeredHandlers.value.forEach(({ event, handler }) => {
        off(event, handler)
      })
      registeredHandlers.value = []
    }
  }

  // Clean up on component unmount
  onUnmounted(() => {
    clear()
  })

  return {
    on,
    off,
    emit,
    clear
  }
}

// Utility functions for common event patterns
export function useEventBusWithPattern() {
  const eventBus = useEventBus()

  // Request-response pattern
  const request = <T = any, R = any>(event: string, payload?: T, timeout = 5000): Promise<R> => {
    return new Promise((resolve, reject) => {
      const responseEvent = `${event}:response:${Date.now()}`
      const timeoutId = setTimeout(() => {
        eventBus.off(responseEvent, handleResponse)
        reject(new Error(`Request timeout for event: ${event}`))
      }, timeout)

      const handleResponse = (response: R) => {
        clearTimeout(timeoutId)
        eventBus.off(responseEvent, handleResponse)
        resolve(response)
      }

      eventBus.on(responseEvent, handleResponse)
      eventBus.emit(event, { ...payload, responseEvent })
    })
  }

  const respond = <T = any, R = any>(event: string, handler: (payload: T & { responseEvent: string }) => R | Promise<R>) => {
    eventBus.on(event, async (payload: T & { responseEvent: string }) => {
      try {
        const response = await handler(payload)
        eventBus.emit(payload.responseEvent, response)
      } catch (error) {
        eventBus.emit(payload.responseEvent, { error: error instanceof Error ? error.message : 'Unknown error' })
      }
    })
  }

  // Pub-sub pattern with namespaces
  const subscribe = <T = any>(namespace: string, event: string, handler: EventHandler<T>) => {
    const fullEvent = `${namespace}:${event}`
    eventBus.on(fullEvent, handler)
  }

  const publish = <T = any>(namespace: string, event: string, payload?: T) => {
    const fullEvent = `${namespace}:${event}`
    eventBus.emit(fullEvent, payload)
  }

  const unsubscribe = <T = any>(namespace: string, event: string, handler: EventHandler<T>) => {
    const fullEvent = `${namespace}:${event}`
    eventBus.off(fullEvent, handler)
  }

  return {
    ...eventBus,
    request,
    respond,
    subscribe,
    publish,
    unsubscribe
  }
}

// Named event bus instances
const namedEventBuses = new Map<string, UseEventBusReturn>()

export function useNamedEventBus(name: string): UseEventBusReturn {
  if (!namedEventBuses.has(name)) {
    namedEventBuses.set(name, useEventBus())
  }
  return namedEventBuses.get(name)!
}

export default useEventBus