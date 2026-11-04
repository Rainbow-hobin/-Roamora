<script setup lang="ts">
import { watch } from 'vue'
import { authToken } from '@/api/http'
import { startChat, stopChat } from '@/stores/chat'
import AppLayout from '@/components/layout/AppLayout.vue'
watch(authToken, token => {
  stopChat()
  if (token) startChat()
}, { immediate:true, flush:'sync' })
</script>

<template>
  <AppLayout />
</template>
