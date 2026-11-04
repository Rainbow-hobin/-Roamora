<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getMe, updateProfile, uploadFile } from '@/api'
import { getToken } from '@/api/http'
import type { Profile } from '@/types'

const profile = ref<Profile>({
  name: '',
  phone: '',
  avatar: null,
  signature: null,
  stats: { trips: 0, notes: 0, comments: 0 },
  activity: null,
})
const form = ref({ name: '', signature: '', avatar: null as string | null })
const saving = ref(false)
const uploading = ref(false)
const err = ref('')
const ok = ref('')
const fileInput = ref<HTMLInputElement | null>(null)

async function onFilePick(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    err.value = '请选择图片文件'
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    err.value = '图片不能超过 2MB'
    return
  }
  uploading.value = true
  err.value = ''
  try {
    const { url } = await uploadFile(file)
    form.value.avatar = url
  } catch (e2) {
    err.value = e2 instanceof Error ? e2.message : '上传失败'
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

async function save() {
  if (!form.value.name.trim()) {
    err.value = '昵称不能为空'
    return
  }
  saving.value = true
  err.value = ''
  ok.value = ''
  try {
    profile.value = await updateProfile({
      name: form.value.name.trim(),
      signature: form.value.signature.trim() || null,
      avatar: form.value.avatar,
    })
    ok.value = '保存成功'
  } catch (e2) {
    err.value = e2 instanceof Error ? e2.message : '保存失败'
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!getToken()) return
  try {
    profile.value = await getMe()
    form.value = {
      name: profile.value.name,
      signature: profile.value.signature || '',
      avatar: profile.value.avatar,
    }
  } catch {
    /* ignore */
  }
})
</script>

<template>
  <div class="container page">
    <div class="page-head">
      <h1 class="section-title">账户设置</h1>
      <p class="section-sub">管理你的昵称、头像与个性签名</p>
    </div>

    <div class="settings-card card">
      <div class="avatar-row">
        <img v-if="form.avatar" :src="form.avatar" class="avatar avatar-img" alt="头像" />
        <span v-else class="avatar">{{ form.name.slice(0, 1) || 'u' }}</span>
        <div class="avatar-actions">
          <button class="btn btn-outline" :disabled="uploading" @click="fileInput?.click()">
            {{ uploading ? '上传中…' : '更换头像' }}
          </button>
          <input ref="fileInput" type="file" accept="image/*" class="hidden-input" @change="onFilePick" />
          <p class="avatar-tip">支持 jpg/png，不超过 2MB</p>
        </div>
      </div>

      <div class="field">
        <label for="settings-name">昵称</label>
        <input id="settings-name" v-model="form.name" type="text" maxlength="16" placeholder="请输入昵称" />
      </div>
      <div class="field">
        <label for="settings-sign">个性签名</label>
        <textarea
          id="settings-sign"
          v-model="form.signature"
          rows="3"
          maxlength="100"
          placeholder="一句话介绍自己"
        ></textarea>
      </div>

      <p v-if="err" class="settings-msg err">{{ err }}</p>
      <p v-if="ok" class="settings-msg ok">{{ ok }}</p>
      <div class="actions">
        <button class="btn btn-gold" :disabled="saving" @click="save">
          {{ saving ? '保存中…' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding-block: 44px 60px;
}

.page-head {
  margin-bottom: 26px;
}

.page-head .section-title {
  font-size: 28px;
}

.settings-card {
  max-width: 560px;
  padding: 26px 28px;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 22px;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--primary-soft);
  color: var(--white);
  font-size: 28px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-img {
  object-fit: cover;
}

.avatar-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--muted);
}

.hidden-input {
  display: none;
}

.settings-msg {
  margin-top: 10px;
  font-size: 13px;
  border-radius: 8px;
  padding: 8px 12px;
}

.settings-msg.err {
  color: #c0392b;
  background: rgba(192, 57, 43, 0.08);
}

.settings-msg.ok {
  color: var(--primary);
  background: rgba(0, 144, 160, 0.08);
}

.actions {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
