<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminReviews, adminUpdateReviewStatus, adminBatchReviews } from '@/api'
import type { CommentItem } from '@/types'

const rows = ref<CommentItem[]>([])
const statusFilter = ref<'全部' | string>('全部')
const selected = ref<Set<number>>(new Set())
const loading = ref(true)
const error = ref('')

const statuses = ['通过', '待审', '删除']

const filtered = computed(() =>
  rows.value.filter((r) => statusFilter.value === '全部' || r.status === statusFilter.value),
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await adminReviews({ status: statusFilter.value === '全部' ? undefined : statusFilter.value })
    rows.value = data.list
  } catch (e) {
    error.value = (e as Error).message
    rows.value = []
  } finally {
    loading.value = false
  }
}

function toggleSelect(id: number) {
  const s = new Set(selected.value)
  if (s.has(id)) s.delete(id)
  else s.add(id)
  selected.value = s
}

async function pass(row: CommentItem) {
  try {
    await adminUpdateReviewStatus(row.id, '通过')
    row.status = '通过'
    const s = new Set(selected.value)
    s.delete(row.id)
    selected.value = s
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function remove(row: CommentItem) {
  try {
    await adminUpdateReviewStatus(row.id, '删除')
    row.status = '删除'
    const s = new Set(selected.value)
    s.delete(row.id)
    selected.value = s
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function batchPass() {
  const ids = [...selected.value]
  if (!ids.length) return
  try {
    await adminBatchReviews(ids, '通过')
    rows.value.forEach((r) => {
      if (selected.value.has(r.id)) r.status = '通过'
    })
    selected.value = new Set()
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function batchRemove() {
  const ids = [...selected.value]
  if (!ids.length) return
  try {
    await adminBatchReviews(ids, '删除')
    rows.value.forEach((r) => {
      if (selected.value.has(r.id)) r.status = '删除'
    })
    selected.value = new Set()
  } catch (e) {
    error.value = (e as Error).message
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <h1 class="page-title">评论审核</h1>
    <p class="page-sub">
      共 {{ filtered.length }} 条评论，待审 {{ rows.filter((r) => r.status === '待审').length }} 条
    </p>

    <div class="toolbar">
      <div class="chips">
        <button
          v-for="s in ['全部', ...statuses]"
          :key="s"
          class="chip"
          :class="{ active: statusFilter === s }"
          @click="statusFilter = s as any; load()"
        >
          {{ s }}
        </button>
      </div>
      <div class="batch-ops">
        <button class="op-btn" :disabled="selected.size === 0" @click="batchPass">
          批量通过（{{ selected.size }}）
        </button>
        <button class="op-btn danger" :disabled="selected.size === 0" @click="batchRemove">
          批量删除（{{ selected.size }}）
        </button>
      </div>
    </div>

    <p v-if="error" class="page-error">{{ error }}</p>

    <div class="review-list">
      <div v-for="row in filtered" :key="row.id" class="review-row card">
        <input
          type="checkbox"
          class="check"
          :checked="selected.has(row.id)"
          @change="toggleSelect(row.id)"
        />
        <div class="review-main">
          <p class="review-post">{{ row.post }}</p>
          <p class="review-content">{{ row.content }}</p>
          <p class="review-meta">{{ row.author }} · {{ (row.date || '').slice(0, 10) }}</p>
        </div>
        <span class="status-pill" :class="row.status">
          {{ row.status }}
        </span>
        <div class="review-ops">
          <button v-if="row.status !== '通过'" class="op-btn" @click="pass(row)">通过</button>
          <button v-if="row.status !== '删除'" class="op-btn danger" @click="remove(row)">
            删除
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--primary);
}

.page-sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--muted);
}

.page-error {
  margin: 14px 0;
  font-size: 13px;
  color: #c0392b;
  background: rgba(192, 57, 43, 0.08);
  border-radius: 8px;
  padding: 8px 12px;
}

.toolbar {
  margin: 18px 0 22px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.chips {
  display: flex;
  gap: 8px;
}

.chip {
  padding: 6px 14px;
  border: 1px solid var(--border);
  border-radius: 999px;
  font-size: 12px;
  color: var(--muted);
  background: var(--card);
  cursor: pointer;
  transition: all 0.15s ease;
}

.chip:hover {
  border-color: var(--gold);
  color: var(--gold-deep);
}

.chip.active {
  background: var(--primary);
  border-color: var(--primary);
  color: var(--gold);
  font-weight: 600;
}

.batch-ops {
  display: flex;
  gap: 10px;
}

.op-btn {
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--primary);
  font-size: 12px;
  padding: 6px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.op-btn:hover {
  border-color: var(--gold);
  color: var(--gold-deep);
}

.op-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.op-btn.danger:hover {
  border-color: #c9504f;
  color: #c9504f;
  background: rgba(201, 80, 79, 0.06);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 22px;
}

.check {
  width: 16px;
  height: 16px;
  accent-color: var(--gold);
  flex-shrink: 0;
}

.review-main {
  flex: 1;
  min-width: 0;
}

.review-post {
  font-size: 13px;
  color: var(--primary);
  font-weight: 700;
}

.review-content {
  margin-top: 4px;
  font-size: 14px;
  color: var(--text);
}

.review-meta {
  margin-top: 2px;
  font-size: 12px;
  color: var(--muted);
}

.status-pill {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.status-pill.通过 {
  background: rgba(0, 144, 160, 0.12);
  color: var(--teal);
}

.status-pill.待审 {
  background: rgba(240, 192, 96, 0.2);
  color: var(--gold-deep);
}

.status-pill.删除 {
  background: rgba(124, 139, 153, 0.14);
  color: var(--muted);
}

.review-ops {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}
</style>
