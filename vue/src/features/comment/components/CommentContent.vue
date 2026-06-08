<template>
  <div class="comment-content" :class="{ 'is-folded': isFolded }">
    <div class="content-main">
      <div class="text-block">
        <div class="name-line">
          <span class="name">{{ item.nickname || '用户' }}</span>
          <template v-if="showReplyTarget">
            <span class="triangle">▶</span>
            <span class="name">{{ replyTargetName }}</span>
          </template>
        </div>

        <p v-if="isFolded" class="folded-text">
          <svg viewBox="0 0 24 24" class="fold-info-icon" aria-hidden="true">
            <circle cx="12" cy="12" r="9" fill="none" stroke="currentColor" stroke-width="1.5" />
            <path d="M12 8v5" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" />
            <circle cx="12" cy="16.2" r="1" fill="currentColor" />
          </svg>
          该评论被折叠
        </p>
        <p v-else class="content-text">{{ item.content }}</p>
      </div>

      <div v-if="isFolded" class="folded-side-actions">
        <button
          type="button"
          class="action-btn like-btn"
          :class="{ active: item.liked }"
          @click="$emit('like', item)"
        >
          <svg viewBox="0 0 24 24" class="action-icon">
            <path
              v-if="item.liked"
              :d="HEART_FILLED"
              fill="currentColor"
            />
            <path
              v-else
              :d="HEART_OUTLINE"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linejoin="round"
            />
          </svg>
          <span v-if="item.likeCount > 0" class="like-count">{{ item.likeCount }}</span>
        </button>
        <button
          type="button"
          class="action-btn fold-btn folded"
          @click="$emit('fold', item)"
        >
          <svg viewBox="0 0 24 24" class="action-icon">
            <path :d="HEART_FILLED" fill="currentColor" />
            <path
              d="M10 7 L10.8 9.2 L9 11.2 L10.8 13.2 L9 15.2 L10.5 17.5"
              fill="none"
              stroke="#fff"
              stroke-width="1.6"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>
    </div>

    <div v-if="!isFolded" class="meta-line">
      <div class="meta-left">
        <span>{{ formatTime(item.createTime) }}</span>
        <button type="button" class="reply-link" @click="$emit('reply', item)">回复</button>
        <button
          v-if="canDelete"
          type="button"
          class="reply-link danger"
          @click="$emit('delete', item)"
        >
          删除
        </button>
      </div>

      <div class="meta-actions">
        <button
          type="button"
          class="action-btn like-btn"
          :class="{ active: item.liked }"
          @click="$emit('like', item)"
        >
          <svg viewBox="0 0 24 24" class="action-icon">
            <path
              v-if="item.liked"
              :d="HEART_FILLED"
              fill="currentColor"
            />
            <path
              v-else
              :d="HEART_OUTLINE"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linejoin="round"
            />
          </svg>
          <span v-if="item.likeCount > 0" class="like-count">{{ item.likeCount }}</span>
        </button>
        <button
          type="button"
          class="action-btn fold-btn"
          @click="$emit('fold', item)"
        >
          <svg viewBox="0 0 24 24" class="action-icon">
            <path
              :d="HEART_OUTLINE"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linejoin="round"
            />
            <path
              :d="HEART_SLASH"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
            />
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuth } from '@/features/auth/composables/useAuth'

const props = defineProps({
  item: { type: Object, required: true },
  isReply: { type: Boolean, default: false },
  rootAuthorName: { type: String, default: '' },
})

defineEmits(['reply', 'like', 'fold', 'delete'])

const HEART_OUTLINE =
  'M16.5 3c-1.74 0-3.41.81-4.5 2.09C10.91 3.81 9.24 3 7.5 3 4.42 3 2 5.42 2 8.5c0 3.78 3.4 6.86 8.55 11.54L12 21.35l1.45-1.32C18.6 15.36 22 12.28 22 8.5 22 5.42 19.58 3 16.5 3z'
const HEART_FILLED =
  'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z'
const HEART_SLASH = 'M4 6 L20 19'

const { user, isLoggedIn } = useAuth()

const isFolded = computed(() => props.item.status === 1)

const canDelete = computed(
  () => isLoggedIn.value && user.value?.userId === props.item.userId,
)

const replyTargetName = computed(() => {
  if (props.item.replyToNickname) return props.item.replyToNickname
  if (props.isReply && props.rootAuthorName) return props.rootAuthorName
  return ''
})

const showReplyTarget = computed(() => {
  if (!props.isReply) return false
  const target = replyTargetName.value
  if (!target) return false
  return target !== props.item.nickname
})

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  const hm = `${pad(date.getHours())}:${pad(date.getMinutes())}`
  const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const dateStart = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.floor((todayStart - dateStart) / 86400000)
  if (diffDays === 0) return hm
  if (diffDays === 1) return `昨天 ${hm}`
  if (diffDays < 7) return `${diffDays}天前`
  return `${date.getMonth() + 1}-${date.getDate()}`
}
</script>

<style scoped>
.comment-content {
  width: 100%;
}

.content-main {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.text-block {
  flex: 1;
  min-width: 0;
}

.name-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 4px;
}

.name {
  font-size: 13px;
  color: rgba(22, 24, 35, 0.6);
}

.triangle {
  font-size: 9px;
  color: rgba(22, 24, 35, 0.35);
  transform: scaleX(0.85);
}

.content-text {
  margin: 0 0 8px;
  font-size: 15px;
  line-height: 1.5;
  color: #161823;
  word-break: break-word;
}

.folded-text {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 0;
  font-size: 13px;
  color: rgba(22, 24, 35, 0.45);
}

.fold-info-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: rgba(22, 24, 35, 0.45);
}

.is-folded .content-main {
  align-items: flex-start;
}

.folded-side-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
  padding-top: 22px;
}

.is-folded .name-line {
  margin-bottom: 6px;
}

.meta-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: rgba(22, 24, 35, 0.34);
  min-width: 0;
}

.reply-link {
  border: none;
  background: none;
  padding: 0;
  font-size: 12px;
  color: rgba(22, 24, 35, 0.5);
  cursor: pointer;
  flex-shrink: 0;
}

.reply-link.danger {
  color: #fe2c55;
}

.meta-actions,
.folded-side-actions {
  flex-shrink: 0;
}

.meta-actions {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 14px;
}

.action-btn {
  display: inline-flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  padding: 0;
  color: rgba(22, 24, 35, 0.34);
  cursor: pointer;
}

.action-btn.like-btn.active {
  color: #fe2c55;
}

.action-btn.like-btn.active .like-count {
  color: #fe2c55;
}

.action-btn.fold-btn.folded {
  color: rgba(22, 24, 35, 0.55);
}

.action-btn.like-btn,
.action-btn.fold-btn {
  min-width: 20px;
  min-height: 20px;
  justify-content: center;
}

.action-icon {
  display: block;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.like-count {
  font-size: 12px;
  color: rgba(22, 24, 35, 0.45);
  line-height: 1;
}
</style>
