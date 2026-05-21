<template>
  <MainLayout>
    <CategoryTabs
      :categories="categories"
      :active="activeCategory"
      @update:active="activeCategory = $event"
    />

    <section class="video-grid">
      <VideoCard
        v-for="video in filteredVideos"
        :key="video.id"
        :video="video"
      />
    </section>
  </MainLayout>
</template>

<script setup>
import { ref, computed } from 'vue'
import MainLayout from '@/layout/MainLayout.vue'
import CategoryTabs from '@/features/home/components/CategoryTabs.vue'
import VideoCard from '@/features/home/components/VideoCard.vue'
import { categories, mockVideos } from '@/features/home/data/mockVideos'

const activeCategory = ref('全部')

const filteredVideos = computed(() => {
  if (activeCategory.value === '全部') return mockVideos
  return mockVideos
})
</script>

<style scoped>
.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px 16px;
}

@media (min-width: 1400px) {
  .video-grid {
    grid-template-columns: repeat(5, 1fr);
  }
}

@media (min-width: 1100px) and (max-width: 1399px) {
  .video-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
