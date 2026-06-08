package com.example.springboot.service;

import com.example.springboot.entity.Comment;
import com.example.springboot.entity.CommentVO;
import com.example.springboot.entity.Video;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.feature.auth.entity.User;
import com.example.springboot.feature.auth.mapper.UserMapper;
import com.example.springboot.mapper.CommentLikeRecordMapper;
import com.example.springboot.mapper.CommentMapper;
import com.example.springboot.mapper.VideoMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CommentService {

    private static final int MAX_CONTENT_LENGTH = 500;

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private CommentLikeRecordMapper commentLikeRecordMapper;

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private UserMapper userMapper;

    public List<CommentVO> listTopLevelComments(String videoId, int limit, int offset) {
        ensureVideoExists(videoId);
        List<CommentVO> list = commentMapper.selectTopLevelByVideoId(videoId, limit, offset);
        for (CommentVO vo : list) {
            vo.setReplyCount(commentMapper.countRepliesByParentId(vo.getCommentId()));
        }
        enrichLikeStatus(list);
        return list;
    }

    public List<CommentVO> listReplies(String commentId, int limit, int offset) {
        Comment parent = commentMapper.selectByCommentId(commentId);
        if (parent == null) {
            throw new CustomerException("404", "评论不存在");
        }
        List<CommentVO> list = commentMapper.selectRepliesByParentId(commentId, limit, offset);
        enrichLikeStatus(list);
        return list;
    }

    public int getCommentCount(String videoId) {
        return commentMapper.countByVideoId(videoId);
    }

    @Transactional(rollbackFor = Exception.class)
    public CommentVO addComment(String videoId, String content, String targetCommentId) {
        String userId = getCurrentUserId();
        ensureVideoExists(videoId);

        String trimmed = content == null ? "" : content.trim();
        if (trimmed.isEmpty()) {
            throw new CustomerException("400", "评论内容不能为空");
        }
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new CustomerException("400", "评论内容不能超过 " + MAX_CONTENT_LENGTH + " 字");
        }

        String rootParentId = null;
        String replyToUserId = null;
        if (targetCommentId != null && !targetCommentId.isBlank()) {
            Comment target = commentMapper.selectByCommentId(targetCommentId);
            if (target == null) {
                throw new CustomerException("404", "被回复的评论不存在");
            }
            if (!videoId.equals(target.getVideoId())) {
                throw new CustomerException("400", "评论与视频不匹配");
            }
            rootParentId = target.getParentId() == null ? target.getCommentId() : target.getParentId();
            replyToUserId = target.getUserId();
        }

        Comment comment = new Comment();
        comment.setCommentId(UUID.randomUUID().toString().replace("-", ""));
        comment.setUserId(userId);
        comment.setVideoId(videoId);
        comment.setParentId(rootParentId);
        comment.setReplyToUserId(replyToUserId);
        comment.setContent(trimmed);
        comment.setLikeCount(0);
        comment.setStatus(0);
        commentMapper.insert(comment);
        videoMapper.incrementCommentCount(videoId);

        return buildCommentVO(comment, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void likeComment(String commentId) {
        String userId = getCurrentUserId();
        ensureCommentExists(commentId);
        if (commentLikeRecordMapper.countByUserAndComment(userId, commentId) > 0) {
            return;
        }
        commentLikeRecordMapper.insertIgnore(userId, commentId);
        commentMapper.incrementLikeCount(commentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unlikeComment(String commentId) {
        String userId = getCurrentUserId();
        ensureCommentExists(commentId);
        if (commentLikeRecordMapper.delete(userId, commentId) > 0) {
            commentMapper.decrementLikeCount(commentId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void foldComment(String commentId) {
        getCurrentUserId();
        ensureCommentExists(commentId);
        commentMapper.foldComment(commentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfoldComment(String commentId) {
        getCurrentUserId();
        ensureCommentExists(commentId);
        commentMapper.unfoldComment(commentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteComment(String commentId) {
        String userId = getCurrentUserId();
        Comment comment = commentMapper.selectByCommentId(commentId);
        if (comment == null) {
            throw new CustomerException("404", "评论不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            throw new CustomerException("403", "只能删除自己的评论");
        }

        int removed = 1;
        if (comment.getParentId() == null) {
            removed += commentMapper.deleteRepliesByParentId(commentId);
        }
        commentMapper.deleteByCommentId(commentId);
        videoMapper.decrementCommentCount(comment.getVideoId(), removed);

        Map<String, Object> data = new HashMap<>();
        data.put("removedCount", removed);
        data.put("commentCount", commentMapper.countByVideoId(comment.getVideoId()));
        return data;
    }

    private CommentVO buildCommentVO(Comment comment, String userId) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getCommentId());
        vo.setUserId(userId);
        vo.setVideoId(comment.getVideoId());
        vo.setParentId(comment.getParentId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(0);
        vo.setStatus(0);
        vo.setReplyCount(0);
        vo.setLiked(false);
        vo.setCreateTime(LocalDateTime.now());
        User user = userMapper.selectByUserId(userId);
        if (user != null) {
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        if (comment.getReplyToUserId() != null) {
            User replyTo = userMapper.selectByUserId(comment.getReplyToUserId());
            if (replyTo != null) {
                vo.setReplyToNickname(replyTo.getNickname());
            }
        }
        return vo;
    }

    private void enrichLikeStatus(List<CommentVO> list) {
        String userId = getCurrentUserIdOptional();
        if (userId == null) {
            for (CommentVO vo : list) {
                vo.setLiked(false);
            }
            return;
        }
        for (CommentVO vo : list) {
            vo.setLiked(commentLikeRecordMapper.countByUserAndComment(userId, vo.getCommentId()) > 0);
        }
    }

    private void ensureCommentExists(String commentId) {
        if (commentMapper.selectByCommentId(commentId) == null) {
            throw new CustomerException("404", "评论不存在");
        }
    }

    private void ensureVideoExists(String videoId) {
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null || video.getStatus() == null || video.getStatus() == 2) {
            throw new CustomerException("404", "视频不存在");
        }
    }

    private String getCurrentUserId() {
        String userId = getCurrentUserIdOptional();
        if (userId == null) {
            throw new CustomerException("401", "用户未登录");
        }
        return userId;
    }

    private String getCurrentUserIdOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String userId = (String) auth.getCredentials();
        if (userId == null || userId.isBlank()) {
            return null;
        }
        return userId;
    }
}
