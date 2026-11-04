<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, ShieldCheck, RefreshCw, Phone } from 'lucide-vue-next'
import heroImg from '@/assets/scenery/hero.jpg'
import LogoMark from '@/components/ui/LogoMark.vue'
import { getCaptcha, login, register } from '@/api'
import { applyLogin } from '@/stores/user'

const router = useRouter()
const mode = ref<'login' | 'register'>('login')
const account = ref('')
const phone = ref('')
const password = ref('')
const captcha = ref('')
const captchaImg = ref('')
const captchaKey = ref('')
const loading = ref(false)
const error = ref('')
const success = ref('')

async function refreshCaptcha() {
  try {
    const data = await getCaptcha()
    captchaKey.value = data.captchaKey
    captchaImg.value = `data:image/png;base64,${data.imageBase64}`
  } catch (e) {
    error.value = (e as Error).message
  }
}

function switchMode(m: 'login' | 'register') {
  if (mode.value === m) return
  mode.value = m
  error.value = ''
  success.value = ''
  captcha.value = ''
  refreshCaptcha()
}

async function submit() {
  if (!account.value || !password.value) {
    error.value = '请输入账号与密码'
    return
  }
  if (mode.value === 'register' && !phone.value) {
    error.value = '请输入手机号'
    return
  }
  if (!captcha.value) {
    error.value = '请输入验证码'
    return
  }
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    if (mode.value === 'login') {
      const data = await login(account.value.trim(), password.value, captchaKey.value, captcha.value.trim())
      applyLogin(data.accessToken, data.user.name, data.user.phone, data.user.role)
      const redirect = new URLSearchParams(window.location.search).get('redirect')
      router.push(redirect && redirect.startsWith('/') ? redirect : '/profile')
    } else {
      await register(account.value.trim(), phone.value.trim(), password.value, captchaKey.value, captcha.value.trim())
      switchMode('login')
      password.value = ''
      captcha.value = ''
      success.value = '注册成功，请使用新账号登录'
    }
  } catch (e) {
    error.value = (e as Error).message
    refreshCaptcha()
    captcha.value = ''
  } finally {
    loading.value = false
  }
}

function showForgot() {
  error.value = '如需重置密码，请联系管理员协助处理'
}

onMounted(refreshCaptcha)
</script>

<template>
  <div class="login-page">
    <svg class="stars" viewBox="0 0 1200 700" preserveAspectRatio="xMidYMid slice" aria-hidden="true">
      <circle cx="120" cy="90" r="2" fill="#ffffff" opacity="0.8" />
      <circle cx="300" cy="60" r="1.5" fill="#ffffff" opacity="0.6" />
      <circle cx="520" cy="140" r="2.2" fill="#ffffff" opacity="0.7" />
      <circle cx="760" cy="80" r="1.6" fill="#ffffff" opacity="0.5" />
      <circle cx="950" cy="160" r="2" fill="#ffffff" opacity="0.75" />
      <circle cx="1080" cy="70" r="1.4" fill="#ffffff" opacity="0.55" />
      <circle cx="180" cy="230" r="1.6" fill="#ffffff" opacity="0.5" />
      <circle cx="420" cy="260" r="1.8" fill="#ffffff" opacity="0.6" />
      <circle cx="660" cy="210" r="1.4" fill="#ffffff" opacity="0.45" />
      <circle cx="880" cy="280" r="1.8" fill="#ffffff" opacity="0.55" />
      <circle cx="1030" cy="400" r="1.6" fill="#ffffff" opacity="0.4" />
    </svg>

    <div class="login-card">
      <!-- 左栏 · 品牌展示区 -->
      <div class="brand-panel">
        <img class="brand-bg" :src="heroImg" alt="雪山云海风光" />
        <div class="brand-mask"></div>

        <div class="brand-top">
          <span class="brand-mark"><LogoMark :size="88" /></span>
          <span class="brand-word">智途星</span>
        </div>

        <div class="brand-body">
          <h1 class="brand-title">智途星</h1>
          <p class="brand-slogan">智为规划 · 途为旅程 · 星为指引</p>
          <p class="brand-desc">智能旅游规划平台，让每一次出行心中有方向</p>
        </div>

        <p class="brand-foot">©2026 智途星智能旅游规划平台</p>
      </div>

      <!-- 右栏 · 登录模块 -->
      <div class="form-panel">
        <div class="mode-tabs">
          <button type="button" class="mode-tab" :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</button>
          <button type="button" class="mode-tab" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</button>
        </div>
        <h2 class="form-title">{{ mode === 'login' ? '账号登录' : '新用户注册' }}</h2>
        <p class="form-sub">{{ mode === 'login' ? '欢迎回来，请登录您的账号' : '创建账号，开启智能出行规划' }}</p>

        <form class="login-form" @submit.prevent="submit">
          <div class="field">
            <label for="account">{{ mode === 'login' ? '*账号' : '*用户名' }}</label>
            <div class="input-wrap focus-ring">
              <User :size="16" class="in-icon" />
              <input id="account" v-model="account" type="text" :placeholder="mode === 'login' ? '用户名或手机号' : '请输入用户名'" />
            </div>
          </div>
          <div class="field" v-if="mode === 'register'">
            <label for="phone">*手机号</label>
            <div class="input-wrap focus-ring">
              <Phone :size="16" class="in-icon" />
              <input id="phone" v-model="phone" type="text" placeholder="请输入手机号" />
            </div>
          </div>
          <div class="field">
            <label for="password">*密码</label>
            <div class="input-wrap focus-ring">
              <Lock :size="16" class="in-icon" />
              <input id="password" v-model="password" type="password" placeholder="请输入密码" />
            </div>
          </div>
          <div class="field">
            <label for="captcha">验证码</label>
            <div class="input-wrap focus-ring">
              <ShieldCheck :size="16" class="in-icon" />
              <input id="captcha" v-model="captcha" type="text" placeholder="请输入验证码" />
              <img
                v-if="captchaImg"
                :src="captchaImg"
                alt="验证码"
                class="captcha-img"
                title="点击刷新"
                @click="refreshCaptcha"
              />
              <RefreshCw v-else :size="14" class="captcha-refresh" @click="refreshCaptcha" />
            </div>
          </div>
          <p v-if="success" class="login-success">{{ success }}</p>
          <p v-if="error" class="login-error">{{ error }}</p>
          <button type="submit" class="btn btn-gold login-btn" :disabled="loading">
            {{ loading ? '请稍候…' : (mode === 'login' ? '登录' : '注册') }}
          </button>
          <div class="login-foot">
            <template v-if="mode === 'login'">
              <button type="button" class="foot-link foot-btn" @click="switchMode('register')">注册新账号</button>
              <button type="button" class="foot-link foot-btn" @click="showForgot">忘记密码？</button>
            </template>
            <template v-else>
              <button type="button" class="foot-link foot-btn" @click="switchMode('login')">已有账号？去登录</button>
            </template>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  background: linear-gradient(180deg, #07514d 0%, #0f7d77 55%, #1a948c 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px 84px;
  overflow: hidden;
}

.stars {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

/* 左右分栏大卡 */
.login-card {
  position: relative;
  width: min(980px, 100%);
  display: grid;
  grid-template-columns: 1.25fr 1fr;
  border-radius: 26px;
  overflow: hidden;
  box-shadow: 0 40px 100px rgba(0, 15, 30, 0.55);
}

/* —— 左栏品牌区 —— */
.brand-panel {
  position: relative;
  min-height: 560px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 36px 40px;
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.brand-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    160deg,
    rgba(7, 81, 77, 0.78) 0%,
    rgba(15, 125, 119, 0.58) 45%,
    rgba(0, 30, 28, 0.85) 100%
  );
}

.brand-top {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  color: var(--gold);
  filter: drop-shadow(0 4px 14px rgba(240, 192, 96, 0.55));
}

.brand-word {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.16em;
  color: var(--white);
}

.brand-body {
  position: relative;
}

.brand-title {
  font-size: 46px;
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: 0.16em;
  background: linear-gradient(135deg, #ffe3a6 0%, #f0c060 55%, #d9a83e 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  filter: drop-shadow(0 6px 22px rgba(0, 20, 35, 0.45));
}

.brand-slogan {
  margin-top: 14px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.94);
  letter-spacing: 0.12em;
}

.brand-desc {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.66);
}

.brand-foot {
  position: relative;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 0.04em;
}

/* —— 右栏登录模块 —— */
.mode-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 22px;
}
.mode-tab {
  flex: 1;
  padding: 9px 0;
  border: 1px solid var(--border);
  background: var(--bg);
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: var(--muted);
  cursor: pointer;
  transition: all .15s ease;
}
.mode-tab.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}
.login-success {
  margin-top: 10px;
  font-size: 13px;
  color: #1e8e5a;
  background: rgba(30, 142, 90, .08);
  border-radius: 8px;
  padding: 7px 10px;
}
.form-panel {
  background: linear-gradient(170deg, #ffffff, #fbfcf5);
  padding: 54px 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-title {
  font-size: 26px;
  font-weight: 800;
  color: var(--primary);
  letter-spacing: 0.04em;
}

.form-sub {
  margin-top: 8px;
  margin-bottom: 30px;
  font-size: 13px;
  color: var(--muted);
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--border);
  border-radius: 12px;
  padding: 11px 14px;
  background: var(--bg);
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.input-wrap input {
  flex: 1;
  min-width: 0;
  border: 0;
  box-shadow: none;
  padding: 0;
  background: transparent;
}

.input-wrap input::placeholder {
  color: #a7b3be;
}

.in-icon {
  color: var(--primary);
  opacity: 0.65;
  flex-shrink: 0;
}

.captcha-code {
  width: 84px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1.5px solid var(--gold);
  border-radius: 10px;
  background: rgba(240, 192, 96, 0.14);
  color: var(--gold-deep);
  font-weight: 800;
  letter-spacing: 0.12em;
  font-size: 15px;
  line-height: 1;
  padding-block: 9px;
  user-select: none;
}

.captcha-img {
  width: 84px;
  height: 36px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1.5px solid var(--gold);
  border-radius: 10px;
  cursor: pointer;
}

.captcha-refresh {
  color: var(--gold-deep);
  cursor: pointer;
  flex-shrink: 0;
}

.login-error {
  margin-top: 10px;
  font-size: 13px;
  color: #c0392b;
  background: rgba(192, 57, 43, 0.08);
  border-radius: 8px;
  padding: 7px 10px;
}

.login-btn {
  width: 100%;
  margin-top: 12px;
  padding: 13px;
  font-size: 15px;
  letter-spacing: 0.3em;
  border-radius: 12px;
}

.login-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.login-foot {
  margin-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.foot-link {
  color: var(--gold-deep);
  font-weight: 600;
  transition: color 0.15s ease;
}

.foot-btn {
  border: 0;
  background: transparent;
  padding: 0;
  font-size: 13px;
  cursor: pointer;
}

.foot-link:hover {
  color: var(--primary);
}
</style>
