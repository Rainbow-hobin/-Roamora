<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NotebookPen } from 'lucide-vue-next'
import { myNotes } from '@/api'
import { getToken } from '@/api/http'
import type { NoteItem } from '@/types'

const notes = ref<NoteItem[]>([])
const loading = ref(true)
const loggedIn = ref(false)

onMounted(async () => {
  loggedIn.value = !!getToken()
  if (!loggedIn.value) {
    loading.value = false
    return
  }
  try {
    const data = await myNotes()
    notes.value = data.list
  } catch {
    notes.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container page">
    <div class="page-head">
      <h1 class="section-title">旅游笔记</h1>
      <p class="section-sub">记录旅途中的所见所想</p>
    </div>

    <div v-if="loading" class="loading-tip">加载中…</div>
    <template v-else-if="notes.length">
      <div v-for="n in notes" :key="n.id" class="note-card card">
        <h3 class="note-title">{{ n.title }}</h3>
        <p class="note-date">{{ (n.createdAt || '').slice(0, 10) }}</p>
        <p v-if="n.content" class="note-content">{{ n.content }}</p>
      </div>
    </template>
    <div v-else class="empty-state">
      <NotebookPen :size="40" class="empty-icon" />
      <p class="empty-text">{{ loggedIn ? '还没有旅游笔记' : '登录后查看旅游笔记' }}</p>
      <p class="empty-sub">{{ loggedIn ? '发布新帖或在社区交流，让旅途更有记录' : '登录账号即可查看你的旅行记录' }}</p>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding-block: 44px 60px;
}

.page-head {
  margin-bottom: 26px;
}

.page-head .section-title {
  font-size: 28px;
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--muted);
}

.loading-tip {
  padding: 30px 0;
  text-align: center;
  color: var(--muted);
}

.note-card {
  padding: 18px 22px;
  margin-bottom: 14px;
}

.note-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary);
}

.note-date {
  margin-top: 4px;
  font-size: 12px;
  color: var(--muted);
}

.note-content {
  margin-top: 10px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--text);
  white-space: pre-wrap;
}

.empty-state {
  padding: 70px 0;
  text-align: center;
  color: var(--muted);
}

.empty-icon {
  color: var(--primary);
  opacity: 0.35;
}

.empty-text {
  margin-top: 16px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
}

.empty-sub {
  margin-top: 6px;
  font-size: 13px;
  color: var(--muted);
}
</style>
