<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminUsers, adminUpdateUserStatus } from '@/api'
import type { AdminUser } from '@/types'

const rows = ref<AdminUser[]>([])
const query = ref('')
const statusFilter = ref<'全部' | '正常' | '禁用'>('全部')
const loading = ref(true)
const error = ref('')

const filtered = computed(() =>
  rows.value.filter((u) => {
    const q = query.value.trim()
    const okQuery = !q || u.name.includes(q) || u.phone.includes(q)
    const okStatus = statusFilter.value === '全部' || u.status === statusFilter.value
    return okQuery && okStatus
  }),
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await adminUsers({ status: statusFilter.value === '全部' ? undefined : statusFilter.value })
    rows.value = data.list
  } catch (e) {
    error.value = (e as Error).message
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function toggleStatus(row: AdminUser) {
  const target = row.status === '正常' ? '禁用' : '正常'
  const msg =
    target === '禁用'
      ? `确定要禁用用户「${row.name}」吗？禁用后该用户将无法登录。`
      : `确定要恢复用户「${row.name}」吗？`
  if (!window.confirm(msg)) return
  try {
    await adminUpdateUserStatus(row.id, target)
    row.status = target
  } catch (e) {
    error.value = (e as Error).message
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <h1 class="page-title">用户管理</h1>
    <p class="page-sub">共 {{ filtered.length }} 个用户</p>

    <div class="toolbar">
      <input v-model="query" type="text" class="search-input" placeholder="搜索用户名 / 手机号" />
      <div class="chips">
        <button
          v-for="s in ['全部', '正常', '禁用']"
          :key="s"
          class="chip"
          :class="{ active: statusFilter === s }"
          @click="statusFilter = s as any; load()"
        >
          {{ s }}
        </button>
      </div>
    </div>

    <p v-if="error" class="page-error">{{ error }}</p>

    <div class="table card">
      <div class="table-head">
        <span class="col-id">ID</span>
        <span class="col-name">用户名</span>
        <span class="col-phone">手机号</span>
        <span class="col-trips">行程数</span>
        <span class="col-status">状态</span>
        <span class="col-ops">操作</span>
      </div>
      <div v-for="row in filtered" :key="row.id" class="table-row">
        <span class="col-id">{{ row.id }}</span>
        <span class="col-name">{{ row.name }}</span>
        <span class="col-phone">{{ row.phone }}</span>
        <span class="col-trips">{{ row.trips }}</span>
        <span class="col-status">
          <span class="status-pill" :class="row.status === '正常' ? 'on' : 'off'">
            {{ row.status }}
          </span>
        </span>
        <span class="col-ops">
          <button class="op-btn" @click="toggleStatus(row)">
            {{ row.status === '正常' ? '禁用' : '恢复' }}
          </button>
        </span>
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
  gap: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 260px;
  padding: 9px 14px;
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 13px;
  color: var(--text);
  background: var(--card);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.search-input:focus {
  outline: none;
  border-color: var(--gold);
  box-shadow: 0 0 0 3px rgba(240, 192, 96, 0.2);
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

.table {
  overflow-x: auto;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 14px;
}

.table-head,
.table-row {
  display: grid;
  grid-template-columns: 60px 1fr 1.4fr 90px 110px 110px;
  align-items: center;
  gap: 12px;
  padding: 14px 22px;
  min-width: 720px;
}

.table-head {
  background: linear-gradient(135deg, var(--primary), var(--primary-soft));
  color: var(--gold);
  font-size: 13px;
  font-weight: 700;
}

.table-row {
  font-size: 14px;
  color: var(--text);
  border-top: 1px solid var(--border);
}

.table-row:hover {
  background: rgba(240, 192, 96, 0.06);
}

.col-id {
  color: var(--muted);
}

.col-name {
  font-weight: 700;
  color: var(--primary);
}

.status-pill {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-pill.on {
  background: rgba(0, 144, 160, 0.12);
  color: var(--teal);
}

.status-pill.off {
  background: rgba(201, 80, 79, 0.1);
  color: #c9504f;
}

.op-btn {
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--primary);
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.op-btn:hover {
  border-color: var(--gold);
  color: var(--gold-deep);
}
</style>
