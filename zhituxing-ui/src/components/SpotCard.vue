<script setup lang="ts">
import { priceBrief } from '@/utils/content'
import type { Spot } from '@/types'
import { spotCover as coverOf } from '@/utils/spotCover'

defineProps<{ spot: Spot }>()
</script>

<template>
  <RouterLink :to="`/spots/${spot.id}`" class="spot-card card">
    <div class="spot-img">
      <img :src="coverOf(spot)" :alt="spot.name" loading="lazy" />
      <span class="spot-cat">{{ spot.category }}</span>
    </div>
    <div class="spot-body">
      <div class="spot-head">
        <h3 class="spot-name">{{ spot.name }}</h3>
        <span class="price-tag">{{ priceBrief(spot.name, spot.price) }}</span>
      </div>
      <p class="spot-desc">{{ spot.summary }}</p>
      <div class="spot-meta">
        <span class="spot-city">{{ spot.city }}</span>
        <span v-if="spot.rating != null" class="spot-rating">★★★★★ {{ Number(spot.rating).toFixed(1) }}</span><span v-else>暂无评分</span>
      </div>
    </div>
  </RouterLink>
</template>

<style scoped>
.spot-card {
  display: block;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.25s ease;
}

.spot-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.spot-img {
  position: relative;
  height: 150px;
  overflow: hidden;
}

.spot-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.spot-card:hover .spot-img img {
  transform: scale(1.05);
}

.spot-cat {
  position: absolute;
  left: 12px;
  bottom: 12px;
  padding: 3px 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(15, 125, 119, 0.92), rgba(7, 81, 77, 0.88));
  color: var(--gold);
  font-size: 12px;
  font-weight: 600;
}

.spot-body {
  padding: 14px 16px 16px;
}

.spot-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.spot-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--primary);
}

.spot-desc {
  margin-top: 4px;
  font-size: 13px;
  color: var(--muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.spot-meta {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--muted);
}

.spot-rating {
  color: var(--gold-deep);
  font-weight: 600;
}

.spot-img { height:210px; }
.spot-body { padding:24px; }
.spot-name { font-size:21px; color:var(--text); }
.spot-desc { font-size:15px; line-height:1.75; }
.spot-meta { font-size:14px; }

.price-tag { font-size:14px; white-space:nowrap; }
</style>
