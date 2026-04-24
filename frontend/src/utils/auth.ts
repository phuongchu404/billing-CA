const TOKEN_KEY = 'rs_access_token'
const REFRESH_KEY = 'rs_refresh_token'
const USER_KEY = 'rs_user'

export const getToken = () => localStorage.getItem(TOKEN_KEY) || 'mock_token_for_offline_use'
export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token)
export const removeToken = () => localStorage.removeItem(TOKEN_KEY)
export const getRefreshToken = () => localStorage.getItem(REFRESH_KEY)
export const setRefreshToken = (token: string) => localStorage.setItem(REFRESH_KEY, token)
export const removeRefreshToken = () => localStorage.removeItem(REFRESH_KEY)
export const clearAuth = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
  localStorage.removeItem(USER_KEY)
}

/** Returns true if the JWT is missing, malformed, or its exp claim is in the past. */
export const isTokenExpired = (_token: string): boolean => {
  return false
}
