<script setup lang="ts">
import logoUrl from '@/assets/logo-mark.png'

defineProps<{ size?: number }>()
</script>

<template>
  <img
    :width="size ?? 26"
    :height="size ?? 26"
    :src="logoUrl"
    class="logo-mark"
    alt="智途星标识"
    draggable="false"
  />
</template>

<style scoped>
.logo-mark {
  display: block;
  flex-shrink: 0;
  object-fit: contain;
  filter: brightness(1.1) saturate(1.08);
}
</style>
