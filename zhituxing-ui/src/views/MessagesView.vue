<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getConversations, getMessages, getPerson, searchPeople, readMessages } from '@/api/chat'
import type { ChatPerson, ChatMessage, Conversation } from '@/api/chat'
import { chatState, onChat, sendChat, refreshUnread } from '@/stores/chat'
const route=useRoute(), router=useRouter()
const conversations=ref<Conversation[]>([]), people=ref<ChatPerson[]>([])
const query=ref(typeof route.query.q==='string'?route.query.q:'')
const peer=ref<ChatPerson|null>(null), messages=ref<ChatMessage[]>([])
const draft=ref(''), error=ref(''), loading=ref(false), searching=ref(false), hasMore=ref(false)
const feed=ref<HTMLElement>()
interface Pending { clientId:string; to:number; content:string; error:string }
const pending=ref<Pending[]>([])
const timers=new Map<string,ReturnType<typeof setTimeout>>()
let activeLoad:{generation:number;peerId:number;events:ChatMessage[]}|undefined
const historyBefore=ref<number>()
const drafts=new Map<number,string>()
let currentPeerId=0
let version=0, listVersion=0, searchVersion=0, searchTimer:ReturnType<typeof setTimeout>|undefined
async function list() {
 const generation=++listVersion
 try {const rows=await getConversations();if(generation===listVersion)conversations.value=rows}
 catch(e){if(generation===listVersion)error.value=(e as Error).message}
}
async function scrollBottom() {await nextTick(); if(feed.value)feed.value.scrollTop=feed.value.scrollHeight}
async function markRead() {
 if(!peer.value)return
 const incoming=messages.value.filter(m=>m.recipientId===chatState.userId)
 if(!incoming.length)return
 const generation=version, id=peer.value.id
 try {await readMessages(id,Math.max(...incoming.map(m=>m.id)));if(generation!==version)return;await refreshUnread();if(generation===version)await list()}catch(e){if(generation===version)error.value=(e as Error).message}
}
async function openPeer(id:number) {
 const generation=++version
 const load={generation,peerId:id,events:[] as ChatMessage[]}
 activeLoad=load;historyBefore.value=undefined
 if(currentPeerId!==id){if(currentPeerId)drafts.set(currentPeerId,draft.value);draft.value=drafts.get(id)||'';currentPeerId=id}
 peer.value=null;messages.value=[];error.value='';loading.value=true
 try {
  const [person,history]=await Promise.all([getPerson(id),getMessages(id)])
  if(generation!==version)return
  peer.value=person
  const combined=new Map(history.map(m=>[m.id,m]))
  load.events.forEach(m=>combined.set(m.id,m))
  historyBefore.value=history[0]?.id
  messages.value=[...combined.values()].sort((a,b)=>a.id-b.id);hasMore.value=history.length===50
  await scrollBottom();if(document.visibilityState==='visible')await markRead()
 }catch(e){if(generation===version)error.value=(e as Error).message}
 finally {if(generation===version){loading.value=false;activeLoad=undefined}}
}
async function older() {
 if(!peer.value||!historyBefore.value||loading.value)return
 const id=peer.value.id, generation=version;loading.value=true
 try {
  const height=feed.value?.scrollHeight||0
  const rows=await getMessages(id,historyBefore.value)
  if(generation!==version)return
  if(rows.length)historyBefore.value=rows[0]!.id
  const seen=new Set(messages.value.map(m=>m.id));messages.value=[...rows.filter(m=>!seen.has(m.id)),...messages.value];hasMore.value=rows.length===50
  await nextTick();if(feed.value)feed.value.scrollTop=feed.value.scrollHeight-height
 }catch(e){if(generation===version)error.value=(e as Error).message}finally{if(generation===version)loading.value=false}
}
function select(person:ChatPerson) {void router.replace({path:'/messages',query:{peer:person.id}})}
async function search() {
 const generation=++searchVersion;people.value=[]
 if(query.value.trim().length<2){searching.value=false;return}
 searching.value=true
 try {const rows=await searchPeople(query.value.trim());if(generation===searchVersion)people.value=rows}
 catch(e){if(generation===searchVersion)error.value=(e as Error).message}finally{if(generation===searchVersion)searching.value=false}
}
watch(query,()=>{clearTimeout(searchTimer);++searchVersion;searchTimer=setTimeout(search,300)})
watch(()=>route.query.peer, value=>{const id=Number(value);if(Number.isSafeInteger(id)&&id>0)void openPeer(id)}, {immediate:true})
function transmit(item:Pending) {
 item.error=''
 try {
  sendChat(item.to,item.content,item.clientId)
  clearTimeout(timers.get(item.clientId));timers.set(item.clientId,setTimeout(()=>{item.error='尚未收到确认，可重试'},15000))
 } catch(e){item.error=(e as Error).message}
}
function send() {
 const content=draft.value.trim();if(!peer.value||!content||content.length>2000)return
 const item={clientId:crypto.randomUUID(),to:peer.value.id,content,error:''}
 pending.value.push(item);draft.value='';transmit(pending.value[pending.value.length-1]!);void scrollBottom()
}
function resetAccount() {
 ++version;++listVersion;++searchVersion;activeLoad=undefined;historyBefore.value=undefined
 clearTimeout(searchTimer);timers.forEach(clearTimeout);timers.clear();drafts.clear()
 peer.value=null;messages.value=[];conversations.value=[];people.value=[];pending.value=[]
 draft.value='';query.value='';error.value='';loading.value=false;searching.value=false;hasMore.value=false;currentPeerId=0
 if(route.query.peer||route.query.q)void router.replace({path:'/messages'})
}
const unsubscribe=onChat(event=>{
 if(event.type==='reset'){resetAccount();return}
 if(event.type==='ready') {void list();const id=Number(route.query.peer);if(id>0)void openPeer(id);return}
 if(event.type==='read'){void list();return}
 if(event.type==='error') {const item=pending.value.find(p=>p.clientId===event.clientId);if(item){clearTimeout(timers.get(item.clientId));item.error=event.error||'发送失败'}else error.value=event.error||'连接失败';return}
 const message=event.message;if(!message)return
 clearTimeout(timers.get(message.clientId));timers.delete(message.clientId)
 pending.value=pending.value.filter(p=>p.clientId!==message.clientId)
 const other=message.senderId===chatState.userId?message.recipientId:message.senderId
 if(activeLoad?.peerId===other)activeLoad.events.push(message)
 if(peer.value?.id===other) {
  if(!messages.value.some(m=>m.id===message.id)){messages.value.push(message);messages.value.sort((a,b)=>a.id-b.id)}
  void scrollBottom();if(document.visibilityState==='visible')void markRead()
 }
 void list()
})
function visible(){if(document.visibilityState==='visible')void markRead()}
onMounted(()=>{void list();void search();document.addEventListener('visibilitychange',visible)})
onUnmounted(()=>{unsubscribe();clearTimeout(searchTimer);timers.forEach(clearTimeout);++version;++listVersion;++searchVersion;activeLoad=undefined;document.removeEventListener('visibilitychange',visible)})
function time(value:string){return value.replace('T',' ').slice(0,16)}
</script>
<template>
 <div class="container messages-page">
  <div class="page-heading"><div><h1>旅途私信</h1><p>和同行的人，聊聊下一程。</p></div><span class="connection" :class="{online:chatState.connected}" role="status">{{ chatState.status }}</span></div>
  <p v-if="error" class="error" role="alert">{{ error }} <button class="text-button" @click="list">重试</button></p>
  <div class="messenger card">
   <aside class="contacts">
    <label for="people-search">寻找旅友</label><input id="people-search" v-model="query" placeholder="输入至少两个字的用户名" maxlength="32" />
    <div v-if="query.trim().length>=2" class="search-results"><p class="muted">{{ searching?'搜索中…':'搜索结果' }}</p>
     <button v-for="person in people" :key="person.id" class="contact" @click="select(person)"><span class="avatar">{{ person.name.slice(0,1) }}</span><span>{{ person.name }}</span><span class="start">私聊</span></button>
     <p v-if="!searching&&!people.length" class="muted">没有找到用户，试试完整用户名。</p>
    </div>
    <h2>最近会话</h2><p v-if="!conversations.length" class="muted">还没有私信，先搜索一位旅友。</p>
    <button v-for="c in conversations" :key="c.peer.id" class="contact" :class="{selected:peer?.id===c.peer.id}" @click="select(c.peer)"><span class="avatar">{{ c.peer.name.slice(0,1) }}</span><span class="contact-copy"><strong>{{ c.peer.name }}</strong><span class="preview">{{ c.lastMessage.content }}</span></span><span v-if="c.unread" class="badge">{{ c.unread>99?'99+':c.unread }}</span></button>
   </aside>
   <section class="thread" aria-label="私聊消息">
    <template v-if="peer"><header class="thread-head"><h2>{{ peer.name }}</h2><span>私聊 · 消息仅对双方可见</span></header>
     <div ref="feed" class="feed" aria-live="polite"><button v-if="hasMore" class="text-button history" :disabled="loading" @click="older">{{ loading?'加载中…':'查看更早的消息' }}</button><p v-if="!messages.length" class="muted">打个招呼，开启一段交流。</p>
      <div v-for="m in messages" :key="m.id" class="message" :class="{mine:m.senderId===chatState.userId}"><p class="bubble">{{ m.content }}</p><time>{{ time(m.createdAt) }}</time></div>
      <div v-for="p in pending.filter(p=>p.to===peer?.id)" :key="p.clientId" class="message mine"><p class="bubble">{{ p.content }}</p><span class="delivery">{{ p.error || '发送中…' }} <button v-if="p.error" class="text-button" @click="transmit(p)">重试</button></span></div>
     </div>
     <form class="composer" @submit.prevent="send"><label class="muted" for="message-draft">消息 <span>{{ draft.length }}/2000 · Ctrl+Enter 发送</span></label><textarea id="message-draft" v-model="draft" maxlength="2000" rows="3" placeholder="写下想说的话…" @keydown.ctrl.enter.prevent="send" /><div class="composer-actions"><span class="muted">{{ chatState.connected?'请友善交流，不向陌生人提供敏感信息。':'正在等待连接恢复…' }}</span><button class="btn btn-gold" :disabled="!chatState.connected || !draft.trim()">发送</button></div></form>
    </template>
    <div v-else class="thread-empty"><h2>{{ loading?'正在打开会话…':'和旅友聊一聊' }}</h2><p>搜索用户名，或从左侧选择一段会话。</p></div>
   </section>
  </div>
 </div>
</template>
<style scoped>
.messages-page{padding-block:32px 48px;max-width:1240px}.page-heading{display:flex;justify-content:space-between;align-items:center;margin-bottom:24px}.page-heading h1{font-size:28px;font-weight:600}.page-heading p,.muted{font-size:14px;color:var(--muted)}.connection{font-size:13px;color:var(--muted)}.connection::before{content:'●';margin-right:7px;color:#aa7964}.connection.online::before{color:var(--primary)}.messenger{display:grid;grid-template-columns:280px minmax(0,1fr);height:690px;overflow:hidden}.contacts{padding:20px;border-right:1px solid var(--border);overflow-y:auto}.contacts label{font-size:14px}.contacts input{width:100%;margin:10px 0 18px;padding:10px;border:1px solid var(--border);border-radius:6px;font-size:14px}.contacts h2{font-size:15px;margin:20px 0 10px}.contact{width:100%;display:flex;align-items:center;gap:10px;padding:12px 8px;border:0;background:transparent;border-radius:6px;text-align:left;color:var(--text);cursor:pointer}.contact:hover,.contact.selected{background:#eef3ec}.avatar{display:flex;align-items:center;justify-content:center;width:34px;height:34px;background:#e0e9dd;color:var(--primary-deep);border-radius:50%;flex-shrink:0}.contact-copy{min-width:0;flex:1}.contact-copy strong{display:block;font-size:14px;font-weight:500}.preview{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:13px;color:var(--muted)}.badge{border-radius:12px;background:#a64c3b;color:white;font-size:11px;padding:1px 6px}.start{margin-left:auto;font-size:12px;color:var(--primary)}.thread{min-width:0;display:flex;flex-direction:column;min-height:0}.thread-head{padding:16px 24px;border-bottom:1px solid var(--border);display:flex;align-items:center;gap:16px}.thread-head h2{font-size:18px;font-weight:600}.thread-head span{font-size:13px;color:var(--muted)}.feed{flex:1;overflow-y:auto;padding:20px 24px;min-height:0}.message{display:flex;align-items:flex-start;flex-direction:column;margin:0 0 16px}.message.mine{align-items:flex-end}.bubble{max-width:80%;border-radius:10px;padding:10px 14px;background:#f0f3ed;white-space:pre-wrap;overflow-wrap:anywhere;font-size:15px}.mine .bubble{background:#e1eee6}.message time,.delivery{font-size:12px;color:var(--muted);margin-top:5px}.composer{border-top:1px solid var(--border);padding:16px 24px}.composer label{display:flex;justify-content:space-between}.composer textarea{width:100%;resize:vertical;min-height:70px;max-height:140px;border:0;padding:10px 0;background:transparent;color:var(--text);font:inherit;font-size:15px}.composer-actions{display:flex;align-items:center;justify-content:space-between;gap:16px}.text-button{border:0;background:transparent;color:var(--primary);cursor:pointer;font:inherit;font-size:13px}.history{display:block;margin:0 auto 20px}.thread-empty{margin:auto;text-align:center;color:var(--muted)}.thread-empty h2{font-size:22px;font-weight:500;color:var(--text);margin-bottom:12px}.thread-empty p{font-size:14px}.error{padding:12px 16px;background:#fbede8;border-radius:6px;margin-bottom:16px;color:#923e29;font-size:14px}.search-results{border-bottom:1px solid var(--border);padding-bottom:12px}
</style>
