import { reactive } from 'vue'
import { getToken, request } from '@/api/http'
import type { ChatMessage } from '@/api/chat'
export type ChatEvent = { type:string; message?:ChatMessage; error?:string; clientId?:string }
export const chatState=reactive({ connected:false, status:'未连接', userId:0, unread:0 })
const listeners=new Set<(e:ChatEvent)=>void>()
let socket:WebSocket|undefined
let reconnect:ReturnType<typeof setTimeout>|undefined
let heartbeat:ReturnType<typeof setInterval>|undefined
let active=false
let connectionToken:string|null=null
let attempts=0
let unreadVersion=0
export function onChat(listener:(e:ChatEvent)=>void) { listeners.add(listener); return ()=>listeners.delete(listener) }
export async function refreshUnread() {
 if(!getToken())return
 const version=++unreadVersion
 try {const result=await request<{count:number}>('/chat/unread');if(version===unreadVersion)chatState.unread=result.count} catch { /* connection status is shown separately */ }
}
export function stopChat() {
 connectionToken=null;attempts=0
 ++unreadVersion;active=false;clearTimeout(reconnect);clearInterval(heartbeat)
 const old=socket;socket=undefined;old?.close();chatState.connected=false;chatState.userId=0;chatState.unread=0;chatState.status='未连接'
 listeners.forEach(fn=>fn({type:'reset'}))
}
export function startChat() { if(active)return;active=true;connect() }
function connect() {
 const token=getToken();if(!active||!token)return
 connectionToken=token
 chatState.status='连接中…'
 const ws=new WebSocket(`${location.protocol==='https:'?'wss':'ws'}://${location.host}/ws/chat`);socket=ws
 ws.onopen=()=>{if(getToken()===token&&socket===ws)ws.send(JSON.stringify({type:'auth',token}))}
 ws.onmessage=event=>{
  if(getToken()!==token||socket!==ws)return
  try {
   const data=JSON.parse(event.data)
   if(data.type==='ready') {
    attempts=0;chatState.connected=true;chatState.userId=data.userId;chatState.status='已连接'
    void refreshUnread();clearInterval(heartbeat);heartbeat=setInterval(()=>{if(ws.readyState===WebSocket.OPEN)ws.send(JSON.stringify({type:'ping'}))},25000)
    listeners.forEach(fn=>fn({type:'ready'}))
   } else if(data.type==='message') {
    void refreshUnread();listeners.forEach(fn=>fn({type:'message',message:data.message}))
   } else if(data.type==='read') {void refreshUnread();listeners.forEach(fn=>fn({type:'read'}))
   } else if(data.type==='error') listeners.forEach(fn=>fn({type:'error',error:data.message,clientId:data.clientId}))
  } catch {chatState.status='消息解析失败，请刷新重试'}
 }
 ws.onclose=event=>{
  if(getToken()!==token||socket!==ws)return
  clearInterval(heartbeat);chatState.connected=false
  if(event.code===1008) {active=false;chatState.status='连接已拒绝，请重新登录';return}
  chatState.status='连接断开，正在重连…'
  if(active)reconnect=setTimeout(connect,Math.min(1000*2**attempts++,30000))
 }
 ws.onerror=()=>{if(socket!==ws)return;chatState.status='连接异常，正在恢复…'}
}
export function sendChat(to:number,content:string,clientId:string) {
 if(getToken()!==connectionToken)throw new Error('登录身份已变更，请重新操作')
 if(!chatState.connected||socket?.readyState!==WebSocket.OPEN)throw new Error('连接尚未恢复，请稍后重试')
 socket.send(JSON.stringify({type:'send',to,content,clientId}))
}
