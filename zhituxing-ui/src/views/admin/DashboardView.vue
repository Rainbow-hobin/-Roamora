<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminDashboard } from '@/api'
import type { DashboardData } from '@/types'

const trendMode = ref<'7' | '30'>('7')
const data = ref<DashboardData | null>(null)
const error = ref('')

const trend = computed(() => data.value?.trend ?? [])
const trendLabel = computed(() => (trendMode.value === '7' ? '近 7 日内容趋势' : '近 30 日内容趋势'))
const adminStats = computed(() => data.value?.stats ?? [])
const categoryShare = computed(() => data.value?.categoryShare ?? [])
const pendingReviews = computed(() => data.value?.pending.reviews ?? 0)
const maxTrend = computed(() => (trend.value.length ? Math.max(...trend.value) : 1))

async function load() {
  error.value = ''
  try {
    data.value = await adminDashboard(Number(trendMode.value))
  } catch (e) {
    error.value = (e as Error).message
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <h1 class="page-title">数据看板</h1>
    <p class="page-sub">平台运营数据总览</p>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div v-for="s in adminStats" :key="s.label" class="stat-card card">
        <span class="stat-label">{{ s.label }}</span>
        <span class="stat-value">{{ s.value }}</span>
        <span class="stat-trend">{{ s.trend }}</span>
      </div>
    </div>

    <p v-if="error" class="dash-error">{{ error }}</p>

    <div class="dash-grid">
      <!-- 访问趋势 -->
      <div class="panel card">
        <div class="panel-head">
          <div>
            <h2 class="panel-title">{{ trendLabel }}</h2>
            <p class="panel-sub">单位：条</p>
          </div>
          <div class="range-switch">
            <button
              class="range-btn"
              :class="{ active: trendMode === '7' }"
              @click="trendMode = '7'; load()"
            >
              近7日
            </button>
            <button
              class="range-btn"
              :class="{ active: trendMode === '30' }"
              @click="trendMode = '30'; load()"
            >
              近30日
            </button>
          </div>
        </div>
        <div class="bars">
          <div v-for="(v, i) in trend" :key="i" class="bar-col">
            <span class="bar-num">{{ v }}</span>
            <div
              class="bar"
              :style="{ height: (v / maxTrend) * 100 + '%', minHeight: '6px' }"
            ></div>
            <span class="bar-label">{{ trendMode === '7' ? i + 1 + '日' : i + 1 + '日' }}</span>
          </div>
        </div>
      </div>

      <!-- 景点分类占比 -->
      <div class="panel card">
        <h2 class="panel-title">景点分类占比</h2>
        <p class="panel-sub">按已上架景点统计</p>
        <div class="share-list">
          <div v-for="c in categoryShare" :key="c.name" class="share-row">
            <span class="share-name">{{ c.name }}</span>
            <div class="share-track">
              <div class="share-fill" :style="{ width: c.value + '%' }"></div>
            </div>
            <span class="share-val">{{ c.value }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 待办 -->
    <div class="panel card todo-panel">
      <h2 class="panel-title">待办事项</h2>
      <div class="todo-row">
        <span class="todo-dot"></span>
        <p class="todo-text">评论审核：{{ pendingReviews }} 条评论待审核</p>
        <RouterLink to="/admin/reviews" class="btn btn-outline todo-btn">去处理</RouterLink>
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

.stat-grid {
  margin-top: 26px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.dash-error {
  margin-top: 18px;
  font-size: 13px;
  color: #c0392b;
  background: rgba(192, 57, 43, 0.08);
  border-radius: 8px;
  padding: 8px 12px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 20px 22px;
}

.stat-label {
  font-size: 13px;
  color: var(--muted);
}

.stat-value {
  font-size: 34px;
  font-weight: 800;
  color: var(--gold-deep);
  line-height: 1.2;
}

.stat-trend {
  font-size: 12px;
  color: var(--primary);
  background: rgba(0, 144, 160, 0.08);
  align-self: flex-start;
  padding: 2px 10px;
  border-radius: 999px;
}

.dash-grid {
  margin-top: 24px;
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 24px;
}

.panel {
  padding: 22px 26px;
}

.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.range-switch {
  display: flex;
  gap: 6px;
  background: #eef2f5;
  padding: 3px;
  border-radius: 10px;
}

.range-btn {
  border: none;
  background: transparent;
  padding: 5px 14px;
  border-radius: 8px;
  font-size: 12px;
  color: var(--muted);
  cursor: pointer;
  transition: all 0.15s ease;
}

.range-btn.active {
  background: var(--primary);
  color: var(--gold);
  font-weight: 700;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--primary);
}

.panel-sub {
  margin-top: 2px;
  font-size: 12px;
  color: var(--muted);
}

.bars {
  margin-top: 18px;
  height: 180px;
  display: flex;
  align-items: flex-end;
  gap: 14px;
}

.bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  height: 100%;
}

.bar-num {
  font-size: 11px;
  color: var(--muted);
}

.bar {
  width: 70%;
  border-radius: 6px 6px 2px 2px;
  background: linear-gradient(180deg, var(--gold), var(--gold-deep));
  box-shadow: 0 6px 14px rgba(240, 192, 96, 0.35);
  transition: height 0.5s ease;
}

.bar-label {
  font-size: 11px;
  color: var(--muted);
}

.share-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.share-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.share-name {
  width: 76px;
  font-size: 13px;
  color: var(--text);
  flex-shrink: 0;
}

.share-track {
  flex: 1;
  height: 12px;
  border-radius: 999px;
  background: #eef2f5;
  overflow: hidden;
}

.share-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--primary), var(--teal));
}

.share-val {
  width: 44px;
  text-align: right;
  font-size: 13px;
  font-weight: 700;
  color: var(--primary);
}

.todo-panel {
  margin-top: 24px;
}

.todo-row {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.todo-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--gold);
  box-shadow: 0 0 0 4px rgba(240, 192, 96, 0.2);
  flex-shrink: 0;
}

.todo-text {
  flex: 1;
  font-size: 14px;
  color: var(--text);
}

.todo-btn {
  padding: 7px 18px;
  font-size: 13px;
}
</style>
