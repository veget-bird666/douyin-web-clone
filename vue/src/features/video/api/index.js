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

/** 获取视频播放地址 */
export function getVideoUrl(videoId) {
  return request(`${VIDEO_PREFIX}/url/${videoId}`)
}
