<template>
  <!-- 点赞：空心 / 实心爱心 -->
  <svg v-if="name === 'heart'" class="dy-icon" viewBox="0 0 48 48" fill="none">
    <path
      v-if="filled"
      d="M24 41.5c-.4 0-.8-.2-1.1-.5C10.5 30.2 4 23.5 4 16.5 4 11 8.5 6.5 14 6.5c3.4 0 6.6 1.6 8.6 4.2 2-2.6 5.2-4.2 8.6-4.2 5.5 0 10 4.5 10 10 0 7-6.5 13.7-18.9 24.5-.3.3-.7.5-1.1.5z"
      fill="currentColor"
    />
    <path
      v-else
      d="M24 41.5c-.4 0-.8-.2-1.1-.5C10.5 30.2 4 23.5 4 16.5 4 11 8.5 6.5 14 6.5c3.4 0 6.6 1.6 8.6 4.2 2-2.6 5.2-4.2 8.6-4.2 5.5 0 10 4.5 10 10 0 7-6.5 13.7-18.9 24.5-.3.3-.7.5-1.1.5z"
      stroke="currentColor"
      stroke-width="2.5"
      fill="none"
    />
  </svg>

  <!-- 评论：气泡 -->
  <svg v-else-if="name === 'comment'" class="dy-icon" viewBox="0 0 48 48" fill="none">
    <path
      d="M8 10c0-2.2 1.8-4 4-4h24c2.2 0 4 1.8 4 4v18c0 2.2-1.8 4-4 4h-14l-8 6v-6H12c-2.2 0-4-1.8-4-4V10z"
      stroke="currentColor"
      stroke-width="2.5"
      fill="none"
    />
    <circle cx="17" cy="21" r="2" fill="currentColor" />
    <circle cx="24" cy="21" r="2" fill="currentColor" />
    <circle cx="31" cy="21" r="2" fill="currentColor" />
  </svg>

  <!-- 收藏：五角星 -->
  <svg v-else-if="name === 'star'" class="dy-icon" viewBox="0 0 48 48" fill="none">
    <path
      v-if="filled"
      d="M24 4l6.2 12.6L44 18.5 32 28.8 35.2 44 24 36.8 12.8 44 16 28.8 4 18.5l13.8-1.9L24 4z"
      fill="currentColor"
    />
    <path
      v-else
      d="M24 4l6.2 12.6L44 18.5 32 28.8 35.2 44 24 36.8 12.8 44 16 28.8 4 18.5l13.8-1.9L24 4z"
      stroke="currentColor"
      stroke-width="2.5"
      fill="none"
    />
  </svg>

  <!-- 分享：弯箭头（抖音/TikTok 风格） -->
  <svg v-else-if="name === 'share'" class="dy-icon" viewBox="0 0 48 48" fill="none">
    <path
      d="M32 9.5L40.5 18 32 26.5V21H20.5C14.5 21 10 25.5 10 31.5V36H7V31.5C7 23.9 13.1 18 20.5 18H32V9.5Z"
      fill="currentColor"
    />
  </svg>
</template>

<script setup>
defineProps({
  name: { type: String, required: true },
  filled: { type: Boolean, default: false },
})
</script>

<style scoped>
.dy-icon {
  width: 32px;
  height: 32px;
  display: block;
}
</style>
