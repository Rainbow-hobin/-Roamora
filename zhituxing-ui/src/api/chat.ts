import { request } from './http'
export interface ChatPerson { id:number; name:string; avatar:string|null }
export interface ChatMessage { id:number; senderId:number; recipientId:number; content:string; clientId:string; createdAt:string; readAt:string|null }
export interface Conversation { peer:ChatPerson; lastMessage:ChatMessage; unread:number }
export const searchPeople=(q:string)=>request<ChatPerson[]>(`/chat/users?q=${encodeURIComponent(q)}`)
export const getPerson=(id:number)=>request<ChatPerson>(`/chat/users/${id}`)
export const getConversations=()=>request<Conversation[]>('/chat/conversations')
export const getMessages=(peer:number,before?:number)=>request<ChatMessage[]>(`/chat/messages/${peer}${before?`?before=${before}`:''}`)
export const readMessages=(peer:number,through:number)=>request<void>(`/chat/read/${peer}`,{method:'POST',body:{through}})
