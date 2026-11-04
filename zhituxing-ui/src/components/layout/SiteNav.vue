<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import { chatState } from '@/stores/chat'
import LogoMark from '@/components/ui/LogoMark.vue'
import { userState, logout } from '@/stores/user'

const route = useRoute()
const router = useRouter()

const navItems = [
  { label: '首页', to: '/' },
  { label: '目的地', to: '/spots' },
  { label: 'AI行程规划', to: '/plan' },
  { label: '旅行灵感', to: '/community' },
  { label: '消息', to: '/messages' },
]

function isActive(to: string): boolean {
  return to === '/' ? route.path === '/' : route.path.startsWith(to)
}

function onLogout() {
  logout()
  router.push('/')
}
</script>

<template>
  <header class="site-nav">
    <div class="container nav-inner">
      <RouterLink to="/" class="brand">
        <span class="brand-mark"><LogoMark :size="48" /></span>
        <span class="brand-text">
          <span class="brand-name">智途星</span>
        </span>
      </RouterLink>


      <nav id="main-links" class="links" aria-label="主导航">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="link"
          :class="{ active: isActive(item.to) }"
        >
          {{ item.label }}<span v-if="item.to === '/messages' && chatState.unread" class="unread-count">{{ chatState.unread > 99 ? '99+' : chatState.unread }}</span>
        </RouterLink>
      </nav>

      <div class="nav-actions">
        <template v-if="!userState.loggedIn">
          <RouterLink to="/login" class="btn btn-gold nav-login">登录</RouterLink>
        </template>
        <template v-else>
          <RouterLink to="/profile" class="btn nav-profile">个人中心</RouterLink>
          <button class="btn nav-logout" type="button" @click="onLogout">
            退出登录
          </button>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.site-nav {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, .95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border);
}
.nav-inner { display:flex; align-items:center; gap:16px; height:56px; }
.brand { display:inline-flex; align-items:center; gap:4px; flex-shrink:0; white-space:nowrap; }
.brand-mark { display:flex; color:var(--primary); }
.brand-name { font-size:18px; font-weight:600; color:var(--primary-deep); letter-spacing:.02em; }
.links { display:flex; align-items:center; gap:4px; margin-inline:auto; }
.link { position:relative; padding:6px 14px; font-size:14px; color:var(--text); border-radius:6px; white-space:nowrap; transition:background .15s, color .15s; }
.link:hover { color:var(--primary); background:#f0f4ed; }
.link.active { color:var(--primary); font-weight:600; }
.link.active::after { content:''; position:absolute; left:14px; right:14px; bottom:1px; height:2px; border-radius:2px; background:var(--primary); }
.nav-actions { display:flex; align-items:center; gap:8px; flex-shrink:0; white-space:nowrap; }
.nav-login, .nav-profile, .nav-logout { padding:5px 12px; min-height:32px; font-size:13px; border-radius:6px; box-shadow:none; }
.nav-login { color:var(--primary-deep); background:#eef3ec; border:1px solid #d7e2d5; }
.nav-profile, .nav-logout { background:transparent; border:1px solid var(--border); color:var(--muted); }
.nav-logout:hover { color:var(--primary); }


.unread-count { margin-left:5px; padding:1px 5px; background:#a64c3b; color:white; border-radius:8px; font-size:11px; }
</style>
