<script setup lang="ts">
import LogoMark from '@/components/ui/LogoMark.vue'

const menus = [
  { to: '/admin', label: '数据看板', exact: true },
  { to: '/admin/spots', label: '景点管理', exact: false },
  { to: '/admin/users', label: '用户管理', exact: false },
  { to: '/admin/reviews', label: '评论审核', exact: false },
]
</script>

<template>
  <div class="admin-shell">
    <aside class="admin-side">
      <RouterLink to="/" class="side-brand">
        <span class="side-mark"><LogoMark :size="50" /></span>
        <span class="side-name">智途星</span>
        <span class="side-sub">管理后台</span>
      </RouterLink>

      <nav class="side-menu" aria-label="后台菜单">
        <RouterLink
          v-for="m in menus"
          :key="m.to"
          :to="m.to"
          class="side-link"
          :class="{ active: m.exact ? $route.path === m.to : $route.path.startsWith(m.to) }"
        >
          {{ m.label }}
        </RouterLink>
      </nav>

      <div class="side-foot">
        <RouterLink to="/" class="back-link">← 返回前台</RouterLink>
      </div>
    </aside>

    <main class="admin-main">
      <RouterView v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </RouterView>
    </main>
  </div>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: flex;
  background: var(--bg);
}

.admin-side {
  width: 232px;
  flex-shrink: 0;
  background: linear-gradient(180deg, var(--primary-deep), var(--primary));
  display: flex;
  flex-direction: column;
  padding: 22px 16px;
  position: sticky;
  top: 0;
  height: 100vh;
}

.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 8px 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.side-mark {
  color: var(--gold);
}

.side-name {
  font-size: 18px;
  font-weight: 800;
  color: var(--white);
  letter-spacing: 0.1em;
}

.side-sub {
  margin-left: auto;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.55);
  letter-spacing: 0.08em;
}

.side-menu {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.side-link {
  position: relative;
  padding: 11px 16px;
  border-radius: 10px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.78);
  transition: color 0.15s ease, background 0.15s ease;
}

.side-link:hover {
  color: var(--white);
  background: rgba(255, 255, 255, 0.08);
}

.side-link.active {
  color: var(--gold);
  font-weight: 700;
  background: rgba(240, 192, 96, 0.12);
}

.side-link.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 3px;
  background: var(--gold);
}

.side-foot {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.back-link {
  display: block;
  padding: 9px 12px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.62);
  border-radius: 10px;
  transition: color 0.15s ease, background 0.15s ease;
}

.back-link:hover {
  color: var(--gold);
  background: rgba(255, 255, 255, 0.08);
}

.admin-main {
  flex: 1;
  min-width: 0;
  padding: 34px 40px 60px;
}
</style>
