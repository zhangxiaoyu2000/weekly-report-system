import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useLayoutStore } from '@/stores/layout'

// Responsive breakpoints configuration
export const BREAKPOINTS = {
  xs: 0,      // Extra small devices (phones, less than 576px)
  sm: 576,    // Small devices (landscape phones, 576px and up)
  md: 768,    // Medium devices (tablets, 768px and up)
  lg: 992,    // Large devices (desktops, 992px and up)
  xl: 1200,   // Extra large devices (large desktops, 1200px and up)
  xxl: 1400   // Extra extra large devices (larger desktops, 1400px and up)
}

// Layout breakpoints (matching the system requirements)
export const LAYOUT_BREAKPOINTS = {
  mobile: 768,    // < 768px
  tablet: 1024,   // 768px - 1024px
  desktop: 1024   // > 1024px
}

export function useResponsive() {
  const layoutStore = useLayoutStore()
  
  // Reactive screen width
  const screenWidth = ref(window.innerWidth)
  const screenHeight = ref(window.innerHeight)
  
  // Device type detection
  const isMobile = computed(() => screenWidth.value < LAYOUT_BREAKPOINTS.mobile)
  const isTablet = computed(() => 
    screenWidth.value >= LAYOUT_BREAKPOINTS.mobile && 
    screenWidth.value < LAYOUT_BREAKPOINTS.desktop
  )
  const isDesktop = computed(() => screenWidth.value >= LAYOUT_BREAKPOINTS.desktop)
  
  // Detailed breakpoint detection
  const isXs = computed(() => screenWidth.value < BREAKPOINTS.sm)
  const isSm = computed(() => 
    screenWidth.value >= BREAKPOINTS.sm && screenWidth.value < BREAKPOINTS.md
  )
  const isMd = computed(() => 
    screenWidth.value >= BREAKPOINTS.md && screenWidth.value < BREAKPOINTS.lg
  )
  const isLg = computed(() => 
    screenWidth.value >= BREAKPOINTS.lg && screenWidth.value < BREAKPOINTS.xl
  )
  const isXl = computed(() => 
    screenWidth.value >= BREAKPOINTS.xl && screenWidth.value < BREAKPOINTS.xxl
  )
  const isXxl = computed(() => screenWidth.value >= BREAKPOINTS.xxl)
  
  // Orientation
  const isLandscape = computed(() => screenWidth.value > screenHeight.value)
  const isPortrait = computed(() => screenWidth.value <= screenHeight.value)
  
  // Device capabilities
  const isTouchDevice = computed(() => 'ontouchstart' in window || navigator.maxTouchPoints > 0)
  const supportsHover = computed(() => window.matchMedia('(hover: hover)').matches)
  
  // Responsive columns for grids
  const getResponsiveColumns = computed(() => {
    if (isXs.value) return 1
    if (isSm.value) return 2
    if (isMd.value) return 3
    if (isLg.value) return 4
    if (isXl.value) return 5
    return 6
  })
  
  // Responsive sidebar behavior
  const shouldCollapseSidebar = computed(() => isMobile.value || isTablet.value)
  
  // Container padding
  const containerPadding = computed(() => {
    if (isMobile.value) return '16px'
    if (isTablet.value) return '24px'
    return '32px'
  })
  
  // Component sizes
  const buttonSize = computed(() => {
    if (isMobile.value) return 'small'
    return 'default'
  })
  
  const inputSize = computed(() => {
    if (isMobile.value) return 'small'
    return 'default'
  })
  
  const tableSize = computed(() => {
    if (isMobile.value) return 'small'
    return 'default'
  })

  // Update screen dimensions
  const updateScreenSize = () => {
    screenWidth.value = window.innerWidth
    screenHeight.value = window.innerHeight
    
    // Update layout store
    if (isMobile.value) {
      layoutStore.setDevice('mobile')
    } else if (isTablet.value) {
      layoutStore.setDevice('tablet')
    } else {
      layoutStore.setDevice('desktop')
    }
  }

  // Debounced resize handler
  let resizeTimer = null
  const handleResize = () => {
    if (resizeTimer) {
      clearTimeout(resizeTimer)
    }
    resizeTimer = setTimeout(updateScreenSize, 150)
  }

  // Media query matcher
  const matchMedia = (query) => {
    const mediaQuery = ref(window.matchMedia(query))
    const matches = ref(mediaQuery.value.matches)
    
    const handler = (e) => {
      matches.value = e.matches
    }
    
    onMounted(() => {
      mediaQuery.value.addEventListener('change', handler)
    })
    
    onUnmounted(() => {
      mediaQuery.value.removeEventListener('change', handler)
    })
    
    return matches
  }

  // Specific media queries
  const prefersDark = matchMedia('(prefers-color-scheme: dark)')
  const prefersReducedMotion = matchMedia('(prefers-reduced-motion: reduce)')
  
  // Lifecycle
  onMounted(() => {
    updateScreenSize()
    window.addEventListener('resize', handleResize)
    window.addEventListener('orientationchange', handleResize)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    window.removeEventListener('orientationchange', handleResize)
    if (resizeTimer) {
      clearTimeout(resizeTimer)
    }
  })

  return {
    // Screen dimensions
    screenWidth,
    screenHeight,
    
    // Device types
    isMobile,
    isTablet,
    isDesktop,
    
    // Breakpoint detection
    isXs,
    isSm,
    isMd,
    isLg,
    isXl,
    isXxl,
    
    // Orientation
    isLandscape,
    isPortrait,
    
    // Device capabilities
    isTouchDevice,
    supportsHover,
    
    // Responsive values
    getResponsiveColumns,
    shouldCollapseSidebar,
    containerPadding,
    buttonSize,
    inputSize,
    tableSize,
    
    // Media queries
    prefersDark,
    prefersReducedMotion,
    matchMedia,
    
    // Utility
    updateScreenSize
  }
}

// Composable for responsive classes
export function useResponsiveClasses() {
  const responsive = useResponsive()
  
  const responsiveClasses = computed(() => ({
    'is-mobile': responsive.isMobile.value,
    'is-tablet': responsive.isTablet.value,
    'is-desktop': responsive.isDesktop.value,
    'is-xs': responsive.isXs.value,
    'is-sm': responsive.isSm.value,
    'is-md': responsive.isMd.value,
    'is-lg': responsive.isLg.value,
    'is-xl': responsive.isXl.value,
    'is-xxl': responsive.isXxl.value,
    'is-landscape': responsive.isLandscape.value,
    'is-portrait': responsive.isPortrait.value,
    'is-touch': responsive.isTouchDevice.value,
    'supports-hover': responsive.supportsHover.value,
    'prefers-dark': responsive.prefersDark.value,
    'prefers-reduced-motion': responsive.prefersReducedMotion.value
  }))
  
  return {
    responsiveClasses
  }
}

// Composable for responsive values
export function useResponsiveValue(values) {
  const responsive = useResponsive()
  
  const getValue = computed(() => {
    if (typeof values === 'function') {
      return values(responsive)
    }
    
    if (typeof values === 'object') {
      if (responsive.isXs.value && values.xs !== undefined) return values.xs
      if (responsive.isSm.value && values.sm !== undefined) return values.sm
      if (responsive.isMd.value && values.md !== undefined) return values.md
      if (responsive.isLg.value && values.lg !== undefined) return values.lg
      if (responsive.isXl.value && values.xl !== undefined) return values.xl
      if (responsive.isXxl.value && values.xxl !== undefined) return values.xxl
      
      // Fallback to mobile/tablet/desktop
      if (responsive.isMobile.value && values.mobile !== undefined) return values.mobile
      if (responsive.isTablet.value && values.tablet !== undefined) return values.tablet
      if (responsive.isDesktop.value && values.desktop !== undefined) return values.desktop
      
      // Default fallback
      return values.default || values
    }
    
    return values
  })
  
  return getValue
}

// Helper function to get current device info
export function getDeviceInfo() {
  const responsive = useResponsive()
  
  return {
    type: responsive.isMobile.value ? 'mobile' : 
          responsive.isTablet.value ? 'tablet' : 'desktop',
    width: responsive.screenWidth.value,
    height: responsive.screenHeight.value,
    orientation: responsive.isLandscape.value ? 'landscape' : 'portrait',
    isTouch: responsive.isTouchDevice.value,
    supportsHover: responsive.supportsHover.value,
    breakpoint: responsive.isXs.value ? 'xs' :
                responsive.isSm.value ? 'sm' :
                responsive.isMd.value ? 'md' :
                responsive.isLg.value ? 'lg' :
                responsive.isXl.value ? 'xl' : 'xxl'
  }
}