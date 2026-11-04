<script setup lang="ts">
import type { TripItem } from '@/types'
import LogoMark from '@/components/ui/LogoMark.vue'

defineProps<{ trip: TripItem }>()

function metaOf(trip: TripItem): string {
  return `${trip.days}天${trip.spotCount}景点`
}

function dateOf(trip: TripItem): string {
  const start = (trip.startDate || '').replaceAll('-', '/')
  const end = (trip.endDate || '').replaceAll('-', '/')
  return start ? `${start}-${end || '…'}` : '—'
}
</script>

<template>
  <div class="trip-card card">
    <div class="trip-icon">
      <LogoMark :size="34" />
    </div>
    <div class="trip-main">
      <div class="trip-head">
        <h4 class="trip-title">{{ trip.title }}</h4>
        <span v-if="trip.status" class="trip-tag">{{ trip.status }}</span>
      </div>
      <p class="trip-meta">
        {{ metaOf(trip) }} · {{ dateOf(trip) }}
      </p>
    </div>
    <RouterLink :to="`/trips/${trip.id}`" class="btn btn-outline trip-btn">查看行程</RouterLink>
  </div>
</template>

<style scoped>
.trip-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
}

.trip-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary), var(--primary-soft));
  color: var(--gold);
  flex-shrink: 0;
}

.trip-main {
  flex: 1;
  min-width: 0;
}

.trip-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trip-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--primary);
}

.trip-tag {
  padding: 1px 8px;
  border-radius: 999px;
  background: rgba(240, 192, 96, 0.18);
  color: var(--gold-deep);
  font-size: 11px;
  font-weight: 600;
}

.trip-meta {
  margin-top: 3px;
  font-size: 12px;
  color: var(--muted);
}

.trip-btn {
  flex-shrink: 0;
  padding: 6px 16px;
  font-size: 12px;
}
</style>
