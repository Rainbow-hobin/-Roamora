<script setup lang="ts">
defineProps<{ sources: { name: string; url: string }[]; date: string }>()
</script>
<template>
  <section class="source-notice" aria-label="资料来源">
    <p>公开资料整理 · 核对日期 {{ date }}</p>
    <ul><li v-for="source in sources" :key="source.url"><a :href="source.url" target="_blank" rel="noopener noreferrer">{{ source.name }} ↗</a></li></ul>
    <p>介绍由智途星归纳；游览时长与行程取舍属于编辑建议。票务、预约和临时开放调整请以官方最新公告为准。</p>
  </section>
</template>
<style scoped>
.source-notice { margin-top:24px; padding:18px 20px; border:1px solid var(--border); border-radius:8px; background:#f5f7f1; color:var(--muted); font-size:14px; line-height:1.8; }
ul { display:flex; flex-wrap:wrap; gap:4px 18px; margin-block:8px; }
a { color:var(--primary-deep); text-decoration:underline; text-underline-offset:3px; }
</style>
