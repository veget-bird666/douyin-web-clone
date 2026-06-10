import { request } from '@/shared/api/http'

const VIDEO_PREFIX = '/video'

/** 获取推荐视频流（按点赞数降序，排除已看过的） */
export function getRecommend(limit = 10, offset = 0) {
  return request(`${VIDEO_PREFIX}/recommend?limit=${limit}&offset=${offset}`)
}

/** 点赞视频 */
export function likeVideo(videoId) {
  return request(`${VIDEO_PREFIX}/like/${videoId}`, { method: 'POST' })
}

/** 取消点赞 */
export function unlikeVideo(videoId) {
  return request(`${VIDEO_PREFIX}/unlike/${videoId}`, { method: 'POST' })
}

/** 查询当前用户是否已点赞 */
export function isLiked(videoId) {
  return request(`${VIDEO_PREFIX}/liked/${videoId}`)
}

/** 记录浏览 */
export function recordView(videoId) {
  return request(`${VIDEO_PREFIX}/view/${videoId}`, { method: 'POST' })
}

/** 获取视频详情 */
export function getVideoInfo(videoId) {
  return request(`${VIDEO_PREFIX}/info/${videoId}`)
}

/** 获取视频播放地址 */
export function getVideoUrl(videoId) {
  return request(`${VIDEO_PREFIX}/url/${videoId}`)
}

/** 收藏视频 */
export function favoriteVideo(videoId) {
  return request(`${VIDEO_PREFIX}/favorite/${videoId}`, { method: 'POST' })
}

/** 取消收藏 */
export function unfavoriteVideo(videoId) {
  return request(`${VIDEO_PREFIX}/unfavorite/${videoId}`, { method: 'POST' })
}

/** 查询当前用户是否已收藏 */
export function isFavorited(videoId) {
  return request(`${VIDEO_PREFIX}/favorited/${videoId}`)
}

/** 获取当前用户收藏列表 */
export function getFavorites() {
  return request(`${VIDEO_PREFIX}/favorites`)
}

/** 获取视频收藏数 */
export function getFavoriteCount(videoId) {
  return request(`${VIDEO_PREFIX}/favorite-count/${videoId}`)
}

/** 获取指定用户的视频列表（按发布时间倒序） */
export function getUserVideos(userId) {
  return request(`${VIDEO_PREFIX}/user/${userId}`)
}

/** 更新视频信息（仅作者可改） */
export function updateVideo(videoId, { title, description }) {
  return request(`${VIDEO_PREFIX}/${videoId}`, {
    method: 'PUT',
    body: { title, description },
  })
}

/** 删除视频（仅作者可删） */
export function deleteVideo(videoId) {
  return request(`${VIDEO_PREFIX}/${videoId}`, { method: 'DELETE' })
}

/** 上传视频（multipart/form-data） */
export async function uploadVideo(file, title, description = '') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('title', title)
  if (description) formData.append('description', description)

  const headers = {}
  const token = localStorage.getItem('douyin_token')
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(`${VIDEO_PREFIX}/upload`, { method: 'POST', headers, body: formData })
  let json
  try {
    json = await res.json()
  } catch {
    if (res.status === 413) {
      return { isSuccess: false, message: '视频文件过大，请上传小于 500MB 的文件', data: null, code: '413' }
    }
    return { isSuccess: false, message: `上传失败（HTTP ${res.status}）`, data: null, code: res.status }
  }

  const code = json.code
  const isSuccess = code === 200 || code === '200'
  return {
    isSuccess,
    message: json.message || json.msg || '',
    data: json.data ?? null,
    code,
  }
}
