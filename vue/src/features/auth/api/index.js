import { request } from '@/shared/api/http'

/** 抖音 Spring Boot 认证接口 */
const AUTH_PREFIX = '/api/auth'

export function sendCode(email) {
  return request(`${AUTH_PREFIX}/send-code`, {
    method: 'POST',
    body: { email: email.trim().toLowerCase() },
  })
}

export function register({ email, password, code }) {
  return request(`${AUTH_PREFIX}/register`, {
    method: 'POST',
    body: {
      email: email.trim().toLowerCase(),
      password,
      code: code.trim(),
    },
  })
}

export function login({ email, password }) {
  return request(`${AUTH_PREFIX}/login`, {
    method: 'POST',
    body: {
      email: email.trim().toLowerCase(),
      password,
    },
  })
}
