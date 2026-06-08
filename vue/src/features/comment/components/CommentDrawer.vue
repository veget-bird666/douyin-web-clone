<template>
  <Teleport to="body">
    <Transition name="drawer-fade">
      <div v-if="visible" class="comment-mask" @click.self="close">
        <div
          ref="drawerRef"
          class="comment-drawer"
          :class="{ 'is-full': isFullscreen, dragging: isDragging }"
          :style="drawerInlineStyle"
          @click.stop
        >
          <div
            class="drag-zone"
            @pointerdown.prevent="onDragStart"
          >
            <div class="drag-bar" />
          </div>

          <header class="drawer-header">
            <span class="header-title">{{ totalCount }}条评论</span>
            <div class="header-actions">
              <button
                type="button"
                class="header-btn"
                :title="isFullscreen ? '缩小' : '全屏'"
                @click.stop="toggleFullscreen"
              >
                <!-- 全屏态：⌝ 右上 + ⌞ 左下（点击缩小） -->
                <svg
                  v-if="isFullscreen"
                  key="collapse"
                  viewBox="0 0 24 24"
                  class="header-icon"
                  fill="none"
                  aria-hidden="true"
                >
                  <path d="M14 6H18V10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                  <path d="M6 14V18H10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
                <!-- 半屏态：⌞ 右上 + ⌝ 左下（点击全屏） -->
                <svg
                  v-else
                  key="expand"
                  viewBox="0 0 24 24"
                  class="header-icon"
                  fill="none"
                  aria-hidden="true"
                >
                  <path d="M18 10V6H14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                  <path d="M10 18H6V14" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                </svg>
              </button>
              <button type="button" class="header-btn close-btn" title="关闭" @click="close">
                <svg viewBox="0 0 24 24" class="header-icon" fill="none" aria-hidden="true">
                  <path
                    d="M7 7l10 10M17 7L7 17"
                    stroke="currentColor"
                    stroke-width="1.5"
                    stroke-linecap="round"
                  />
                </svg>
              </button>
            </div>
          </header>

          <div ref="listRef" class="drawer-list" @wheel.stop>
            <div v-if="loading && !comments.length" class="drawer-empty">加载中...</div>
            <div v-else-if="!comments.length" class="drawer-empty">还没有评论，快来抢沙发</div>

            <div v-for="item in comments" :key="item.commentId" class="comment-row">
              <div class="comment-avatar">{{ avatarText(item) }}</div>
              <div class="comment-main">
                <CommentContent
                  :item="item"
                  @reply="(target) => startReply(item, target)"
                  @like="toggleLike"
                  @fold="toggleFold"
                  @delete="onDelete"
                />

                <div v-if="item.replyCount > 0" class="reply-section">
                  <template v-if="repliesExpanded[item.commentId]">
                    <div
                      v-for="reply in replies[item.commentId] || []"
                      :key="reply.commentId"
                      class="reply-row"
                    >
                      <div class="reply-avatar">{{ avatarText(reply) }}</div>
                      <div class="reply-main">
                        <CommentContent
                          :item="reply"
                          is-reply
                          :root-author-name="item.nickname || '用户'"
                          @reply="() => startReply(item, reply)"
                          @like="toggleLike"
                          @fold="toggleFold"
                          @delete="onDelete"
                        />
                      </div>
                    </div>
                    <button type="button" class="expand-btn" @click="collapseReplies(item)">
                      <span class="expand-dash">——</span>
                      收起 <span class="arrow">∧</span>
                    </button>
                  </template>
                  <button
                    v-else
                    type="button"
                    class="expand-btn"
                    @click="expandReplies(item)"
                  >
                    <span class="expand-dash">——</span>
                    展开 {{ item.replyCount }} 条回复 <span class="arrow">∨</span>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <footer class="drawer-footer">
            <div v-if="replyTarget" class="reply-hint">
              回复 @{{ replyTarget.nickname || '用户' }}
              <button type="button" @click="cancelReply">取消</button>
            </div>
            <div class="input-bar">
              <input
                v-model="inputText"
                type="text"
                maxlength="500"
                :placeholder="replyTarget ? '写下你的回复...' : inputPlaceholder"
                @keyup.enter="submit"
              />
              <button
                type="button"
                class="send-btn"
                :disabled="submitting || !inputText.trim()"
                @click="submit"
              >
                发送
              </button>
            </div>
          </footer>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch, reactive, computed, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/features/auth/composables/useAuth'
import * as commentApi from '@/features/comment/api'
import CommentContent from './CommentContent.vue'

const DEFAULT_HEIGHT_VH = 62
const FULL_HEIGHT_VH = 100
const CLOSE_THRESHOLD_VH = 18
/** 顶部约 1/4 区域：松手后吸附全屏，不在此区间细分高度 */
const FULL_SNAP_THRESHOLD_VH = 75

const INPUT_PLACEHOLDERS = ['分享你此刻的想法', '有什么想法，展开说说']

const props = defineProps({
  visible: { type: Boolean, default: false },
  videoId: { type: String, default: '' },
  count: { type: Number, default: 0 },
  likeCount: { type: Number, default: 0 },
  favoriteCount: { type: Number, default: 0 },
  authorId: { type: String, default: '' },
})

const emit = defineEmits(['update:visible', 'update:count'])

const { user, isLoggedIn } = useAuth()

const comments = ref([])
const loading = ref(false)
const submitting = ref(false)
const inputText = ref('')
const inputPlaceholder = ref(INPUT_PLACEHOLDERS[0])
const totalCount = ref(0)
const replyTarget = ref(null)
const replyTargetId = ref(null)
const repliesExpanded = reactive({})
const replies = reactive({})
const listRef = ref(null)
const drawerRef = ref(null)

const drawerHeightVh = ref(DEFAULT_HEIGHT_VH)
const isFullscreen = ref(false)
const isDragging = ref(false)

let dragStartY = 0
let dragStartHeight = 0

const drawerInlineStyle = computed(() => {
  if (isFullscreen.value) return null
  return { height: `${drawerHeightVh.value}vh` }
})

watch(
  () => [props.visible, props.videoId],
  ([vis, vid]) => {
    if (vis && vid) {
      totalCount.value = props.count
      drawerHeightVh.value = DEFAULT_HEIGHT_VH
      isFullscreen.value = false
      inputPlaceholder.value = INPUT_PLACEHOLDERS[Math.floor(Math.random() * INPUT_PLACEHOLDERS.length)]
      loadComments()
    } else {
      resetState()
    }
  },
)

function resetState() {
  comments.value = []
  inputText.value = ''
  replyTarget.value = null
  replyTargetId.value = null
  drawerHeightVh.value = DEFAULT_HEIGHT_VH
  isFullscreen.value = false
  Object.keys(repliesExpanded).forEach(k => delete repliesExpanded[k])
  Object.keys(replies).forEach(k => delete replies[k])
}

function close() {
  emit('update:visible', false)
}

function toggleFullscreen() {
  isFullscreen.value = !isFullscreen.value
  if (!isFullscreen.value) {
    drawerHeightVh.value = DEFAULT_HEIGHT_VH
  }
}

function onDragStart(e) {
  if (e.button != null && e.button !== 0) return
  isDragging.value = true
  if (isFullscreen.value) {
    isFullscreen.value = false
    drawerHeightVh.value = FULL_HEIGHT_VH
  }
  dragStartY = e.clientY
  dragStartHeight = drawerHeightVh.value
  window.addEventListener('pointermove', onDragMove)
  window.addEventListener('pointerup', onDragEnd)
  window.addEventListener('pointercancel', onDragEnd)
}

function onDragMove(e) {
  if (!isDragging.value) return
  const deltaY = dragStartY - e.clientY
  const vhDelta = (deltaY / window.innerHeight) * 100
  drawerHeightVh.value = Math.max(12, Math.min(FULL_HEIGHT_VH, dragStartHeight + vhDelta))
}

function onDragEnd() {
  if (!isDragging.value) return
  isDragging.value = false
  window.removeEventListener('pointermove', onDragMove)
  window.removeEventListener('pointerup', onDragEnd)
  window.removeEventListener('pointercancel', onDragEnd)

  let h = drawerHeightVh.value
  if (h <= CLOSE_THRESHOLD_VH) {
    isFullscreen.value = false
    close()
    return
  }
  if (h >= FULL_SNAP_THRESHOLD_VH) {
    isFullscreen.value = true
  } else {
    isFullscreen.value = false
    drawerHeightVh.value = Math.round(h * 10) / 10
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('pointermove', onDragMove)
  window.removeEventListener('pointerup', onDragEnd)
  window.removeEventListener('pointercancel', onDragEnd)
})

function avatarText(item) {
  const name = item.nickname || item.userId || '用'
  return name.charAt(0).toUpperCase()
}

async function loadComments() {
  if (!props.videoId) return
  loading.value = true
  try {
    const res = await commentApi.getComments(props.videoId, 50, 0)
    if (res.isSuccess) {
      comments.value = (Array.isArray(res.data) ? res.data : []).map(normalizeItem)
    }
    const countRes = await commentApi.getCommentCount(props.videoId)
    if (countRes.isSuccess) {
      totalCount.value = countRes.data?.count ?? comments.value.length
      emit('update:count', totalCount.value)
    }
  } catch {
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

function normalizeItem(item) {
  return {
    ...item,
    liked: Boolean(item.liked),
    status: item.status ?? 0,
    likeCount: item.likeCount ?? 0,
  }
}

async function expandReplies(item) {
  repliesExpanded[item.commentId] = true
  if (replies[item.commentId]) return
  try {
    const res = await commentApi.getReplies(item.commentId, 100, 0)
    if (res.isSuccess) {
      replies[item.commentId] = (Array.isArray(res.data) ? res.data : []).map(normalizeItem)
    }
  } catch {
    ElMessage.error('加载回复失败')
  }
}

function collapseReplies(item) {
  repliesExpanded[item.commentId] = false
}

function startReply(rootItem, target = null) {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录再评论')
    return
  }
  replyTarget.value = target || rootItem
  replyTargetId.value = (target || rootItem).commentId
  inputText.value = ''
}

function cancelReply() {
  replyTarget.value = null
  replyTargetId.value = null
}

async function submit() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录再评论')
    return
  }
  const text = inputText.value.trim()
  if (!text) return

  submitting.value = true
  try {
    const res = await commentApi.addComment(props.videoId, text, replyTargetId.value)
    if (res.isSuccess) {
      const newComment = normalizeItem(res.data)
      inputText.value = ''
      if (replyTargetId.value) {
        const rootId = findRootId(replyTargetId.value)
        repliesExpanded[rootId] = true
        if (!replies[rootId]) replies[rootId] = []
        replies[rootId].push(newComment)
        const parent = comments.value.find(c => c.commentId === rootId)
        if (parent) parent.replyCount = (parent.replyCount || 0) + 1
        cancelReply()
      } else {
        comments.value = [newComment, ...comments.value]
      }
      totalCount.value += 1
      emit('update:count', totalCount.value)
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch {
    ElMessage.error('网络异常')
  } finally {
    submitting.value = false
  }
}

function findRootId(targetId) {
  const top = comments.value.find(c => c.commentId === targetId)
  if (top) return top.commentId
  for (const [rootId, list] of Object.entries(replies)) {
    if (list.some(r => r.commentId === targetId)) return rootId
  }
  return replyTargetId.value
}

function updateItemInLists(updated) {
  const idx = comments.value.findIndex(c => c.commentId === updated.commentId)
  if (idx >= 0) {
    comments.value[idx] = { ...comments.value[idx], ...updated }
    return
  }
  for (const key of Object.keys(replies)) {
    const ri = replies[key].findIndex(r => r.commentId === updated.commentId)
    if (ri >= 0) {
      replies[key][ri] = { ...replies[key][ri], ...updated }
      return
    }
  }
}

async function toggleLike(item) {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  const wasLiked = item.liked
  const delta = wasLiked ? -1 : 1
  updateItemInLists({
    commentId: item.commentId,
    liked: !wasLiked,
    likeCount: Math.max(0, (item.likeCount || 0) + delta),
  })
  try {
    const res = wasLiked
      ? await commentApi.unlikeComment(item.commentId)
      : await commentApi.likeComment(item.commentId)
    if (!res.isSuccess) {
      updateItemInLists({ commentId: item.commentId, liked: wasLiked, likeCount: item.likeCount })
      const msg = res.message || '操作失败'
      ElMessage.error(msg.includes('接口不存在') ? '请重启后端后再试（需执行 comment_upgrade.sql）' : msg)
    }
  } catch {
    updateItemInLists({ commentId: item.commentId, liked: wasLiked, likeCount: item.likeCount })
    ElMessage.error('网络异常，请确认后端已重启')
  }
}

async function toggleFold(item) {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  const wasFolded = item.status === 1
  const nextStatus = wasFolded ? 0 : 1
  updateItemInLists({ commentId: item.commentId, status: nextStatus })
  try {
    const res = wasFolded
      ? await commentApi.unfoldComment(item.commentId)
      : await commentApi.foldComment(item.commentId)
    if (!res.isSuccess) {
      updateItemInLists({ commentId: item.commentId, status: wasFolded ? 1 : 0 })
      const msg = res.message || '操作失败'
      ElMessage.error(msg.includes('接口不存在') ? '请重启后端后再试（需执行 comment_upgrade.sql）' : msg)
    }
  } catch {
    updateItemInLists({ commentId: item.commentId, status: wasFolded ? 1 : 0 })
    ElMessage.error('网络异常，请确认后端已重启')
  }
}

async function onDelete(item) {
  if (!isLoggedIn.value || user.value?.userId !== item.userId) return

  const isTopLevel = comments.value.some(c => c.commentId === item.commentId)
  let parentId = null
  if (!isTopLevel) {
    for (const [rootId, list] of Object.entries(replies)) {
      if (list.some(r => r.commentId === item.commentId)) {
        parentId = rootId
        break
      }
    }
  }

  const prevComments = comments.value.map(c => ({ ...c }))
  const prevReplies = {}
  for (const key of Object.keys(replies)) {
    prevReplies[key] = [...replies[key]]
  }
  const prevTotal = totalCount.value

  applyDeleteLocally(item, parentId, isTopLevel)

  try {
    const res = await commentApi.deleteComment(item.commentId)
    if (res.isSuccess) {
      const newCount = res.data?.commentCount ?? totalCount.value
      totalCount.value = newCount
      emit('update:count', newCount)
      ElMessage.success('已删除')
    } else {
      revertDelete(prevComments, prevReplies, prevTotal)
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    revertDelete(prevComments, prevReplies, prevTotal)
    ElMessage.error('删除失败')
  }
}

function applyDeleteLocally(item, parentId, isTopLevel) {
  if (isTopLevel) {
    comments.value = comments.value.filter(c => c.commentId !== item.commentId)
    delete replies[item.commentId]
    delete repliesExpanded[item.commentId]
  } else if (parentId) {
    replies[parentId] = (replies[parentId] || []).filter(r => r.commentId !== item.commentId)
    const parent = comments.value.find(c => c.commentId === parentId)
    if (parent) {
      parent.replyCount = Math.max(0, (parent.replyCount || 0) - 1)
      if (parent.replyCount === 0) {
        repliesExpanded[parentId] = false
      }
    }
  }
  totalCount.value = Math.max(0, totalCount.value - 1)
  emit('update:count', totalCount.value)
}

function revertDelete(prevComments, prevReplies, prevTotal) {
  comments.value = prevComments
  Object.keys(replies).forEach(k => delete replies[k])
  Object.assign(replies, prevReplies)
  totalCount.value = prevTotal
  emit('update:count', prevTotal)
}
</script>

<style scoped>
.comment-mask {
  position: fixed;
  inset: 0;
  z-index: 500;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: flex-end;
}

.comment-drawer {
  width: 100%;
  height: 62vh;
  background: #fff;
  border-radius: 12px 12px 0 0;
  display: flex;
  flex-direction: column;
  color: #161823;
  overflow: hidden;
  transition: height 0.28s cubic-bezier(0.25, 0.8, 0.25, 1), border-radius 0.28s ease;
  animation: drawer-enter 0.28s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.comment-drawer.dragging {
  transition: none;
}

.comment-drawer.is-full {
  height: 100%;
  border-radius: 0;
}

@keyframes drawer-enter {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}

.drag-zone {
  padding: 8px 0 4px;
  cursor: grab;
  touch-action: none;
  user-select: none;
}

.drag-zone:active {
  cursor: grabbing;
}

.drag-bar {
  width: 36px;
  height: 4px;
  margin: 0 auto;
  border-radius: 2px;
  background: rgba(22, 24, 35, 0.15);
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 0 16px 12px;
  border-bottom: 1px solid #f1f1f2;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #161823;
}

.header-actions {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  color: rgba(22, 24, 35, 0.55);
  cursor: pointer;
  border-radius: 50%;
}

.header-btn:active {
  background: rgba(22, 24, 35, 0.06);
}

.header-icon {
  width: 20px;
  height: 20px;
}

.drawer-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 4px 16px 8px;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior: contain;
  touch-action: pan-y;
}

.drawer-empty {
  padding: 48px 0;
  text-align: center;
  color: #999;
  font-size: 14px;
}

.comment-row {
  display: flex;
  gap: 10px;
  padding: 14px 0;
}

.comment-avatar,
.reply-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #d0d0d0, #a8a8a8);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.reply-avatar {
  width: 22px;
  height: 22px;
  font-size: 10px;
}

.comment-main,
.reply-main {
  flex: 1;
  min-width: 0;
}

.reply-section {
  margin-top: 8px;
  padding-left: 0;
}

.reply-row {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.expand-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: none;
  padding: 4px 0;
  font-size: 13px;
  color: #576b95;
  cursor: pointer;
}

.expand-dash {
  color: rgba(22, 24, 35, 0.25);
  margin-right: 2px;
}

.expand-btn .arrow {
  font-size: 11px;
  margin-left: 2px;
}

.drawer-footer {
  border-top: 1px solid #f1f1f2;
  padding: 8px 12px calc(8px + env(safe-area-inset-bottom));
  flex-shrink: 0;
}

.reply-hint {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 12px;
  color: #576b95;
}

.reply-hint button {
  border: none;
  background: none;
  color: #999;
  cursor: pointer;
}

.input-bar {
  display: flex;
  gap: 8px;
  align-items: center;
}

.input-bar input {
  flex: 1;
  padding: 10px 16px;
  border: none;
  border-radius: 22px;
  background: #f1f1f2;
  font-size: 14px;
  outline: none;
}

.send-btn {
  padding: 8px 14px;
  border: none;
  border-radius: 16px;
  background: #fe2c55;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  flex-shrink: 0;
}

.send-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.drawer-fade-enter-active,
.drawer-fade-leave-active { transition: opacity 0.25s; }
.drawer-fade-enter-from,
.drawer-fade-leave-to { opacity: 0; }
</style>
