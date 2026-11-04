<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, CalendarDays, MapPin, Clock } from 'lucide-vue-next'
import { getTripDetail } from '@/api'
import type { TripDetail } from '@/types'

const route = useRoute()
const router = useRouter()
const trip = ref<TripDetail | null>(null)
const loading = ref(true)

function dateOf(d: string | null): string {
  return d ? d.replaceAll('-', '/') : '—'
}

onMounted(async () => {
  try {
    trip.value = await getTripDetail(Number(route.params.id))
  } catch {
    trip.value = null
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container page">
    <button class="back-btn" @click="router.push('/plan')">
      <ArrowLeft :size="15" /> 返回智能规划
    </button>

    <div v-if="loading" class="loading-tip">加载中…</div>

    <template v-else-if="trip">
      <div class="trip-head card">
        <div class="trip-head-main">
          <h1 class="trip-title">{{ trip.title }}</h1>
          <span v-if="trip.status" class="trip-tag">{{ trip.status }}</span>
        </div>
        <p class="trip-meta">
          <CalendarDays :size="14" />
          {{ trip.days }}天 · {{ dateOf(trip.startDate) }} - {{ dateOf(trip.endDate) }}
        </p>
        <p class="trip-meta">共 {{ trip.spotCount }} 个景点</p>
      </div>

      <div class="spot-list">
        <h2 class="section-title">行程景点</h2>
        <div v-for="(s, i) in trip.spots" :key="s.id" class="spot-row card">
          <span class="spot-index">{{ i + 1 }}</span>
          <div class="spot-main">
            <h3 class="spot-name">{{ s.name }}</h3>
            <p class="spot-info">
              <MapPin :size="13" /> {{ s.city }} · {{ s.category }}
            </p>
          </div>
          <div class="spot-side">
            <p class="spot-price">¥{{ s.price }}</p>
            <p class="spot-hours"><Clock :size="12" /> {{ s.openingHours || '以景区公告为准' }}</p>
          </div>
        </div>
        <p v-if="!trip.spots.length" class="empty-tip">该行程暂未安排景点</p>
      </div>
    </template>

    <p v-else class="empty-tip">行程不存在或已被删除</p>
  </div>
</template>

<style scoped>
.page {
  padding-block: 34px 60px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 0;
  background: transparent;
  color: var(--muted);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
  margin-bottom: 20px;
}

.back-btn:hover {
  color: var(--primary);
}

.loading-tip,
.empty-tip {
  padding: 40px 0;
  text-align: center;
  color: var(--muted);
}

.trip-head {
  padding: 24px 28px;
}

.trip-head-main {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.trip-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--primary);
}

.trip-tag {
  padding: 3px 12px;
  border-radius: 999px;
  background: rgba(240, 192, 96, 0.18);
  color: var(--gold-deep);
  font-size: 12px;
  font-weight: 600;
}

.trip-meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--muted);
}

.spot-list {
  margin-top: 28px;
}

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 16px;
}

.spot-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  margin-bottom: 12px;
}

.spot-index {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--primary), var(--primary-soft));
  color: var(--gold);
  font-size: 15px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.spot-main {
  flex: 1;
  min-width: 0;
}

.spot-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}

.spot-info {
  margin-top: 3px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--muted);
}

.spot-side {
  text-align: right;
  flex-shrink: 0;
}

.spot-price {
  font-size: 17px;
  font-weight: 800;
  color: var(--gold-deep);
}

.spot-hours {
  margin-top: 2px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  font-size: 12px;
  color: var(--muted);
}
</style>
