import axios from 'axios'
import type { AxiosError, AxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import router from '@/router'
import { clearAuth, getToken, isTokenExpired } from './auth'

NProgress.configure({ showSpinner: false })

const request = axios.create({
  baseURL: '',
  timeout: 15000,
})

let sessionExpiredNotified = false

export async function confirm(message: string, title?: string): Promise<boolean> {
  try {
    await ElMessageBox.confirm(message, title || 'Warning', { type: 'warning' })
    return true
  } catch {
    return false
  }
}

export async function validateForm(formRef: any): Promise<boolean> {
  if (!formRef) return false
  try {
    await formRef.validate()
    return true
  } catch {
    return false
  }
}

function redirectToLogin(message = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.') {
  if (sessionExpiredNotified) return
  sessionExpiredNotified = true
  clearAuth()
  ElMessage.warning(message)
  setTimeout(() => {
    router.replace({ path: '/login' })
    sessionExpiredNotified = false
  }, 300)
}

function getErrorMessage(error: AxiosError<any>) {
  return error.response?.data?.message || error.message || 'Yêu cầu không thành công'
}

request.interceptors.request.use(
  (config) => {
    NProgress.start()
    config.headers.lang = localStorage.getItem('locale') || window.navigator.language

    const token = getToken()
    if (token) {
      if (isTokenExpired(token)) {
        NProgress.done()
        redirectToLogin()
        return Promise.reject(new Error('Token expired'))
      }
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    NProgress.done()
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    NProgress.done()
    return response.data
  },
  (error: AxiosError<any>) => {
    NProgress.done()
    if (error.response?.status === 401) {
      redirectToLogin()
    } else if (error.response?.status === 403) {
      ElMessage.error('Bạn không có quyền truy cập chức năng này.')
    } else if (error.response) {
      ElMessage.error(getErrorMessage(error))
    }
    return Promise.reject(error)
  }
)

export async function doGet<T = any>(url: string, config?: AxiosRequestConfig) {
  return request.get<any, T>(url, config)
}

export async function doPost<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
  return request.post<any, T>(url, data, config)
}

export async function doPut<T = any>(url: string, data?: any, config?: AxiosRequestConfig) {
  return request.put<any, T>(url, data, config)
}

export async function doDelete<T = any>(url: string, config?: AxiosRequestConfig) {
  return request.delete<any, T>(url, config)
}

export function showWarning(message: string) {
  ElMessage.warning(message)
}

export function showInfo(message: string) {
  ElMessage.info(message)
}

export function showSuccess(message: string) {
  ElMessage.success(message)
}

export default request
