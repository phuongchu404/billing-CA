import request from '@/utils/request'
import type { ApiResponse } from '@/types'

export interface GroupStatsSummary {
  activePartners: number
  newCts: number
  signings: number
  expiringSoon: number
  newCtsPct: number
  signingsPct: number
}

export interface CertDataByType {
  individual: number[]
  organization: number[]
  individualOfOrg: number[]
}

export interface GrowthItem {
  current: number
  prev: number
  growth: number
}

export interface RatioItem {
  name: string
  individual: number
  organization: number
  individualOfOrg: number
}

export interface GroupReportResponse {
  stats: GroupStatsSummary
  agencies: string[]
  certData: CertDataByType
  signingData: number[]
  growthData: GrowthItem[]
  ratioData: RatioItem[]
  expiringRows: ExpiringGroupRow[]
  lastUpdated: string
}

export interface ExpiringGroupRow {
  code: string
  name: string
  plan: string
  expiry: string
}

export interface IndividualStatsSummary {
  activeCustomers: number
  newCts: number
  signings: number
  uploads: number
  uploadPct: number
}

export interface ChartByType {
  individual: number[]
  organization: number[]
  individualOfOrg: number[]
}

export interface FailureChart {
  pin: number[]
  otp: number[]
  moc: number[]
}

export interface IndividualReportResponse {
  stats: IndividualStatsSummary
  weeks: string[]
  newCustChart: number[]
  ctsChart: ChartByType
  signingChart: ChartByType
  failureChart: FailureChart
}

export const getGroupReport = (periodKey: string) =>
  request.get<any, ApiResponse<GroupReportResponse>>('/api/v1/reports/group', {
    params: { periodKey }
  })

export const getExpiringSoon = () =>
  request.get<any, ApiResponse<ExpiringGroupRow[]>>('/api/v1/reports/group/expiring-soon')

export const getIndividualReport = (periodKey: string) =>
  request.get<any, ApiResponse<IndividualReportResponse>>('/api/v1/reports/individual', {
    params: { periodKey }
  })
