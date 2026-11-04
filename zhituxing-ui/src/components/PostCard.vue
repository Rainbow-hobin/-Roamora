<script setup lang="ts">
import { guideInfo, destinationInfo } from '@/utils/content'
import { spotCover } from '@/utils/spotCover'
import { getToken } from '@/api/http'
import type { PostItem } from '@/types'
import heroImg from '@/assets/scenery/hero.jpg'
import gugongImg from '@/assets/scenery/gugong.jpg'
import dazuImg from '@/assets/scenery/dazu.jpg'
import karstImg from '@/assets/scenery/karst.jpg'

defineProps<{ post: PostItem }>()

// 帖子无配图时按板块匹配默认封面，避免社区列表纯文字
const boardCovers: Record<string, string> = {
  景点分享: gugongImg,
  人文地理: dazuImg,
  出行攻略: karstImg,
}

function coverOf(post: PostItem): string {
  const guide = guideInfo(post.title)
  const destination = guide ? destinationInfo(guide.related) : undefined
  return post.cover || (destination ? spotCover({name: destination.name, cover: destination.slug ? `/content/${destination.slug}.svg` : null}) : boardCovers[post.board] || heroImg)
}
</script>

<template>
  <article class="post-card card">
    <RouterLink :to="`/community/${post.id}`" class="post-cover">
      <img :src="coverOf(post)" :alt="post.title" loading="lazy" />
    </RouterLink>
    <div class="post-main">
      <RouterLink :to="`/community/${post.id}`" class="post-title-link">
        <h3 class="post-title">{{ post.title }}</h3>
      </RouterLink>
      <p v-if="post.content" class="post-excerpt">{{ post.content }}</p>
      <p class="post-meta">
        {{ post.board }} · {{ post.author }} · {{ (post.date || '').slice(0, 10) }}
      </p>
      <p class="post-stats">浏览({{ post.views }}) 回复({{ post.comments }})</p>
    </div>
    <RouterLink
      :to="getToken() ? `/community/${post.id}` : { path: '/login', query: { redirect: `/community/${post.id}` } }"
      class="btn btn-outline post-btn"
    >
      评论
    </RouterLink>
  </article>
</template>

<style scoped>
.post-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 16px 20px;
  transition: box-shadow 0.2s ease;
}

.post-card:hover {
  box-shadow: var(--shadow-lg);
}

.post-cover {
  flex-shrink: 0;
  width: 108px;
  height: 72px;
  border-radius: 10px;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.35s ease;
}

.post-card:hover .post-cover img {
  transform: scale(1.06);
}

.post-main {
  flex: 1;
  min-width: 0;
}

.post-title-link {
  text-decoration: none;
  display: inline-block;
}

.post-title-link:hover .post-title {
  color: var(--gold-deep);
}

.post-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  line-height: 1.55;
  transition: color 0.15s ease;
}

.post-meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--muted);
}

.post-stats {
  margin-top: 2px;
  font-size: 12px;
  color: var(--muted);
}

.post-btn {
  flex-shrink: 0;
  padding: 7px 18px;
  font-size: 13px;
}

.post-card { padding:20px; gap:20px; border-radius:12px; }
.post-cover { width:160px; height:112px; border-radius:8px; }
.post-title { font-size:19px; line-height:1.5; }
.post-excerpt { color:var(--muted); font-size:15px; line-height:1.75; margin-top:8px; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }
.post-meta, .post-stats { font-size:14px; }
.post-btn { border:0; color:var(--muted); padding:8px; background:transparent; }


</style>
