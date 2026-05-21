<template>
  <Teleport to="body">
    <Transition name="auth-fade">
      <div v-if="visible" class="auth-overlay" @click.self="close">
        <div class="auth-dialog" role="dialog" aria-labelledby="auth-title">
          <button type="button" class="auth-close" aria-label="关闭" @click="close">
            <el-icon><Close /></el-icon>
          </button>

          <div class="auth-brand">
            <div class="auth-logo">抖</div>
            <h2 id="auth-title" class="auth-title">登录抖音精选</h2>
            <p class="auth-subtitle">使用邮箱登录或注册新账号</p>
          </div>

          <div class="auth-tabs">
            <button
              type="button"
              :class="['auth-tab', { active: tab === 'login' }]"
              @click="tab = 'login'"
            >
              登录
            </button>
            <button
              type="button"
              :class="['auth-tab', { active: tab === 'register' }]"
              @click="tab = 'register'"
            >
              注册
            </button>
            <span class="auth-tab-indicator" :style="indicatorStyle" />
          </div>

          <!-- 登录 -->
          <form v-show="tab === 'login'" class="auth-form" @submit.prevent="onLogin">
            <label class="auth-field">
              <span class="auth-label">邮箱</span>
              <input
                v-model="loginEmail"
                type="email"
                placeholder="请输入邮箱"
                autocomplete="email"
              />
            </label>
            <label class="auth-field">
              <span class="auth-label">密码</span>
              <div class="auth-input-wrap">
                <input
                  v-model="loginPassword"
                  :type="loginPwdVisible ? 'text' : 'password'"
                  placeholder="至少 6 位"
                  autocomplete="current-password"
                />
                <button
                  type="button"
                  class="auth-eye"
                  @click="loginPwdVisible = !loginPwdVisible"
                >
                  <el-icon>
                    <View v-if="loginPwdVisible" />
                    <Hide v-else />
                  </el-icon>
                </button>
              </div>
            </label>
            <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>
            <button type="submit" class="auth-submit" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
            <p class="auth-switch">
              还没有账号？
              <button type="button" class="auth-link" @click="tab = 'register'">去注册</button>
            </p>
          </form>

          <!-- 注册 -->
          <form v-show="tab === 'register'" class="auth-form" @submit.prevent="onRegister">
            <label class="auth-field">
              <span class="auth-label">邮箱</span>
              <input
                v-model="registerEmail"
                type="email"
                placeholder="请输入邮箱"
                autocomplete="email"
              />
            </label>
            <label class="auth-field">
              <span class="auth-label">验证码</span>
              <div class="auth-code-row">
                <input
                  v-model="registerCode"
                  type="text"
                  inputmode="numeric"
                  maxlength="6"
                  placeholder="6 位验证码"
                />
                <button
                  type="button"
                  class="auth-code-btn"
                  :disabled="countdown > 0 || sendingCode"
                  @click="onSendCode"
                >
                  {{ sendingCode ? '发送中' : countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </button>
              </div>
            </label>
            <label class="auth-field">
              <span class="auth-label">密码</span>
              <div class="auth-input-wrap">
                <input
                  v-model="registerPassword"
                  :type="registerPwdVisible ? 'text' : 'password'"
                  placeholder="至少 6 位"
                  autocomplete="new-password"
                />
                <button
                  type="button"
                  class="auth-eye"
                  @click="registerPwdVisible = !registerPwdVisible"
                >
                  <el-icon>
                    <View v-if="registerPwdVisible" />
                    <Hide v-else />
                  </el-icon>
                </button>
              </div>
            </label>
            <label class="auth-field">
              <span class="auth-label">确认密码</span>
              <input
                v-model="registerConfirm"
                :type="registerPwdVisible ? 'text' : 'password'"
                placeholder="再次输入密码"
                autocomplete="new-password"
              />
            </label>
            <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>
            <button type="submit" class="auth-submit" :disabled="loading">
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Close, View, Hide } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useAuth } from '@/features/auth/composables/useAuth'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'success'])

const { login, register, sendCode } = useAuth()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const tab = ref('login')
const loading = ref(false)
const sendingCode = ref(false)
const errorMsg = ref('')
const countdown = ref(0)
let countdownTimer = null

const loginEmail = ref('')
const loginPassword = ref('')
const loginPwdVisible = ref(false)

const registerEmail = ref('')
const registerCode = ref('')
const registerPassword = ref('')
const registerConfirm = ref('')
const registerPwdVisible = ref(false)

const indicatorStyle = computed(() => ({
  transform: tab.value === 'login' ? 'translateX(0)' : 'translateX(100%)',
}))

watch(visible, (open) => {
  if (open) {
    errorMsg.value = ''
    tab.value = 'login'
  }
})

function close() {
  visible.value = false
}

function validateEmail(email) {
  if (!email?.trim()) return '请输入邮箱'
  if (!/^[\w.-]+@[\w-]+(\.[\w-]+)+$/.test(email.trim())) return '邮箱格式不正确'
  return null
}

function validatePassword(password) {
  if (!password) return '请输入密码'
  if (password.length < 6) return '密码至少 6 位'
  if (password.length > 32) return '密码不能超过 32 位'
  return null
}

async function onLogin() {
  errorMsg.value = ''
  const emailErr = validateEmail(loginEmail.value)
  if (emailErr) {
    errorMsg.value = emailErr
    return
  }
  const pwdErr = validatePassword(loginPassword.value)
  if (pwdErr) {
    errorMsg.value = pwdErr
    return
  }

  loading.value = true
  const res = await login(loginEmail.value, loginPassword.value)
  loading.value = false

  if (!res.isSuccess) {
    errorMsg.value = res.message || '登录失败'
    return
  }

  ElMessage.success(res.message || '登录成功')
  emit('success')
  close()
}

async function onRegister() {
  errorMsg.value = ''
  const emailErr = validateEmail(registerEmail.value)
  if (emailErr) {
    errorMsg.value = emailErr
    return
  }
  if (!registerCode.value.trim()) {
    errorMsg.value = '请输入验证码'
    return
  }
  const pwdErr = validatePassword(registerPassword.value)
  if (pwdErr) {
    errorMsg.value = pwdErr
    return
  }
  if (registerPassword.value !== registerConfirm.value) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  const res = await register(
    registerEmail.value,
    registerPassword.value,
    registerCode.value,
  )
  loading.value = false

  if (!res.isSuccess) {
    errorMsg.value = res.message || '注册失败'
    return
  }

  ElMessage.success(res.message || '注册成功，请登录')
  tab.value = 'login'
  loginEmail.value = registerEmail.value
}

async function onSendCode() {
  errorMsg.value = ''
  const emailErr = validateEmail(registerEmail.value)
  if (emailErr) {
    ElMessage.warning(emailErr)
    return
  }

  sendingCode.value = true
  const res = await sendCode(registerEmail.value)
  sendingCode.value = false

  if (!res.isSuccess) {
    ElMessage.error(res.message || '发送失败')
    return
  }

  ElMessage.success(res.message || '验证码已发送，请查收邮箱')
  countdown.value = 30
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    if (countdown.value <= 1) {
      clearInterval(countdownTimer)
      countdown.value = 0
    } else {
      countdown.value -= 1
    }
  }, 1000)
}
</script>

<style scoped>
.auth-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.65);
  backdrop-filter: blur(4px);
}

.auth-dialog {
  position: relative;
  width: 420px;
  max-width: calc(100vw - 32px);
  padding: 32px 36px 28px;
  background: linear-gradient(165deg, #1f2030 0%, #161823 100%);
  border: 1px solid var(--dy-border);
  border-radius: 16px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.5);
}

.auth-close {
  position: absolute;
  top: 14px;
  right: 14px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--dy-text-secondary);
  transition: background 0.2s, color 0.2s;
}

.auth-close:hover {
  background: var(--dy-bg-hover);
  color: var(--dy-text);
}

.auth-brand {
  text-align: center;
  margin-bottom: 24px;
}

.auth-logo {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #25f4ee, #fe2c55);
  border-radius: 12px;
}

.auth-title {
  margin: 0 0 6px;
  font-size: 20px;
  font-weight: 600;
}

.auth-subtitle {
  margin: 0;
  font-size: 13px;
  color: var(--dy-text-secondary);
}

.auth-tabs {
  position: relative;
  display: flex;
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 10px;
  padding: 4px;
}

.auth-tab {
  flex: 1;
  z-index: 1;
  padding: 8px 0;
  border: none;
  background: transparent;
  color: var(--dy-text-secondary);
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}

.auth-tab.active {
  color: var(--dy-text);
}

.auth-tab-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: var(--dy-bg-hover);
  border-radius: 8px;
  transition: transform 0.25s ease;
  pointer-events: none;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.auth-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.auth-label {
  font-size: 12px;
  color: var(--dy-text-secondary);
}

.auth-field input {
  width: 100%;
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius);
  background: rgba(0, 0, 0, 0.25);
  color: var(--dy-text);
  outline: none;
  transition: border-color 0.2s;
}

.auth-field input:focus {
  border-color: rgba(254, 44, 85, 0.5);
}

.auth-field input::placeholder {
  color: var(--dy-text-muted);
}

.auth-input-wrap {
  position: relative;
}

.auth-input-wrap input {
  padding-right: 40px;
}

.auth-eye {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--dy-text-secondary);
}

.auth-code-row {
  display: flex;
  gap: 10px;
}

.auth-code-row input {
  flex: 1;
}

.auth-code-btn {
  flex-shrink: 0;
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--dy-border);
  border-radius: var(--dy-radius);
  background: transparent;
  color: var(--dy-text);
  font-size: 13px;
  white-space: nowrap;
  transition: border-color 0.2s, background 0.2s;
}

.auth-code-btn:hover:not(:disabled) {
  border-color: var(--dy-accent);
  background: var(--dy-accent-soft);
}

.auth-code-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.auth-error {
  margin: 0;
  font-size: 12px;
  color: #ff6b6b;
}

.auth-submit {
  height: 44px;
  margin-top: 4px;
  border: none;
  border-radius: var(--dy-radius);
  background: linear-gradient(90deg, #fe2c55, #ff6b8a);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  transition: opacity 0.2s, transform 0.15s;
}

.auth-submit:hover:not(:disabled) {
  opacity: 0.92;
  transform: translateY(-1px);
}

.auth-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.auth-switch {
  margin: 4px 0 0;
  text-align: center;
  font-size: 13px;
  color: var(--dy-text-secondary);
}

.auth-link {
  border: none;
  background: none;
  color: var(--dy-accent);
  font-size: 13px;
  padding: 0;
}

.auth-fade-enter-active,
.auth-fade-leave-active {
  transition: opacity 0.2s ease;
}

.auth-fade-enter-active .auth-dialog,
.auth-fade-leave-active .auth-dialog {
  transition: transform 0.2s ease, opacity 0.2s ease;
}

.auth-fade-enter-from,
.auth-fade-leave-to {
  opacity: 0;
}

.auth-fade-enter-from .auth-dialog,
.auth-fade-leave-to .auth-dialog {
  transform: scale(0.96) translateY(8px);
  opacity: 0;
}
</style>
