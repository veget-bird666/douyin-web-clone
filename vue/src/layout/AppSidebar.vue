<template>
  <aside class="sidebar">
    <RouterLink to="/home" class="sidebar-logo" title="抖音精选">
      <span class="logo-icon">抖</span>
      <span class="logo-text">抖音精选</span>
    </RouterLink>

    <nav class="sidebar-nav">
      <RouterLink
        v-for="item in mainNav"
        :key="item.path"
        :to="item.path"
        :class="['nav-item', { active: isActive(item.path) }]"
      >
        <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
        <span class="nav-label">{{ item.label }}</span>
      </RouterLink>
    </nav>

    <div class="sidebar-footer">
      <a
        href="http://localhost:8080/swagger-ui.html"
        target="_blank"
        rel="noopener noreferrer"
        class="footer-link"
        title="接口文档"
      >
        <el-icon><Document /></el-icon>
        <span>接口文档</span>
      </a>
    </div>
  </aside>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { Compass, Document, Upload, VideoCamera } from '@element-plus/icons-vue'

const route = useRoute()

const mainNav = [
  { label: '推荐', icon: Compass, path: '/home' },
  { label: '发布视频', icon: Upload, path: '/upload' },
  { label: '我的视频', icon: VideoCamera, path: '/my-videos' },
]

function isActive(path) {
  return route.path === path
}
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
  color: inherit;
  text-decoration: none;
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
  text-decoration: none;
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

.sidebar-footer {
  padding: 12px 16px 20px;
  margin-top: auto;
}

.footer-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  color: var(--dy-text-muted);
  font-size: 13px;
  text-decoration: none;
  transition: color 0.15s;
}

.footer-link:hover {
  color: var(--dy-text-secondary);
}
</style>
