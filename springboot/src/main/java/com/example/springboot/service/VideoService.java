package com.example.springboot.service;

import com.example.springboot.entity.Video;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.FavoriteRecordMapper;
import com.example.springboot.mapper.LikeRecordMapper;
import com.example.springboot.mapper.VideoMapper;
import com.example.springboot.mapper.ViewRecordMapper;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    private static final String BUCKET_NAME = "videos";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("mp4", "mov", "avi", "mkv", "webm", "flv");

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private LikeRecordMapper likeRecordMapper;

    @Resource
    private FavoriteRecordMapper favoriteRecordMapper;

    @Resource
    private ViewRecordMapper viewRecordMapper;

    @Resource
    private MinioClient minioClient;

    @Transactional(rollbackFor = Exception.class)
    public Video uploadVideo(MultipartFile file, String title, String description) {
        String userId = getCurrentUserId();
        validateFile(file);
        String format = extractFormat(file.getOriginalFilename());
        ensureBucketExists();

        String objectName = UUID.randomUUID() + "-" + System.currentTimeMillis() + "." + format;
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("MinIO上传成功 - bucket: {}, object: {}, size: {}", BUCKET_NAME, objectName, file.getSize());
        } catch (Exception e) {
            log.error("MinIO上传失败: {}", e.getMessage(), e);
            throw new CustomerException("500", "视频文件上传存储失败");
        }

        String videoId = UUID.randomUUID().toString().replace("-", "");
        Video video = new Video();
        video.setVideoId(videoId);
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description);
        video.setLikeCount(0);
        video.setCommentCount(0);
        video.setViewCount(0);
        video.setStatus(1);
        video.setObjectName(objectName);
        video.setSize(file.getSize());
        video.setFormat(format);

        try {
            videoMapper.insert(video);
            log.info("视频记录入库成功 - videoId: {}", videoId);
        } catch (Exception e) {
            log.error("视频记录入库失败，尝试清理MinIO文件: {}", e.getMessage(), e);
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder().bucket(BUCKET_NAME).object(objectName).build()
                );
            } catch (Exception cleanupEx) {
                log.warn("清理MinIO文件失败，需手动处理 - object: {}", objectName, cleanupEx);
            }
            throw new CustomerException("500", "视频信息保存失败");
        }

        return video;
    }

    /**
     * 推荐视频流：按点赞数排序，排除已看过的
     */
    public List<Video> getRecommendedFeed(int limit, int offset) {
        String userId = getCurrentUserId();
        return videoMapper.selectRecommended(userId, limit, offset);
    }

    /**
     * 记录浏览 + 返回视频详情
     */
    public Video getVideoInfo(String videoId) {
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new CustomerException("404", "视频不存在");
        }
        // 自动记录浏览
        try {
            String userId = getCurrentUserId();
            viewRecordMapper.insertIgnore(userId, videoId);
        } catch (Exception ignored) {
            // 浏览记录不影响主流程
        }
        return video;
    }

    /**
     * 手动记录浏览（前端主动调用）
     */
    public void recordView(String videoId) {
        String userId = getCurrentUserId();
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new CustomerException("404", "视频不存在");
        }
        viewRecordMapper.insertIgnore(userId, videoId);
    }

    /**
     * 查询当前用户是否已浏览过
     */
    public boolean isViewed(String videoId) {
        String userId = getCurrentUserId();
        return viewRecordMapper.countByUserAndVideo(userId, videoId) > 0;
    }

    public String getVideoUrl(String videoId) {
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new CustomerException("404", "视频不存在");
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(video.getObjectName())
                            .method(Method.GET)
                            .expiry(1, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取视频URL失败: {}", e.getMessage(), e);
            throw new CustomerException("500", "获取视频播放地址失败");
        }
    }

    public List<Video> getVideoListByUser(String userId) {
        return videoMapper.selectByUserId(userId);
    }

    public List<Video> getFeed(int limit, int offset) {
        return videoMapper.selectFeed(limit, offset);
    }

    /**
     * 点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public void likeVideo(String videoId) {
        String userId = getCurrentUserId();
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new CustomerException("404", "视频不存在");
        }
        int inserted = likeRecordMapper.insertIgnore(userId, videoId);
        if (inserted > 0) {
            videoMapper.incrementLikeCount(videoId);
            log.info("用户 {} 点赞视频 {} 成功", userId, videoId);
        } else {
            log.info("用户 {} 已经点过赞了", userId);
        }
    }

    /**
     * 取消点赞
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlikeVideo(String videoId) {
        String userId = getCurrentUserId();
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new CustomerException("404", "视频不存在");
        }
        int deleted = likeRecordMapper.delete(userId, videoId);
        if (deleted > 0) {
            videoMapper.decrementLikeCount(videoId);
            log.info("用户 {} 取消点赞视频 {} 成功", userId, videoId);
        } else {
            log.info("用户 {} 未点过赞，无需取消", userId);
        }
    }

    /**
     * 查询当前用户是否已点赞
     */
    public boolean isLiked(String videoId) {
        String userId = getCurrentUserId();
        return likeRecordMapper.countByUserAndVideo(userId, videoId) > 0;
    }

    /**
     * 收藏视频
     */
    @Transactional(rollbackFor = Exception.class)
    public void favoriteVideo(String videoId) {
        String userId = getCurrentUserId();
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new CustomerException("404", "视频不存在");
        }
        int inserted = favoriteRecordMapper.insertIgnore(userId, videoId);
        if (inserted > 0) {
            log.info("用户 {} 收藏视频 {} 成功", userId, videoId);
        } else {
            log.info("用户 {} 已经收藏过了", userId);
        }
    }

    /**
     * 取消收藏
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteVideo(String videoId) {
        String userId = getCurrentUserId();
        int deleted = favoriteRecordMapper.delete(userId, videoId);
        if (deleted > 0) {
            log.info("用户 {} 取消收藏视频 {} 成功", userId, videoId);
        }
    }

    /**
     * 查询当前用户是否已收藏
     */
    public boolean isFavorited(String videoId) {
        String userId = getCurrentUserId();
        return favoriteRecordMapper.countByUserAndVideo(userId, videoId) > 0;
    }

    /**
     * 获取用户收藏列表
     */
    public List<Video> getFavoriteList() {
        String userId = getCurrentUserId();
        return favoriteRecordMapper.selectFavoritesByUserId(userId);
    }

    /**
     * 删除视频（软删除，仅作者可删）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteVideo(String videoId) {
        String userId = getCurrentUserId();
        int affected = videoMapper.softDelete(videoId, userId);
        if (affected == 0) {
            Video video = videoMapper.selectByVideoId(videoId);
            if (video == null) {
                throw new CustomerException("404", "视频不存在");
            }
            throw new CustomerException("403", "只能删除自己的视频");
        }
        log.info("用户 {} 删除视频 {} 成功", userId, videoId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomerException("400", "上传文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new CustomerException("400", "仅支持视频文件上传");
        }

        String extension = extractFormat(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomerException("400", "不支持的视频格式: " + extension);
        }
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(BUCKET_NAME).build()
            );
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
                log.info("MinIO bucket '{}' 已自动创建", BUCKET_NAME);
            }
        } catch (Exception e) {
            log.error("MinIO bucket检查/创建失败: {}", e.getMessage(), e);
            throw new CustomerException("500", "存储服务初始化失败");
        }
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new CustomerException("401", "用户未登录");
        }
        String userId = (String) auth.getCredentials();
        if (userId == null || userId.isBlank()) {
            throw new CustomerException("401", "用户身份无效");
        }
        return userId;
    }

    private String extractFormat(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "mp4";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
