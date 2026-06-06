<template>
  <MainLayout>
    <section class="home-panel">
      <div class="panel-icon">抖</div>
      <h2 class="panel-title">视频流推荐系统</h2>
      <p class="panel-desc">
        登录后可使用推荐、点赞、发布与管理视频等功能。当前推荐流页面开发中，可通过接口文档调试后端 API。
      </p>

      <ul class="feature-list">
        <li v-for="item in features" :key="item.label">
          <span :class="['feature-dot', item.status]" />
          <span class="feature-label">{{ item.label }}</span>
          <span class="feature-status">{{ item.statusText }}</span>
        </li>
      </ul>

      <p v-if="!isLoggedIn" class="panel-hint">
        点击右上角头像登录或注册
      </p>
      <p v-else class="panel-hint">
        你好，{{ displayName }}！后端接口已就绪，推荐页联调开发中。
      </p>
    </section>
  </MainLayout>
</template>

<script setup>
import MainLayout from '@/layout/MainLayout.vue'
import { useAuth } from '@/features/auth/composables/useAuth'

const { isLoggedIn, displayName } = useAuth()

const features = [
  { label: '用户注册 / 登录 / 注销', status: 'done', statusText: '已可用' },
  { label: '视频推荐（按点赞排序 + 去重）', status: 'api', statusText: '后端已就绪' },
  { label: '视频点赞 / 取消点赞', status: 'api', statusText: '后端已就绪' },
  { label: '视频上传 / 我的视频 / 删除', status: 'api', statusText: '后端已就绪' },
  { label: '推荐流上下滑动播放', status: 'pending', statusText: '开发中' },
]
</script>

<style scoped>
.home-panel {
  max-width: 560px;
  margin: 48px auto 0;
  padding: 40px 32px;
  text-align: center;
  background: var(--dy-bg-elevated);
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius-lg);
}

.panel-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #25f4ee, #fe2c55);
  border-radius: 14px;
}

.panel-title {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 600;
}

.panel-desc {
  margin: 0 0 28px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--dy-text-secondary);
}

.feature-list {
  margin: 0 0 24px;
  padding: 0;
  list-style: none;
  text-align: left;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--dy-border);
  font-size: 14px;
}

.feature-list li:last-child {
  border-bottom: none;
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.feature-dot.done {
  background: #52c41a;
}

.feature-dot.api {
  background: #1890ff;
}

.feature-dot.pending {
  background: var(--dy-text-muted);
}

.feature-label {
  flex: 1;
  color: var(--dy-text);
}

.feature-status {
  font-size: 12px;
  color: var(--dy-text-muted);
}

.panel-hint {
  margin: 0;
  font-size: 13px;
  color: var(--dy-text-muted);
}
</style>
