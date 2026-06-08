import { request } from '@/shared/api/http'

const PREFIX = '/comment'

export function getComments(videoId, limit = 20, offset = 0) {
  return request(`${PREFIX}/list/${videoId}?limit=${limit}&offset=${offset}`)
}

export function getReplies(commentId, limit = 50, offset = 0) {
  return request(`${PREFIX}/replies/${commentId}?limit=${limit}&offset=${offset}`)
}

export function getCommentCount(videoId) {
  return request(`${PREFIX}/count/${videoId}`)
}

export function addComment(videoId, content, parentId = null) {
  const body = { videoId, content }
  if (parentId) body.parentId = parentId
  return request(PREFIX, { method: 'POST', body })
}

export function deleteComment(commentId) {
  return request(`${PREFIX}/${commentId}`, { method: 'DELETE' })
}

export function likeComment(commentId) {
  return request(`${PREFIX}/like/${commentId}`, { method: 'POST' })
}

export function unlikeComment(commentId) {
  return request(`${PREFIX}/unlike/${commentId}`, { method: 'POST' })
}

export function foldComment(commentId) {
  return request(`${PREFIX}/fold/${commentId}`, { method: 'POST' })
}

export function unfoldComment(commentId) {
  return request(`${PREFIX}/unfold/${commentId}`, { method: 'POST' })
}
