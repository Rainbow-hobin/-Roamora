import { reactive, watch } from 'vue'
import { authToken, getToken, setToken } from '@/api/http'
import { getMe } from '@/api'

interface UserState {
  loggedIn: boolean
  name: string
  phone: string
  role: string
  ready: boolean
}

export const userState = reactive<UserState>({
  loggedIn: false,
  name: '',
  phone: '',
  role: 'user',
  ready: false,
})

export function setLoggedIn(value: boolean) {
  userState.loggedIn = value
}

/** 应用启动时用本地 token 恢复登录态 */
export async function restoreSession(): Promise<boolean> {
  const token = getToken()
  if (!token) {
    userState.ready = true
    return false
  }
  try {
    const profile = await getMe()
    if (token !== getToken()) return false
    userState.loggedIn = true
    userState.name = profile.name
    userState.phone = profile.phone
    userState.role = profile.role ?? 'user'
    return true
  } catch {
    if (token !== getToken()) return false
    setToken(null)
    userState.loggedIn = false
    return false
  } finally {
    if (token === getToken()) userState.ready = true
  }
}

export function applyLogin(token: string, name: string, phone: string, role: string) {
  setToken(token)
  userState.loggedIn = true
  userState.name = name
  userState.phone = phone
  userState.role = role
}

export function logout() {
  setToken(null)
  userState.loggedIn = false
  userState.name = ''
  userState.phone = ''
  userState.role = 'user'
}

// Credentials can change in another tab without changing this tab's loggedIn flag.
watch(authToken, token => {
  userState.loggedIn = false
  userState.name = ''; userState.phone = ''; userState.role = 'user'
  userState.ready = !token
  if (token) void restoreSession()
}, { flush: 'sync' })
