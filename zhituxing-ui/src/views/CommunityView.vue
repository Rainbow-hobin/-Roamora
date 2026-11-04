<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { X } from 'lucide-vue-next'
import PostCard from '@/components/PostCard.vue'
import { listPosts, createPost } from '@/api'
import { getToken } from '@/api/http'
import type { PostItem } from '@/types'

const router = useRouter()
const posts = ref<PostItem[]>([])
const loading = ref(true)

// —— 发帖弹窗 ——
const postOpen = ref(false)
const submitting = ref(false)
const postErr = ref('')
const postForm = ref({ title: '', board: '出行攻略', content: '' })
const boards = ['景点分享', '人文地理', '出行攻略']

function openPost() {
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: '/community' } })
    return
  }
  postForm.value = { title: '', board: '出行攻略', content: '' }
  postErr.value = ''
  postOpen.value = true
}

async function submitPost() {
  if (!postForm.value.title.trim()) {
    postErr.value = '请填写标题'
    return
  }
  if (!postForm.value.content.trim()) {
    postErr.value = '请填写正文'
    return
  }
  submitting.value = true
  postErr.value = ''
  try {
    await createPost({
      title: postForm.value.title.trim(),
      board: postForm.value.board,
      content: postForm.value.content.trim(),
    })
    postOpen.value = false
    const data = await listPosts()
    posts.value = data.list
  } catch (e) {
    postErr.value = e instanceof Error ? e.message : '发布失败'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    const data = await listPosts()
    posts.value = data.list
  } catch {
    posts.value = []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="container page">
    <div class="page-head">
      <h1 class="section-title">旅行灵感</h1>
      <p class="section-sub">在别人的故事里，遇见下一次出发。</p>
      <button class="btn btn-outline post-btn" @click="openPost">分享我的旅途</button>
    </div>

    <div class="post-list">
      <div v-if="loading" class="loading-tip">加载中…</div>
      <PostCard v-for="post in posts" :key="post.id" :post="post" />
    </div>

    <!-- 发帖弹窗 -->
    <div v-if="postOpen" class="modal-mask" @click.self="postOpen = false">
      <div class="post-modal card">
        <div class="modal-head">
          <h3 class="modal-title">发布新帖</h3>
          <button class="modal-close" aria-label="关闭" @click="postOpen = false">
            <X :size="18" />
          </button>
        </div>
        <div class="field">
          <label for="post-title">标题</label>
          <input id="post-title" v-model="postForm.title" type="text" maxlength="50" placeholder="一句话说清楚主题" />
        </div>
        <div class="field">
          <label for="post-board">版块</label>
          <select id="post-board" v-model="postForm.board">
            <option v-for="b in boards" :key="b" :value="b">{{ b }}</option>
          </select>
        </div>
        <div class="field">
          <label for="post-content">正文</label>
          <textarea id="post-content" v-model="postForm.content" rows="5" placeholder="分享你的旅途见闻与经验…"></textarea>
        </div>
        <p v-if="postErr" class="modal-err">{{ postErr }}</p>
        <div class="modal-actions">
          <button class="btn btn-plain" :disabled="submitting" @click="postOpen = false">取消</button>
          <button class="btn btn-gold" :disabled="submitting" @click="submitPost">
            {{ submitting ? '发布中…' : '发布' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding-block: 44px 60px;
}

.page-head {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 26px;
}

.page-head .section-title {
  font-size: 28px;
}

.post-btn {
  position: absolute;
  right: 0;
  top: 4px;
  padding: 9px 22px;
  font-size: 14px;
  border: 0;
  cursor: pointer;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.loading-tip {
  padding: 30px 0;
  text-align: center;
  color: var(--muted);
}

/* 发帖弹窗 */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(2, 20, 34, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  z-index: 200;
}

.post-modal {
  width: min(520px, 100%);
  padding: 24px 26px;
  border-radius: var(--radius-lg);
}

.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary);
}

.modal-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 0;
  background: var(--bg);
  color: var(--muted);
  cursor: pointer;
}

.modal-close:hover {
  background: rgba(0, 144, 160, 0.1);
  color: var(--primary);
}

.modal-err {
  margin-top: 8px;
  font-size: 13px;
  color: #c0392b;
  background: rgba(192, 57, 43, 0.08);
  border-radius: 8px;
  padding: 7px 10px;
}

.modal-actions {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.page { max-width:1100px; padding-block:40px 56px; }
.page-head { margin-bottom:24px; gap:8px; }
.page-head .section-title { color:var(--text); font-size:30px; font-weight:600; }
.page-head .section-sub { font-size:16px; }
.post-btn { border:1px solid #bdcdc0; border-radius:8px; color:var(--primary-deep); }
.post-list { gap:16px; }


</style>
