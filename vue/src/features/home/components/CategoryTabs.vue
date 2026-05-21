<template>
  <div class="category-tabs">
    <button
      v-for="cat in categories"
      :key="cat"
      type="button"
      :class="['category-tab', { active: cat === active }]"
      @click="$emit('update:active', cat)"
    >
      {{ cat }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  categories: { type: Array, required: true },
  active: { type: String, required: true },
})

defineEmits(['update:active'])
</script>

<style scoped>
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
  padding: 12px 0 16px;
  border-bottom: 1px solid var(--dy-border);
}

.category-tab {
  position: relative;
  padding: 6px 0;
  border: none;
  background: none;
  color: var(--dy-text-secondary);
  font-size: 15px;
  white-space: nowrap;
  transition: color 0.15s;
}

.category-tab:hover {
  color: var(--dy-text);
}

.category-tab.active {
  color: var(--dy-text);
  font-weight: 600;
}

.category-tab.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  transform: translateX(-50%);
  width: 20px;
  height: 3px;
  background: var(--dy-accent);
  border-radius: 2px;
}
</style>
