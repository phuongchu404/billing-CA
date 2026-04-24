import request from '@/utils/request'
import type { ApiResponse, SubscriptionSummary, TypedSubscriptionSummary, Subscription } from '@/types'

export const getSubscriptionSummary = () =>
  request.get<any, ApiResponse<SubscriptionSummary>>('/api/v1/reports/subscriptions/summary')

export const getTypedSubscriptionSummary = (subscriberType: 'INDIVIDUAL' | 'GROUP') =>
  request.get<any, ApiResponse<TypedSubscriptionSummary>>('/api/v1/reports/subscriptions/summary', {
    params: { subscriberType }
  })

export const getExpiringSoon = (days = 30, subscriberType?: 'INDIVIDUAL' | 'GROUP') =>
  request.get<any, ApiResponse<Subscription[]>>('/api/v1/reports/subscriptions/expiring-soon', {
    params: { days, ...(subscriberType ? { subscriberType } : {}) }
  })

export const getAllSubscriptions = () =>
  request.get<any, ApiResponse<Subscription[]>>('/api/v1/reports/subscriptions/all')
