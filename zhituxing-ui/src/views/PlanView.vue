<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { Compass, MapPin, Leaf, Wallet } from 'lucide-vue-next'
import { useRoute } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import TripCard from '@/components/TripCard.vue'
import { listTrips } from '@/api'
import { agentHealth, agentChatStream } from '@/api/agent'
import { getToken } from '@/api/http'
import type { TripItem } from '@/types'

interface ChatMsg {
  role: 'user' | 'assistant'
  content: string
}

const messages = ref<ChatMsg[]>([])
const route = useRoute()
const draft = [route.query.q ? `我想去${route.query.q}` : '', route.query.date ? `出发日期${route.query.date}` : '', route.query.days ? `游玩${route.query.days}天` : ''].filter(Boolean).join('，')
const input = ref(draft)
const suggestions = [
  { title: '周末山野', detail: '短途出发 · 自然风景', icon: Leaf, prompt: '我想从重庆出发周末玩两天，喜欢自然风光，请先帮我确认预算和交通方式。' },
  { title: '亲子慢游', detail: '亲子同行 · 轻松节奏', icon: MapPin, prompt: '我想安排一次三天的亲子旅行，希望少走路、节奏轻松，请先帮我确认目的地和预算。' },
  { title: '预算优先', detail: '量入为出 · 精选体验', icon: Wallet, prompt: '我想安排一次两个人、总预算1500元的旅行，请先询问我的出发地、日期和天数。' },
]
function choose(prompt: string) { input.value = prompt }
const elapsed = ref(0)
let controller: AbortController | undefined
let ticker: ReturnType<typeof setInterval> | undefined
function render(content: string) { return DOMPurify.sanitize(marked.parse(content, { async: false }) as string) }
function cancel() { controller?.abort(); sessionId.value = undefined }
onUnmounted(() => { cancel(); clearInterval(ticker) })
function download(content: string) {
  const url = URL.createObjectURL(new Blob([content], { type: 'text/markdown;charset=utf-8' }))
  const a = document.createElement('a'); a.href = url; a.download = '旅行方案.md'; a.click(); URL.revokeObjectURL(url)
}
const sending = ref(false)
const agentNode = ref('')
const sessionId = ref<string | undefined>(undefined)
const agentReady = ref<boolean | null>(null)
const chatError = ref('')

const myTrips = ref<TripItem[]>([])

const AGENT_LABELS: Record<string, string> = {
  main: '意图理解',
  planner: '需求解析',
  executor: '查询数据',
  summarizer: '生成方案',
  feedback: '偏好处理',
}

function labelOf(agent: string): string {
  return AGENT_LABELS[agent] || agent || '规划分析'
}

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  chatError.value = ''
  sending.value = true
  agentNode.value = ''
  controller = new AbortController()
  elapsed.value = 0
  ticker = setInterval(() => elapsed.value++, 1000)
  const timeout = setTimeout(() => controller?.abort(), 180000)
  try {
    await agentChatStream(text, sessionId.value, {
      onAgent: (agent) => {
        agentNode.value = labelOf(agent)
      },
      onDone: (result) => {
        sessionId.value = result.session_id
        messages.value.push({ role: 'assistant', content: result.answer })
      },
      onError: (msg) => {
        chatError.value = msg
      },
    }, controller.signal)
  } catch (e) {
    chatError.value = controller?.signal.aborted ? '本次生成已停止，您可以修改需求后重新发送' : (e as Error).message
    input.value = text
  } finally {
    clearInterval(ticker)
    clearTimeout(timeout)
    sending.value = false
    agentNode.value = ''
  }
}

onMounted(async () => {
  try {
    await agentHealth()
    agentReady.value = true
  } catch {
    agentReady.value = false
  }
  if (getToken()) {
    try {
      myTrips.value = await listTrips()
    } catch {
      /* ignore */
    }
  }
})
</script>

<template>
  <div class="container page">
    <div class="page-head">
      <h1 class="section-title">下一程，想去哪里？</h1>
      <p class="section-sub">聊聊你的期待，一起把旅行安排好。</p>
    </div>

    <p v-if="agentReady === false" class="agent-warn">
      规划服务暂时不可用，请稍后刷新重试。您也可以先浏览目的地。
    </p>

    <div class="plan-grid">
      <!-- 左栏 · 对话 -->
      <div class="chat-card card" :class="{ 'is-empty': !messages.length && !sending }">
        <div v-if="messages.length || sending" class="chat-head">
          <span class="chat-logo"><Compass :size="24" /></span>
          <div>
            <h2>AI 旅行规划师</h2>
            <p>你的专属旅行规划师</p>
          </div>
        </div>

        <div class="chat-body">
          <div v-if="!messages.length && !sending" class="chat-empty">
            <p>告诉我目的地、出发时间和预算，或选择一种旅行方式。</p>
            <div class="suggestions">
              <button v-for="s in suggestions" :key="s.title" class="suggestion" @click="choose(s.prompt)">
                <component :is="s.icon" :size="22" />
                <strong>{{ s.title }}</strong>
              </button>
            </div>
          </div>

          <div v-for="(m, i) in messages" :key="i" class="chat-msg" :class="m.role">
            <div v-if="m.role === 'assistant'" class="bubble markdown" v-html="render(m.content)"></div>
            <div v-else class="bubble">{{ m.content }}</div>
            <button v-if="m.role === 'assistant'" class="btn" @click="download(m.content)">下载方案</button>
          </div>

          <div v-if="sending" class="chat-msg assistant">
            <div class="bubble typing">
              <span class="typing-dot"></span>
              {{ agentNode ? `${agentNode} 处理中…` : '正在思考…' }} 已等待 {{ elapsed }} 秒
            </div>
          </div>
        </div>

        <div v-if="sending" class="progress-actions" role="status">正在核对旅行信息，复杂方案可能需要几分钟。<button class="btn" @click="cancel">停止生成</button></div>
        <div class="chat-input">
          <input
            v-model="input"
            type="text"
            placeholder="请输入旅行需求，例如：想去成都玩 3 天"
            :disabled="sending || agentReady === false"
            @keyup.enter="send"
          />
          <button
            class="btn btn-gold"
            :disabled="sending || !input.trim() || agentReady === false"
            @click="send"
          >
            发送
          </button>
        </div>
        <p v-if="chatError" class="chat-error">{{ chatError }}</p>
      </div>

      <!-- 右栏 · 我的行程 -->
      <aside class="trips-col card">
        <h2 class="section-title">我的行程</h2>
        <div v-if="!getToken()"><p class="section-sub">登录后可查看并管理您的行程</p><RouterLink class="btn btn-outline" to="/login?redirect=/plan">登录查看行程</RouterLink><p class="section-sub">生成的方案可直接下载，请在出行前核实票价与班次。</p></div>
        <p v-else class="section-sub">已生成 {{ myTrips.length }} 条行程</p>
        <div class="trip-list">
          <TripCard v-for="trip in myTrips" :key="trip.id" :trip="trip" />
        </div>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding-block: 44px 60px;
}

.page-head {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 22px;
}

.page-head .section-title {
  font-size: 28px;
}

.agent-warn {
  margin-bottom: 18px;
  padding: 12px 16px;
  border-radius: 10px;
  background: rgba(233, 183, 69, 0.12);
  border: 1px solid rgba(233, 183, 69, 0.42);
  font-size: 13px;
  color: var(--gold-deep);
}

.agent-warn code {
  font-family: 'Consolas', 'Menlo', monospace;
  background: rgba(7, 81, 77, 0.08);
  padding: 1px 6px;
  border-radius: 6px;
}

.plan-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 28px;
  align-items: start;
}

/* 对话卡片 */
.chat-card {
  display: flex;
  flex-direction: column;
  height: min(760px, 85vh);
  min-height: 540px;
  overflow: hidden;
}

.chat-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
  background: linear-gradient(120deg, rgba(15, 125, 119, 0.07), rgba(15, 125, 119, 0.02));
}

.chat-logo {
  font-size: 30px;
  line-height: 1;
}

.chat-head h2 {
  font-size: 18px;
  font-weight: 800;
  color: var(--primary-deep);
}

.chat-head p {
  font-size: 12.5px;
  color: var(--muted);
  margin-top: 2px;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.chat-empty {
  margin: auto;
  text-align: center;
  font-size: 13px;
  color: var(--muted);
  line-height: 2;
}

.chat-msg {
  display: flex;
}

.chat-msg.user {
  justify-content: flex-end;
}

.chat-msg.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: 78%;
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-msg.user .bubble {
  background: var(--primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.chat-msg.assistant .bubble {
  background: var(--bg);
  border: 1px solid var(--border);
  color: var(--text);
  border-bottom-left-radius: 4px;
}

.bubble.typing {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--muted);
  font-size: 13px;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--gold);
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  50% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.chat-input {
  display: flex;
  gap: 10px;
  padding: 14px 18px;
  border-top: 1px solid var(--border);
}

.chat-input input {
  flex: 1;
  min-width: 0;
  border: 1px solid var(--border);
  border-radius: 999px;
  padding: 11px 18px;
  font: inherit;
  font-size: 14px;
  outline: none;
  background: var(--bg);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.chat-input input:focus {
  border-color: var(--gold-deep);
  box-shadow: 0 0 0 3px rgba(233, 183, 69, 0.18);
}

.chat-input .btn {
  border-radius: 999px;
  padding: 0 22px;
}

.chat-error {
  padding: 4px 18px 12px;
  font-size: 12.5px;
  color: #c0392b;
  text-align: center;
}

/* 我的行程 */
.trips-col {
  min-width: 0;
}

.trip-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
 .progress-actions { padding: 10px 18px; font-size: 13px; }
.markdown { white-space: normal; overflow-wrap: anywhere; overflow-x: auto; }
.markdown :deep(table) { border-collapse: collapse; min-width: 100%; font-size:13px; }
.markdown :deep(td), .markdown :deep(th) { border:1px solid var(--border); padding:8px; }
.markdown :deep(p), .markdown :deep(ul), .markdown :deep(h3) { margin-block:12px; }
.markdown :deep(ul), .markdown :deep(ol) { padding-left:22px; }



.page { max-width:1100px; padding-block:40px 56px; }
.page-head { text-align:left; align-items:flex-start; margin-bottom:24px; gap:8px; }
.page-head .section-title { color:var(--text); font-size:30px; font-weight:600; }
.page-head .section-sub { font-size:15px; }
.plan-grid { grid-template-columns:minmax(0,1fr) 230px; gap:24px; }
.chat-card { min-height:420px; height:60vh; border-radius:10px; }
.chat-card.is-empty { height:auto; min-height:0; }
.chat-head { background:#f5f7f2; padding:14px 20px; }
.chat-head h2 { font-size:16px; color:var(--text); font-weight:600; }
.chat-head p { display:none; }
.chat-logo { color:var(--primary); display:flex; }
.chat-empty { width:100%; margin:0; text-align:left; padding:24px; color:var(--muted); }
.chat-empty p { font-size:15px; line-height:1.75; }
.suggestions { display:flex; flex-wrap:wrap; gap:10px; margin-top:20px; }
.suggestion { display:inline-flex; align-items:center; gap:8px; padding:9px 13px; border:1px solid var(--border); background:#fafbf8; border-radius:6px; color:var(--primary); cursor:pointer; }
.suggestion:hover { background:#eff4ec; border-color:#adc1ae; }
.suggestion strong { font-size:14px; font-weight:400; color:var(--text); }
.suggestion svg { width:17px; height:17px; }
.chat-input { padding:16px 20px; gap:10px; }
.chat-input input { font-size:15px; border-radius:6px; background:#fff; }
.chat-input .btn { border-radius:6px; }
.chat-body { padding:20px; }
.is-empty .chat-body { padding:0; flex:initial; }
.trips-col { padding:20px; border-radius:10px; }
.trips-col .section-title { font-size:18px; color:var(--text); }
.trips-col .section-sub { font-size:14px; margin-block:8px 16px; }
.trips-col .btn { padding:8px 14px; font-size:14px; }
.bubble { font-size:16px; }
.chat-msg.assistant { flex-wrap:wrap; }


</style>
