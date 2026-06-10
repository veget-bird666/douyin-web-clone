<template>
  <MainLayout>
    <section class="my-videos-page">
      <div class="page-header">
        <div>
          <h2 class="page-title">我的视频</h2>
          <p class="page-desc">点击视频可观看详情，支持编辑标题与描述</p>
        </div>
        <RouterLink v-if="isLoggedIn" to="/upload" class="upload-link">
          <el-icon><Plus /></el-icon>
          发布视频
        </RouterLink>
      </div>

      <div v-if="!isLoggedIn" class="login-hint">
        <el-icon class="hint-icon"><VideoCamera /></el-icon>
        <p>请先登录后查看你的视频</p>
      </div>

      <div v-else-if="loading" class="state-box">
        <p>加载中...</p>
      </div>

      <div v-else-if="allVideos.length === 0" class="state-box">
        <el-icon class="hint-icon"><VideoCamera /></el-icon>
        <p>还没有发布过视频</p>
        <RouterLink to="/upload" class="empty-link">去发布第一个视频</RouterLink>
      </div>

      <template v-else>
        <div class="video-list">
          <article
            v-for="video in pagedVideos"
            :key="video.videoId"
            class="video-card"
          >
            <button type="button" class="video-preview" @click="openDetail(video)">
              <el-icon class="play-icon"><VideoPlay /></el-icon>
              <span v-if="video.format" class="format-badge">{{ video.format }}</span>
            </button>

            <button type="button" class="video-main" @click="openDetail(video)">
              <h3 class="video-title">{{ video.title || '未命名视频' }}</h3>
              <p v-if="video.description" class="video-desc">{{ video.description }}</p>
              <div class="video-meta">
                <span>{{ formatTime(video.createTime) }}</span>
                <span>播放 {{ video.viewCount ?? 0 }}</span>
                <span>点赞 {{ video.likeCount ?? 0 }}</span>
                <span>评论 {{ video.commentCount ?? 0 }}</span>
              </div>
            </button>

            <div class="card-actions">
              <button type="button" class="action-btn" @click="openDetail(video)">
                观看
              </button>
              <button
                type="button"
                class="delete-btn"
                :disabled="deletingId === video.videoId"
                @click.stop="onDelete(video)"
              >
                {{ deletingId === video.videoId ? '删除中...' : '删除' }}
              </button>
            </div>
          </article>
        </div>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="allVideos.length"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            background
          />
        </div>
      </template>
    </section>

    <VideoDetailDialog
      v-model="detailVisible"
      :video-id="activeVideoId"
      @updated="onVideoUpdated"
    />
  </MainLayout>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { Plus, VideoCamera, VideoPlay } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MainLayout from '@/layout/MainLayout.vue'
import VideoDetailDialog from '@/features/video/components/VideoDetailDialog.vue'
import { useAuth } from '@/features/auth/composables/useAuth'
import { getUserVideos, deleteVideo } from '@/features/video/api'

const { isLoggedIn, user } = useAuth()

const loading = ref(false)
const allVideos = ref([])
const page = ref(1)
const pageSize = ref(10)
const deletingId = ref('')
const detailVisible = ref(false)
const activeVideoId = ref('')

const pagedVideos = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return allVideos.value.slice(start, start + pageSize.value)
})

function formatTime(value) {
  if (!value) return '-'
  const str = String(value).replace('T', ' ')
  return str.slice(0, 16)
}

function openDetail(video) {
  activeVideoId.value = video.videoId
  detailVisible.value = true
}

function onVideoUpdated(updated) {
  const idx = allVideos.value.findIndex((v) => v.videoId === updated.videoId)
  if (idx !== -1) {
    allVideos.value[idx] = { ...allVideos.value[idx], ...updated }
  }
}

async function fetchVideos() {
  if (!isLoggedIn.value || !user.value?.userId) {
    allVideos.value = []
    return
  }

  loading.value = true
  try {
    const res = await getUserVideos(user.value.userId)
    if (res.isSuccess) {
      const list = Array.isArray(res.data) ? res.data : []
      allVideos.value = list.filter((v) => v.status === 1 || v.status === undefined)
    } else {
      allVideos.value = []
      ElMessage.error(res.message || '获取视频列表失败')
    }
  } catch {
    allVideos.value = []
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function onDelete(video) {
  try {
    await ElMessageBox.confirm(
      `确定删除视频「${video.title || '未命名视频'}」吗？删除后不可恢复。`,
      '删除视频',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
      },
    )
  } catch {
    return
  }

  deletingId.value = video.videoId
  try {
    const res = await deleteVideo(video.videoId)
    if (res.isSuccess) {
      ElMessage.success('删除成功')
      if (activeVideoId.value === video.videoId) detailVisible.value = false
      allVideos.value = allVideos.value.filter((v) => v.videoId !== video.videoId)
      const maxPage = Math.max(1, Math.ceil(allVideos.value.length / pageSize.value))
      if (page.value > maxPage) page.value = maxPage
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    deletingId.value = ''
  }
}

watch([isLoggedIn, () => user.value?.userId], fetchVideos, { immediate: true })

watch(pageSize, () => {
  page.value = 1
})
</script>

<style scoped>
.my-videos-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 600;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: var(--dy-text-secondary);
}

.upload-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  background: var(--dy-accent);
  color: #fff;
  font-size: 14px;
  white-space: nowrap;
}

.login-hint,
.state-box {
  padding: 64px 32px;
  text-align: center;
  background: var(--dy-bg-elevated);
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius-lg);
  color: var(--dy-text-secondary);
}

.hint-icon {
  font-size: 48px;
  color: var(--dy-text-muted);
}

.empty-link {
  display: inline-block;
  margin-top: 12px;
  color: var(--dy-link);
}

.video-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.video-card {
  display: flex;
  align-items: stretch;
  gap: 16px;
  padding: 16px;
  background: var(--dy-bg-elevated);
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius-lg);
}

.video-preview {
  position: relative;
  flex-shrink: 0;
  width: 120px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--dy-radius);
  background: var(--dy-bg);
  cursor: pointer;
  transition: background 0.15s;
}

.video-preview:hover {
  background: var(--dy-bg-hover);
}

.play-icon {
  font-size: 32px;
  color: var(--dy-text-secondary);
}

.format-badge {
  position: absolute;
  right: 6px;
  bottom: 6px;
  padding: 1px 5px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.6);
  font-size: 10px;
  text-transform: uppercase;
}

.video-main {
  flex: 1;
  min-width: 0;
  padding: 0;
  border: none;
  background: none;
  text-align: left;
  color: inherit;
  cursor: pointer;
}

.video-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
}

.video-desc {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--dy-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.video-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--dy-text-muted);
}

.card-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  padding: 8px 16px;
  border: 1px solid var(--dy-border);
  border-radius: 6px;
  background: transparent;
  color: var(--dy-text);
  font-size: 13px;
}

.action-btn:hover {
  background: var(--dy-bg-hover);
}

.delete-btn {
  padding: 8px 16px;
  border: 1px solid rgba(254, 44, 85, 0.4);
  border-radius: 6px;
  background: transparent;
  color: var(--dy-accent);
  font-size: 13px;
}

.delete-btn:hover:not(:disabled) {
  background: var(--dy-accent-soft);
}

.delete-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

.pagination-wrap :deep(.el-pagination) {
  --el-pagination-bg-color: var(--dy-bg-elevated);
  --el-pagination-button-bg-color: var(--dy-bg-hover);
  --el-pagination-hover-color: var(--dy-accent);
}
</style>
