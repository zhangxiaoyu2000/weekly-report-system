import { createI18n } from 'vue-i18n'
import type { SupportedLocale } from '@/types/global'

// Import locale messages
import en from './locales/en.json'
import zh from './locales/zh.json'

const messages = {
  en,
  zh
}

// Get default locale from browser or localStorage
const getDefaultLocale = (): SupportedLocale => {
  const savedLocale = localStorage.getItem('app-locale') as SupportedLocale
  if (savedLocale && messages[savedLocale]) {
    return savedLocale
  }

  const browserLocale = navigator.language.split('-')[0] as SupportedLocale
  if (messages[browserLocale]) {
    return browserLocale
  }

  return 'zh' // fallback to Chinese
}

export const i18n = createI18n({
  legacy: false,
  locale: getDefaultLocale(),
  fallbackLocale: 'zh',
  messages,
  globalInjection: true,
  silentTranslationWarn: true,
  silentFallbackWarn: true
})

// Helper function to set locale
export const setLocale = (locale: SupportedLocale) => {
  i18n.global.locale.value = locale
  localStorage.setItem('app-locale', locale)
  document.documentElement.lang = locale
}

// Helper function to get current locale
export const getCurrentLocale = (): SupportedLocale => {
  return i18n.global.locale.value as SupportedLocale
}

// Helper function to get available locales
export const getAvailableLocales = (): SupportedLocale[] => {
  return Object.keys(messages) as SupportedLocale[]
}

export default i18n