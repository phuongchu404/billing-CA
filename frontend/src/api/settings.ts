import request from '@/utils/request'

export interface SettingItem {
  key: string
  value: string
  type: string   // BOOLEAN | INTEGER | STRING
  category: string
  description: string
}

export const getSettings = (category: string) =>
  request.get(`/api/v1/settings/${category}`)

export const updateSettings = (category: string, updates: Record<string, string>) =>
  request.put(`/api/v1/settings/${category}`, updates)

export const testEmail = (recipient: string) =>
  request.post('/api/v1/settings/email/test', { recipient })

export const triggerNotifications = () =>
  request.post('/api/v1/settings/notification/trigger', {})
