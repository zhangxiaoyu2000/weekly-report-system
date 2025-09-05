// Store exports for easier importing
export { useUserStore } from './user'
export { useReportStore } from './report'
export { useLayoutStore } from './layout'
export { useTabsStore } from './tabs'
export { useAppStore } from './app'
export { useSettingsStore } from './settings'

// Store initialization helper
export function initializeStores() {
  // Import stores dynamically to avoid circular dependencies
  import('./user').then(({ useUserStore }) => {
    const userStore = useUserStore()
    userStore.initializeUser()
  })

  import('./layout').then(({ useLayoutStore }) => {
    const layoutStore = useLayoutStore()
    layoutStore.initLayout()
  })

  import('./tabs').then(({ useTabsStore }) => {
    const tabsStore = useTabsStore()
    tabsStore.initTabs()
  })

  import('./app').then(({ useAppStore }) => {
    const appStore = useAppStore()
    appStore.initializeApp()
  })

  import('./settings').then(({ useSettingsStore }) => {
    const settingsStore = useSettingsStore()
    settingsStore.initializeSettings()
  })
}

// Store reset helper for testing or logout
export function resetAllStores() {
  import('./user').then(({ useUserStore }) => {
    const userStore = useUserStore()
    userStore.resetUser()
  })

  import('./tabs').then(({ useTabsStore }) => {
    const tabsStore = useTabsStore()
    tabsStore.clearTabs()
  })

  import('./app').then(({ useAppStore }) => {
    const appStore = useAppStore()
    appStore.resetApp()
  })

  import('./settings').then(({ useSettingsStore }) => {
    const settingsStore = useSettingsStore()
    settingsStore.clearSettings()
  })
}

// Store persistence helper
export function persistStores() {
  import('./user').then(({ useUserStore }) => {
    const userStore = useUserStore()
    // User store auto-persists via localStorage
  })

  import('./tabs').then(({ useTabsStore }) => {
    const tabsStore = useTabsStore()
    tabsStore.saveTabsToStorage()
  })

  import('./app').then(({ useAppStore }) => {
    const appStore = useAppStore()
    appStore.saveNotifications()
  })

  import('./settings').then(({ useSettingsStore }) => {
    const settingsStore = useSettingsStore()
    // Settings store auto-persists
  })
}