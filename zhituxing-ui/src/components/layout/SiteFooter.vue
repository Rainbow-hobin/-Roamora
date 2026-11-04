<template>
  <footer class="site-footer">
    <div class="footer-inner">
      <p class="footer-slogan">
        <span class="footer-line"></span>
        <span class="footer-slogan-text">让旅行回归热爱</span>
        <span class="footer-line"></span>
      </p>
      <p class="footer-links">首页 · 目的地 · AI行程规划 · 旅行灵感</p>
      <p class="footer-copy">©2026 智途星智能旅游规划平台</p>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  background: #ffffff;
  border-top: 1px solid var(--border);
  padding: 44px 20px 48px;
}

.footer-inner {
  max-width: 640px;
  margin-inline: auto;
  text-align: center;
}

.footer-slogan {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
}

.footer-line {
  width: 64px;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--gold));
}

.footer-slogan-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--primary-deep);
  letter-spacing: 0.16em;
}

.footer-links {
  margin-top: 18px;
  font-size: 12px;
  color: var(--muted);
  letter-spacing: 0.04em;
}

.footer-copy {
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
  opacity: 0.72;
}

.site-footer { padding-block:36px; background:#f4f5ef; }
.footer-slogan-text { font-size:17px; font-weight:500; letter-spacing:.1em; }
.footer-line { background:#d9dfd3; width:32px; }
.footer-links, .footer-copy { font-size:14px; opacity:1; }

</style>