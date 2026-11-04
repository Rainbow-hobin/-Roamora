// 智途星平台 API（对应《后端接口文档》V1.3）
import { request } from './http'
import type {
  AdminUser,
  CaptchaData,
  CommentItem,
  DashboardData,
  Destination,
  NoteItem,
  PageResult,
  PostItem,
  Profile,
  Spot,
  TokenData,
  TripDetail,
  TripItem,
} from '@/types'

// —— 3 认证 ——
export function getCaptcha() {
  return request<CaptchaData>('/captchas')
}
export function login(account: string, password: string, captchaKey: string, captcha: string) {
  return request<TokenData>('/auth/token', { method: 'POST', body: { account, password, captchaKey, captcha } })
}
export function register(account: string, phone: string, password: string, captchaKey: string, captcha: string) {
  return request<{ userId: number }>('/users', {
    method: 'POST',
    body: { account, phone, password, captchaKey, captcha },
  })
}

// —— 4 景点 ——
export function listSpots(params: {
  keyword?: string
  city?: string
  category?: string
  sort?: string
  page?: number
  pageSize?: number
} = {}) {
  const qs = new URLSearchParams()
  if (params.keyword) qs.set('keyword', params.keyword)
  if (params.city) qs.set('city', params.city)
  if (params.category) qs.set('category', params.category)
  if (params.sort) qs.set('sort', params.sort)
  qs.set('page', String(params.page ?? 1))
  qs.set('pageSize', String(params.pageSize ?? 100))
  return request<PageResult<Spot>>(`/spots?${qs.toString()}`)
}
export function getSpot(id: number | string) {
  return request<Spot>(`/spots/${id}`)
}
export function listDestinations() {
  return request<{ list: Destination[] }>('/destinations')
}
export function listSpotComments(id: number | string, page = 1, pageSize = 20) {
  return request<PageResult<CommentItem>>(`/spots/${id}/comments?page=${page}&pageSize=${pageSize}`)
}
export function addSpotComment(id: number | string, content: string, rating?: number) {
  return request<CommentItem>(`/spots/${id}/comments`, {
    method: 'POST',
    body: { content, rating },
  })
}

// —— 6 行程 ——
export function listTrips() {
  return request<TripItem[]>('/trips')
}
export function createTrip(data: { title: string; days: number; spotIds: number[]; startDate?: string }) {
  return request<TripItem>('/trips', { method: 'POST', body: data })
}
export function getTripDetail(id: number | string) {
  return request<TripDetail>(`/trips/${id}`)
}

// —— 7 社区 ——
export function listPosts(page = 1, pageSize = 50) {
  return request<PageResult<PostItem>>(`/posts?page=${page}&pageSize=${pageSize}`)
}
export function getPost(id: number | string) {
  return request<PostItem>(`/posts/${id}`)
}
export function createPost(data: { title: string; content: string; board?: string }) {
  return request<PostItem>('/posts', { method: 'POST', body: data })
}
export function listPostComments(id: number | string, page = 1, pageSize = 20) {
  return request<PageResult<CommentItem>>(`/posts/${id}/comments?page=${page}&pageSize=${pageSize}`)
}
export function addPostComment(id: number | string, content: string) {
  return request<CommentItem>(`/posts/${id}/comments`, { method: 'POST', body: { content } })
}

// —— 8 个人中心 ——
export function getMe() {
  return request<Profile>('/users/me')
}
export function updateProfile(data: { name?: string; avatar?: string | null; signature?: string | null }) {
  return request<Profile>('/users/me', { method: 'PATCH', body: data })
}
export function myTrips() {
  return request<TripItem[]>('/users/me/trips')
}
export function myComments(page = 1, pageSize = 100) {
  return request<PageResult<CommentItem>>(`/users/me/comments?page=${page}&pageSize=${pageSize}`)
}
export function myNotes(page = 1, pageSize = 100) {
  return request<PageResult<NoteItem>>(`/users/me/notes?page=${page}&pageSize=${pageSize}`)
}
export function myFavorites() {
  return request<Spot[]>('/users/me/favorites')
}
export function addFavorite(spotId: number) {
  return request<void>('/users/me/favorites', { method: 'POST', body: { spotId } })
}
export function removeFavorite(spotId: number) {
  return request<void>(`/users/me/favorites/${spotId}`, { method: 'DELETE' })
}

// —— 9 管理后台 ——
export function adminDashboard(range: number = 7) {
  return request<DashboardData>(`/admin/dashboard?range=${range}`)
}
export function adminSpots(params: { keyword?: string; city?: string; category?: string; page?: number; pageSize?: number } = {}) {
  const qs = new URLSearchParams()
  if (params.keyword) qs.set('keyword', params.keyword)
  if (params.city) qs.set('city', params.city)
  if (params.category) qs.set('category', params.category)
  qs.set('page', String(params.page ?? 1))
  qs.set('pageSize', String(params.pageSize ?? 100))
  return request<PageResult<Spot>>(`/admin/spots?${qs.toString()}`)
}
export function adminCreateSpot(data: {
  name: string
  city: string
  category: string
  price: number
  description?: string
  cover?: string
  address?: string
  openingHours?: string
}) {
  return request<Spot>('/admin/spots', { method: 'POST', body: data })
}
export function adminUpdateSpot(id: number, data: Record<string, unknown>) {
  return request<Spot>(`/admin/spots/${id}`, { method: 'PUT', body: data })
}
export function adminUpdateSpotStatus(id: number, status: string) {
  return request<Spot>(`/admin/spots/${id}/status`, { method: 'PUT', body: { status } })
}
export function adminDeleteSpot(id: number) {
  return request<void>(`/admin/spots/${id}`, { method: 'DELETE' })
}
export function adminUsers(params: { keyword?: string; status?: string; page?: number; pageSize?: number } = {}) {
  const qs = new URLSearchParams()
  if (params.keyword) qs.set('keyword', params.keyword)
  if (params.status) qs.set('status', params.status)
  qs.set('page', String(params.page ?? 1))
  qs.set('pageSize', String(params.pageSize ?? 100))
  return request<PageResult<AdminUser>>(`/admin/users?${qs.toString()}`)
}
export function adminUpdateUserStatus(id: number, status: string) {
  return request<AdminUser>(`/admin/users/${id}/status`, { method: 'PUT', body: { status } })
}
export function adminReviews(params: { status?: string; keyword?: string; page?: number; pageSize?: number } = {}) {
  const qs = new URLSearchParams()
  if (params.status) qs.set('status', params.status)
  if (params.keyword) qs.set('keyword', params.keyword)
  qs.set('page', String(params.page ?? 1))
  qs.set('pageSize', String(params.pageSize ?? 100))
  return request<PageResult<CommentItem>>(`/admin/reviews?${qs.toString()}`)
}
export function adminUpdateReviewStatus(id: number, status: string) {
  return request<CommentItem>(`/admin/reviews/${id}/status`, { method: 'PUT', body: { status } })
}
export function adminBatchReviews(ids: number[], status: string) {
  return request<void>('/admin/reviews/batch', { method: 'POST', body: { ids, status } })
}

// —— 文件上传 ——
export function uploadFile(file: File) {
  const form = new FormData()
  form.append('file', file)
  return request<{ url: string }>('/files', { method: 'POST', form })
}
