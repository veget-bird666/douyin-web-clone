import { ref, computed } from 'vue'
import * as authApi from '@/features/auth/api'

const TOKEN_KEY = 'douyin_token'
const USER_KEY = 'douyin_user'

const token = ref(localStorage.getItem(TOKEN_KEY) || '')
const user = ref(readStoredUser())

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function useAuth() {
  const isLoggedIn = computed(() => Boolean(token.value))

  const displayName = computed(() => {
    if (!user.value) return ''
    return user.value.nickname || user.value.email?.split('@')[0] || '用户'
  })

  const avatarText = computed(() => {
    const name = displayName.value
    return name ? name.charAt(0).toUpperCase() : ''
  })

  function setSession(newToken, newUser) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem(TOKEN_KEY, newToken)
    localStorage.setItem(USER_KEY, JSON.stringify(newUser))
  }

  function clearSession() {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  async function login(email, password) {
    const res = await authApi.login({ email, password })
    if (!res.isSuccess) return res

    const newToken = res.data?.token
    const newUser = res.data?.user
    if (newToken && newUser) {
      setSession(newToken, newUser)
    }
    return res
  }

  async function register(email, password, code) {
    return authApi.register({ email, password, code })
  }

  async function sendCode(email) {
    return authApi.sendCode(email)
  }

  function logout() {
    clearSession()
  }

  return {
    token,
    user,
    isLoggedIn,
    displayName,
    avatarText,
    login,
    register,
    sendCode,
    logout,
  }
}
