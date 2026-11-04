<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  MapPin,
  CalendarDays,
  Clock,
  Sparkles,
  ArrowRight,
  ArrowUpRight,
} from 'lucide-vue-next'
import heroImg from '@/assets/scenery/hero.jpg'
import { listSpots } from '@/api'
import { spotCover } from '@/utils/spotCover'
import type { Spot } from '@/types'

const router = useRouter()

const query = ref('')
const tripDate = ref('')
const tripDays = ref('')

const categories = computed(() => ['全部', ...new Set(destCards.value.map(d => d.category))])
const activeCat = ref('全部')
const destCards = ref<Spot[]>([])
const loadError = ref(false)
onMounted(async () => {
  try { destCards.value = (await listSpots()).list } catch { loadError.value = true }
})

const filteredCards = computed(() =>
  activeCat.value === '全部'
    ? destCards.value.slice(0, 3)
    : destCards.value.filter((d) => d.category === activeCat.value),
)

function search() {
  router.push({ path: tripDate.value || tripDays.value ? '/plan' : '/spots', query: { q: query.value.trim() || undefined, date: tripDate.value || undefined, days: tripDays.value || undefined } })
}
</script>

<template>
  <div>
    <!-- Hero -->
    <section class="hero">
      <img class="hero-bg" :src="heroImg" alt="雪山云海风光" />
      <div class="hero-overlay"></div>
      <div class="container hero-inner">
        <p class="hero-kicker">走进风景，也走近自己</p>
        <h1 class="hero-title">下一程，去向心之所往</h1>
        <p class="hero-sub">从一个旅行想法，到一份专属行程。</p>

        <div class="hero-search focus-ring">
          <div class="search-field">
            <MapPin :size="18" class="search-ico" />
            <input
              v-model="query"
              class="search-input"
              type="text"
              placeholder="想去哪里旅行？"
              @keyup.enter="search"
            />
          </div>
          <div class="search-divider"></div>
          <div class="search-field">
            <CalendarDays :size="18" class="search-ico" />
            <input v-model="tripDate" type="date" aria-label="出发日期" class="search-input" />
          </div>
          <div class="search-divider"></div>
          <div class="search-field">
            <Clock :size="18" class="search-ico" />
            <select v-model="tripDays" class="search-input search-select">
              <option value="" disabled selected>选择天数</option>
              <option v-for="n in 10" :key="n" :value="n">{{ n }} 天</option>
            </select>
          </div>
          <button class="btn btn-gold search-btn" @click="search">
            {{ tripDate || tripDays ? '规划这次旅行' : '探索目的地' }}
            <ArrowRight :size="16" />
          </button>
        </div>
      </div>
    </section>

    <!-- AI 规划 banner -->
    <section class="container ai-section">
      <div class="ai-banner">
        <div class="ai-icon">
          <Sparkles :size="26" />
        </div>
        <div class="ai-text">
          <h2 class="ai-title">AI 旅行规划师</h2>
          <p class="ai-desc">路线、预算与停留，把想法慢慢安排好</p>
        </div>
        <RouterLink to="/plan" class="ai-btn">
          开始规划
          <ArrowRight :size="16" />
        </RouterLink>
      </div>
    </section>

    <!-- 值得出发的地方 -->
    <section class="container dest-section">
      <div class="dest-head">
        <div class="dest-heading">
          <span class="dest-bar"></span>
          <div>
            <h2 class="dest-title">值得出发的地方</h2>
            <p class="dest-sub">从心动的风景，出发。</p>
          </div>
        </div>
        <div class="dest-filters">
          <button
            v-for="c in categories"
            :key="c"
            class="dest-chip"
            :class="{ active: activeCat === c }"
            @click="activeCat = c"
          >
            {{ c }}
          </button>
        </div>
      </div>

      <div v-if="filteredCards.length" class="dest-grid">
        <RouterLink
          v-for="d in filteredCards"
          :key="d.name"
          :to="`/spots/${d.id}`"
          class="dest-card card"
        >
          <div class="dest-img">
            <img :src="spotCover(d)" :alt="d.name" loading="lazy" />
            <span class="dest-tag">{{ d.category }}</span>
          </div>
          <div class="dest-body">
            <div class="dest-row">
              <div class="dest-titles">
                <h3 class="dest-name">{{ d.name }}</h3>
                <p class="dest-location">{{ d.city }}</p>
              </div>
              <span class="dest-arrow"><ArrowUpRight :size="18" /></span>
            </div>
            <p class="dest-desc">{{ d.summary }}</p>
          </div>
        </RouterLink>
      </div>
      <RouterLink v-if="filteredCards.length" to="/spots" class="browse-all">查看全部目的地 <ArrowRight :size="15" /></RouterLink>
      <p v-else class="dest-empty">{{ loadError ? '目的地加载失败，请刷新重试' : '暂无该分类的目的地' }}</p>
    </section>
  </div>
</template>

<style scoped>
.hero {
  position: relative;
  height: 640px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 40%;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(7, 81, 77, 0.08) 0%,
    rgba(7, 81, 77, 0.04) 42%,
    rgba(7, 81, 77, 0.58) 100%
  );
}

.hero-inner {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding-top: 40px;
}

.hero-kicker {
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.44em;
  color: rgba(255, 255, 255, 0.92);
  text-shadow: 0 2px 12px rgba(0, 20, 35, 0.4);
}

.hero-title {
  margin-top: 20px;
  font-size: clamp(42px, 6vw, 68px);
  font-weight: 800;
  letter-spacing: 0.06em;
  color: #fff;
  text-shadow: 0 6px 30px rgba(0, 20, 35, 0.42);
}

.hero-sub {
  margin-top: 20px;
  font-size: 17px;
  color: rgba(255, 255, 255, 0.94);
  letter-spacing: 0.05em;
  text-shadow: 0 2px 12px rgba(0, 20, 35, 0.35);
}

.hero-search {
  margin-top: 44px;
  width: min(880px, 100%);
  display: flex;
  align-items: center;
  gap: 4px;
  background: var(--white);
  border-radius: 16px;
  padding: 10px 12px;
  box-shadow: 0 24px 60px rgba(0, 20, 35, 0.35);
}

.search-field {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 6px 12px;
  min-width: 0;
}

.search-ico {
  color: var(--primary);
  flex-shrink: 0;
}

.search-input {
  width: 100%;
  min-width: 0;
  border: 0;
  outline: none;
  font: inherit;
  font-size: 14px;
  color: var(--text);
  background: transparent;
}

.search-input::placeholder {
  color: #a7b3be;
}

.search-divider {
  width: 1px;
  height: 26px;
  background: var(--border);
  flex-shrink: 0;
}

.search-select {
  appearance: none;
  cursor: pointer;
}

.search-btn {
  flex-shrink: 0;
  border-radius: 12px;
  padding: 13px 26px;
  font-size: 15px;
  gap: 8px;
}

/* AI 规划 banner */
.ai-section {
  margin-top: 56px;
}

.ai-banner {
  position: relative;
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 34px 40px;
  border-radius: var(--radius-lg);
  background: linear-gradient(120deg, #07514d 0%, #0f7d77 60%, #127f73 100%);
  color: #fff;
  overflow: hidden;
  box-shadow: var(--shadow-lg);
}

.ai-icon {
  flex-shrink: 0;
  width: 58px;
  height: 58px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.18);
  color: var(--gold);
}

.ai-text {
  flex: 1;
  text-align: left;
}

.ai-title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.02em;
}

.ai-desc {
  margin-top: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.84);
}

.ai-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 26px;
  border-radius: 999px;
  border: 1.5px solid rgba(255, 255, 255, 0.55);
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s ease;
}

.ai-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  border-color: #fff;
}

/* 值得出发的地方 */
.dest-section {
  margin-top: 64px;
  padding-bottom: 72px;
}

.dest-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.dest-heading {
  display: flex;
  align-items: center;
  gap: 14px;
}

.dest-bar {
  width: 5px;
  height: 42px;
  border-radius: 3px;
  background: linear-gradient(180deg, #f2c05c, #dd9a2e);
}

.dest-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--text);
}

.dest-sub {
  font-size: 14px;
  color: var(--muted);
  margin-top: 2px;
}

.dest-filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.dest-chip {
  padding: 8px 20px;
  border-radius: 999px;
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--muted);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.dest-chip:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.dest-chip.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
  font-weight: 600;
}

.dest-grid {
  margin-top: 28px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.dest-card {
  position: relative;
  display: block;
  overflow: hidden;
  transition: transform 0.22s ease, box-shadow 0.25s ease;
}

.dest-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.dest-img {
  position: relative;
  height: 190px;
  overflow: hidden;
}

.dest-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.dest-card:hover .dest-img img {
  transform: scale(1.06);
}

.dest-tag {
  position: absolute;
  left: 14px;
  top: 14px;
  padding: 4px 13px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--primary-deep);
  font-size: 12px;
  font-weight: 700;
}

.dest-body {
  padding: 18px 20px 20px;
}

.dest-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dest-titles {
  min-width: 0;
}

.dest-name {
  font-size: 18px;
  font-weight: 800;
  color: var(--text);
}

.dest-location {
  margin-top: 3px;
  font-size: 13px;
  color: var(--primary);
  font-weight: 600;
}

.dest-arrow {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg);
  color: var(--primary);
  border: 1px solid var(--border);
  transition: all 0.2s ease;
}

.dest-card:hover .dest-arrow {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.dest-desc {
  margin-top: 10px;
  font-size: 13px;
  color: var(--muted);
  line-height: 1.7;
}

.dest-empty {
  margin-top: 28px;
  text-align: center;
  color: var(--muted);
}


.hero { height:460px; }
.hero-overlay { background:linear-gradient(180deg,rgba(12,32,29,.12),rgba(12,32,29,.32)); }
.hero-inner { padding-top:0; }
.hero-kicker { letter-spacing:.25em; font-size:14px; font-weight:400; }
.hero-title { font-size:clamp(30px,3.6vw,46px); font-weight:600; letter-spacing:.04em; text-shadow:none; }
.hero-sub { font-size:17px; text-shadow:none; }
.hero-search { border-radius:12px; box-shadow:0 1px 3px #00000012; margin-top:28px; }
.search-btn { border-radius:8px; }
.ai-section { margin-top:24px; }
.ai-banner { padding:16px 24px; background:#f1f4ef; color:var(--text); border:1px solid #dce5da; border-radius:12px; box-shadow:none; gap:20px; }
.ai-icon { width:44px; height:44px; background:transparent; border:0; color:var(--primary); }
.ai-title { font-size:17px; font-weight:600; }
.ai-desc { color:var(--muted); font-size:14px; }
.ai-btn { color:var(--primary-deep); border:1px solid #b7c8bb; border-radius:8px; padding:10px 20px; }
.ai-btn:hover { background:#e1eade; }
.dest-section { margin-top:48px; }
.dest-title { font-size:25px; font-weight:600; }
.dest-bar { display:none; }
.dest-grid { grid-template-columns:repeat(3,minmax(0,1fr)); gap:24px; }
.dest-img { height:185px; }
.dest-body { padding:20px; }
.dest-name { font-size:20px; font-weight:600; }
.dest-location { font-size:14px; font-weight:400; }
.dest-desc { font-size:14px; line-height:1.75; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.dest-card { border-radius:12px; }
.dest-card:hover { transform:translateY(-2px); }
.dest-tag { border-radius:6px; font-weight:500; font-size:14px; }
.dest-arrow { border:0; background:#f0f3ed; }
.browse-all { display:flex; align-items:center; justify-content:center; gap:8px; color:var(--primary-deep); font-size:14px; margin-top:28px; }
.browse-all:hover { text-decoration:underline; }



</style>