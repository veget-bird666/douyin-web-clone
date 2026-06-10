<template>
  <VideoCollectionView
    title="我的点赞"
    description="查看你点赞过的视频，点击可观看详情"
    login-hint="请先登录后查看你的点赞"
    empty-text="还没有点赞过视频"
    empty-link-to="/home"
    empty-link-text="去推荐页看看"
    :empty-icon="Pointer"
    :fetch-list="getLikes"
    :remove-item="unlikeVideo"
    remove-label="取消点赞"
    removing-label="取消中..."
    confirm-title="取消点赞"
    :confirm-message="(video) => `确定取消对「${video.title || '未命名视频'}」的点赞吗？`"
    success-message="已取消点赞"
  />
</template>

<script setup>
import { Pointer } from '@element-plus/icons-vue'
import VideoCollectionView from '@/features/video/components/VideoCollectionView.vue'
import { getLikes, unlikeVideo } from '@/features/video/api'
</script>
