<template>
  <article class="video-card">
    <a href="#" class="card-cover" @click.prevent>
      <img :src="video.cover" :alt="video.title" loading="lazy" />
      <span class="cover-like">
        <el-icon><Star /></el-icon>
        {{ formatCount(video.likeCount) }}
      </span>
      <span class="cover-duration">{{ video.duration }}</span>
    </a>

    <h3 class="card-title">
      <a href="#" @click.prevent>{{ video.title }}</a>
    </h3>

    <div v-if="video.tags?.length" class="card-tags">
      <a
        v-for="tag in video.tags"
        :key="tag"
        href="#"
        class="tag"
        @click.prevent
      >
        {{ tag }}
      </a>
    </div>

    <div class="card-meta">
      <a href="#" class="author" @click.prevent>
        <img :src="video.authorAvatar" alt="" class="author-avatar" />
        <span class="author-name">{{ video.author }}</span>
        <span v-if="video.following" class="author-follow">你的关注</span>
      </a>
      <span class="time-ago">· {{ video.timeAgo }}</span>
    </div>
  </article>
</template>

<script setup>
import { Star } from '@element-plus/icons-vue'

defineProps({
  video: { type: Object, required: true },
})

function formatCount(n) {
  if (n >= 10000) return `${(n / 10000).toFixed(1)}万`
  return String(n)
}
</script>

<style scoped>
.video-card {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.card-cover {
  position: relative;
  display: block;
  aspect-ratio: 3 / 4;
  border-radius: var(--dy-radius-lg);
  overflow: hidden;
  background: var(--dy-bg-elevated);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.video-card:hover .card-cover img {
  transform: scale(1.03);
}

.cover-like,
.cover-duration {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: 12px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.cover-like {
  left: 8px;
  bottom: 8px;
}

.cover-duration {
  right: 8px;
  bottom: 8px;
}

.card-title {
  margin: 10px 0 6px;
  font-size: 14px;
  font-weight: 500;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-title a:hover {
  color: var(--dy-text);
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.tag {
  font-size: 13px;
  color: var(--dy-link);
}

.tag:hover {
  text-decoration: underline;
}

.card-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  font-size: 12px;
  color: var(--dy-text-secondary);
}

.author {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.author-avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.author-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100px;
}

.author-name:hover {
  color: var(--dy-text);
}

.author-follow {
  flex-shrink: 0;
  padding: 0 4px;
  font-size: 11px;
  color: #6bcf7f;
}

.time-ago {
  flex-shrink: 0;
}
</style>
