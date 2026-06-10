<template>
  <VideoCollectionView
    title="我的收藏"
    description="查看你收藏的视频，点击可观看详情"
    login-hint="请先登录后查看你的收藏"
    empty-text="还没有收藏过视频"
    empty-link-to="/home"
    empty-link-text="去推荐页看看"
    :empty-icon="Star"
    :fetch-list="getFavorites"
    :remove-item="unfavoriteVideo"
    remove-label="取消收藏"
    removing-label="取消中..."
    confirm-title="取消收藏"
    :confirm-message="(video) => `确定取消收藏「${video.title || '未命名视频'}」吗？`"
    success-message="已取消收藏"
  />
</template>

<script setup>
import { Star } from '@element-plus/icons-vue'
import VideoCollectionView from '@/features/video/components/VideoCollectionView.vue'
import { getFavorites, unfavoriteVideo } from '@/features/video/api'
</script>
