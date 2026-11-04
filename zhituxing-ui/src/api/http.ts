// 轻量 fetch 封装：统一 baseURL、携带 JWT、解包 Result、抛出可读错误
import { shallowRef } from 'vue'
import type { Result } from '@/types'

export const BASE_URL = '/api/v1'
const TOKEN_KEY = 'zhituxing_token'
export const authToken = shallowRef<string | null>(localStorage.getItem(TOKEN_KEY))

export function getToken(): string | null {
  const current = localStorage.getItem(TOKEN_KEY)
  if (current !== authToken.value) authToken.value = current
  return current
}

export function setToken(token: string | null) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
  authToken.value = token
}

window.addEventListener('storage', event => {
  if (event.storageArea === localStorage && (event.key === TOKEN_KEY || event.key === null)) getToken()
})

interface FetchOptions {
  method?: string
  body?: unknown
  form?: FormData
  headers?: Record<string, string>
}

export async function request<T>(path: string, options: FetchOptions = {}): Promise<T> {
  const headers: Record<string, string> = { ...(options.headers ?? {}) }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`

  let body: BodyInit | undefined
  if (options.form) {
    body = options.form
  } else if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json; charset=utf-8'
    body = JSON.stringify(options.body)
  }

  let resp: Response
  try {
    resp = await fetch(`${BASE_URL}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body,
    })
  } catch {
    throw new Error('网络异常，请确认后端服务已启动（http://localhost:8080）')
  }

  let payload: Result<T> | null = null
  try {
    payload = (await resp.json()) as Result<T>
  } catch {
    /* 非 JSON 响应 */
  }

  if (token !== getToken()) throw new Error('登录身份已变更，请重新操作')
  if (!resp.ok || !payload || payload.code !== 0) {
    const message = payload?.message || `请求失败（HTTP ${resp.status}）`
    if (resp.status === 401) setToken(null)
    throw new Error(message)
  }
  return payload.data
}

export function fmtDate(value: string | null | undefined): string {
  if (!value) return '—'
  return value.slice(0, 10)
}

export function fmtDateTime(value: string | null | undefined): string {
  if (!value) return '—'
  return value.slice(0, 16).replace('T', ' ')
}
