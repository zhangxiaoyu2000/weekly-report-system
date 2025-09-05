/**
 * useBreakpoint composable for responsive design
 */

import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { UseBreakpointReturn, Breakpoints } from '@/types'

// Default breakpoints (Element Plus breakpoints)
const defaultBreakpoints: Breakpoints = {
  xs: 0,
  sm: 768,
  md: 992,
  lg: 1200,
  xl: 1920,
  xxl: 2560
}

export function useBreakpoint(customBreakpoints?: Partial<Breakpoints>): UseBreakpointReturn {
  const breakpoints = { ...defaultBreakpoints, ...customBreakpoints }
  const windowWidth = ref(0)

  // Update window width
  const updateWidth = () => {
    windowWidth.value = window.innerWidth
  }

  // Get current breakpoint
  const current = computed(() => {
    const width = windowWidth.value
    
    if (width >= breakpoints.xxl) return 'xxl'
    if (width >= breakpoints.xl) return 'xl'
    if (width >= breakpoints.lg) return 'lg'
    if (width >= breakpoints.md) return 'md'
    if (width >= breakpoints.sm) return 'sm'
    return 'xs'
  })

  // Breakpoint checkers
  const xs = computed(() => windowWidth.value >= breakpoints.xs && windowWidth.value < breakpoints.sm)
  const sm = computed(() => windowWidth.value >= breakpoints.sm && windowWidth.value < breakpoints.md)
  const md = computed(() => windowWidth.value >= breakpoints.md && windowWidth.value < breakpoints.lg)
  const lg = computed(() => windowWidth.value >= breakpoints.lg && windowWidth.value < breakpoints.xl)
  const xl = computed(() => windowWidth.value >= breakpoints.xl && windowWidth.value < breakpoints.xxl)
  const xxl = computed(() => windowWidth.value >= breakpoints.xxl)

  // Greater than breakpoint
  const greater = (breakpoint: string) => {
    return computed(() => {
      const breakpointValue = breakpoints[breakpoint as keyof Breakpoints]
      return windowWidth.value >= breakpointValue
    })
  }

  // Smaller than breakpoint
  const smaller = (breakpoint: string) => {
    return computed(() => {
      const breakpointValue = breakpoints[breakpoint as keyof Breakpoints]
      return windowWidth.value < breakpointValue
    })
  }

  // Between breakpoints
  const between = (min: string, max: string) => {
    return computed(() => {
      const minValue = breakpoints[min as keyof Breakpoints]
      const maxValue = breakpoints[max as keyof Breakpoints]
      return windowWidth.value >= minValue && windowWidth.value < maxValue
    })
  }

  // Lifecycle
  onMounted(() => {
    updateWidth()
    window.addEventListener('resize', updateWidth)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateWidth)
  })

  return {
    current,
    xs,
    sm,
    md,
    lg,
    xl,
    xxl,
    greater,
    smaller,
    between
  }
}

// Utility functions for responsive design
export const responsiveUtils = {
  // Get responsive value based on current breakpoint
  getResponsiveValue: <T>(
    values: Partial<Record<keyof Breakpoints, T>>,
    current: string,
    fallback: T
  ): T => {
    // Try exact match first
    if (values[current as keyof Breakpoints] !== undefined) {
      return values[current as keyof Breakpoints]!
    }

    // Fallback to smaller breakpoints
    const breakpointOrder: (keyof Breakpoints)[] = ['xxl', 'xl', 'lg', 'md', 'sm', 'xs']
    const currentIndex = breakpointOrder.indexOf(current as keyof Breakpoints)
    
    for (let i = currentIndex + 1; i < breakpointOrder.length; i++) {
      const breakpoint = breakpointOrder[i]
      if (values[breakpoint] !== undefined) {
        return values[breakpoint]!
      }
    }

    return fallback
  },

  // Convert Element Plus responsive props to CSS classes
  getResponsiveClasses: (props: Partial<Record<keyof Breakpoints, number | { span?: number; offset?: number }>>) => {
    const classes: string[] = []
    
    Object.entries(props).forEach(([breakpoint, value]) => {
      if (typeof value === 'number') {
        classes.push(`el-col-${breakpoint}-${value}`)
      } else if (typeof value === 'object' && value !== null) {
        if (value.span !== undefined) {
          classes.push(`el-col-${breakpoint}-${value.span}`)
        }
        if (value.offset !== undefined) {
          classes.push(`el-col-${breakpoint}-offset-${value.offset}`)
        }
      }
    })
    
    return classes
  },

  // Media query helpers
  createMediaQuery: (breakpoint: string, breakpoints: Breakpoints = defaultBreakpoints) => {
    const value = breakpoints[breakpoint as keyof Breakpoints]
    return `(min-width: ${value}px)`
  },

  createMaxMediaQuery: (breakpoint: string, breakpoints: Breakpoints = defaultBreakpoints) => {
    const value = breakpoints[breakpoint as keyof Breakpoints]
    return `(max-width: ${value - 1}px)`
  },

  createBetweenMediaQuery: (
    min: string,
    max: string,
    breakpoints: Breakpoints = defaultBreakpoints
  ) => {
    const minValue = breakpoints[min as keyof Breakpoints]
    const maxValue = breakpoints[max as keyof Breakpoints]
    return `(min-width: ${minValue}px) and (max-width: ${maxValue - 1}px)`
  }
}

// Hook for matching media queries
export function useMediaQuery(query: string) {
  const matches = ref(false)
  let mediaQuery: MediaQueryList

  const updateMatches = (event: MediaQueryListEvent) => {
    matches.value = event.matches
  }

  onMounted(() => {
    mediaQuery = window.matchMedia(query)
    matches.value = mediaQuery.matches
    
    // Use the newer addEventListener if available
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', updateMatches)
    } else {
      // Fallback for older browsers
      mediaQuery.addListener(updateMatches)
    }
  })

  onUnmounted(() => {
    if (mediaQuery) {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', updateMatches)
      } else {
        // Fallback for older browsers
        mediaQuery.removeListener(updateMatches)
      }
    }
  })

  return matches
}

// Presets for common responsive patterns
export function useMobileFirst() {
  const breakpoint = useBreakpoint()
  
  return {
    isMobile: computed(() => breakpoint.xs.value),
    isTablet: computed(() => breakpoint.sm.value || breakpoint.md.value),
    isDesktop: computed(() => breakpoint.lg.value || breakpoint.xl.value || breakpoint.xxl.value),
    isMobileOrTablet: computed(() => !breakpoint.lg.value && !breakpoint.xl.value && !breakpoint.xxl.value)
  }
}

export default useBreakpoint