import { ref, computed, watch, readonly } from 'vue'
import type { ThemeMode } from '@/types/global'

const THEME_KEY = 'app-theme'

// Reactive theme state
const themeMode = ref<ThemeMode>('light')

// Get theme from localStorage or system preference
const getInitialTheme = (): ThemeMode => {
  const savedTheme = localStorage.getItem(THEME_KEY) as ThemeMode
  if (savedTheme && ['light', 'dark', 'auto'].includes(savedTheme)) {
    return savedTheme
  }
  return 'auto'
}

// Detect system theme preference
const getSystemTheme = (): 'light' | 'dark' => {
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
}

// Get effective theme (resolves 'auto' to actual theme)
const getEffectiveTheme = (mode: ThemeMode): 'light' | 'dark' => {
  if (mode === 'auto') {
    return getSystemTheme()
  }
  return mode
}

// Apply theme to DOM
const applyTheme = (theme: 'light' | 'dark') => {
  const root = document.documentElement
  
  if (theme === 'dark') {
    root.setAttribute('data-theme', 'dark')
    root.classList.add('dark')
  } else {
    root.setAttribute('data-theme', 'light')
    root.classList.remove('dark')
  }
}

// Initialize theme
const initializeTheme = () => {
  themeMode.value = getInitialTheme()
  const effectiveTheme = getEffectiveTheme(themeMode.value)
  applyTheme(effectiveTheme)
}

// Watch for theme changes
watch(themeMode, (newMode) => {
  const effectiveTheme = getEffectiveTheme(newMode)
  applyTheme(effectiveTheme)
  localStorage.setItem(THEME_KEY, newMode)
}, { immediate: false })

// Listen for system theme changes when in auto mode
if (typeof window !== 'undefined' && window.matchMedia) {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addListener((e) => {
    if (themeMode.value === 'auto') {
      const systemTheme = e.matches ? 'dark' : 'light'
      applyTheme(systemTheme)
    }
  })
}

export const useTheme = () => {
  // Computed properties
  const currentTheme = computed(() => themeMode.value)
  const effectiveTheme = computed(() => getEffectiveTheme(themeMode.value))
  const isDark = computed(() => effectiveTheme.value === 'dark')
  const isLight = computed(() => effectiveTheme.value === 'light')
  const isAuto = computed(() => themeMode.value === 'auto')

  // Theme options for UI
  const themeOptions = computed(() => [
    { value: 'light', label: 'Light Theme', icon: '☀️' },
    { value: 'dark', label: 'Dark Theme', icon: '🌙' },
    { value: 'auto', label: 'Follow System', icon: '🔄' }
  ] as const)

  // Methods
  const setTheme = (theme: ThemeMode) => {
    themeMode.value = theme
  }

  const toggleTheme = () => {
    const current = effectiveTheme.value
    setTheme(current === 'light' ? 'dark' : 'light')
  }

  const resetToSystem = () => {
    setTheme('auto')
  }

  // Initialize on first use
  if (!document.documentElement.hasAttribute('data-theme')) {
    initializeTheme()
  }

  return {
    // State
    currentTheme: readonly(currentTheme),
    effectiveTheme: readonly(effectiveTheme),
    isDark: readonly(isDark),
    isLight: readonly(isLight),
    isAuto: readonly(isAuto),
    themeOptions: readonly(themeOptions),

    // Methods
    setTheme,
    toggleTheme,
    resetToSystem,
    getSystemTheme,
    initializeTheme
  }
}

// Auto-initialize theme on module load
if (typeof window !== 'undefined') {
  initializeTheme()
}