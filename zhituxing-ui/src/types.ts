// 与后端《接口文档》V1.3 对应的类型定义（字段名与后端 DTO 一致）

export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

// —— 认证 ——
export interface UserInfo {
  id: number
  name: string
  phone: string
  avatar: string | null
  signature: string | null
  role: string
  status: string
  createdAt: string
}

export interface TokenData {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: UserInfo
}

export interface CaptchaData {
  captchaKey: string
  imageBase64: string
}

// —— 景点 / 目的地 ——
export interface Spot {
  id: number
  name: string
  city: string
  category: string
  price: number
  rating: number | null
  cover: string | null
  summary: string
  status: string
  description?: string | null
  address?: string | null
  openingHours?: string | null
  auditStatus?: string
}

export interface Destination {
  id: number
  name: string
  tripDays: number
  cover: string | null
}

// —— 社区 ——
export interface PostItem {
  id: number
  title: string
  author: string
  board: string
  views: number
  comments: number
  cover: string | null
  images: string[] | null
  content: string | null
  date: string
}

export interface CommentItem {
  id: number
  post: string | null
  author: string
  rating: number | null
  content: string
  date: string
  status: string
}

// —— 行程 ——
export interface TripItem {
  id: number
  title: string
  days: number
  spotCount: number
  startDate: string | null
  endDate: string | null
  status: string
}

export interface TripDetail extends TripItem {
  spots: Spot[]
}

// —— 个人中心 ——
export interface ProfileStats {
  trips: number
  notes: number
  comments: number
}

export interface Profile {
  name: string
  phone: string
  avatar: string | null
  signature: string | null
  role?: string
  stats: ProfileStats
  activity: string | null
}

export interface NoteItem {
  id: number
  title: string
  content: string | null
  createdAt: string
}

// —— 管理后台 ——
export interface AdminStat {
  label: string
  value: number
  trend: string
}

export interface DashboardData {
  stats: AdminStat[]
  trend: number[]
  range: number
  categoryShare: { name: string; value: number }[]
  pending: { reviews: number }
}

export interface AdminSpot {
  id: number
  name: string
  city: string
  category: string
  price: number
  status: string
  auditStatus?: string
  rating?: number
}

export interface AdminUser {
  id: number
  name: string
  phone: string
  trips: number
  status: string
}

export interface AdminReview {
  id: number
  post: string
  author: string
  content: string
  date: string
  status: string
}
