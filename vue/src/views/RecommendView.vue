<template>
  <div
    ref="pageRef"
    class="recommend-page"
    @touchstart="onTouchStart"
    @touchmove="onTouchMove"
    @touchend="onTouchEnd"
  >
    <!-- 顶部标签栏 -->
    <header class="top-bar">
      <button class="back-btn" @click="goBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </button>
      <span class="top-title">推荐</span>
    </header>

    <!-- 视频播放区 -->
    <div class="video-stage">
      <video
        v-if="currentVideo"
        ref="videoRef"
        :key="currentVideo.videoId"
        :src="currentVideoUrl"
        class="video-player"
        autoplay
        playsinline
        webkit-playsinline
        @click="onVideoClick"
        @ended="onVideoEnded"
        @timeupdate="onTimeUpdate"
        @loadeddata="onVideoLoaded"
        @error="onVideoError"
      />

      <!-- 暂停 / 播放遮罩图标 -->
      <Transition name="fade">
        <div v-if="showPlayIcon" class="play-overlay" @click="togglePlay">
          <el-icon :size="72"><VideoPause v-if="!paused" /><VideoPlay v-else /></el-icon>
        </div>
      </Transition>

      <!-- 双击点赞动画 -->
      <Transition
        v-for="h in hearts"
        :key="h.id"
        @after-enter="hearts = hearts.filter(x => x.id !== h.id)"
      >
        <div v-if="h.active" class="heart-anim" :style="{ left: h.x + 'px', top: h.y + 'px' }">
          <el-icon :size="80"><StarFilled /></el-icon>
        </div>
      </Transition>
    </div>

    <!-- 加载动画 -->
    <div v-if="isLoadingUrl" class="loading-overlay">
      <el-icon class="loading-icon" :size="40"><Loading /></el-icon>
    </div>

    <!-- 右侧操作栏 -->
    <aside class="side-actions">
      <!-- 头像 -->
      <div class="avatar-wrap">
        <div class="avatar-circle">
          <span class="avatar-text">{{ avatarChar }}</span>
        </div>
        <div class="follow-badge">+</div>
      </div>

      <!-- 点赞 -->
      <button class="action-btn" :class="{ active: isCurrentLiked }" @click.stop="toggleLike">
        <el-icon :size="32"><StarFilled v-if="isCurrentLiked" /><Star v-else /></el-icon>
        <span class="action-count">{{ fmtCount(currentLikeCount) }}</span>
      </button>

      <!-- 评论 -->
      <button class="action-btn" @click.stop>
        <el-icon :size="32"><ChatDotRound /></el-icon>
        <span class="action-count">{{ fmtCount(currentVideo?.commentCount || 0) }}</span>
      </button>

      <!-- 收藏 -->
      <button class="action-btn" @click.stop>
        <el-icon :size="32"><Star /></el-icon>
        <span class="action-count">收藏</span>
      </button>

      <!-- 分享 -->
      <button class="action-btn" @click.stop>
        <el-icon :size="32"><Share /></el-icon>
        <span class="action-count">分享</span>
      </button>

      <!-- 音乐碟片 -->
      <div class="music-disc" :class="{ spinning: !paused }">
        <div class="disc-cover">
          <span class="disc-avatar">{{ avatarChar }}</span>
        </div>
      </div>
    </aside>

    <!-- 底部信息区 -->
    <footer class="bottom-info">
      <div class="info-author">
        <span class="author-name">@{{ currentVideo?.userId?.slice(0, 8) || '用户' }}</span>
        <button class="follow-btn">关注</button>
      </div>
      <p class="info-desc">{{ currentVideo?.description || currentVideo?.title || '' }}</p>
      <div class="music-bar">
        <span class="music-icon">♪</span>
        <span class="music-text marquee">{{ currentVideo?.title || '原声' }} · 抖音精选</span>
      </div>
    </footer>

    <!-- 底部进度条 -->
    <div
      class="progress-bar"
      ref="progressBarRef"
      @mousedown="onProgressMouseDown"
      @touchstart.prevent="onProgressTouchStart"
    >
      <div class="progress-track">
        <div class="progress-fill" :style="{ width: progressPct + '%' }" />
        <div class="progress-thumb" :style="{ left: progressPct + '%' }" />
      </div>
      <div class="progress-time">
        <span>{{ formatTime(currentTime) }}</span>
        <span>{{ formatTime(duration) }}</span>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && !currentVideo && !videos.length" class="empty-state">
      <p>暂无推荐视频</p>
      <button @click="fetchVideos">重新加载</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/features/auth/composables/useAuth'
import * as videoApi from '@/features/video/api'

const router = useRouter()
const { isLoggedIn } = useAuth()

// —— 核心状态 ——
const pageRef = ref(null)
const videos = shallowRef([])
const currentIndex = ref(0)
const videoUrls = ref({})
const likedMap = ref({})
const likeCountMap = ref({})
const loading = ref(false)
const isLoadingUrl = ref(false)
const isSwiping = ref(false)
const hasMore = ref(true)
const pageSize = 10
const paused = ref(false)
const showPlayIcon = ref(false)
const hearts = ref([])
const progressPct = ref(0)
const currentTime = ref(0)
const duration = ref(0)
const progressBarRef = ref(null)
let isDragging = false

function formatTime(sec) {
  if (!sec || !isFinite(sec)) return '00:00'
  const m = Math.floor(sec / 60)
  const s = Math.floor(sec % 60)
  return String(m).padStart(2, '0') + ':' + String(s).padStart(2, '0')
}

// 触摸
const touchStartY = ref(0)
const touchDeltaY = ref(0)
const swipeThreshold = 60

// —— 计算属性 ——
const currentVideo = computed(() => videos.value[currentIndex.value] ?? null)
const currentVideoUrl = computed(() => {
  if (!currentVideo.value) return ''
  return videoUrls.value[currentVideo.value.videoId] || ''
})
const isCurrentLiked = computed(() => {
  if (!currentVideo.value) return false
  return likedMap.value[currentVideo.value.videoId] || false
})
const currentLikeCount = computed(() => {
  if (!currentVideo.value) return 0
  const vid = currentVideo.value.videoId
  if (likeCountMap.value[vid] !== undefined) return likeCountMap.value[vid]
  return currentVideo.value.likeCount || 0
})

const avatarChar = computed(() => {
  if (!currentVideo.value) return ''
  return (currentVideo.value.userId || '抖').charAt(0).toUpperCase()
})

function fmtCount(n) {
  if (n >= 10000) return (n / 10000).toFixed(1) + 'w'
  return String(n)
}

let playIconTimer = null
function flashPlayIcon() {
  showPlayIcon.value = true
  clearTimeout(playIconTimer)
  playIconTimer = setTimeout(() => { showPlayIcon.value = false }, 600)
}

// —— 进度条 ——
function onTimeUpdate() {
  const el = videoRef.value
  if (el && el.duration) {
    if (!isDragging) progressPct.value = (el.currentTime / el.duration) * 100
    currentTime.value = el.currentTime
    duration.value = el.duration
  }
}

function seekTo(clientX) {
  const bar = progressBarRef.value
  if (!bar) return
  const rect = bar.getBoundingClientRect()
  const pct = Math.max(0, Math.min(1, (clientX - rect.left) / rect.width))
  const el = videoRef.value
  if (el && el.duration) {
    el.currentTime = pct * el.duration
    progressPct.value = pct * 100
  }
}

function onProgressMouseDown(e) {
  isDragging = true
  seekTo(e.clientX)
  document.addEventListener('mousemove', onProgressMouseMove)
  document.addEventListener('mouseup', onProgressMouseUp)
}
function onProgressMouseMove(e) { seekTo(e.clientX) }
function onProgressMouseUp() {
  isDragging = false
  document.removeEventListener('mousemove', onProgressMouseMove)
  document.removeEventListener('mouseup', onProgressMouseUp)
}

function onProgressTouchStart(e) {
  isDragging = true
  seekTo(e.touches[0].clientX)
  document.addEventListener('touchmove', onProgressTouchMove)
  document.addEventListener('touchend', onProgressTouchEnd)
}
function onProgressTouchMove(e) { seekTo(e.touches[0].clientX) }
function onProgressTouchEnd() {
  isDragging = false
  document.removeEventListener('touchmove', onProgressTouchMove)
  document.removeEventListener('touchend', onProgressTouchEnd)
}

// —— 数据加载 ——
async function fetchVideos() {
  if (loading.value || !hasMore.value) return
  loading.value = true
  try {
    const offset = videos.value.length
    const res = await videoApi.getRecommend(pageSize, offset)
    if (res.isSuccess) {
      const list = Array.isArray(res.data) ? res.data : []
      if (list.length < pageSize) hasMore.value = false
      videos.value = [...videos.value, ...list]
      list.forEach(v => {
        if (likeCountMap.value[v.videoId] === undefined) {
          likeCountMap.value[v.videoId] = v.likeCount || 0
        }
      })
      if (offset === 0 && list.length > 0) {
        await loadCurrentVideo()
      }
    } else {
      ElMessage.error(res.message || '加载推荐视频失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function loadCurrentVideo() {
  const v = currentVideo.value
  if (!v) return
  if (!videoUrls.value[v.videoId]) {
    isLoadingUrl.value = true
    try {
      const urlRes = await videoApi.getVideoUrl(v.videoId)
      if (urlRes.isSuccess && urlRes.data?.url) {
        videoUrls.value = { ...videoUrls.value, [v.videoId]: urlRes.data.url }
      }
    } catch { /* ignore */ }
    isLoadingUrl.value = false
  }
  if (isLoggedIn.value && likedMap.value[v.videoId] === undefined) {
    try {
      const likedRes = await videoApi.isLiked(v.videoId)
      if (likedRes.isSuccess) {
        likedMap.value = { ...likedMap.value, [v.videoId]: likedRes.data?.liked || false }
      }
    } catch { /* ignore */ }
  }
}

// —— 视频切换 ——
async function switchVideo(direction) {
  const newIndex = currentIndex.value + direction
  if (newIndex < 0 || newIndex >= videos.value.length) return
  await recordCurrentView()
  currentIndex.value = newIndex
  progressPct.value = 0
  await loadCurrentVideo()
  if (newIndex >= videos.value.length - 3 && hasMore.value) fetchVideos()
}

async function recordCurrentView() {
  const v = currentVideo.value
  if (!v || !isLoggedIn.value) return
  try { await videoApi.recordView(v.videoId) } catch { /* ignore */ }
}

// —— 点赞 ——
async function toggleLike() {
  const v = currentVideo.value
  if (!v) return
  if (!isLoggedIn.value) { ElMessage.warning('请先登录再点赞'); return }
  const videoId = v.videoId
  const wasLiked = likedMap.value[videoId] || false
  likedMap.value = { ...likedMap.value, [videoId]: !wasLiked }
  likeCountMap.value = {
    ...likeCountMap.value,
    [videoId]: Math.max(0, (likeCountMap.value[videoId] ?? v.likeCount ?? 0) + (wasLiked ? -1 : 1)),
  }
  try {
    const res = wasLiked ? await videoApi.unlikeVideo(videoId) : await videoApi.likeVideo(videoId)
    if (!res.isSuccess) {
      likedMap.value = { ...likedMap.value, [videoId]: wasLiked }
      likeCountMap.value = {
        ...likeCountMap.value,
        [videoId]: Math.max(0, (likeCountMap.value[videoId] ?? v.likeCount ?? 0) + (wasLiked ? 1 : -1)),
      }
    }
  } catch {
    likedMap.value = { ...likedMap.value, [videoId]: wasLiked }
    likeCountMap.value = {
      ...likeCountMap.value,
      [videoId]: Math.max(0, (likeCountMap.value[videoId] ?? v.likeCount ?? 0) + (wasLiked ? 1 : -1)),
    }
  }
}

// —— 单击暂停 / 双击点赞 ——
let clickTimer = null
function onVideoClick(e) {
  if (clickTimer) {
    // 第二次点击 → 双击：点赞
    clearTimeout(clickTimer)
    clickTimer = null
    const rect = pageRef.value?.getBoundingClientRect()
    if (rect) {
      const id = Date.now()
      hearts.value = [...hearts.value, { id, active: true, x: e.clientX - rect.left - 40, y: e.clientY - rect.top - 40 }]
      setTimeout(() => { hearts.value = hearts.value.filter(h => h.id !== id) }, 800)
    }
    if (isLoggedIn.value) toggleLike()
  } else {
    // 第一次点击 → 等 300ms 判断是否双击
    clickTimer = setTimeout(() => {
      clickTimer = null
      togglePlay()
    }, 300)
  }
}

// —— 触摸 ——
function onTouchStart(e) { touchStartY.value = e.touches[0].clientY; touchDeltaY.value = 0 }
function onTouchMove(e) {
  touchDeltaY.value = e.touches[0].clientY - touchStartY.value
  isSwiping.value = true
}
function onTouchEnd() {
  isSwiping.value = false
  const delta = touchDeltaY.value
  touchDeltaY.value = 0
  if (Math.abs(delta) > swipeThreshold) switchVideo(delta < 0 ? -1 : 1)
}

// —— 滚轮 ——
let wheelTimer = null
function onWheel(e) {
  e.preventDefault()
  if (wheelTimer) return
  wheelTimer = setTimeout(() => { wheelTimer = null }, 600)
  if (e.deltaY > 30) switchVideo(1)
  else if (e.deltaY < -30) switchVideo(-1)
}

// —— 键盘 ——
function onKeyDown(e) {
  if (e.key === 'ArrowDown' || e.key === 'j') { e.preventDefault(); switchVideo(1) }
  else if (e.key === 'ArrowUp' || e.key === 'k') { e.preventDefault(); switchVideo(-1) }
  else if (e.key === ' ') { e.preventDefault(); togglePlay() }
}

// —— 视频控制 ——
const videoRef = ref(null)
function togglePlay() {
  const el = videoRef.value
  if (!el) return
  if (el.paused) { el.play(); paused.value = false }
  else { el.pause(); paused.value = true }
  flashPlayIcon()
}
function onVideoLoaded() {
  isLoadingUrl.value = false
  paused.value = false
}
function onVideoError() { isLoadingUrl.value = false }
function onVideoEnded() { switchVideo(1) }

function goBack() {
  recordCurrentView()
  router.push('/home')
}

watch(isLoggedIn, (loggedIn) => { if (!loggedIn) likedMap.value = {} })

onMounted(() => {
  document.body.style.overflow = 'hidden'
  document.addEventListener('keydown', onKeyDown)
  document.addEventListener('wheel', onWheel, { passive: false })
  fetchVideos()
})
onUnmounted(() => {
  document.body.style.overflow = ''
  document.removeEventListener('keydown', onKeyDown)
  document.removeEventListener('wheel', onWheel)
})
</script>

<style scoped>
.recommend-page {
  position: fixed; inset: 0; background: #000; z-index: 200;
  display: flex; flex-direction: column;
  overscroll-behavior: none;
  user-select: none; -webkit-user-select: none;
}

/* ====== 顶部栏 ====== */
.top-bar {
  position: absolute; top: 0; left: 0; right: 0; z-index: 10;
  display: flex; align-items: center; gap: 8px;
  padding: 14px 16px;
  padding-top: max(14px, env(safe-area-inset-top));
  background: linear-gradient(to bottom, rgba(0,0,0,0.55) 0%, transparent 100%);
}
.back-btn {
  width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
  border: none; border-radius: 50%; background: transparent; color: #fff; cursor: pointer;
}
.top-title {
  font-size: 17px; font-weight: 600; color: #fff; letter-spacing: 0.5px;
}

/* ====== 视频区 ====== */
.video-stage {
  flex: 1; display: flex; align-items: center; justify-content: center;
  position: relative; overflow: hidden;
}
.video-player {
  width: 100%; height: 100%; object-fit: contain; background: #000;
}

/* 播放/暂停图标 */
.play-overlay {
  position: absolute; inset: 0; z-index: 4;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0.15); pointer-events: none;
}
.play-overlay .el-icon {
  color: rgba(255,255,255,0.85);
  filter: drop-shadow(0 2px 8px rgba(0,0,0,0.5));
}
.fade-enter-active { transition: opacity 0.1s; }
.fade-leave-active { transition: opacity 0.4s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

/* 双击心 */
.heart-anim {
  position: absolute; z-index: 20; pointer-events: none;
  animation: heartPop 0.8s ease-out forwards;
}
.heart-anim .el-icon { color: #fe2c55; }
@keyframes heartPop {
  0% { transform: scale(0); opacity: 1; }
  30% { transform: scale(1.3); opacity: 1; }
  100% { transform: scale(0.6); opacity: 0; }
}

/* ====== 加载 ====== */
.loading-overlay {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); z-index: 5;
}
.loading-icon { color: rgba(255,255,255,0.6); animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ====== 右侧操作栏 ====== */
.side-actions {
  position: absolute; right: 10px; bottom: 160px;
  display: flex; flex-direction: column; align-items: center; gap: 22px; z-index: 10;
}

.avatar-wrap { position: relative; }
.avatar-circle {
  width: 44px; height: 44px; border-radius: 50%; overflow: hidden;
  border: 2px solid #fff; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #fe2c55, #ff6b81);
}
.avatar-text { color: #fff; font-size: 18px; font-weight: 700; }
.follow-badge {
  position: absolute; bottom: -6px; left: 50%; transform: translateX(-50%);
  width: 20px; height: 20px; border-radius: 50%;
  background: #fe2c55; color: #fff; font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
}

.action-btn {
  display: flex; flex-direction: column; align-items: center; gap: 2px;
  padding: 0; border: none; background: none;
  color: #fff; cursor: pointer;
  transition: transform 0.1s;
}
.action-btn:active { transform: scale(0.85); }
.action-btn.active { color: #fe2c55; }
.action-count { font-size: 11px; font-weight: 500; color: #fff; text-shadow: 0 1px 2px rgba(0,0,0,0.5); }

/* 音乐碟片 */
.music-disc {
  width: 40px; height: 40px; border-radius: 50%;
  background: rgba(255,255,255,0.12); display: flex;
  align-items: center; justify-content: center;
  margin-top: 4px;
}
.music-disc.spinning { animation: discSpin 3s linear infinite; }
@keyframes discSpin { to { transform: rotate(360deg); } }
.disc-cover {
  width: 22px; height: 22px; border-radius: 50%;
  background: linear-gradient(135deg, #333, #555);
  display: flex; align-items: center; justify-content: center;
}
.disc-avatar { color: #fff; font-size: 10px; font-weight: 600; }

/* ====== 底部信息 ====== */
.bottom-info {
  position: absolute; left: 14px; right: 72px; bottom: 40px; z-index: 10;
  padding-bottom: env(safe-area-inset-bottom, 0px);
  text-shadow: 0 1px 3px rgba(0,0,0,0.5);
}
.info-author { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.author-name { font-size: 15px; font-weight: 600; color: #fff; }
.follow-btn {
  padding: 2px 12px; border: 1px solid rgba(255,255,255,0.6); border-radius: 14px;
  background: transparent; color: #fff; font-size: 11px; cursor: pointer;
}
.info-desc {
  margin: 0 0 10px; font-size: 14px; color: rgba(255,255,255,0.9); line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.music-bar { display: flex; align-items: center; gap: 6px; }
.music-icon { font-size: 14px; color: #fff; }
.music-text { font-size: 12px; color: rgba(255,255,255,0.7); white-space: nowrap; overflow: hidden; }
.marquee {
  animation: marquee 8s linear infinite;
  max-width: 200px;
}
@keyframes marquee {
  0% { transform: translateX(60px); }
  100% { transform: translateX(-100%); }
}

/* ====== 进度条 ====== */
.progress-bar {
  position: absolute; bottom: 0; left: 0; right: 0; z-index: 10;
  padding: 8px 0 4px; cursor: pointer;
}
.progress-bar:hover .progress-track { height: 4px; }
.progress-bar:hover .progress-thumb { opacity: 1; }

.progress-track {
  position: relative; height: 3px; margin: 0 12px;
  background: rgba(255,255,255,0.2); border-radius: 2px;
  transition: height 0.15s;
}
.progress-fill {
  height: 100%; background: #fff; border-radius: 2px;
  transition: width 0.1s linear;
}
.progress-thumb {
  position: absolute; top: 50%; transform: translate(-50%, -50%);
  width: 12px; height: 12px; border-radius: 50%; background: #fff;
  opacity: 0; transition: opacity 0.15s;
}

.progress-time {
  display: flex; justify-content: space-between;
  padding: 2px 12px 0;
  font-size: 11px; color: rgba(255,255,255,0.5);
}

/* ====== 空状态 ====== */
.empty-state {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  text-align: center; color: rgba(255,255,255,0.5); z-index: 10;
}
.empty-state p { margin: 0 0 16px; font-size: 16px; }
.empty-state button {
  padding: 8px 24px; border: 1px solid rgba(255,255,255,0.3); border-radius: 20px;
  background: none; color: #fff; font-size: 14px; cursor: pointer;
}
</style>
