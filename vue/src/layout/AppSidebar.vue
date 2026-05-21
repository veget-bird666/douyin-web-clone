<template>
  <aside class="sidebar">
    <div class="sidebar-logo" title="抖音精选">
      <span class="logo-icon">抖</span>
      <span class="logo-text">抖音精选</span>
    </div>

    <nav class="sidebar-nav">
      <a
        v-for="item in mainNav"
        :key="item.label"
        href="#"
        :class="['nav-item', { active: item.active }]"
        @click.prevent
      >
        <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
        <span class="nav-label">{{ item.label }}</span>
        <span v-if="item.badge" class="nav-badge">{{ item.badge }}</span>
      </a>
    </nav>

    <div class="sidebar-divider" />

    <nav class="sidebar-nav secondary">
      <a
        v-for="item in secondaryNav"
        :key="item.label"
        href="#"
        class="nav-item"
        @click.prevent
      >
        <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
        <span class="nav-label">{{ item.label }}</span>
      </a>
    </nav>

    <div class="sidebar-footer">
      <button
        v-for="item in footerNav"
        :key="item.label"
        type="button"
        class="footer-btn"
        :title="item.label"
      >
        <el-icon><component :is="item.icon" /></el-icon>
      </button>
    </div>
  </aside>
</template>

<script setup>
import {
  Star,
  Compass,
  MagicStick,
  User,
  UserFilled,
  Avatar,
  VideoCamera,
  Film,
  Grid,
  Trophy,
  Setting,
  QuestionFilled,
  ChatDotRound,
} from '@element-plus/icons-vue'

const mainNav = [
  { label: '精选', icon: Star, active: true },
  { label: '推荐', icon: Compass, active: false },
  { label: 'AI 搜索', icon: MagicStick, active: false },
  { label: '关注', icon: User, badge: '99+', active: false },
  { label: '朋友', icon: UserFilled, active: false },
  { label: '我的', icon: Avatar, active: false },
]

const secondaryNav = [
  { label: '直播', icon: VideoCamera },
  { label: '放映厅', icon: Film },
  { label: '短剧', icon: Grid },
  { label: '小游戏', icon: Trophy },
]

const footerNav = [
  { label: '设置', icon: Setting },
  { label: '帮助', icon: QuestionFilled },
  { label: '反馈', icon: ChatDotRound },
]
</script>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 160px;
  display: flex;
  flex-direction: column;
  background: var(--dy-sidebar);
  border-right: 1px solid var(--dy-border);
  z-index: 100;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 16px 16px;
  flex-shrink: 0;
}

.logo-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #25f4ee, #fe2c55);
  border-radius: 8px;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  padding: 0 8px;
  gap: 2px;
}

.sidebar-nav.secondary {
  flex: 1;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: var(--dy-radius);
  color: var(--dy-text-secondary);
  transition: background 0.15s, color 0.15s;
}

.nav-item:hover {
  background: var(--dy-bg-hover);
  color: var(--dy-text);
}

.nav-item.active {
  background: var(--dy-bg-hover);
  color: var(--dy-text);
  font-weight: 500;
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.nav-label {
  font-size: 14px;
  white-space: nowrap;
}

.nav-badge {
  margin-left: auto;
  padding: 0 6px;
  font-size: 11px;
  line-height: 18px;
  background: var(--dy-accent);
  color: #fff;
  border-radius: 9px;
}

.sidebar-divider {
  height: 1px;
  margin: 12px 16px;
  background: var(--dy-border);
}

.sidebar-footer {
  display: flex;
  gap: 4px;
  padding: 12px 16px 20px;
  margin-top: auto;
}

.footer-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--dy-radius);
  background: transparent;
  color: var(--dy-text-muted);
  transition: background 0.15s, color 0.15s;
}

.footer-btn:hover {
  background: var(--dy-bg-hover);
  color: var(--dy-text-secondary);
}
</style>
