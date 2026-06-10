<template>
  <el-dialog
    v-model="visible"
    :title="editing ? '编辑视频' : '视频详情'"
    width="720px"
    class="video-detail-dialog"
    destroy-on-close
    @closed="onClosed"
  >
    <div v-if="loading" class="dialog-loading">加载中...</div>

    <template v-else-if="detail">
      <div class="player-wrap">
        <video
          v-if="playUrl"
          ref="videoRef"
          :src="playUrl"
          class="player"
          controls
          playsinline
        />
        <div v-else class="player-placeholder">
          <el-icon><VideoPlay /></el-icon>
          <span>视频加载失败</span>
        </div>
      </div>

      <div v-if="editing" class="edit-form">
        <label class="field-label">标题</label>
        <input v-model="editTitle" type="text" class="text-input" maxlength="200" />
        <label class="field-label">描述</label>
        <textarea v-model="editDescription" class="text-area" rows="4" />
      </div>

      <div v-else class="detail-info">
        <h3 class="detail-title">{{ detail.title || '未命名视频' }}</h3>
        <p class="detail-desc">{{ detail.description || '暂无描述' }}</p>
        <div class="detail-meta">
          <span>发布时间 {{ formatTime(detail.createTime) }}</span>
          <span>播放 {{ detail.viewCount ?? 0 }}</span>
          <span>点赞 {{ detail.likeCount ?? 0 }}</span>
          <span>评论 {{ detail.commentCount ?? 0 }}</span>
          <span v-if="detail.format">{{ detail.format.toUpperCase() }}</span>
          <span v-if="detail.size">{{ formatSize(detail.size) }}</span>
        </div>
      </div>
    </template>

    <template #footer>
      <template v-if="editing">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
      <template v-else>
        <el-button @click="visible = false">关闭</el-button>
        <el-button type="primary" @click="startEdit">编辑</el-button>
      </template>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { VideoPlay } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getVideoInfo, getVideoUrl, updateVideo } from '@/features/video/api'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  videoId: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue', 'updated'])

const visible = ref(false)
const loading = ref(false)
const saving = ref(false)
const editing = ref(false)
const detail = ref(null)
const playUrl = ref('')
const editTitle = ref('')
const editDescription = ref('')
const videoRef = ref(null)

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val && props.videoId) loadDetail()
  },
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

function formatTime(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 16)
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

async function loadDetail() {
  loading.value = true
  editing.value = false
  detail.value = null
  playUrl.value = ''

  try {
    const [infoRes, urlRes] = await Promise.all([
      getVideoInfo(props.videoId),
      getVideoUrl(props.videoId),
    ])
    if (infoRes.isSuccess) {
      detail.value = infoRes.data
      editTitle.value = infoRes.data?.title || ''
      editDescription.value = infoRes.data?.description || ''
    } else {
      ElMessage.error(infoRes.message || '获取视频详情失败')
      visible.value = false
      return
    }
    if (urlRes.isSuccess && urlRes.data?.url) {
      playUrl.value = urlRes.data.url
    }
  } catch {
    ElMessage.error('加载视频失败')
    visible.value = false
  } finally {
    loading.value = false
  }
}

function startEdit() {
  editTitle.value = detail.value?.title || ''
  editDescription.value = detail.value?.description || ''
  editing.value = true
  videoRef.value?.pause()
}

function cancelEdit() {
  editing.value = false
  editTitle.value = detail.value?.title || ''
  editDescription.value = detail.value?.description || ''
}

async function onSave() {
  if (!editTitle.value.trim()) {
    ElMessage.warning('请填写视频标题')
    return
  }

  saving.value = true
  try {
    const res = await updateVideo(props.videoId, {
      title: editTitle.value.trim(),
      description: editDescription.value.trim(),
    })
    if (res.isSuccess) {
      ElMessage.success('保存成功')
      detail.value = res.data || {
        ...detail.value,
        title: editTitle.value.trim(),
        description: editDescription.value.trim(),
      }
      editing.value = false
      emit('updated', detail.value)
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    saving.value = false
  }
}

function onClosed() {
  editing.value = false
  detail.value = null
  playUrl.value = ''
  videoRef.value?.pause()
}
</script>

<style scoped>
.dialog-loading {
  padding: 48px;
  text-align: center;
  color: var(--dy-text-secondary);
}

.player-wrap {
  margin-bottom: 20px;
  border-radius: var(--dy-radius-lg);
  overflow: hidden;
  background: #000;
}

.player {
  display: block;
  width: 100%;
  max-height: 400px;
  background: #000;
}

.player-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 240px;
  color: var(--dy-text-muted);
  font-size: 14px;
}

.player-placeholder .el-icon {
  font-size: 40px;
}

.detail-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
}

.detail-desc {
  margin: 0 0 12px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--dy-text-secondary);
  white-space: pre-wrap;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--dy-text-muted);
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: 13px;
  font-weight: 500;
}

.text-input,
.text-area {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius);
  background: var(--dy-bg);
  color: var(--dy-text);
  font-size: 14px;
  resize: vertical;
}

.text-input:focus,
.text-area:focus {
  outline: none;
  border-color: rgba(254, 44, 85, 0.5);
}
</style>
