<template>
  <MainLayout>
    <section class="home-panel">
      <div class="panel-icon">抖</div>
      <h2 class="panel-title">视频流推荐系统</h2>
      <p class="panel-desc">
        登录后可上传视频、在推荐页播放，并测试点赞与收藏功能。
      </p>

      <ul class="feature-list">
        <li v-for="item in features" :key="item.label">
          <span :class="['feature-dot', item.status]" />
          <span class="feature-label">{{ item.label }}</span>
          <span class="feature-status">{{ item.statusText }}</span>
        </li>
      </ul>

      <p v-if="!isLoggedIn" class="panel-hint">
        点击右上角头像登录或注册
      </p>
      <p v-else class="panel-hint">
        你好，{{ displayName }}！先上传视频，再进入推荐页体验。
      </p>

      <div v-if="isLoggedIn" class="upload-box">
        <h3 class="upload-title">上传测试视频</h3>
        <input
          v-model="uploadTitle"
          type="text"
          class="upload-input"
          placeholder="视频标题（必填）"
        />
        <input
          ref="fileInputRef"
          type="file"
          accept="video/mp4,video/webm,video/quicktime,.mp4,.webm,.mov"
          class="upload-file"
          @change="onFileChange"
        />
        <p v-if="selectedFile" class="upload-file-name">
          {{ selectedFile.name }}（{{ formatSize(selectedFile.size) }}）
        </p>
        <p class="upload-tip">支持 mp4 等格式，单文件不超过 500MB</p>
        <button class="upload-btn" :disabled="uploading" @click="onUpload">
          {{ uploading ? '上传中...' : '上传视频' }}
        </button>
      </div>

      <div class="action-links">
        <RouterLink to="/recommend" class="link-button">进入推荐页</RouterLink>
      </div>
    </section>
  </MainLayout>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ElMessage } from 'element-plus'
import MainLayout from '@/layout/MainLayout.vue'
import { useAuth } from '@/features/auth/composables/useAuth'
import { uploadVideo } from '@/features/video/api'

const { isLoggedIn, displayName } = useAuth()

const uploadTitle = ref('')
const selectedFile = ref(null)
const uploading = ref(false)
const fileInputRef = ref(null)

const features = [
  { label: '用户注册 / 登录 / 注销', status: 'done', statusText: '已可用' },
  { label: '视频推荐 / 播放', status: 'done', statusText: '已可用' },
  { label: '视频点赞 / 收藏', status: 'done', statusText: '已可用' },
  { label: '视频上传', status: 'done', statusText: '本页可上传' },
  { label: '视频评论', status: 'done', statusText: '已可用' },
]

const MAX_UPLOAD_BYTES = 500 * 1024 * 1024

function formatSize(bytes) {
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function onFileChange(e) {
  selectedFile.value = e.target.files?.[0] || null
}

async function onUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择视频文件（mp4 等）')
    return
  }
  if (!uploadTitle.value.trim()) {
    ElMessage.warning('请填写视频标题')
    return
  }
  if (selectedFile.value.size > MAX_UPLOAD_BYTES) {
    ElMessage.warning('视频不能超过 500MB，请换一个小一点的文件')
    return
  }

  uploading.value = true
  try {
    const res = await uploadVideo(selectedFile.value, uploadTitle.value.trim())
    if (res.isSuccess) {
      ElMessage.success('上传成功，可以去推荐页观看了')
      uploadTitle.value = ''
      selectedFile.value = null
      if (fileInputRef.value) fileInputRef.value.value = ''
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (err) {
    ElMessage.error(err?.message || '网络异常，请稍后重试')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.home-panel {
  max-width: 560px;
  margin: 48px auto 0;
  padding: 40px 32px;
  text-align: center;
  background: var(--dy-bg-elevated);
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius-lg);
}

.panel-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #25f4ee, #fe2c55);
  border-radius: 14px;
}

.panel-title {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 600;
}

.panel-desc {
  margin: 0 0 28px;
  font-size: 14px;
  line-height: 1.7;
  color: var(--dy-text-secondary);
}

.feature-list {
  margin: 0 0 24px;
  padding: 0;
  list-style: none;
  text-align: left;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--dy-border);
  font-size: 14px;
}

.feature-list li:last-child {
  border-bottom: none;
}

.feature-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.feature-dot.done { background: #52c41a; }
.feature-dot.api { background: #1890ff; }
.feature-dot.pending { background: var(--dy-text-muted); }

.feature-label {
  flex: 1;
  color: var(--dy-text);
}

.feature-status {
  font-size: 12px;
  color: var(--dy-text-muted);
}

.panel-hint {
  margin: 0;
  font-size: 13px;
  color: var(--dy-text-muted);
}

.upload-box {
  margin-top: 24px;
  padding: 20px;
  text-align: left;
  background: var(--dy-bg);
  border: 1px dashed var(--dy-border);
  border-radius: var(--dy-radius-lg);
}

.upload-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
}

.upload-input {
  width: 100%;
  margin-bottom: 12px;
  padding: 10px 12px;
  border: 1px solid var(--dy-border);
  border-radius: 8px;
  background: var(--dy-bg-elevated);
  color: var(--dy-text);
  box-sizing: border-box;
}

.upload-file {
  width: 100%;
  font-size: 13px;
}

.upload-file-name {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--dy-text-secondary);
  word-break: break-all;
}

.upload-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--dy-text-muted);
}

.upload-btn {
  width: 100%;
  margin-top: 14px;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: #fe2c55;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.upload-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-links {
  margin-top: 20px;
}

.link-button {
  display: inline-block;
  padding: 10px 18px;
  color: #fff;
  background: #1890ff;
  border-radius: 20px;
  text-decoration: none;
}
</style>
