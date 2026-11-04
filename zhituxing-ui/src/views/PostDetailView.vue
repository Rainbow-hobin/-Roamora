<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, MessageCircle } from 'lucide-vue-next'
import { getPost, listPostComments, addPostComment } from '@/api'
import SourceNotice from '@/components/SourceNotice.vue'
import { guideInfo, destinationInfo } from '@/utils/content'
import { spotCover } from '@/utils/spotCover'
import { getToken } from '@/api/http'
import heroImg from '@/assets/scenery/hero.jpg'
import gugongImg from '@/assets/scenery/gugong.jpg'
import dazuImg from '@/assets/scenery/dazu.jpg'
import karstImg from '@/assets/scenery/karst.jpg'
import type { CommentItem, PostItem } from '@/types'

const route = useRoute()
const router = useRouter()
const post = ref<PostItem | null>(null)
const comments = ref<CommentItem[]>([])
const commentTotal = ref(0)
const commentPage = ref(1)
const moreError = ref('')
async function loadMore() {
  try { const page = commentPage.value + 1; const data = await listPostComments(postId(), page); comments.value.push(...data.list); commentTotal.value = data.total; commentPage.value = page } catch { moreError.value = '评论加载失败，请重试' }
}
const loading = ref(true)
const commentText = ref('')
const sending = ref(false)
const commentErr = ref('')

const postId = () => Number(route.params.id)

// 帖子无配图时按板块匹配默认封面
const boardCovers: Record<string, string> = {
  景点分享: gugongImg,
  人文地理: dazuImg,
  出行攻略: karstImg,
}

function coverOf(p: PostItem): string {
  const guide = guideInfo(p.title)
  const destination = guide ? destinationInfo(guide.related) : undefined
  return p.cover || (destination ? spotCover({name: destination.name, cover: destination.slug ? `/content/${destination.slug}.svg` : null}) : boardCovers[p.board] || heroImg)
}

async function load() {
  loading.value = true
  try {
    const [p, c] = await Promise.all([
      getPost(postId()),
      listPostComments(postId()),
    ])
    post.value = p
    comments.value = c.list
    commentTotal.value = c.total
    commentPage.value = 1
  } catch {
    post.value = null
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: `/community/${postId()}` } })
    return
  }
  if (!commentText.value.trim()) {
    commentErr.value = '评论内容不能为空'
    return
  }
  sending.value = true
  commentErr.value = ''
  try {
    await addPostComment(postId(), commentText.value.trim())
    commentText.value = ''
    const c = await listPostComments(postId())
    comments.value = c.list
    commentTotal.value = c.total
    commentPage.value = 1
    if (post.value) post.value.comments = c.total
  } catch (e) {
    commentErr.value = e instanceof Error ? e.message : '评论失败'
  } finally {
    sending.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="container page">
    <button class="back-btn" @click="router.push('/community')">
      <ArrowLeft :size="15" /> 返回社区
    </button>

    <div v-if="loading" class="loading-tip">加载中…</div>

    <template v-else-if="post">
      <article class="post-detail card">
        <h1 class="post-title">{{ post.title }}</h1>
        <p class="post-meta">
          {{ post.board }} · {{ post.author }} · {{ (post.date || '').slice(0, 10) }} · 浏览({{ post.views }})
        </p>
        <RouterLink v-if="post.author !== '智途星资料编辑'" :to="{path: '/messages', query:{q:post.author}}" class="btn btn-outline">私聊作者</RouterLink>
        <p class="post-content">{{ post.content }}</p>
        <SourceNotice v-if="guideInfo(post.title)" :sources="guideInfo(post.title)!.sources" :date="guideInfo(post.title)!.verifiedAt" />
        <div v-if="post.images && post.images.length" class="post-gallery">
          <img v-for="(src, i) in post.images" :key="i" :src="src" :alt="`${post.title} 配图 ${i + 1}`" loading="lazy" />
        </div>
        <img v-else class="post-cover" :src="coverOf(post)" :alt="post.title" loading="lazy" />
      </article>

      <section class="comments card">
        <h2 class="comments-title">
          <MessageCircle :size="16" /> 全部评论（{{ commentTotal }}）
        </h2>
        <p v-if="moreError">{{ moreError }}</p>
        <button v-if="comments.length < commentTotal" class="btn" @click="loadMore">加载更多评论</button>
        <div v-if="comments.length" class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment">
            <span class="avatar">{{ c.author.slice(0, 1) }}</span>
            <div class="comment-main">
              <span class="comment-user">{{ c.author }}</span>
              <span class="comment-date">{{ (c.date || '').slice(0, 10) }}</span>
              <p class="comment-text">{{ c.content }}</p>
            </div>
          </div>
        </div>
        <p v-else class="comment-empty">还没有评论，来抢沙发吧</p>

        <div class="comment-editor">
          <textarea
            v-model="commentText"
            rows="3"
            maxlength="500"
            placeholder="写下你的看法…"
            class="comment-input"
          ></textarea>
          <p v-if="commentErr" class="comment-err">{{ commentErr }}</p>
          <div class="editor-actions">
            <button class="btn btn-gold" :disabled="sending" @click="submitComment">
              {{ sending ? '发表中…' : '发表评论' }}
            </button>
          </div>
        </div>
      </section>
    </template>

    <p v-else class="empty-tip">帖子不存在或已被删除</p>
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

.post-detail {
  padding: 26px 28px;
}

.post-title {
  font-size: 24px;
  font-weight: 800;
  color: var(--primary);
  line-height: 1.4;
}

.post-meta {
  margin-top: 10px;
  font-size: 13px;
  color: var(--muted);
}

.post-content {
  margin-top: 18px;
  font-size: 15px;
  line-height: 2;
  color: var(--text);
  white-space: pre-wrap;
}

.post-cover {
  margin-top: 18px;
  width: 100%;
  border-radius: 12px;
}

.post-gallery {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-gallery img {
  width: 100%;
  border-radius: 12px;
}

.comments {
  margin-top: 24px;
  padding: 22px 26px;
}

.comments-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 16px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment {
  display: flex;
  gap: 10px;
}

.avatar {
  width: 32px;
  height: 32px;
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

.comment-date {
  margin-left: 8px;
  font-size: 12px;
  color: var(--muted);
}

.comment-text {
  margin-top: 3px;
  font-size: 13px;
  color: var(--text);
  line-height: 1.7;
}

.comment-empty {
  font-size: 13px;
  color: var(--muted);
  padding: 12px 0;
}

.comment-editor {
  margin-top: 20px;
  padding-top: 18px;
  border-top: 1px solid var(--border);
}

.comment-input {
  width: 100%;
}

.comment-err {
  margin-top: 8px;
  font-size: 13px;
  color: #c0392b;
}

.editor-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>
