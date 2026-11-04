<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { destinationCities } from '@/utils/content'
import SpotCard from '@/components/SpotCard.vue'
import { listSpots } from '@/api'
import type { Spot } from '@/types'

const route = useRoute()
const router = useRouter()
const loadError = ref(false)
let requestId = 0
function reset() { keyword.value = ''; city.value = '全部'; category.value = '全部'; router.replace({ path: '/spots' }) }

const cities = ['全部', ...destinationCities]
const categories = ['全部', '自然风光', '历史文化']
const sorts = ['最新发布']

const city = ref('全部')
const category = ref('全部')
const keyword = ref('')
const spots = ref<Spot[]>([])
const loading = ref(true)
const empty = ref(false)

async function load() {
  const id = ++requestId
  loading.value = true
  loadError.value = false
  try {
    const data = await listSpots({
      keyword: keyword.value || undefined,
      city: city.value === '全部' ? undefined : city.value,
      category: category.value === '全部' ? undefined : category.value,
    })
    if (id !== requestId) return
    spots.value = data.list
    empty.value = spots.value.length === 0
  } catch {
    if (id !== requestId) return
    loadError.value = true
    spots.value = []
    empty.value = true
  } finally {
    if (id === requestId) loading.value = false
  }
}

const filtered = computed(() => spots.value)

watch(city, () => { keyword.value = ''; router.replace({ path: '/spots' }) })
watch([city, category, keyword], load)
watch(() => route.query.q, q => { keyword.value = typeof q === 'string' ? q : '' })

onMounted(() => {
  const q = route.query.q as string | undefined
  if (q) keyword.value = q
  load()
})
</script>

<template>
  <div class="container page">
    <div class="page-head">
      <h1 class="section-title">探索精彩景点</h1>
      <p class="section-sub">发现世界各地的美景与故事</p>
    </div>

    <form class="search-row" @submit.prevent="load">
      <input v-model="keyword" aria-label="搜索景点或城市" placeholder="搜索景点或城市" />
      <button class="btn btn-gold">搜索</button>
      <button type="button" class="btn" @click="reset">重置全部</button>
    </form>
    <p class="section-sub">{{ keyword ? `关键词：${keyword} · ` : '' }}共 {{ spots.length }} 个景点</p>
    <div class="filter-bar">
      <div class="filter-group">
        <span class="filter-label">城市</span>
        <button
          v-for="c in cities"
          :key="c"
          class="chip"
          :class="{ active: city === c }"
          @click="city = c"
        >
          {{ c }}
        </button>
      </div>
      <div class="filter-group">
        <span class="filter-label">分类</span>
        <button
          v-for="c in categories"
          :key="c"
          class="chip"
          :class="{ active: category === c }"
          @click="category = c"
        >
          {{ c }}
        </button>
      </div>
      <div class="filter-group">
        <span class="filter-label">排序</span>
        <button v-for="s in sorts" :key="s" class="chip active">{{ s }}</button>
      </div>
    </div>

    <div v-if="loading" class="loading-tip">加载中…</div>
    <div v-else-if="filtered.length" class="spot-grid">
      <SpotCard v-for="spot in filtered" :key="spot.id" :spot="spot" />
    </div>
    <div v-else class="empty-tip"><p>{{ loadError ? '景点加载失败，请重试' : '没有符合条件的景点，试试清除关键词或切换城市' }}</p><button class="btn" @click="loadError ? load() : reset()">{{ loadError ? '重新加载' : '清除所有条件' }}</button></div>
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
}

.page-head .section-title {
  font-size: 28px;
}

.filter-bar {
  margin-top: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 18px 34px;
  align-items: center;
  padding: 16px 20px;
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 13px;
  font-weight: 700;
  color: var(--primary);
}

.spot-grid {
  margin-top: 26px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
}

.empty-tip {
  margin-top: 40px;
  text-align: center;
  color: var(--muted);
}

.loading-tip {
  margin-top: 40px;
  text-align: center;
  color: var(--muted);
}
.search-row { display:flex; flex-wrap:wrap; gap:10px; margin-block:20px 12px; }
.search-row input { flex:1; min-width:160px; padding:12px; border:1px solid var(--border); border-radius:10px; }

</style>
