<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Package, Map, NotebookPen, Settings, X, Upload } from 'lucide-vue-next'
import { getMe, myTrips, myNotes, myComments, myFavorites, updateProfile, uploadFile } from '@/api'
import { getToken } from '@/api/http'
import type { Profile, Spot } from '@/types'

const quickIcons = [Package, Map, NotebookPen, Settings]

const profile = ref<Profile>({
  name: '',
  phone: '',
  avatar: null,
  signature: null,
  stats: { trips: 0, notes: 0, comments: 0 },
  activity: null,
})
const favSpots = ref<Spot[]>([])
const favLoaded = ref(false)

onMounted(async () => {
  if (!getToken()) return
  try {
    profile.value = await getMe()
  } catch {
    /* ignore */
  }
  try {
    const [trips, notes, comments] = await Promise.all([
      myTrips(),
      myNotes(),
      myComments(),
    ])
    profile.value.stats = {
      trips: trips.length,
      notes: notes.total,
      comments: comments.total,
    }
  } catch {
    /* ignore */
  }
  try {
    favSpots.value = await myFavorites()
  } catch {
    /* ignore */
  }
  favLoaded.value = true
})

const statItems = [
  { key: 'trips' as const, label: '我的行程' },
  { key: 'notes' as const, label: '旅游笔记' },
  { key: 'comments' as const, label: '我的评论' },
]
const quick = [
  { label: '我的行程', to: '/plan' },
  { label: '旅游笔记', to: '/notes' },
  { label: '账户设置', to: '/settings' },
]

// —— 编辑资料 / 上传头像 ——
const editOpen = ref(false)
const saving = ref(false)
const uploading = ref(false)
const editErr = ref('')
const fileInput = ref<HTMLInputElement | null>(null)
const editForm = ref({ name: '', signature: '', avatar: null as string | null })
const previewUrl = ref<string | null>(null)

function openEdit() {
  editForm.value = { name: profile.value.name, signature: profile.value.signature || '', avatar: profile.value.avatar }
  previewUrl.value = profile.value.avatar
  editErr.value = ''
  editOpen.value = true
}

function closeEdit() {
  if (saving.value || uploading.value) return
  editOpen.value = false
  previewUrl.value = null
  if (fileInput.value) fileInput.value.value = ''
}

async function onFilePick(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    editErr.value = '请选择图片文件'
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    editErr.value = '图片不能超过 2MB'
    return
  }
  previewUrl.value = URL.createObjectURL(file)
  uploading.value = true
  editErr.value = ''
  try {
    const { url } = await uploadFile(file)
    editForm.value.avatar = url
    previewUrl.value = url
  } catch (err) {
    editErr.value = err instanceof Error ? err.message : '上传失败'
    previewUrl.value = null
  } finally {
    uploading.value = false
  }
}

async function saveEdit() {
  if (!editForm.value.name.trim()) {
    editErr.value = '昵称不能为空'
    return
  }
  saving.value = true
  editErr.value = ''
  try {
    profile.value = await updateProfile({
      name: editForm.value.name.trim(),
      signature: editForm.value.signature.trim() || null,
      avatar: editForm.value.avatar,
    })
    editOpen.value = false
  } catch (err) {
    editErr.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="container page">
    <!-- 个人信息横幅 -->
    <div class="profile-banner">
      <img v-if="profile.avatar" :src="profile.avatar" class="avatar avatar-img" alt="头像" />
      <span v-else class="avatar">{{ profile.name.slice(0, 1) || 'u' }}</span>
      <div class="profile-main">
        <div class="profile-line">
          <h1 class="profile-name">{{ profile.name }}</h1>
          <span class="profile-handle">@{{ profile.name }}</span>
        </div>
        <p class="profile-phone">{{ profile.phone }}</p>
        <p v-if="profile.signature" class="profile-signature">{{ profile.signature }}</p>
      </div>
      <button class="edit-btn" @click="openEdit">编辑资料</button>
    </div>

    <!-- 数据统计 -->
    <div class="stat-row">
      <div v-for="s in statItems" :key="s.label" class="stat-card card">
        <span class="stat-value">{{ profile.stats[s.key] }}</span>
        <span class="stat-label">{{ s.label }}</span>
      </div>
    </div>

    <!-- 快捷入口 -->
    <h2 class="section-title block-title">快捷入口</h2>
    <div class="quick-grid">
      <RouterLink v-for="(item, idx) in quick" :key="item.label" :to="item.to" class="quick-item card">
        <span class="quick-icon">
          <component :is="quickIcons[idx % quickIcons.length]" :size="24" />
        </span>
        <span class="quick-label">{{ item.label }}</span>
      </RouterLink>
    </div>

    <!-- 最近活动 -->
    <h2 class="section-title block-title">最近活动</h2>
    <div class="activity card">
      <span class="activity-dot"></span>
      <p class="activity-text">{{ profile.activity || '暂无动态' }}</p>
    </div>

    <!-- 我的收藏 -->
    <h2 class="section-title block-title">我的收藏</h2>
    <div v-if="favLoaded && favSpots.length" class="fav-grid">
      <RouterLink v-for="f in favSpots" :key="f.id" :to="`/spots/${f.id}`" class="fav-chip card">
        <span class="fav-name">{{ f.name }}</span>
        <span class="fav-city">{{ f.city }} · ¥{{ f.price }}</span>
      </RouterLink>
    </div>
    <p v-else-if="favLoaded" class="fav-empty">暂无收藏景点</p>

    <!-- 编辑资料弹窗 -->
    <Teleport to="body">
      <div v-if="editOpen" class="modal-mask" @click.self="closeEdit">
        <div class="modal">
          <div class="modal-head">
            <h3 class="modal-title">编辑资料</h3>
            <button class="modal-close" @click="closeEdit"><X :size="18" /></button>
          </div>
          <div class="modal-body">
            <div class="avatar-zone">
              <img v-if="previewUrl" :src="previewUrl" class="avatar-preview" alt="头像预览" />
              <span v-else class="avatar-preview avatar-placeholder">{{ (editForm.name || 'u').slice(0, 1) }}</span>
              <button class="upload-btn" :disabled="uploading" @click="fileInput?.click()">
                <Upload :size="15" />{{ uploading ? '上传中…' : '更换头像' }}
              </button>
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                class="file-input"
                @change="onFilePick"
              />
            </div>
            <label class="field">
              <span class="field-label">昵称</span>
              <input v-model="editForm.name" class="field-input" maxlength="20" placeholder="请输入昵称" />
            </label>
            <label class="field">
              <span class="field-label">个性签名</span>
              <textarea v-model="editForm.signature" class="field-input field-textarea" maxlength="60" rows="2" placeholder="一句话介绍自己"></textarea>
            </label>
            <p v-if="editErr" class="modal-error">{{ editErr }}</p>
          </div>
          <div class="modal-foot">
            <button class="btn-plain" :disabled="saving || uploading" @click="closeEdit">取消</button>
            <button class="btn-primary" :disabled="saving || uploading" @click="saveEdit">
              {{ saving ? '保存中…' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.page {
  padding-block: 44px 60px;
}

.profile-banner {
  display: flex;
  align-items: center;
  gap: 22px;
  padding: 30px 34px;
  background: linear-gradient(135deg, var(--primary), var(--primary-soft));
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  color: var(--white);
}

.avatar {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
  color: var(--gold);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  font-weight: 800;
  border: 2px solid rgba(240, 192, 96, 0.7);
  flex-shrink: 0;
}

.avatar-img {
  object-fit: cover;
  border-radius: 50%;
}

.profile-main {
  flex: 1;
  min-width: 0;
}

.profile-line {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.profile-name {
  font-size: 26px;
  font-weight: 800;
  color: var(--white);
}

.profile-handle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.65);
}

.profile-phone {
  margin-top: 4px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
  letter-spacing: 0.05em;
}

.profile-signature {
  margin-top: 6px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.edit-btn {
  padding: 8px 20px;
  border-radius: 999px;
  border: 1.5px solid var(--gold);
  background: transparent;
  color: var(--gold);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
  transition: background 0.2s ease;
}

.edit-btn:hover {
  background: rgba(240, 192, 96, 0.16);
}

.stat-row {
  margin-top: 24px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 20px 12px;
}

.stat-value {
  font-size: 30px;
  font-weight: 800;
  color: var(--gold-deep);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--muted);
}

.block-title {
  margin-top: 34px;
  font-size: 20px;
}

.quick-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 14px;
}

.quick-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 18px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.25s ease;
  text-decoration: none;
  color: inherit;
}

.quick-item:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.quick-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 144, 160, 0.1);
  color: var(--primary);
}

.quick-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.activity {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
}

.activity-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--gold);
  box-shadow: 0 0 0 5px rgba(240, 192, 96, 0.2);
  flex-shrink: 0;
}

.activity-text {
  font-size: 13px;
  color: var(--text);
}

.fav-grid {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.fav-chip {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 20px;
  min-width: 160px;
  transition: transform 0.2s ease, box-shadow 0.25s ease;
}

.fav-chip:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.fav-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--primary);
}

.fav-city {
  font-size: 12px;
  color: var(--muted);
}

.fav-empty {
  margin-top: 16px;
  font-size: 13px;
  color: var(--muted);
}

/* —— 编辑资料弹窗 —— */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 22, 40, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal {
  width: 440px;
  max-width: 100%;
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 22px;
  border-bottom: 1px solid var(--line);
}

.modal-title {
  font-size: 17px;
  font-weight: 800;
  color: var(--text);
}

.modal-close {
  border: none;
  background: none;
  color: var(--muted);
  cursor: pointer;
  display: flex;
  padding: 4px;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.modal-close:hover {
  background: rgba(0, 0, 0, 0.05);
}

.modal-body {
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.avatar-zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-preview {
  width: 92px;
  height: 92px;
  border-radius: 50%;
  object-fit: cover;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder {
  background: rgba(0, 144, 160, 0.12);
  color: var(--primary);
  font-size: 34px;
  font-weight: 800;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border-radius: 999px;
  border: 1.5px solid var(--primary);
  background: transparent;
  color: var(--primary);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.upload-btn:hover:not(:disabled) {
  background: rgba(0, 144, 160, 0.08);
}

.upload-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.file-input {
  display: none;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
}

.field-input {
  padding: 10px 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  font-size: 14px;
  color: var(--text);
  background: var(--white);
  outline: none;
  transition: border-color 0.2s ease;
  font-family: inherit;
  resize: none;
}

.field-input:focus {
  border-color: var(--primary);
}

.modal-error {
  font-size: 13px;
  color: var(--danger, #d9534f);
}

.modal-foot {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 14px 22px;
  border-top: 1px solid var(--line);
}

.btn-plain {
  padding: 9px 22px;
  border-radius: 10px;
  border: 1px solid var(--line);
  background: var(--white);
  color: var(--muted);
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-plain:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.04);
}

.btn-primary {
  padding: 9px 26px;
  border-radius: 10px;
  border: none;
  background: var(--primary);
  color: var(--white);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-primary:hover:not(:disabled) {
  background: var(--primary-soft);
}

.btn-plain:disabled,
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
