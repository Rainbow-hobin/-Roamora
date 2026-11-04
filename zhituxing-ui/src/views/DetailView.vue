<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MapPin, Clock, CalendarDays, Star, Heart } from 'lucide-vue-next'
import { getSpot, listSpotComments, addFavorite, removeFavorite, myFavorites } from '@/api'
import { getToken } from '@/api/http'
import type { CommentItem, Spot } from '@/types'
import SourceNotice from '@/components/SourceNotice.vue'
import { destinationInfo } from '@/utils/content'
import { spotCover } from '@/utils/spotCover'

const route = useRoute()
const router = useRouter()
const spot = ref<Spot | null>(null)
const comments = ref<CommentItem[]>([])
const liked = ref(false)
const favLoaded = ref(false)

/** 演示景点经纬度（用于地图展示；后台新增景点未收录时降级为占位图） */
const SPOT_COORDS: Record<number, { lat: number; lng: number }> = {
  1: { lat: 29.0238, lng: 107.1412 }, // 金佛山
  2: { lat: 29.4021, lng: 107.7563 }, // 喀斯特（武隆）
  3: { lat: 29.707, lng: 105.7113 }, // 大足石刻
  4: { lat: 39.9163, lng: 116.3972 }, // 故宫
}

const info = computed(() => spot.value ? destinationInfo(spot.value.name) : undefined)
const cover = computed(() => (spot.value ? spotCover(spot.value) : ''))

const mapSrc = computed(() => {
  const c = spot.value ? SPOT_COORDS[spot.value.id] : undefined
  if (!c) return null
  const d = 0.02
  const bbox = `${c.lng - d},${c.lat - d},${c.lng + d},${c.lat + d}`
  return `https://www.openstreetmap.org/export/embed.html?bbox=${bbox}&layer=mapnik&marker=${c.lat},${c.lng}`
})

function starsOf(rating: number | null): string {
  const r = Math.max(1, Math.min(5, Math.round(Number(rating) || 5)))
  return '★'.repeat(r) + '☆'.repeat(5 - r)
}

async function toggleLike() {
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    if (liked.value) {
      await removeFavorite(spot.value!.id)
      liked.value = false
    } else {
      await addFavorite(spot.value!.id)
      liked.value = true
    }
  } catch (e) {
    alert((e as Error).message)
  }
}

onMounted(async () => {
  const id = Number(route.params.id)
  try {
    const data = await getSpot(id)
    spot.value = data
  } catch {
    spot.value = null
  }
  try {
    const page = await listSpotComments(id)
    comments.value = page.list
  } catch {
    comments.value = []
  }
  if (getToken()) {
    try {
      const favs = await myFavorites()
      liked.value = favs.some((f) => f.id === Number(route.params.id))
    } catch {
      /* ignore */
    }
  }
  favLoaded.value = true
})
</script>

<template>
  <div v-if="spot" class="container page">
    <!-- 顶部大图 -->
    <div class="hero-panel">
      <img :src="cover" :alt="spot.name" />
      <span class="hero-tag">{{ spot.name }} {{ spot.city }} {{ spot.category }}{{ spot.cover?.endsWith('.svg') ? ' · 示意封面' : '' }}</span>
    </div>

    <div class="detail-grid">
      <!-- 左栏信息 -->
      <div class="info-col">

        <h1 class="spot-name">{{ spot.name }}</h1>
        <ul class="info-list">
          <li class="info-item">
            <MapPin :size="16" />
            <span class="info-label">地址</span>
            <span class="info-value">{{ spot.address || '详见景区地图' }}</span>
          </li>
          <li class="info-item">
            <span class="info-label">价格</span>
            <span v-if="info" class="info-value">{{ info.priceLabel }}</span>
            <span v-else class="price-tag"><span class="cur">¥</span><span class="num">{{ spot.price }}</span></span>
          </li>
          <li class="info-item">
            <Clock :size="16" />
            <span class="info-label">游玩时长</span>
            <span class="info-value">{{ info?.duration || '视个人安排' }}</span>
          </li>
          <li class="info-item">
            <CalendarDays :size="16" />
            <span class="info-label">开放时间</span>
            <span class="info-value">{{ spot.openingHours || '以景区公告为准' }}</span>
          </li>
          <li class="info-item">
            <Star :size="16" />
            <span class="info-label">评分</span>
            <span v-if="spot.rating != null" class="rating">★★★★★ {{ Number(spot.rating).toFixed(1) }}</span>
            <span v-else class="info-value">暂无评分</span>
          </li>
        </ul>

        <div class="action-row">
<RouterLink v-if="spot" :to="{ path: '/plan', query: { q: spot.name } }" class="btn btn-gold">用此景点规划</RouterLink>
          <button class="btn btn-outline action-btn" :class="{ liked }" @click="toggleLike">
            <Heart :size="15" :fill="liked ? 'currentColor' : 'none'" />
            {{ liked ? '已收藏' : '收藏景点' }}
          </button>
        </div>

        <div class="intro">
          <h2 class="intro-title">景区简介</h2>
          <p class="intro-text">{{ spot.description || spot.summary }}</p>
          <SourceNotice v-if="info" :sources="info.sources" :date="info.verifiedAt" />
        </div>
      </div>

      <!-- 右栏 地图 + 评论 -->
      <aside class="side-col">
        <div class="side-card card">
          <h3 class="side-title">景区地图</h3>
          <iframe
            v-if="mapSrc"
            :src="mapSrc"
            class="map-frame"
            loading="lazy"
            title="景区地图"
          ></iframe>
          <template v-else>
            <svg class="map-svg" viewBox="0 0 320 180" aria-label="景区示意地图">
              <path
                d="M10 140 C60 90 110 180 160 120 S270 60 310 100"
                fill="none"
                stroke="#d5e0e8"
                stroke-width="3"
              />
              <path
                d="M10 150 C70 110 130 185 180 135 S260 80 310 115"
                fill="none"
                stroke="#e6edf3"
                stroke-width="2"
              />
              <path
                d="M20 60 C80 30 140 90 190 50 S280 30 305 55"
                fill="none"
                stroke="#c3d3de"
                stroke-width="2"
                stroke-dasharray="5 6"
              />
              <circle cx="160" cy="105" r="7" fill="#f0c060" stroke="#ffffff" stroke-width="2.5" />
              <circle cx="160" cy="105" r="14" fill="none" stroke="#f0c060" stroke-width="1.2" opacity="0.55" />
            </svg>
          </template>
          <p class="map-note"><MapPin :size="13" /> {{ mapSrc ? '高德地图数据参考（OpenStreetMap 展示）' : '地图暂未收录，以景区现场指示为准' }}</p>
        </div>

        <div class="side-card card">
          <h3 class="side-title">用户评论</h3>
          <div v-if="comments.length" class="comment-list">
            <div v-for="c in comments" :key="c.id" class="comment">
              <span class="avatar">{{ c.author.slice(0, 1) }}</span>
              <div class="comment-main">
                <span class="comment-user">{{ c.author }}</span>
                <span v-if="c.rating" class="comment-stars">{{ starsOf(c.rating) }}</span>
                <p class="comment-text">{{ c.content }}</p>
              </div>
            </div>
          </div>
          <p v-else class="comment-empty">暂无评论，快来抢沙发</p>
        </div>
      </aside>
    </div>
  </div>
  <p v-else class="container empty-tip">景点不存在或已下架</p>
</template>

<style scoped>
.page {
  padding-block: 36px 60px;
}

.hero-panel {
  position: relative;
  height: 400px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.hero-panel img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-tag {
  position: absolute;
  left: 18px;
  bottom: 18px;
  padding: 6px 18px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(15, 125, 119, 0.92), rgba(7, 81, 77, 0.88));
  color: var(--gold);
  font-size: 13px;
  font-weight: 600;
}

.detail-grid {
  margin-top: 30px;
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 34px;
  align-items: start;
}

.info-col {
  min-width: 0;
}

.spot-name {
  font-size: 30px;
  font-weight: 800;
  color: var(--primary);
}

.info-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--text);
}

.info-item svg {
  color: var(--primary);
  opacity: 0.7;
}

.info-label {
  color: var(--muted);
  width: 72px;
  flex-shrink: 0;
}

.info-value {
  color: var(--text);
}

.price-tag {
  display: inline-flex;
  align-items: baseline;
  gap: 2px;
  color: var(--gold-deep);
  font-weight: 800;
}

.price-tag .cur {
  font-size: 13px;
}

.price-tag .num {
  font-size: 22px;
}

.rating {
  color: var(--gold-deep);
  font-weight: 600;
}

.action-row {
  margin-top: 24px;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.action-btn {
  min-width: 150px;
  padding: 11px 26px;
}

.action-btn.liked {
  border-color: var(--gold);
  color: var(--gold-deep);
  background: rgba(240, 192, 96, 0.1);
}

.intro {
  margin-top: 30px;
}

.intro-title {
  font-size: 19px;
  font-weight: 700;
  color: var(--primary);
  padding-bottom: 12px;
  border-bottom: 2px solid rgba(240, 192, 96, 0.45);
}

.intro-text {
  margin-top: 14px;
  font-size: 14px;
  line-height: 1.9;
  color: var(--text);
  text-align: justify;
}

.side-col {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.side-card {
  padding: 18px 20px;
}

.side-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 12px;
}

.map-svg {
  width: 100%;
  height: auto;
  border-radius: 10px;
  background: var(--bg);
}

.map-frame {
  width: 100%;
  height: 220px;
  border: 0;
  border-radius: 10px;
  background: var(--bg);
}

.map-note {
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
  display: flex;
  align-items: center;
  gap: 4px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.comment {
  display: flex;
  gap: 10px;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--white);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.comment-main {
  min-width: 0;
  flex: 1;
}

.comment-user {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
}

.comment-stars {
  margin-left: 6px;
  font-size: 11px;
  color: var(--gold-deep);
}

.comment-text {
  margin-top: 3px;
  font-size: 13px;
  color: var(--text);
  line-height: 1.6;
}

.comment-empty {
  font-size: 13px;
  color: var(--muted);
}

.empty-tip {
  padding-block: 60px;
  text-align: center;
  color: var(--muted);
}


.page { padding-block:40px 72px; }
.hero-panel { height:360px; border-radius:12px; box-shadow:none; }
.detail-grid { gap:40px; margin-top:32px; grid-template-columns:minmax(0,1fr) 320px; }
.spot-name { color:var(--text); font-size:36px; font-weight:600; }
.info-list { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:12px 24px; margin-block:24px; }
.info-item { flex-wrap:wrap; font-size:15px; }
.info-label { color:var(--muted); }
.action-row { display:flex; flex-wrap:wrap; gap:12px; margin-block:24px 40px; }
.intro-title, .side-title { color:var(--text); font-weight:600; }
.intro-title { border-color:var(--border); }
.intro-text { font-size:16px; line-height:1.85; }
.side-card { padding:24px; }
.comment-text { font-size:14px; }



.intro-text { white-space:pre-line; }
.hero-panel img[src$=".svg"] { object-fit:contain; background:#edf1e7; }
</style>
