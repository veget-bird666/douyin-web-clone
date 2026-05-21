<template>
  <header class="header">
    <div class="header-search">
      <el-icon class="search-icon"><Search /></el-icon>
      <input
        type="search"
        placeholder="搜索你感兴趣的内容"
        aria-label="搜索"
      />
    </div>

    <div class="header-actions">
      <button
        v-for="action in actions"
        :key="action.label"
        type="button"
        class="action-btn"
        :title="action.label"
      >
        <el-icon><component :is="action.icon" /></el-icon>
      </button>

      <div class="avatar-wrap">
        <button
          type="button"
          class="avatar-btn"
          :title="isLoggedIn ? displayName : '登录 / 注册'"
          @click="onAvatarClick"
        >
          <img
            v-if="isLoggedIn && user?.avatar"
            :src="user.avatar"
            alt=""
            class="avatar-img"
          />
          <span v-else-if="isLoggedIn" class="avatar-letter">{{ avatarText }}</span>
          <span v-else class="avatar-guest">
            <el-icon><User /></el-icon>
          </span>
        </button>

        <div v-if="isLoggedIn && showMenu" class="user-menu">
          <div class="menu-user">
            <span class="menu-name">{{ displayName }}</span>
            <span class="menu-email">{{ user?.email }}</span>
          </div>
          <button type="button" class="menu-item" @click="logout(); showMenu = false">
            退出登录
          </button>
        </div>
      </div>
    </div>

    <AuthDialog v-model="authVisible" @success="showMenu = false" />
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import {
  Search,
  User,
  Coin,
  Monitor,
  Trophy,
  Bell,
  ChatDotRound,
  Upload,
} from '@element-plus/icons-vue'
import { useAuth } from '@/features/auth/composables/useAuth'
import AuthDialog from '@/features/auth/components/AuthDialog.vue'

const { isLoggedIn, displayName, avatarText, user, logout } = useAuth()

const authVisible = ref(false)
const showMenu = ref(false)

const actions = [
  { label: '充值', icon: Coin },
  { label: '客户端', icon: Monitor },
  { label: '壁纸', icon: Trophy },
  { label: '通知', icon: Bell },
  { label: '私信', icon: ChatDotRound },
  { label: '投稿', icon: Upload },
]

function onAvatarClick() {
  if (isLoggedIn.value) {
    showMenu.value = !showMenu.value
  } else {
    authVisible.value = true
  }
}

function onDocClick(e) {
  if (!e.target.closest?.('.avatar-wrap')) {
    showMenu.value = false
  }
}

onMounted(() => document.addEventListener('click', onDocClick))
onUnmounted(() => document.removeEventListener('click', onDocClick))
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 160px;
  right: 0;
  height: var(--dy-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 24px 0 20px;
  background: var(--dy-bg);
  border-bottom: 1px solid var(--dy-border);
  z-index: 90;
}

.header-search {
  flex: 1;
  max-width: 480px;
  display: flex;
  align-items: center;
  gap: 10px;
  height: 40px;
  padding: 0 16px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid transparent;
  border-radius: 20px;
  transition: border-color 0.2s, background 0.2s;
}

.header-search:focus-within {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.12);
}

.search-icon {
  color: var(--dy-text-muted);
  font-size: 18px;
}

.header-search input {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--dy-text);
  font-size: 14px;
  outline: none;
}

.header-search input::placeholder {
  color: var(--dy-text-muted);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--dy-text-secondary);
  font-size: 20px;
  transition: background 0.15s, color 0.15s;
}

.action-btn:hover {
  background: var(--dy-bg-hover);
  color: var(--dy-text);
}

.avatar-wrap {
  position: relative;
  margin-left: 8px;
}

.avatar-btn {
  width: 40px;
  height: 40px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  transition: border-color 0.2s;
}

.avatar-btn:hover {
  border-color: rgba(255, 255, 255, 0.25);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-letter {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #fe2c55, #ff8a5c);
}

.avatar-guest {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--dy-text-muted);
  font-size: 20px;
  background: rgba(255, 255, 255, 0.06);
}

.user-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  padding: 8px 0;
  background: var(--dy-bg-elevated);
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.menu-user {
  padding: 10px 16px 8px;
  border-bottom: 1px solid var(--dy-border);
}

.menu-name {
  display: block;
  font-weight: 500;
}

.menu-email {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: var(--dy-text-secondary);
}

.menu-item {
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: none;
  color: var(--dy-text);
  font-size: 14px;
  text-align: left;
  transition: background 0.15s;
}

.menu-item:hover {
  background: var(--dy-bg-hover);
}
</style>
