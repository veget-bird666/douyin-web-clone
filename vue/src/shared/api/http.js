/**
 * 抖音 Spring Boot 统一响应：{ code, msg, data }，code 为 "200" 表示成功
 */
export async function request(path, options = {}) {
  const { method = 'GET', body, headers = {} } = options
  const config = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
  }

  if (body !== undefined) {
    config.body = JSON.stringify(body)
  }

  const token = localStorage.getItem('douyin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  const res = await fetch(path, config)
  let json
  try {
    json = await res.json()
  } catch {
    return {
      isSuccess: false,
      message: '服务器响应异常',
      data: null,
      code: res.status,
    }
  }

  const code = json.code
  const isSuccess = code === 200 || code === '200'

  if (code === 401 || code === '401') {
    localStorage.removeItem('douyin_token')
    localStorage.removeItem('douyin_user')
    window.dispatchEvent(new Event('douyin:auth-expired'))
  }

  return {
    isSuccess,
    message: json.message || json.msg || '',
    data: json.data ?? null,
    code,
  }
}
