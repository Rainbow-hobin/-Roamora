<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminSpots, adminCreateSpot, adminUpdateSpot, adminUpdateSpotStatus, adminDeleteSpot } from '@/api'
import type { Spot } from '@/types'

const rows = ref<Spot[]>([])
const query = ref('')
const cityFilter = ref('全部')
const catFilter = ref('全部')
const loading = ref(true)
const error = ref('')

const spotCities = ['重庆', '北京', '成都', '敦煌']
const spotCategories = ['自然风光', '历史文化']
const cities = ['全部', ...spotCities]
const cats = ['全部', ...spotCategories]

const filtered = computed(() =>
  rows.value.filter((s) => {
    const q = query.value.trim()
    const okQuery = !q || s.name.includes(q)
    const okCity = cityFilter.value === '全部' || s.city === cityFilter.value
    const okCat = catFilter.value === '全部' || s.category === catFilter.value
    return okQuery && okCity && okCat
  }),
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await adminSpots()
    rows.value = data.list
  } catch (e) {
    error.value = (e as Error).message
    rows.value = []
  } finally {
    loading.value = false
  }
}

/* 新增 / 编辑 */
const showForm = ref(false)
const editingId = ref<number | null>(null)
const form = ref<{ name: string; city: string; category: string; price: number | null; description: string }>({
  name: '',
  city: '重庆',
  category: '自然风光',
  price: null,
  description: '',
})

function openCreate() {
  editingId.value = null
  form.value = { name: '', city: '重庆', category: '自然风光', price: null, description: '' }
  showForm.value = true
}

function openEdit(s: Spot) {
  editingId.value = s.id
  form.value = {
    name: s.name,
    city: s.city,
    category: s.category,
    price: Number(s.price),
    description: s.description ?? '',
  }
  showForm.value = true
}

async function save() {
  const name = form.value.name.trim()
  const price = form.value.price
  if (!name || price === null || price <= 0) return
  try {
    if (editingId.value === null) {
      await adminCreateSpot({
        name,
        city: form.value.city,
        category: form.value.category,
        price,
        description: form.value.description || undefined,
      })
    } else {
      await adminUpdateSpot(editingId.value, {
        name,
        city: form.value.city,
        category: form.value.category,
        price,
        description: form.value.description || undefined,
      })
    }
    showForm.value = false
    await load()
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function toggleStatus(row: Spot) {
  const target = row.status === '上架' ? '下架' : '上架'
  try {
    await adminUpdateSpotStatus(row.id, target)
    row.status = target
  } catch (e) {
    error.value = (e as Error).message
  }
}

async function remove(row: Spot) {
  if (!window.confirm(`确认删除景点「${row.name}」？`)) return
  try {
    await adminDeleteSpot(row.id)
    rows.value = rows.value.filter((r) => r.id !== row.id)
  } catch (e) {
    error.value = (e as Error).message
  }
}

onMounted(load)
</script>

<template>
  <div class="admin-page">
    <h1 class="page-title">景点管理</h1>
    <p class="page-sub">共 {{ filtered.length }} 个景点</p>

    <div class="toolbar">
      <input v-model="query" type="text" class="search-input" placeholder="搜索景点名称" />
      <div class="chips">
        <button
          v-for="c in cities"
          :key="c"
          class="chip"
          :class="{ active: cityFilter === c }"
          @click="cityFilter = c"
        >
          {{ c }}
        </button>
      </div>
      <div class="chips">
        <button
          v-for="c in cats"
          :key="c"
          class="chip"
          :class="{ active: catFilter === c }"
          @click="catFilter = c"
        >
          {{ c }}
        </button>
      </div>
      <button class="btn btn-gold add-btn" @click="openCreate">+ 新增景点</button>
    </div>

    <p v-if="error" class="page-error">{{ error }}</p>

    <div class="table card">
      <div class="table-head">
        <span class="col-id">ID</span>
        <span class="col-name">景点名称</span>
        <span class="col-city">城市</span>
        <span class="col-cat">分类</span>
        <span class="col-price">价格</span>
        <span class="col-status">状态</span>
        <span class="col-ops">操作</span>
      </div>
      <div v-for="row in filtered" :key="row.id" class="table-row">
        <span class="col-id">{{ row.id }}</span>
        <span class="col-name">{{ row.name }}</span>
        <span class="col-city">{{ row.city }}</span>
        <span class="col-cat">{{ row.category }}</span>
        <span class="col-price">¥{{ row.price }}</span>
        <span class="col-status">
          <span class="status-pill" :class="row.status === '上架' ? 'on' : 'off'">
            {{ row.status }}
          </span>
        </span>
        <span class="col-ops">
          <button class="op-btn" @click="openEdit(row)">编辑</button>
          <button class="op-btn" @click="toggleStatus(row)">
            {{ row.status === '上架' ? '下架' : '上架' }}
          </button>
          <button class="op-btn danger" @click="remove(row)">删除</button>
        </span>
      </div>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="showForm" class="modal-mask" @click.self="showForm = false">
      <div class="modal card">
        <h3 class="modal-title">{{ editingId === null ? '新增景点' : '编辑景点' }}</h3>
        <label class="field">
          <span>景点名称</span>
          <input v-model="form.name" type="text" class="input" placeholder="如：青城山" />
        </label>
        <div class="field-row">
          <label class="field">
            <span>城市</span>
            <select v-model="form.city" class="input">
              <option v-for="c in spotCities" :key="c" :value="c">{{ c }}</option>
            </select>
          </label>
          <label class="field">
            <span>分类</span>
            <select v-model="form.category" class="input">
              <option v-for="c in spotCategories" :key="c" :value="c">{{ c }}</option>
            </select>
          </label>
          <label class="field">
            <span>价格（元）</span>
            <input v-model.number="form.price" type="number" min="1" class="input" placeholder="100" />
          </label>
        </div>
        <label class="field">
          <span>景点介绍</span>
          <textarea v-model="form.description" class="input" rows="3" placeholder="请输入景点简介"></textarea>
        </label>
        <div class="modal-ops">
          <button class="btn btn-outline" @click="showForm = false">取消</button>
          <button class="btn btn-gold" @click="save">保存</button>
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
  gap: 14px;
  flex-wrap: wrap;
}

.search-input {
  width: 220px;
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

.add-btn {
  margin-left: auto;
  padding: 9px 20px;
  font-size: 13px;
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
  grid-template-columns: 60px 1.4fr 1fr 1fr 90px 110px 190px;
  align-items: center;
  gap: 12px;
  padding: 14px 22px;
  min-width: 860px;
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

.col-price {
  font-weight: 700;
  color: var(--gold-deep);
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
  background: rgba(124, 139, 153, 0.14);
  color: var(--muted);
}

.op-btn {
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--primary);
  font-size: 12px;
  padding: 5px 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-right: 6px;
  transition: all 0.15s ease;
}

.op-btn:hover {
  border-color: var(--gold);
  color: var(--gold-deep);
}

.op-btn.danger:hover {
  border-color: #c9504f;
  color: #c9504f;
  background: rgba(201, 80, 79, 0.06);
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 28, 46, 0.45);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal {
  width: 480px;
  padding: 26px 28px;
}

.modal-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--primary);
  margin-bottom: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.field > span {
  font-size: 12px;
  color: var(--muted);
}

.input {
  padding: 9px 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 14px;
  color: var(--text);
  background: var(--card);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.input:focus {
  outline: none;
  border-color: var(--gold);
  box-shadow: 0 0 0 3px rgba(240, 192, 96, 0.2);
}

.field-row {
  display: flex;
  gap: 12px;
  margin-top: 14px;
}

.modal-ops {
  margin-top: 22px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-ops .btn {
  padding: 9px 22px;
  font-size: 13px;
}
</style>
