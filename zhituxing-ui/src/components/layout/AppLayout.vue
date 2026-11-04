<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import SiteNav from '@/components/layout/SiteNav.vue'
import SiteFooter from '@/components/layout/SiteFooter.vue'

const route = useRoute()
const bare = computed(() => route.meta.bare === true)
</script>

<template>
  <div class="app-shell">
    <SiteNav v-if="!bare" />
    <main class="app-main">
      <RouterView v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
    <SiteFooter v-if="!bare" />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
}
</style>
