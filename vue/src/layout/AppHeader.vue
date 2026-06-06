<template>
  <header class="header">
    <h1 class="header-title">{{ pageTitle }}</h1>

    <div class="header-actions">
      <div class="avatar-wrap">
        <button
          type="button"
          class="avatar-btn"
          :title="isLoggedIn ? displayName : '登录 / 注册'"
          @click="onAvatarClick"
        >
          <img
            v-if="isLoggedIn && user?.avatar"
            :src="user.avatar"
            alt=""
            class="avatar-img"
          />
          <span v-else-if="isLoggedIn" class="avatar-letter">{{ avatarText }}</span>
          <span v-else class="avatar-guest">
            <el-icon><User /></el-icon>
          </span>
        </button>

        <div v-if="isLoggedIn && showMenu" class="user-menu">
          <div class="menu-user">
            <span class="menu-name">{{ displayName }}</span>
            <span class="menu-email">{{ user?.email }}</span>
          </div>
          <button type="button" class="menu-item" @click="onLogout">
            退出登录
          </button>
          <button type="button" class="menu-item menu-item-danger" @click="onDeleteAccount">
            注销账号
          </button>
        </div>
      </div>
    </div>

    <AuthDialog v-model="authVisible" @success="showMenu = false" />
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuth } from '@/features/auth/composables/useAuth'
import AuthDialog from '@/features/auth/components/AuthDialog.vue'

const route = useRoute()
const { isLoggedIn, displayName, avatarText, user, logout, deleteAccount } = useAuth()

const authVisible = ref(false)
const showMenu = ref(false)

const pageTitle = computed(() => route.meta?.title || '推荐')

function onAvatarClick() {
  if (isLoggedIn.value) {
    showMenu.value = !showMenu.value
  } else {
    authVisible.value = true
  }
}

function onLogout() {
  logout()
  showMenu.value = false
  ElMessage.success('已退出登录')
}

async function onDeleteAccount() {
  showMenu.value = false
  try {
    await ElMessageBox.confirm(
      '注销后账号及相关数据将无法恢复，确定要注销吗？',
      '注销账号',
      {
        confirmButtonText: '确定注销',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger',
      },
    )
  } catch {
    return
  }

  const res = await deleteAccount()
  if (res.isSuccess) {
    ElMessage.success('账号已注销')
  } else {
    ElMessage.error(res.message || '注销失败')
  }
}

function onDocClick(e) {
  if (!e.target.closest?.('.avatar-wrap')) {
    showMenu.value = false
  }
}

onMounted(() => document.addEventListener('click', onDocClick))
onUnmounted(() => document.removeEventListener('click', onDocClick))
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 160px;
  right: 0;
  height: var(--dy-header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  padding: 0 24px 0 20px;
  background: var(--dy-bg);
  border-bottom: 1px solid var(--dy-border);
  z-index: 90;
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--dy-text);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.avatar-wrap {
  position: relative;
  margin-left: 8px;
}

.avatar-btn {
  width: 40px;
  height: 40px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  transition: border-color 0.2s;
}

.avatar-btn:hover {
  border-color: rgba(255, 255, 255, 0.25);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-letter {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #fe2c55, #ff8a5c);
}

.avatar-guest {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: var(--dy-text-muted);
  font-size: 20px;
  background: rgba(255, 255, 255, 0.06);
}

.user-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 180px;
  padding: 8px 0;
  background: var(--dy-bg-elevated);
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.menu-user {
  padding: 10px 16px 8px;
  border-bottom: 1px solid var(--dy-border);
}

.menu-name {
  display: block;
  font-weight: 500;
}

.menu-email {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: var(--dy-text-secondary);
}

.menu-item {
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: none;
  color: var(--dy-text);
  font-size: 14px;
  text-align: left;
  transition: background 0.15s;
}

.menu-item:hover {
  background: var(--dy-bg-hover);
}

.menu-item-danger {
  color: #fe2c55;
}

.menu-item-danger:hover {
  background: rgba(254, 44, 85, 0.1);
}
</style>
