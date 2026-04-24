import { createI18n } from 'vue-i18n'
import en from '@/locales/en'
import vi from '@/locales/vi'

const savedLocale = localStorage.getItem('locale') || 'en'

const i18n = createI18n({
  legacy: false,
  locale: savedLocale,
  fallbackLocale: 'en',
  messages: { en, vi },
})

export default i18n
