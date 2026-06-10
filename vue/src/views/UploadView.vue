<template>
  <MainLayout>
    <section class="upload-page">
      <div v-if="!isLoggedIn" class="login-hint">
        <el-icon class="hint-icon"><Upload /></el-icon>
        <h2>发布视频</h2>
        <p>请先登录后再上传视频</p>
      </div>

      <form v-else class="upload-form" @submit.prevent="onSubmit">
        <h2 class="form-title">发布视频</h2>
        <p class="form-desc">选择本地视频文件，填写标题和描述后提交发布</p>

        <div class="form-field">
          <label class="field-label">视频文件 <span class="required">*</span></label>
          <div
            class="file-drop"
            :class="{ 'has-file': selectedFile }"
            @click="fileInputRef?.click()"
            @dragover.prevent
            @drop.prevent="onDrop"
          >
            <input
              ref="fileInputRef"
              type="file"
              accept="video/mp4,video/webm,video/quicktime,video/x-msvideo,.mp4,.webm,.mov,.avi,.mkv,.flv"
              class="file-input"
              @change="onFileChange"
            />
            <template v-if="selectedFile">
              <el-icon class="drop-icon"><VideoCamera /></el-icon>
              <p class="drop-name">{{ selectedFile.name }}</p>
              <p class="drop-meta">{{ formatSize(selectedFile.size) }}</p>
              <button type="button" class="change-btn" @click.stop="fileInputRef?.click()">
                重新选择
              </button>
            </template>
            <template v-else>
              <el-icon class="drop-icon"><Upload /></el-icon>
              <p class="drop-text">点击或拖拽视频文件到此处</p>
              <p class="drop-tip">支持 mp4、mov、avi、mkv、webm、flv，单文件不超过 500MB</p>
            </template>
          </div>
        </div>

        <div class="form-field">
          <label class="field-label" for="video-title">视频标题 <span class="required">*</span></label>
          <input
            id="video-title"
            v-model="title"
            type="text"
            class="text-input"
            placeholder="请输入视频标题"
            maxlength="200"
          />
        </div>

        <div class="form-field">
          <label class="field-label" for="video-desc">视频描述</label>
          <textarea
            id="video-desc"
            v-model="description"
            class="text-area"
            placeholder="选填，介绍一下你的视频内容"
            rows="4"
          />
        </div>

        <div class="form-actions">
          <button type="submit" class="submit-btn" :disabled="uploading">
            {{ uploading ? '发布中...' : '发布视频' }}
          </button>
          <RouterLink to="/my-videos" class="link-btn">查看我的视频</RouterLink>
        </div>
      </form>
    </section>
  </MainLayout>
</template>

<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Upload, VideoCamera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import MainLayout from '@/layout/MainLayout.vue'
import { useAuth } from '@/features/auth/composables/useAuth'
import { uploadVideo } from '@/features/video/api'

const { isLoggedIn } = useAuth()

const title = ref('')
const description = ref('')
const selectedFile = ref(null)
const uploading = ref(false)
const fileInputRef = ref(null)

const MAX_UPLOAD_BYTES = 500 * 1024 * 1024

function formatSize(bytes) {
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / 1024 / 1024).toFixed(1) + ' MB'
}

function setFile(file) {
  if (!file) return
  if (!file.type.startsWith('video/')) {
    ElMessage.warning('请选择视频文件')
    return
  }
  if (file.size > MAX_UPLOAD_BYTES) {
    ElMessage.warning('视频不能超过 500MB')
    return
  }
  selectedFile.value = file
}

function onFileChange(e) {
  setFile(e.target.files?.[0] || null)
}

function onDrop(e) {
  setFile(e.dataTransfer?.files?.[0] || null)
}

async function onSubmit() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择视频文件')
    return
  }
  if (!title.value.trim()) {
    ElMessage.warning('请填写视频标题')
    return
  }

  uploading.value = true
  try {
    const res = await uploadVideo(
      selectedFile.value,
      title.value.trim(),
      description.value.trim(),
    )
    if (res.isSuccess) {
      ElMessage.success('视频发布成功')
      title.value = ''
      description.value = ''
      selectedFile.value = null
      if (fileInputRef.value) fileInputRef.value.value = ''
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.upload-page {
  max-width: 640px;
  margin: 0 auto;
}

.login-hint {
  margin-top: 80px;
  padding: 48px 32px;
  text-align: center;
  background: var(--dy-bg-elevated);
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius-lg);
}

.hint-icon {
  font-size: 48px;
  color: var(--dy-text-muted);
}

.login-hint h2 {
  margin: 16px 0 8px;
  font-size: 20px;
}

.login-hint p {
  margin: 0;
  color: var(--dy-text-secondary);
}

.upload-form {
  padding: 32px;
  background: var(--dy-bg-elevated);
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius-lg);
}

.form-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
}

.form-desc {
  margin: 0 0 28px;
  font-size: 14px;
  color: var(--dy-text-secondary);
}

.form-field {
  margin-bottom: 20px;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
}

.required {
  color: var(--dy-accent);
}

.file-input {
  display: none;
}

.file-drop {
  padding: 32px 20px;
  text-align: center;
  border: 2px dashed var(--dy-border);
  border-radius: var(--dy-radius-lg);
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}

.file-drop:hover,
.file-drop.has-file {
  border-color: rgba(254, 44, 85, 0.4);
  background: rgba(254, 44, 85, 0.04);
}

.drop-icon {
  font-size: 40px;
  color: var(--dy-text-muted);
}

.drop-text {
  margin: 12px 0 4px;
  font-size: 15px;
}

.drop-tip,
.drop-meta {
  margin: 0;
  font-size: 12px;
  color: var(--dy-text-muted);
}

.drop-name {
  margin: 12px 0 4px;
  font-size: 14px;
  word-break: break-all;
}

.change-btn {
  margin-top: 12px;
  padding: 6px 14px;
  border: 1px solid var(--dy-border);
  border-radius: 6px;
  background: transparent;
  color: var(--dy-text-secondary);
  font-size: 13px;
}

.change-btn:hover {
  background: var(--dy-bg-hover);
  color: var(--dy-text);
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

.form-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 28px;
}

.submit-btn {
  padding: 10px 28px;
  border: none;
  border-radius: 8px;
  background: var(--dy-accent);
  color: #fff;
  font-size: 15px;
  font-weight: 500;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.link-btn {
  font-size: 14px;
  color: var(--dy-link);
}
</style>
