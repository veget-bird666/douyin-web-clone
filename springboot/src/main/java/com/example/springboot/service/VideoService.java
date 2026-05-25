package com.example.springboot.service;

import com.example.springboot.entity.Video;
import com.example.springboot.mapper.VideoMapper;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);

    @Resource
    private VideoMapper videoMapper;

    @Resource
    private MinioClient minioClient;

    public String uploadVideo(MultipartFile file, String title, String description) throws Exception {
        String userId = getCurrentUserId();
        log.info("当前用户userId: {}", userId);

        String bucketName = "videos";
        String objectName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        log.info("准备上传文件到MinIO - bucket: {}, objectName: {}, size: {}",
                bucketName, objectName, file.getSize());

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
        log.info("文件已上传到MinIO");

        String videoId = UUID.randomUUID().toString();

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
        video.setFormat(extractFormat(file.getOriginalFilename()));
        videoMapper.insert(video);

        return videoId;
    }

    public String getVideoUrl(String videoId) throws Exception {
        Video video = videoMapper.selectByVideoId(videoId);
        if (video == null) {
            throw new RuntimeException("Video not found");
        }

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket("videos")
                        .object(video.getObjectName())
                        .method(Method.GET)
                        .expiry(1, TimeUnit.HOURS)
                        .build()
        );
    }

    public Video getVideoInfo(String videoId) {
        return videoMapper.selectByVideoId(videoId);
    }

    public List<Video> getVideoListByUser(String userId) {
        return videoMapper.selectByUserId(userId);
    }

    public List<Video> getFeed(int limit, int offset) {
        return videoMapper.selectFeed(limit, offset);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("SecurityContext auth: {}", auth);
        if (auth != null) {
            log.info("  principal: {}, credentials: {}", auth.getPrincipal(), auth.getCredentials());
        }
        return auth != null ? (String) auth.getCredentials() : null;
    }

    private String extractFormat(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "mp4";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}