<template>
  <div class="main-layout">
    <AppSidebar />
    <AppHeader />
    <main class="main-content">
      <slot />
    </main>
  </div>
</template>

<script setup>
import AppSidebar from '@/layout/AppSidebar.vue'
import AppHeader from '@/layout/AppHeader.vue'
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
}

.main-content {
  margin-left: 160px;
  margin-top: var(--dy-header-height);
  padding: 16px 24px 40px;
  min-height: calc(100vh - var(--dy-header-height));
}
</style>
